package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;

class ApplicationChainCodeServiceTest {
    @Mock
    private BlockchainService blockchainService;

    @Mock
    private Application application;

    @InjectMocks
    private ApplicationChainCodeService applicationChainCodeService;

    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCODE_NAME = "application-chaincode";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInsertApplication() {
        when(application.getHash()).thenReturn("testHash");

        applicationChainCodeService.insertApplication(application);

        verify(blockchainService, times(1)).submitTransaction(CHANNEL_NAME, CHAINCODE_NAME, "insert",
                new String[] { "testHash", new Gson().toJson(application) });
    }

    @Test
    void testValidateApplication_DebugMode_ValidHash() {
        try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
            mockedStatic.when(Utils::isDebug).thenReturn(true);
            when(application.getHash()).thenReturn("testHash");
            when(blockchainService.evaluateTransaction(anyString(), anyString(), anyString(), any())).thenReturn("true");
    
            boolean result = applicationChainCodeService.validateApplication(application);
    
            assertTrue(result);
            verify(blockchainService, times(1)).evaluateTransaction(CHANNEL_NAME, CHAINCODE_NAME, "validateApplication", new String[] { "testHash" });
        }
    }   

    @Test
    void testValidateApplication_DebugMode_InvalidHash_InsertAndValidateAgain() {
        try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
            mockedStatic.when(Utils::isDebug).thenReturn(true);
            when(application.getHash()).thenReturn("testHash");
            when(blockchainService.evaluateTransaction(anyString(), anyString(), anyString(), any()))
                    .thenReturn("false")
                    .thenReturn("true");
    
            boolean result = applicationChainCodeService.validateApplication(application);
    
            assertTrue(result);
            verify(blockchainService, times(2)).evaluateTransaction(CHANNEL_NAME, CHAINCODE_NAME, "validateApplication",
                    new String[] { "testHash" });
            verify(blockchainService, times(1)).submitTransaction(CHANNEL_NAME, CHAINCODE_NAME, "insert",
                    new String[] { "testHash", new Gson().toJson(application) });
        }
    }

    @Test
    void testValidateApplication_DebugMode_InvalidHash_Failure() {
        try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
            mockedStatic.when(Utils::isDebug).thenReturn(true);
            when(application.getHash()).thenReturn("testHash");
            doThrow(new RuntimeException("Test Exception")).when(blockchainService).evaluateTransaction(anyString(), anyString(), anyString(), any());
    
            boolean result = applicationChainCodeService.validateApplication(application);
    
            assertFalse(result);
            verify(blockchainService, times(1)).evaluateTransaction(CHANNEL_NAME, CHAINCODE_NAME, "validateApplication", new String[] { "testHash" });
        }
    }

    @Test
    void testValidateApplication_NonDebugMode_ValidHash() {
        try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
            mockedStatic.when(Utils::isDebug).thenReturn(false);
            when(application.getHash()).thenReturn("testHash");
            when(blockchainService.evaluateTransaction(anyString(), anyString(), anyString(), any())).thenReturn("true");
    
            boolean result = applicationChainCodeService.validateApplication(application);
    
            assertTrue(result);
            verify(blockchainService, times(1)).evaluateTransaction(CHANNEL_NAME, CHAINCODE_NAME, "validateApplication",
                    new String[] { "testHash" });
        }
    }

   @Test
    void testValidateApplication_NonDebugMode_InvalidHash() {
        try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
            mockedStatic.when(Utils::isDebug).thenReturn(false);
            when(application.getHash()).thenReturn("testHash");
            when(blockchainService.evaluateTransaction(anyString(), anyString(), anyString(), any())).thenReturn("false");

            boolean result = applicationChainCodeService.validateApplication(application);

            assertFalse(result);
            verify(blockchainService, times(1)).evaluateTransaction(CHANNEL_NAME, CHAINCODE_NAME, "validateApplication",
                    new String[] { "testHash" });
        }
    }

    @Test
    void testValidateApplicationHash_Valid() {
        when(application.getHash()).thenReturn("testHash");
        when(blockchainService.evaluateTransaction(anyString(), anyString(), anyString(), any())).thenReturn("true");

        boolean result = applicationChainCodeService.validateApplication(application);

        assertTrue(result);
    }
}