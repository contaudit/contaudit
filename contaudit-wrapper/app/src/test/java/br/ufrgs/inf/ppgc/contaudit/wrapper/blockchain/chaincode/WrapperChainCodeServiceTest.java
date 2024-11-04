package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.Wrapper;

class WrapperChainCodeServiceTest {
    @Mock
    private BlockchainService blockchainService;

    @Mock
    private Wrapper wrapper;

    @InjectMocks
    private WrapperChainCodeService wrapperChainCodeService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.closeable.close();
    }

    @Test
    void testUpdateWrapperHash() {
        when(wrapper.getHash()).thenReturn("someHash");

        this.wrapperChainCodeService.updateWrapperHash(wrapper);

        verify(blockchainService).submitTransaction("c1", "wrapper-chaincode", "updateHash", new String[]{"someHash"});
    }

    @Test
    void testValidateWrapper_WhenDebugAndInvalidWrapper() {
        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            utilities.when(Utils::isDebug).thenReturn(true);
            when(wrapper.getHash()).thenReturn("invalidHash");
            when(blockchainService.evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"invalidHash"}))
                .thenReturn("false");

            boolean result = this.wrapperChainCodeService.validateWrapper(wrapper);

            assertFalse(result);
            verify(blockchainService).submitTransaction("c1", "wrapper-chaincode", "updateHash", new String[]{"invalidHash"});
            verify(blockchainService, times(2))
                .evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"invalidHash"});
        }
    }

    @Test
    void testValidateWrapper_WhenDebugAndValidWrapper() {
        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            utilities.when(Utils::isDebug).thenReturn(true);
            when(wrapper.getHash()).thenReturn("validHash");
            when(blockchainService.evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"validHash"}))
                .thenReturn("true");

            boolean result = this.wrapperChainCodeService.validateWrapper(wrapper);

            assertTrue(result);
            verify(blockchainService).evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"validHash"});
        }
    }

    @Test
    void testValidateWrapper_WhenNotDebugAndValidWrapper() {
        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            utilities.when(Utils::isDebug).thenReturn(false);
            when(wrapper.getHash()).thenReturn("validHash");
            when(blockchainService.evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"validHash"}))
                .thenReturn("true");

            boolean result = this.wrapperChainCodeService.validateWrapper(wrapper);

            assertTrue(result);
            verify(blockchainService).evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"validHash"});
        }
    }

    @Test
    void testValidateWrapper_WhenNotDebugAndInvalidWrapper() {
        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            utilities.when(Utils::isDebug).thenReturn(false);
            when(wrapper.getHash()).thenReturn("invalidHash");
            when(blockchainService.evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"invalidHash"}))
                .thenReturn("false");

            boolean result = this.wrapperChainCodeService.validateWrapper(wrapper);

            assertFalse(result);
            verify(blockchainService).evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"invalidHash"});
        }
    }

    @Test
    void testValidateWrapper_ExceptionHandling() {
        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            utilities.when(Utils::isDebug).thenReturn(true);
            when(wrapper.getHash()).thenReturn("invalidHash");
            when(blockchainService.evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"invalidHash"}))
                .thenThrow(new RuntimeException("Test exception"));

            boolean result = this.wrapperChainCodeService.validateWrapper(wrapper);

            assertFalse(result);
            verify(blockchainService).evaluateTransaction("c1", "wrapper-chaincode", "validateHash", new String[]{"invalidHash"});
        }
    }
}