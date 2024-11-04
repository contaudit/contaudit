package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.admin.wrapper.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WrapperChainCodeServiceTest {
    private WrapperChainCodeService wrapperChainCodeService;
    private BlockchainService blockchainService;

    @BeforeEach
    public void setUp() {
        blockchainService = Mockito.mock(BlockchainService.class);
        wrapperChainCodeService = new WrapperChainCodeService(blockchainService);
    }

    @Test
    void testGetWrapperHash() {
        String expectedHash = "abc123";
        when(blockchainService.evaluateTransaction(
                eq("c1"),
                eq("wrapper-chaincode"),
                eq("getHash"),
                any(String[].class)
        )).thenReturn(expectedHash);

        Wrapper result = wrapperChainCodeService.getWrapperHash();

        assertEquals(expectedHash, result.getHash());
        verify(blockchainService, times(1)).evaluateTransaction(
                eq("c1"),
                eq("wrapper-chaincode"),
                eq("getHash"),
                any(String[].class)
        );
    }

    @Test
    void testUpdateWrapperHash() {
        Wrapper wrapper = new Wrapper();
        wrapper.setHash("newHash");

        wrapperChainCodeService.updateWrapperHash(wrapper);

        verify(blockchainService, times(1)).submitTransaction(
            "c1",
            "wrapper-chaincode",
            "updateHash",
            new String[]{"newHash"}
        );
    }
}