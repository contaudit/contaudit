package br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.wrapper.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class ArtifactService {
    private Logger logger = LoggerInstance.get();

    public List<Artifact> loadData(Application application, String command) throws IOException {
        List<Artifact> artifacts = new ArrayList<>();
        
        StringTokenizer st = new StringTokenizer(command, " ");
        // Ignore first token 'cause it should be the application
        st.nextToken();
        while (st.hasMoreTokens()) {  
            File file = new File(st.nextToken());
            Path filePath = file.toPath();
            if (Files.isDirectory(filePath) || Files.isRegularFile(filePath)) {
                // Others tokens are the artifacts needed by application
                Artifact artifact = new Artifact();
                artifact.setId(UUID.randomUUID());
                artifact.setApplication(String.format("%s-%s", application.getName(), application.getHash()));
                artifact.setName(filePath.getFileName().toString());
                artifact.setFullPath(filePath.toRealPath().toString());
                artifact.setHash(new HashService().generateSHA3256Hash(filePath));
                logger.info(String.format("Artifact %s hash: %s", artifact.getName(), artifact.getHash()));

                try (Scanner scan = new Scanner(file).useDelimiter("\\Z")) {
                    artifact.setContent(scan.next());
                }
                
                artifacts.add(artifact);
            }
        }
        
        return artifacts;
    }
}
