package br.ufrgs.inf.ppgc.contaudit.wrapper.environment;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

class EnvironmentServiceTest {
    @Mock
    private HashService hashService;

    @InjectMocks
    private EnvironmentService environmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckStateHash() throws IOException, InterruptedException {
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        doReturn("state_data").when(spiedEnvironmentService).getCurrentState();
        when(hashService.generateSHA3256Hash("state_data")).thenReturn("mock_hash");
        doReturn(true).when(spiedEnvironmentService).saveCurrentState(anyString(), anyString());
    
        String result = spiedEnvironmentService.checkStateHash("stateName");
    
        assertEquals("mock_hash", result);
        verify(spiedEnvironmentService).getCurrentState();
        verify(hashService).generateSHA3256Hash("state_data");
        verify(spiedEnvironmentService).saveCurrentState(anyString(), anyString());
    }
    
    @Test
    void testCheckDiffState() throws IOException, InterruptedException {
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        doReturn("diff_output").when(spiedEnvironmentService).runCommand(anyString());
    
        String result = spiedEnvironmentService.checkDiffState("firstState", "secondState");
    
        assertEquals("diff_output", result);
        verify(spiedEnvironmentService).runCommand("diff firstState_state_temp.txt secondState_state_temp.txt");
    }

    @Test
    void testClearStates() throws IOException, InterruptedException {
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        doReturn("command_output").when(spiedEnvironmentService).runCommand(anyString());
    
        Boolean result = spiedEnvironmentService.clearStates();
    
        assertTrue(result);
        verify(spiedEnvironmentService).runCommand("rm -rf *_state_temp.txt");
    }    

    @Test
    void testSaveCurrentState() throws IOException {
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        doReturn(true).when(spiedEnvironmentService).createFile(anyString(), anyString());
    
        Boolean result = spiedEnvironmentService.saveCurrentState("currentState", "stateName");
    
        assertTrue(result);
        verify(spiedEnvironmentService).createFile("currentState", "stateName_state_temp.txt");
    }

    @Test
    void testGetCurrentState() throws IOException, InterruptedException {
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        String mockCommandOutput = "command_output";
        doReturn(mockCommandOutput).when(spiedEnvironmentService).runCommand(anyString());
    
        String result = spiedEnvironmentService.getCurrentState();
    
        assertEquals(mockCommandOutput, result);
        verify(spiedEnvironmentService).runCommand("dpkg -l | awk '{print $2, $3}'");
    }

    @Test
    void testGetFilesDiff() throws IOException, InterruptedException {
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        String mockDiff = "file_diff";
        doReturn(mockDiff).when(spiedEnvironmentService).runCommand(anyString());

        String result = spiedEnvironmentService.getFilesDiff("file1", "file2");

        assertEquals(mockDiff, result);
        verify(spiedEnvironmentService).runCommand("diff file1_state_temp.txt file2_state_temp.txt");
    }

    @Test
    void testRunCommand() throws IOException, InterruptedException {
        Process mockProcess = mock(Process.class);
        InputStream mockInputStream = new ByteArrayInputStream("mock_output".getBytes());
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);
        when(mockProcess.waitFor()).thenReturn(0);
        ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        EnvironmentService spiedEnvironmentService = spy(environmentService);
        doReturn(mockProcessBuilder).when(spiedEnvironmentService).createProcessBuilder();

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            when(Utils.transformInputStreamToString(any(InputStream.class), anyBoolean())).thenReturn("command_output");

            String result = spiedEnvironmentService.runCommand("mockCommand");

            assertEquals("command_output", result);
        }
    }

    @Test
    void testCreateProcessBuilder() {
        EnvironmentService spiedEnvironmentService = spy(environmentService);

        ProcessBuilder processBuilder = spiedEnvironmentService.createProcessBuilder();

        assertNotNull(processBuilder);
        verify(spiedEnvironmentService).createProcessBuilder();
    }

    @Test
    void testCreateFile() throws IOException {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            Path mockPath = mock(Path.class);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(mockPath);

            Boolean result = environmentService.createFile("file_content", "fileName.txt");

            assertTrue(result);
            mockedFiles.verify(() -> Files.write(Paths.get("fileName.txt"), "file_content".getBytes()), times(1));
        }
    }
}