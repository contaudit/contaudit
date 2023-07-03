package br.ufrgs.inf.ppgc.contaudit.wrapper.environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.wrapper.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class EnvironmentService {
    private static final String STATE_DEF_FILE_SUFFIX = "_state_temp.txt";
    
    private Logger logger = LoggerInstance.get();

    public String checkStateHash(String stateName) throws InterruptedException, IOException {
        String currentState = this.getCurrentState();
        this.saveCurrentState(currentState, stateName);
        String hash = new HashService().generateSHA3256Hash(currentState);

        this.logger.info(String.format("Environment state hash: %s", hash));
        return hash;
    }

    public String checkDiffState(String firstStateName, String secondStateName) throws InterruptedException, IOException {
        String packageDiff = this.getFilesDiff(firstStateName, secondStateName);
        this.logger.info(String.format("Environment diff: %s", packageDiff));

        return packageDiff;
    }

    public void clearStates() throws IOException, InterruptedException {
        this.logger.info("Cleaning temporary files...");
        this.runCommand("rm -rf *" + STATE_DEF_FILE_SUFFIX);
    }

    private void saveCurrentState(String currentState, String stateName) throws IOException {
        this.logger.info("Saving current environment state...");
        this.createFile(currentState, stateName + STATE_DEF_FILE_SUFFIX);
    }

    private String getCurrentState() throws IOException, InterruptedException {
        this.logger.info("Analyzing current environment state...");
        String command = "dpkg -l | awk '{print $2, $3}'";
        return this.runCommand(command);
    }

    private String getFilesDiff(String firstFileName, String secondFileName) throws IOException, InterruptedException {
        this.logger.info("Checking environment diff...");
        return this.runCommand("diff "+ firstFileName + STATE_DEF_FILE_SUFFIX + " " + secondFileName + STATE_DEF_FILE_SUFFIX);
    } 

    private String runCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);
        Process process = builder.start();
        String output = Utils.transformInputStreamToString(process.getInputStream(), false);
        process.waitFor();
        return output;
    }

    private void createFile(String fileContent, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        byte[] contentBytes = fileContent.getBytes();
        Files.write(path, contentBytes);
    }
}
