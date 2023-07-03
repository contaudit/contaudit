package br.ufrgs.inf.ppgc.contaudit.checker.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.comparator.NameFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufrgs.inf.ppgc.contaudit.checker.Utils;
import br.ufrgs.inf.ppgc.contaudit.checker.log.Log;
import br.ufrgs.inf.ppgc.contaudit.checker.log.LogService;
import br.ufrgs.inf.ppgc.contaudit.checker.security.HashService;

public class EnvironmentService {
    private static final String STATE_DEF_FILE_SUFFIX = "_state.txt";
    private static final String STATE_TEMP_FILE_SUFFIX = "_state_temp.txt";
    private static final String STATE_DIFF_FILE_SUFFIX = "_state_diff.txt";
    private static final String STATE_PATCH_FILE_SUFFIX = "_state_patch.txt";

    private Logger logger = LoggerFactory.getLogger(EnvironmentService.class);

    public void saveCurrentState() throws InterruptedException, IOException {
        String currentState = this.getCurrentState();
        this.saveCurrentState(currentState, Utils.dateFormatter().format(new Date()) + STATE_DEF_FILE_SUFFIX);
    }

    public void validateStateByLastHashStored() throws IOException, URISyntaxException, InterruptedException {
        // Get environment post-state hash in last log saved
        Log log = new LogService().getLastLog();
        if (log == null)
            return;
        String lastLogSavedPostStateHash = log.getEnvironmentPostStateHash();
        this.logger.info(String.format("Simulated state hash: %s", lastLogSavedPostStateHash));

        // Get the current state hash
        String currentStateHash = this.getCurrentStateHash(this.getCurrentState());
        this.logger.info(String.format("Current state hash: %s", currentStateHash));

        // Compare hashes
        this.logger.info("Comparing current state hash and post-state hash in last log saved...");
        if (lastLogSavedPostStateHash.equals(currentStateHash)) 
            this.logger.info("Current State Valid!");
        else
            this.logger.info("Current State Invalid!");
    }

    public void validateStateByCalculatedHash() throws URISyntaxException, IOException, InterruptedException {
        // Determine the last saved state date
        String lastSavedStateDate = this.getLastSavedStateDate();
        // Search for all logs since the last saved state
        List<Log> logs = new LogService().queryLogByDateRange(lastSavedStateDate, Utils.dateFormatter().format(new Date()));
        if (logs == null || logs.isEmpty())
            return;

        // Load the logs diffs in file system
        List<String> logsIds = this.readEnvironmentDiffsForSimulation(logs);
        // Apply logs diffs in the temporary state def file
        this.applyEnvironmentDiffsForSimulation(lastSavedStateDate, logsIds);
        // Save current environment state in a temp file
        this.saveCurrentTempState(lastSavedStateDate);

        // Calculates hashes from current temp state and simulated state
        String simulatedStateHash = this.getSimulatedStateHash(lastSavedStateDate);
        this.logger.info(String.format("Simulated state hash: %s", simulatedStateHash));
        String currentTempStateHash = this.getCurrentTempStateHash(lastSavedStateDate);
        this.logger.info(String.format("Current temp state hash: %s", currentTempStateHash));

        // Clean temporary state files
        this.clearTempStateFiles();

        // Compare hashes
        this.logger.info("Comparing current state hash and calculated state hash...");
        if (currentTempStateHash.equals(simulatedStateHash)) 
            this.logger.info("Current State Valid!");
        else
            this.logger.info("Current State Invalid!");
    }

    private void saveCurrentState(String currentState, String stateName) throws IOException {
        this.logger.info("Saving current environment state...");
        this.createFile(currentState, stateName);
    }

    private String getCurrentState() throws IOException, InterruptedException {
        this.logger.info("Analyzing current environment state...");
        String command = "dpkg -l | awk '{print $2, $3}'";
        return this.runCommand(command);
    }

    private void saveCurrentTempState(String filePrefix) throws InterruptedException, IOException {
        this.logger.info("Saving current environment temp state...");
        String currentState = this.getCurrentState();
        this.createFile(currentState, filePrefix + STATE_TEMP_FILE_SUFFIX);
    }

    private String getLastSavedStateDate() throws FileNotFoundException {
        this.logger.info("Loading environment diffs since last saved state...");

        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(STATE_DEF_FILE_SUFFIX);
        File[] files = new File(Utils.currentDirectory()).listFiles(filter);
        if (files == null || files.length == 0)
            throw new FileNotFoundException("Last saved state file not found!");
        Arrays.sort(files, NameFileComparator.NAME_REVERSE);

        return files[0].getName().substring(0, 23);
    }

    private List<String> readEnvironmentDiffsForSimulation(List<Log> logs) throws IOException, InterruptedException {
        this.logger.info("Loading environment diffs since last saved state...");
        List<String> logsIds = new ArrayList<>();
        for (Log log : logs) {
            String logId = log.getId();
            String diff = log.getEnvironmentDiff();
            logsIds.add(logId);
            
            this.createFile(diff, logId + STATE_DIFF_FILE_SUFFIX);
        }
        return logsIds;
    }

    private void applyEnvironmentDiffsForSimulation(String lastSavedStateDate, List<String> logsIds) throws IOException, InterruptedException {
        this.cloneLastSavedStateForSimulation(lastSavedStateDate);
        
        this.logger.info("Applying environment diffs since last saved state...");
        for (String id : logsIds) {
            String command = "patch " + lastSavedStateDate + STATE_PATCH_FILE_SUFFIX + " -i " + id + STATE_DIFF_FILE_SUFFIX;
            this.runCommand(command);
        }
    }

    private void cloneLastSavedStateForSimulation(String lastSavedStateDate) throws IOException, InterruptedException {
        this.logger.info("Cloning last saved state for simulation...");
        String command = "cp " + lastSavedStateDate + STATE_DEF_FILE_SUFFIX + " " + lastSavedStateDate + STATE_PATCH_FILE_SUFFIX;
        this.runCommand(command);
    }

    private String getCurrentStateHash(String currentState) {
        this.logger.info("Generating current state hash...");
        return new HashService().generateSHA3256Hash(currentState);
    }

    private String getCurrentTempStateHash(String lastSavedStateDate) throws IOException {
        this.logger.info("Generating current temp state hash...");
        return new HashService().generateSHA3256Hash(new File(lastSavedStateDate + STATE_TEMP_FILE_SUFFIX).toPath());
    }

    private String getSimulatedStateHash(String lastSavedStateDate) throws IOException {
        this.logger.info("Generating simulated state hash...");
        return new HashService().generateSHA3256Hash(new File(lastSavedStateDate + STATE_PATCH_FILE_SUFFIX).toPath());
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

    private void clearTempStateFiles() throws IOException, InterruptedException {
        this.logger.info("Cleaning temporary state files...");
        String command = "rm -rf *_state_*";
        this.runCommand(command);
    }
}
