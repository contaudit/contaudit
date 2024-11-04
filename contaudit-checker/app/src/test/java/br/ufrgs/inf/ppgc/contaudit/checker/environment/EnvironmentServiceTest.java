package br.ufrgs.inf.ppgc.contaudit.checker.environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import br.ufrgs.inf.ppgc.contaudit.checker.Utils;
import br.ufrgs.inf.ppgc.contaudit.checker.log.Log;
import br.ufrgs.inf.ppgc.contaudit.checker.log.LogService;
import br.ufrgs.inf.ppgc.contaudit.checker.security.HashService;

class EnvironmentServiceTest {
    private EnvironmentService environmentService;
    private LogService logService;
    private HashService hashService;
    private Logger logger;

    @TempDir
    private Path tempDir;

    @BeforeEach
    public void setUp() {
        logService = mock(LogService.class);
        hashService = mock(HashService.class);
        logger = mock(Logger.class);
        
        environmentService = spy(new EnvironmentService(logService, hashService));
        environmentService.logger = logger;
    }
    
    @Test
    void testValidateStateByLastHashStored_ValidState() throws IOException, InterruptedException {
        Log log = mock(Log.class);
        when(logService.getLastLog()).thenReturn(log);
        when(log.getEnvironmentPostStateHash()).thenReturn("currentHash");
        doReturn("currentHash").when(environmentService).getCurrentStateHash(anyString());

        boolean result = environmentService.validateStateByLastHashStored();
    
        assertTrue(result);
        verify(logger).info("Current State Valid!");
    }
        
    @Test
    void testValidateStateByLastHashStored_InvalidState() throws IOException, InterruptedException {
        Log log = mock(Log.class);
        when(logService.getLastLog()).thenReturn(log);
        when(log.getEnvironmentPostStateHash()).thenReturn("lastHash");
        doReturn("differentHash").when(environmentService).getCurrentStateHash(anyString());

        boolean result = environmentService.validateStateByLastHashStored();

        assertFalse(result);
        verify(logger).info("Current State Invalid!");
    }

    @Test
    void testValidateStateByLastHashStored_NullLog() throws IOException, InterruptedException {
        when(logService.getLastLog()).thenReturn(null);

        boolean result = environmentService.validateStateByLastHashStored();
        
        assertFalse(result);
        verify(logger, never()).info("Last log post-state hash: null");
    }

    @Test
    void testValidateStateByLastHashStored_NullOrEmptyHash() throws IOException, InterruptedException {
        Log log = mock(Log.class);
        when(logService.getLastLog()).thenReturn(log);
        when(log.getEnvironmentPostStateHash()).thenReturn("");
        doReturn("currentHash").when(environmentService).getCurrentStateHash(anyString());

        boolean result = environmentService.validateStateByLastHashStored();

        assertFalse(result);
        verify(logger).info("Current State Invalid!");
    }

    @Test
    void testValidateStateByCalculatedHash_ValidStateWithEqualHashes() throws IOException, InterruptedException {
        List<Log> logs = new ArrayList<>();
        logs.add(mock(Log.class));
        doReturn("2023-06-15").when(environmentService).getLastSavedStateDate();
        when(logService.queryLogByDateRange(anyString(), anyString())).thenReturn(logs);
        doReturn(new ArrayList<>()).when(environmentService).readEnvironmentDiffsForSimulation(anyList());
        doReturn(true).when(environmentService).applyEnvironmentDiffsForSimulation(anyString(), anyList());
        doReturn(true).when(environmentService).saveCurrentTempState(anyString());
        doReturn("equalHash").when(environmentService).getSimulatedStateHash(anyString());
        doReturn("equalHash").when(environmentService).getCurrentTempStateHash(anyString());
    
        boolean result = environmentService.validateStateByCalculatedHash();
    
        assertTrue(result);
        verify(logger).info("Simulated state hash: equalHash");
        verify(logger).info("Current temp state hash: equalHash");
        verify(logger).info("Current State Valid!");
    }
    
