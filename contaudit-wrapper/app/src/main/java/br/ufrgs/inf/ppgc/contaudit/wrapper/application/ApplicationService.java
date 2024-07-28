package br.ufrgs.inf.ppgc.contaudit.wrapper.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class ApplicationService {
    private Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    public Application loadData(String commandLine) throws IOException {
        Application application = new Application();

        Path path = null;
        StringTokenizer st = new StringTokenizer(commandLine, " ");
        if (st.hasMoreTokens()) {
            path = new File(st.nextToken()).toPath();
            if (!Files.isDirectory(path) && !Files.isExecutable(path) && Files.isRegularFile(path))
                throw new FileNotFoundException(path.toString());
        }

        if (path != null) {
            application.setId(UUID.randomUUID());
            application.setName(path.getFileName().toString());
            application.setFullPath(path.toRealPath().toString());
            application.setHash(new HashService().generateSHA3256Hash(path));
            logger.info(String.format("Application hash: %s", application.getHash()));
        }

        return application;
    }
}
