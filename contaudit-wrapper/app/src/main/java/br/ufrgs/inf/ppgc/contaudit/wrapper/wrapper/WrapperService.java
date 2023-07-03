package br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.wrapper.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Startup;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class WrapperService {
    private Logger logger = LoggerInstance.get();

    public Wrapper loadData() throws IOException, URISyntaxException {
        Wrapper wrapper = new Wrapper();
        wrapper.setHash(this.generateWrapperHash());
        return wrapper;
    }

    public String generateWrapperHash() throws IOException, URISyntaxException {
        String currentDirectory = new File(HashService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        File tarFile = this.createTarFile(currentDirectory);
        String wrapperHash = "";
        if (tarFile != null) {
            wrapperHash = new HashService().generateSHA3256Hash(tarFile.toPath());
            logger.info(String.format("Wrapper hash: %s", wrapperHash));
            boolean deletionResult = Files.deleteIfExists(tarFile.toPath());
            if (!deletionResult)
                logger.warn("Wrapper compressed file deletion failed...");
        }
        return wrapperHash;
    }

    private File createTarFile(String sourceDir){
        TarArchiveOutputStream tarOs = null;
        File source = new File(sourceDir);
        String filePath = String.format("%s_%s.%s", source.getAbsolutePath(), Startup.getInstanceName(), "tar.gz");
        try (FileOutputStream fos = new FileOutputStream(filePath);
            GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));) {
            tarOs = new TarArchiveOutputStream(gos);
            this.addFilesToTarGZ(source, "", tarOs);

            return new File(filePath);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }
         
    private void addFilesToTarGZ(File file, String parent, TarArchiveOutputStream tarArchive) throws IOException {
        String entryName = parent + file.getName();
        tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));

        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            IOUtils.copy(bis, tarArchive);
            tarArchive.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            tarArchive.closeArchiveEntry();
            for (File f : file.listFiles()) {
                this.addFilesToTarGZ(f, entryName + File.separator, tarArchive);
            }
        }
    }
}
