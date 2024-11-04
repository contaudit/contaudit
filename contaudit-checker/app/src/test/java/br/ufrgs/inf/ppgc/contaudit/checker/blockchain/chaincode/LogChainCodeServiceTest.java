package br.ufrgs.inf.ppgc.contaudit.checker.blockchain.chaincode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import br.ufrgs.inf.ppgc.contaudit.checker.application.Application;
import br.ufrgs.inf.ppgc.contaudit.checker.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.checker.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.checker.log.Log;

class LogChainCodeServiceTest {
    @Mock
    private BlockchainService blockchainServiceMock;

    @InjectMocks
    private LogChainCodeService logChainCodeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInsertLog() {
        Application mockApplication = mock(Application.class);
        List<Artifact> mockArtifacts = new ArrayList<>();
        Log log = new Log(
            "sample-command", 
            mockApplication, 
            mockArtifacts, 
            "pre-state-hash", 
            "output", 
            "post-state-hash", 
            "env-diff"
        );

        logChainCodeService.insertLog(log);

        verify(blockchainServiceMock, times(1)).submitTransaction(
                eq("c1"),
                eq("log-chaincode"),
                eq("insert"),
                any(String[].class)
        );
    }

    @Test
    void testQueryLogsByDateRange_Success() {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        Application mockApplication = mock(Application.class);
        List<Artifact> mockArtifacts = new ArrayList<>();
        Log log1 = new Log(
            "sample-command",
            mockApplication,
            mockArtifacts,
            "pre-state-hash",
            "output",
            "post-state-hash",
            "env-diff"
        );

        List<Log> expectedLogs = new ArrayList<>();
        expectedLogs.add(log1);
        String jsonResponse = new Gson().toJson(expectedLogs);

        when(blockchainServiceMock.evaluateTransaction(
                eq("c1"),
                eq("log-chaincode"),
                eq("queryByRange"),
                any(String[].class)
        )).thenReturn(jsonResponse);

        List<Log> logs = logChainCodeService.queryLogsByDateRange(startDate, endDate);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals("sample-command", logs.get(0).getCommand());
        assertEquals("pre-state-hash", logs.get(0).getEnvironmentPreStateHash());
    }

    @Test
    void testQueryLogsByDateRange_InvalidJsonSyntax() {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";

        when(blockchainServiceMock.evaluateTransaction(
                eq("c1"),
                eq("log-chaincode"),
                eq("queryByRange"),
                any(String[].class)
        )).thenReturn("invalid json");

        List<Log> logs = logChainCodeService.queryLogsByDateRange(startDate, endDate);

        assertNotNull(logs);
        assertEquals(0, logs.size());
    }
}