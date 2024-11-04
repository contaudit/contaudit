package br.ufrgs.inf.ppgc.contaudit.checker.log;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import br.ufrgs.inf.ppgc.contaudit.checker.application.Application;
import br.ufrgs.inf.ppgc.contaudit.checker.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.checker.blockchain.chaincode.LogChainCodeService;

class LogServiceTest {
    private LogService logService;
    private LogChainCodeService logChainCodeServiceMock;
    private Logger loggerMock;

    @BeforeEach
    public void setUp() {
        logChainCodeServiceMock = mock(LogChainCodeService.class);
        logService = new LogService(logChainCodeServiceMock);
        loggerMock = mock(Logger.class);
        logService.logger = loggerMock;
    }

    @Test
    void testSaveToBlockchain() {
        String commandLine = "command";
        Application application = mock(Application.class);
        List<Artifact> artifacts = Arrays.asList(mock(Artifact.class));
        String environmentPreStateHash = "preStateHash";
        String output = "output";
        String environmentPostStateHash = "postStateHash";
        String environmentDiff = "diff";
        doNothing().when(logChainCodeServiceMock).insertLog(any(Log.class));

        logService.saveToBlockchain(commandLine, application, artifacts, environmentPreStateHash, output, environmentPostStateHash, environmentDiff);

        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logChainCodeServiceMock, times(1)).insertLog(logCaptor.capture());
        verify(loggerMock, times(1)).info("Sending to blockchain...");

        Log capturedLog = logCaptor.getValue();

        assertEquals(commandLine, capturedLog.getCommand());
        assertEquals(application, capturedLog.getApplication());
        assertEquals(artifacts, capturedLog.getArtifacts());
        assertEquals(environmentPreStateHash, capturedLog.getEnvironmentPreStateHash());
        assertEquals(output, capturedLog.getOutput());
        assertEquals(environmentPostStateHash, capturedLog.getEnvironmentPostStateHash());
        assertEquals(environmentDiff, capturedLog.getEnvironmentDiff());
    }

    @Test
    void testGetLastLog_LogsIsNull() {
        when(logChainCodeServiceMock.queryLogsByDateRange(anyString(), anyString())).thenReturn(null);

        Log lastLog = logService.getLastLog();

        assertNull(lastLog);
        verify(logChainCodeServiceMock, atLeastOnce()).queryLogsByDateRange(anyString(), anyString());
    }

    @Test
    void testGetLastLog_LogsIsEmpty() {
        when(logChainCodeServiceMock.queryLogsByDateRange(anyString(), anyString())).thenReturn(new ArrayList<>());

        Log lastLog = logService.getLastLog();

        assertNull(lastLog);
        verify(logChainCodeServiceMock, atLeastOnce()).queryLogsByDateRange(anyString(), anyString());
    }

    @Test
    void testGetLastLog_LogsHasEntries() {
        Log mockLog = mock(Log.class);
        when(logChainCodeServiceMock.queryLogsByDateRange(anyString(), anyString())).thenReturn(Arrays.asList(mockLog));

        Log lastLog = logService.getLastLog();

        assertNotNull(lastLog);
        assertEquals(mockLog, lastLog);
        verify(logChainCodeServiceMock, atLeastOnce()).queryLogsByDateRange(anyString(), anyString());
    }
    
    @Test
    void testQueryLogByDateRange_WithLogs() {
        List<Log> logs = Arrays.asList(mock(Log.class));
        String startDate = "2022-01-01";
        String endDate = "2022-01-31";
        when(logChainCodeServiceMock.queryLogsByDateRange(startDate, endDate)).thenReturn(logs);

        List<Log> result = logService.queryLogByDateRange(startDate, endDate);

        assertEquals(logs, result);
        verify(loggerMock, times(1)).info("Searching logs in blockchain...");
    }

    @Test
    void testQueryLogByDateRange_NoLogs() {
        String startDate = "2022-01-01";
        String endDate = "2022-01-31";
        when(logChainCodeServiceMock.queryLogsByDateRange(startDate, endDate)).thenReturn(Collections.emptyList());

        List<Log> result = logService.queryLogByDateRange(startDate, endDate);

        assertTrue(result.isEmpty());
        verify(loggerMock, times(1)).info("Searching logs in blockchain...");
        verify(loggerMock, times(1)).info("No logs founded!");
    }
}