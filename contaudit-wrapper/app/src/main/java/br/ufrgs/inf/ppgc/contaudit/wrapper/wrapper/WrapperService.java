package br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Startup;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class WrapperService {
    protected static final String TAR_GZ_EXTENSION = ".tar.gz";
    protected final Logger logger = LoggerFactory.getLogger(WrapperService.class);
    protected final HashService hashService;

    public WrapperService(HashService hashService) {
        this.hashService = hashService;
    }

    public Wrapper loadData() throws IOException {
        Wrapper wrapper = new Wrapper();
        wrapper.setHash(generateWrapperHash());
        return wrapper;
    }

    public String generateWrapperHash() throws IOException {
        String path = Utils.jarPath();
        this.logger.info("Generating Wrapper hash. Path: {}", path);
        Path tarFilePath = createTarFile(path);
        String wrapperHash = "";
        
        if (tarFilePath != null) {
            wrapperHash = this.hashService.generateSHA3256Hash(tarFilePath);
            this.logger.info("Wrapper hash: {}", wrapperHash);

            boolean deletionResult = Files.deleteIfExists(tarFilePath);
            if (!deletionResult) {
                this.logger.warn("Wrapper compressed file deletion failed...");
            }
        }
        
        return wrapperHash;
    }

    protected Path createTarFile(String sourceDir) throws IOException {
        File source = new File(sourceDir);
        String tarFilePath = generateTarFilePath(source.getAbsolutePath());
        
        try (TarArchiveOutputStream tarOs = createTarArchiveOutputStream(tarFilePath)) {
            addFilesToTarGZ(source, "", tarOs);
        }
    
        return Path.of(tarFilePath);
    }

    protected String generateTarFilePath(String baseDirPath) {
        return baseDirPath + "_" + Startup.getInstanceName() + TAR_GZ_EXTENSION;
    }

    protected TarArchiveOutputStream createTarArchiveOutputStream(String tarFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(tarFilePath);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        TarArchiveOutputStream tarOs = new TarArchiveOutputStream(gos);
        tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        return tarOs;
    }

    protected void addFilesToTarGZ(File file, String parent, TarArchiveOutputStream tarArchive) throws IOException {
        String ignoreDirName = "experiments";
        if (file.isDirectory() && file.getName().equals(ignoreDirName)) {
            return;
        }

        String entryName = parent + file.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
        tarArchive.putArchiveEntry(tarEntry);
    
        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                IOUtils.copy(bis, tarArchive);
            }
            tarArchive.closeArchiveEntry();
        } else if (file.isDirectory()) {
            tarArchive.closeArchiveEntry();
            for (File childFile : file.listFiles()) {
                addFilesToTarGZ(childFile, entryName + File.separator, tarArchive);
            }
        }
    }    
}