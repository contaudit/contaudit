package br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class ArtifactService {
    protected Logger logger = LoggerFactory.getLogger(ArtifactService.class);
    private final HashService hashService;

    public ArtifactService(HashService hashService) {
        this.hashService = hashService;
    }

    public List<Artifact> loadData(Application application, String command) throws IOException {
        List<Artifact> artifacts = new ArrayList<>();
        
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        if (!tokenizer.hasMoreTokens()) {
            this.logger.warn("No command provided");
            return artifacts;
        }

        // Ignore the first token (application)
        tokenizer.nextToken();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            try {
                Path filePath = Path.of(token).toRealPath();
                Artifact artifact = createArtifact(application, filePath);
                artifacts.add(artifact);
            } catch (InvalidPathException | IOException e) {
                this.logger.error("Error processing file: {}", token, e);
            }
        }

        return artifacts;
    }

    protected Artifact createArtifact(Application application, Path filePath) throws IOException {
        Artifact artifact = new Artifact();
        artifact.setId(UUID.randomUUID());
        artifact.setApplication(formatApplicationName(application));
        artifact.setName(filePath.getFileName().toString());
        artifact.setFullPath(filePath.toString());

        String hash = hashService.generateSHA3256Hash(filePath);
        artifact.setHash(hash);
        this.logger.info("Artifact {} hash: {}", artifact.getName(), hash);

        if (Files.isRegularFile(filePath)) {
            artifact.setContent(readFileContent(filePath));
        }

        return artifact;
    }

    protected String formatApplicationName(Application application) {
        return String.format("%s-%s", application.getName(), application.getHash());
    }

    protected String readFileContent(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.reduce("", (acc, line) -> acc + line + System.lineSeparator());
        }
    }
}