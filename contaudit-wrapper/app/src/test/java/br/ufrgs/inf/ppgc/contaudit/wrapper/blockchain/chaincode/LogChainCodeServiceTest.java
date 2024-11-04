package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.log.Log;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogChainCodeServiceTest {
    private BlockchainService blockchainService;
    private LogChainCodeService logChainCodeService;
    private Log mockLog;

    @BeforeEach
    void setUp() {
        blockchainService = mock(BlockchainService.class);
        logChainCodeService = new LogChainCodeService(blockchainService);

        mockLog = mock(Log.class);
        when(mockLog.getId()).thenReturn("log123");
        when(mockLog.getCommand()).thenReturn("execute");
        when(mockLog.getApplication()).thenReturn(null); 
        when(mockLog.getArtifacts()).thenReturn(null);
        when(mockLog.getEnvironmentPreStateHash()).thenReturn("hash123");
        when(mockLog.getOutput()).thenReturn("output123");
        when(mockLog.getEnvironmentPostStateHash()).thenReturn("hash456");
        when(mockLog.getEnvironmentDiff()).thenReturn("diff789");
    }

    @Test
    void testInsertLog() {
        Gson gson = new Gson();
        String expectedLogJson = gson.toJson(mockLog);

        logChainCodeService.insertLog(mockLog);

        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        verify(blockchainService, times(1)).submitTransaction(eq("c1"), eq("log-chaincode"), eq("insert"), argumentCaptor.capture());
        String[] capturedArgs = argumentCaptor.getValue();
        assertEquals(2, capturedArgs.length);
        assertEquals("log123", capturedArgs[0]);
        assertEquals(expectedLogJson, capturedArgs[1]);
    }
}