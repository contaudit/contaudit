package br.ufrgs.inf.ppgc.contaudit.wrapper.environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

public class EnvironmentService {
    private static final String STATE_DEF_FILE_SUFFIX = "_state_temp.txt";
    protected Logger logger = LoggerFactory.getLogger(EnvironmentService.class);
    protected HashService hashService;

    public EnvironmentService(HashService hashService) {
        this.hashService = hashService;
    }

    public String checkStateHash(String stateName) throws InterruptedException, IOException {
        String currentState = this.getCurrentState();
        this.saveCurrentState(currentState, stateName);
        String hash = this.hashService.generateSHA3256Hash(currentState);

        String logString = String.format("Environment state hash: %s", hash);
        this.logger.info(logString);
        return hash;
    }

    public String checkDiffState(String firstStateName, String secondStateName) throws InterruptedException, IOException {
        String packageDiff = this.getFilesDiff(firstStateName, secondStateName);
        String logString = String.format("Environment diff: %s", packageDiff);
        this.logger.info(logString);

        return packageDiff;
    }

    public Boolean clearStates() throws IOException, InterruptedException {
        this.logger.info("Cleaning temporary files...");
        this.runCommand("rm -rf *" + STATE_DEF_FILE_SUFFIX);
        return true;
    }

    protected Boolean saveCurrentState(String currentState, String stateName) throws IOException {
        this.logger.info("Saving current environment state...");
        this.createFile(currentState, stateName + STATE_DEF_FILE_SUFFIX);
        return true;
    }

    protected String getCurrentState() throws IOException, InterruptedException {
        this.logger.info("Analyzing current environment state...");
        // Define here the commands to collect environment state
        
        // List packages on operation system
        String command = "dpkg -l | awk '{print $2, $3}'";

        // List files modified since specific date
        //String command = "find / -newermt '2024-06-23T00:00:00' ! -newermt $(date --iso-8601=ns) -printf '%CY-%Cm-%CdT%CH:%CM:%CS %p\n'";
        
        return this.runCommand(command);
    }

    protected String getFilesDiff(String firstFileName, String secondFileName) throws IOException, InterruptedException {
        this.logger.info("Checking environment diff...");
        return this.runCommand("diff "+ firstFileName + STATE_DEF_FILE_SUFFIX + " " + secondFileName + STATE_DEF_FILE_SUFFIX);
    } 

    protected String runCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = this.createProcessBuilder();
        builder.command("sh", "-c", command);
        Process process = builder.start();
        String output = Utils.transformInputStreamToString(process.getInputStream(), false);
        process.waitFor();
        return output;
    }

    protected ProcessBuilder createProcessBuilder() {
        return new ProcessBuilder();
    }

    protected Boolean createFile(String fileContent, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        byte[] contentBytes = fileContent.getBytes();
        Files.write(path, contentBytes);

        return true;
    }
}