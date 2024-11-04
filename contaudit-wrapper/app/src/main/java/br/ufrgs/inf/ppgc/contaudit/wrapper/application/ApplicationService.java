package br.ufrgs.inf.ppgc.contaudit.wrapper.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class ApplicationService {
    protected Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    protected HashService hashService;

    public ApplicationService(HashService hashService) {
        this.hashService = hashService;
    }

    public Application loadData(String commandLine) throws IOException {
        String filePathString = extractFilePath(commandLine);
        Path filePath = validateAndResolvePath(filePathString);

        return buildApplication(filePath);
    }

    protected String extractFilePath(String commandLine) {
        String[] tokens = commandLine.split(" ");
        if (tokens.length == 0) {
            throw new IllegalArgumentException("Command line must contain at least one argument with the file path.");
        }
        return tokens[0];
    }

    protected Path validateAndResolvePath(String filePathString) throws IOException {
        Path path = new File(filePathString).toPath();
        if (!Files.isRegularFile(path) || !Files.isExecutable(path)) {
            throw new FileNotFoundException("File not found or is not a regular executable file: " + path.toString());
        }
        return path.toRealPath();
    }

    protected Application buildApplication(Path filePath) throws IOException {
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setName(filePath.getFileName().toString());
        application.setFullPath(filePath.toString());
        application.setHash(hashService.generateSHA3256Hash(filePath));

        logger.info("Application hash: {}", application.getHash());
        return application;
    }
}