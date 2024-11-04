package br.ufrgs.inf.ppgc.contaudit.wrapper.log;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.LogChainCodeService;

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
}