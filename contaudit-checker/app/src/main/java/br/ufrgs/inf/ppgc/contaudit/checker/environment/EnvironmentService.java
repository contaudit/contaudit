package br.ufrgs.inf.ppgc.contaudit.checker.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
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
    protected Logger logger = LoggerFactory.getLogger(EnvironmentService.class);
    protected LogService logService;
    protected HashService hashService;

    public EnvironmentService(LogService logService, HashService hashService) {
        this.logService = logService;
        this.hashService = hashService;
    }

    public Boolean saveCurrentState() throws InterruptedException, IOException {
        String currentState = this.getCurrentState();
        this.saveCurrentState(currentState, Utils.dateFormatter().format(new Date()) + STATE_DEF_FILE_SUFFIX);
        return true;
    }

    public Boolean validateStateByLastHashStored() throws IOException, InterruptedException {
        // Get environment post-state hash in last log saved
        Log log = this.logService.getLastLog();
        if (log == null)
            return false;
        String lastLogSavedPostStateHash = log.getEnvironmentPostStateHash();
        String logString = String.format("Last log post-state hash: %s", lastLogSavedPostStateHash);
        this.logger.info(logString);

        // Get the current state hash
        String currentStateHash = this.getCurrentStateHash(this.getCurrentState());
        String logString2 = String.format("Current state hash: %s", currentStateHash);
        this.logger.info(logString2);

        // Compare hashes
        this.logger.info("Comparing current state hash and post-state hash in last log saved...");
        if (lastLogSavedPostStateHash.equals(currentStateHash)) {
            this.logger.info("Current State Valid!");
            return true;
        } else {
            this.logger.info("Current State Invalid!");
            return false;
        }
    }

    public Boolean validateStateByCalculatedHash() throws IOException, InterruptedException {
        // Determine the last saved state date
        String lastSavedStateDate = this.getLastSavedStateDate();
        // Search for all logs since the last saved state
        List<Log> logs = this.logService.queryLogByDateRange(lastSavedStateDate, Utils.dateFormatter().format(new Date()));
        if (logs == null || logs.isEmpty())
            return false;

        // Load the logs diffs in file system
        List<String> logsIds = this.readEnvironmentDiffsForSimulation(logs);
        // Apply logs diffs in the temporary state def file
        this.applyEnvironmentDiffsForSimulation(lastSavedStateDate, logsIds);
        // Save current environment state in a temp file
        this.saveCurrentTempState(lastSavedStateDate);

        // Calculates hashes from current temp state and simulated state
        String simulatedStateHash = this.getSimulatedStateHash(lastSavedStateDate);
        String logString = String.format("Simulated state hash: %s", simulatedStateHash);
        this.logger.info(logString);
        String currentTempStateHash = this.getCurrentTempStateHash(lastSavedStateDate);
        String logString2 = String.format("Current temp state hash: %s", currentTempStateHash);
        this.logger.info(logString2);

        // Clean temporary state files
        this.clearTempStateFiles();

        // Compare hashes
        this.logger.info("Comparing current state hash and calculated state hash...");
        if (currentTempStateHash.equals(simulatedStateHash)) {
            this.logger.info("Current State Valid!");
            return true;
        } else {
            this.logger.info("Current State Invalid!");
            return false;
        }
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

    protected Boolean saveCurrentState(String currentState, String stateName) throws IOException {
        this.logger.info("Saving current environment state...");
        this.createFile(currentState, stateName);
        return true;
    }

    protected Boolean saveCurrentTempState(String filePrefix) throws InterruptedException, IOException {
        this.logger.info("Saving current environment temp state...");
        String currentState = this.getCurrentState();
        this.createFile(currentState, filePrefix + STATE_TEMP_FILE_SUFFIX);
        return true;
    }

    protected String getLastSavedStateDate() throws FileNotFoundException {
        this.logger.info("Loading environment diffs since last saved state...");
    
        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(STATE_DEF_FILE_SUFFIX);
        File[] files = this.getDirectory(Utils.currentDirectory()).listFiles(filter);
        if (files == null || files.length == 0) {
            throw new FileNotFoundException("Last saved state file not found!");
        }
    
        Arrays.sort(files, NameFileComparator.NAME_REVERSE);
    
        // Verifica se o arquivo contém o sufixo antes de tentar remover
        String fileName = files[0].getName();
        if (!fileName.endsWith(STATE_DEF_FILE_SUFFIX)) {
            throw new FileNotFoundException("Last saved state file not found with expected suffix!");
        }

        int suffixIndex = fileName.lastIndexOf(STATE_DEF_FILE_SUFFIX);
        return fileName.substring(0, suffixIndex);
    }

    protected List<String> readEnvironmentDiffsForSimulation(List<Log> logs) throws IOException {
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

    protected Boolean applyEnvironmentDiffsForSimulation(String lastSavedStateDate, List<String> logsIds) throws IOException, InterruptedException {
        this.cloneLastSavedStateForSimulation(lastSavedStateDate);
        
        this.logger.info("Applying environment diffs since last saved state...");
        for (String id : logsIds) {
            String command = "patch " + lastSavedStateDate + STATE_PATCH_FILE_SUFFIX + " -i " + id + STATE_DIFF_FILE_SUFFIX;
            this.runCommand(command);
        }
        return true;
    }

    protected Boolean cloneLastSavedStateForSimulation(String lastSavedStateDate) throws IOException, InterruptedException {
        this.logger.info("Cloning last saved state for simulation...");
        String command = "cp " + lastSavedStateDate + STATE_DEF_FILE_SUFFIX + " " + lastSavedStateDate + STATE_PATCH_FILE_SUFFIX;
        this.runCommand(command);
        return true;
    }

    protected String getCurrentStateHash(String currentState) {
        this.logger.info("Generating current state hash...");
        return this.hashService.generateSHA3256Hash(currentState);
    }

    protected String getCurrentTempStateHash(String lastSavedStateDate) throws IOException {
        this.logger.info("Generating current temp state hash...");
        return this.hashService.generateSHA3256Hash(this.getDirectory(lastSavedStateDate + STATE_TEMP_FILE_SUFFIX).toPath());
    }

    protected String getSimulatedStateHash(String lastSavedStateDate) throws IOException {
        this.logger.info("Generating simulated state hash...");
        return this.hashService.generateSHA3256Hash(this.getDirectory(lastSavedStateDate + STATE_PATCH_FILE_SUFFIX).toPath());
    }

    protected String runCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = createProcessBuilder(command);
        Process process = builder.start();
        String output = Utils.transformInputStreamToString(process.getInputStream(), false);
        process.waitFor();
        return output;
    }

    protected Boolean createFile(String fileContent, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        byte[] contentBytes = fileContent.getBytes();
        Files.write(path, contentBytes);
        return true;
    }

    protected Boolean clearTempStateFiles() throws IOException, InterruptedException {
        this.logger.info("Cleaning temporary state files...");
        String command = "rm -rf *_state_*";
        this.runCommand(command);
        return true;
    }

    protected ProcessBuilder createProcessBuilder(String command) {
        return new ProcessBuilder("sh", "-c", command);
    }

    protected File getDirectory(String path) {
        return new File(path);
    }
}