    @Test
    void testValidateStateByCalculatedHash_InvalidStateWithDifferentHashes() throws IOException, InterruptedException {
        List<Log> logs = new ArrayList<>();
        logs.add(mock(Log.class));
        doReturn("2023-06-15").when(environmentService).getLastSavedStateDate();
        when(logService.queryLogByDateRange(anyString(), anyString())).thenReturn(logs);
        doReturn(new ArrayList<>()).when(environmentService).readEnvironmentDiffsForSimulation(anyList());
        doReturn(true).when(environmentService).applyEnvironmentDiffsForSimulation(anyString(), anyList());
        doReturn(true).when(environmentService).saveCurrentTempState(anyString());
        doReturn("simulatedHash").when(environmentService).getSimulatedStateHash(anyString());
        doReturn("differentHash").when(environmentService).getCurrentTempStateHash(anyString());
    
        boolean result = environmentService.validateStateByCalculatedHash();
    
        assertFalse(result);
        verify(logger).info("Current State Invalid!");
    }

    @Test
    void testValidateStateByCalculatedHash_NullLogs() throws IOException, InterruptedException {
        doReturn("2023-06-15").when(environmentService).getLastSavedStateDate();
        when(logService.queryLogByDateRange(anyString(), anyString())).thenReturn(null);

        boolean result = environmentService.validateStateByCalculatedHash();

        assertFalse(result);
        verify(logger, never()).info("Simulated state hash: null");
    }

    @Test
    void testValidateStateByCalculatedHash_EmptyLogs() throws IOException, InterruptedException {
        List<Log> logs = new ArrayList<>();
        doReturn("2023-06-15").when(environmentService).getLastSavedStateDate();
        when(logService.queryLogByDateRange(anyString(), anyString())).thenReturn(logs);

        boolean result = environmentService.validateStateByCalculatedHash();

        assertFalse(result);
        verify(logger, never()).info("Simulated state hash: null");
    }

    @Test
    void testSaveCurrentState_success() throws IOException, InterruptedException {
        String mockCurrentState = "mocked_state";
        doReturn(mockCurrentState).when(environmentService).getCurrentState();
        doReturn(true).when(environmentService).createFile(eq(mockCurrentState), anyString());

        Boolean result = environmentService.saveCurrentState();
    
        assertTrue(result);
        verify(environmentService).saveCurrentState(eq(mockCurrentState), anyString());
        verify(environmentService).getCurrentState();
        verify(logger, times(1)).info("Saving current environment state...");
    }

    @Test
    void testSaveCurrentTempState() throws IOException, InterruptedException {
        doReturn("dummyState").when(environmentService).getCurrentState();
        Path expectedTempFile = tempDir.resolve("tempStatePrefix_state_temp.txt");
        doReturn(true).when(environmentService).createFile(anyString(), eq(expectedTempFile.toString()));

        boolean result = environmentService.saveCurrentTempState(tempDir.resolve("tempStatePrefix").toString());
    
        assertTrue(result);
        verify(environmentService).getCurrentState();
        verify(environmentService).createFile(anyString(), eq(expectedTempFile.toString()));
    }
    
    @Test
    void testGetLastSavedStateDate_FileFound() throws FileNotFoundException {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("2023-06-15_12-34-56_state.txt");
        File[] files = new File[] { mockFile };
        doReturn(mock(File.class)).when(environmentService).getDirectory(anyString());
        when(environmentService.getDirectory(anyString()).listFiles(any(FilenameFilter.class))).thenReturn(files);
    
        String result = environmentService.getLastSavedStateDate();

        assertEquals("2023-06-15_12-34-56", result);
    }

    @Test
    void testGetLastSavedStateDate_FileNotFoundException() {
        File mockedDir = mock(File.class);
        when(mockedDir.listFiles(any(FilenameFilter.class))).thenReturn(null);
        doReturn(mockedDir).when(environmentService).getDirectory(anyString());
    
        assertThrows(FileNotFoundException.class, () -> {
            environmentService.getLastSavedStateDate();
        });
        verify(logger).info("Loading environment diffs since last saved state...");
    }
    
    @Test
    void testGetLastSavedStateDate_EmptyFiles() {
        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(Utils::currentDirectory).thenReturn("/mocked/dir");
            File mockedDir = mock(File.class);
            when(mockedDir.listFiles(any(FilenameFilter.class))).thenReturn(new File[0]);
            doReturn(mockedDir).when(environmentService).getDirectory(anyString());
        
            assertThrows(FileNotFoundException.class, () -> {
                environmentService.getLastSavedStateDate();
            });
        }
    }
    
    @Test
    void testGetLastSavedStateDate_NoMatchingFiles() {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("other_file.txt");
        File[] files = new File[] { mockFile };
        doReturn(mock(File.class)).when(environmentService).getDirectory(anyString());
        when(environmentService.getDirectory(anyString()).listFiles(any(FilenameFilter.class))).thenReturn(files);
    
        assertThrows(FileNotFoundException.class, () -> {
            environmentService.getLastSavedStateDate();
        });
    }

    @Test
    void testFilenameFilterLambda() throws FileNotFoundException {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("2023-06-15_state.txt");
    
        File[] files = new File[]{mockFile};
        
        doReturn(mock(File.class)).when(environmentService).getDirectory(anyString());
        when(environmentService.getDirectory(anyString()).listFiles(any(FilenameFilter.class))).thenAnswer(invocation -> {
            FilenameFilter filter = invocation.getArgument(0);
            assertTrue(filter.accept(mockFile.getParentFile(), mockFile.getName()));
            return files;
        });
    
        String result = environmentService.getLastSavedStateDate();
        assertEquals("2023-06-15", result);
    }  

    @Test
    void testReadEnvironmentDiffsForSimulation() throws IOException {
        Log log1 = mock(Log.class);
        Log log2 = mock(Log.class);
        when(log1.getId()).thenReturn("log1");
        when(log1.getEnvironmentDiff()).thenReturn("diff1");
        when(log2.getId()).thenReturn("log2");
        when(log2.getEnvironmentDiff()).thenReturn("diff2");
        List<Log> logs = new ArrayList<>();
        logs.add(log1);
        logs.add(log2);
        doReturn(true).when(environmentService).createFile(eq("diff1"), eq("log1_state_diff.txt"));
        doReturn(true).when(environmentService).createFile(eq("diff2"), eq("log2_state_diff.txt"));
    
        List<String> logIds = environmentService.readEnvironmentDiffsForSimulation(logs);
    
        assertEquals(2, logIds.size());
        assertTrue(logIds.contains("log1"));
        assertTrue(logIds.contains("log2"));
        verify(environmentService).createFile(eq("diff1"), eq("log1_state_diff.txt"));
        verify(environmentService).createFile(eq("diff2"), eq("log2_state_diff.txt"));
    }    

    @Test
    void testApplyEnvironmentDiffsForSimulation() throws IOException, InterruptedException {
        List<String> logIds = new ArrayList<>();
        logIds.add("log1");
        logIds.add("log2");

        doReturn(true).when(environmentService).cloneLastSavedStateForSimulation(anyString());
        doReturn("someOutput").when(environmentService).runCommand(anyString());

        boolean result = environmentService.applyEnvironmentDiffsForSimulation("2023-06-15", logIds);
        assertTrue(result);

        verify(environmentService).cloneLastSavedStateForSimulation("2023-06-15");
        verify(environmentService, times(2)).runCommand(anyString());
    }

    @Test
    void testApplyEnvironmentDiffsForSimulation_withInterruptedException() throws IOException, InterruptedException {
        List<String> logIds = new ArrayList<>();
        logIds.add("log1");
        doReturn(true).when(environmentService).cloneLastSavedStateForSimulation(anyString());
        doThrow(new InterruptedException("Simulated InterruptedException")).when(environmentService).runCommand(anyString());
    
        assertThrows(InterruptedException.class, () -> {
            environmentService.applyEnvironmentDiffsForSimulation("2023-06-15", logIds);
        });
        verify(logger).info("Applying environment diffs since last saved state...");
    }

    @Test
    void testCloneLastSavedStateForSimulation() throws IOException, InterruptedException {
        doReturn("someOutput").when(environmentService).runCommand(anyString());

        boolean result = environmentService.cloneLastSavedStateForSimulation("2023-06-15");

        assertTrue(result);
        verify(environmentService).runCommand("cp 2023-06-15_state.txt 2023-06-15_state_patch.txt");
    }
    
    @Test
    void testGetCurrentStateHash() {
        String currentState = "dummyState";
        when(hashService.generateSHA3256Hash(currentState)).thenReturn("mockedHash");

        String hash = environmentService.getCurrentStateHash(currentState);

        assertEquals("mockedHash", hash);
        verify(hashService).generateSHA3256Hash(currentState);
    }

    @Test
    void testGetCurrentTempStateHash() throws IOException {
        File mockFile = mock(File.class);
        doReturn(mockFile).when(environmentService).getDirectory(anyString());
        when(hashService.generateSHA3256Hash(mockFile.toPath())).thenReturn("tempHash");

        String hash = environmentService.getCurrentTempStateHash("2023-06-15");

        assertEquals("tempHash", hash);
        verify(hashService).generateSHA3256Hash(mockFile.toPath());
    }

    @Test
    void testGetCurrentTempStateHash_withIOException() throws IOException {
        File mockFile = mock(File.class);
        Path mockPath = mock(Path.class);
        doReturn(mockFile).when(environmentService).getDirectory(anyString());
        when(mockFile.toPath()).thenReturn(mockPath);
        doThrow(new IOException("Simulated IOException")).when(hashService).generateSHA3256Hash(mockPath);
    
        assertThrows(IOException.class, () -> {
            environmentService.getCurrentTempStateHash("2023-06-15");
        });
        verify(hashService).generateSHA3256Hash(mockPath);
    }
    
    @Test
    void testGetSimulatedStateHash() throws IOException {
        File mockFile = mock(File.class);
        Path mockPath = mock(Path.class);
        when(mockFile.toPath()).thenReturn(mockPath);
        doReturn(mockFile).when(environmentService).getDirectory(anyString());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(mockPath)).thenReturn("simulated content".getBytes());
            when(hashService.generateSHA3256Hash(any(Path.class))).thenCallRealMethod();

            String hash = environmentService.getSimulatedStateHash("2023-06-15");

            assertNotNull(hash);
            verify(hashService).generateSHA3256Hash(mockPath);
        }
    }

    @Test
    void testRunCommand() throws InterruptedException, IOException {
        Process process = mock(Process.class);
        InputStream mockInputStream = new ByteArrayInputStream("mock output".getBytes());
        when(process.getInputStream()).thenReturn(mockInputStream);
        when(process.waitFor()).thenReturn(0);
        ProcessBuilder builder = mock(ProcessBuilder.class);
        when(builder.start()).thenReturn(process);
        doReturn(builder).when(environmentService).createProcessBuilder(anyString());

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.transformInputStreamToString(mockInputStream, false))
                    .thenReturn("mock output");

            String result = environmentService.runCommand("mockCommand");

            assertNotNull(result);
            assertEquals("mock output", result);
            verify(process).waitFor();
            mockedUtils.verify(() -> Utils.transformInputStreamToString(mockInputStream, false));
        }
    }

    @Test
    void testRunCommand_withIOException() throws IOException {
        ProcessBuilder builder = mock(ProcessBuilder.class);
        when(builder.start()).thenThrow(new IOException("Simulated IOException"));
        doReturn(builder).when(environmentService).createProcessBuilder(anyString());

        assertThrows(IOException.class, () -> {
            environmentService.runCommand("mockCommand");
        });
        verify(builder).start();
    }

    @Test
    void testCreateFile() throws IOException {
        Path tempFile = tempDir.resolve("mockFile.txt");
        String fileContent = "mock content";
        boolean result = environmentService.createFile(fileContent, tempFile.toString());

        assertTrue(result);
        assertTrue(Files.exists(tempFile));
    }

    @Test
    void testCreateFile_withIOException() {
        Path tempFile = tempDir.resolve("mockFile.txt");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.write(eq(tempFile), any(byte[].class)))
                       .thenThrow(new IOException("Simulated IOException"));

            assertThrows(IOException.class, () -> {
                environmentService.createFile("mock content", tempFile.toString());
            });
        }
    }

    @Test
    void testClearTempStateFiles() throws IOException, InterruptedException {
        doReturn("someOutput").when(environmentService).runCommand(anyString());
    
        boolean result = environmentService.clearTempStateFiles();

        assertTrue(result);
        verify(environmentService).runCommand("rm -rf *_state_*");
    }

    @Test
    void testClearTempStateFiles_withIOException() throws IOException, InterruptedException {
        doThrow(new IOException("Simulated IOException")).when(environmentService).runCommand(anyString());

        assertThrows(IOException.class, () -> {
            environmentService.clearTempStateFiles();
        });
        verify(logger).info("Cleaning temporary state files...");
    }

    @Test
    void testCreateProcessBuilder() {
        ProcessBuilder builder = environmentService.createProcessBuilder("mockCommand");
    
        assertNotNull(builder);
        assertEquals("sh", builder.command().get(0));
        assertEquals("-c", builder.command().get(1));
        assertEquals("mockCommand", builder.command().get(2));
    }

    @Test
    void testGetDirectoryReturnsCorrectPath() {
        String testPath = "test_directory";
        File directory = environmentService.getDirectory(testPath);
        
        assertNotNull(directory);
        assertEquals(testPath, directory.getPath());
    }
}