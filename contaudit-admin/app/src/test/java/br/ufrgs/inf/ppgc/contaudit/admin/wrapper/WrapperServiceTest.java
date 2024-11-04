package br.ufrgs.inf.ppgc.contaudit.admin.wrapper;

import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.WrapperChainCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WrapperServiceTest {
    @Mock
    private WrapperChainCodeService wrapperChainCodeService;

    @InjectMocks
    private WrapperService wrapperService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWrapperHash() {
        Wrapper mockWrapper = new Wrapper();
        mockWrapper.setHash("mockHash");
        when(wrapperChainCodeService.getWrapperHash()).thenReturn(mockWrapper);

        String result = wrapperService.getWrapperHash();

        assertEquals("mockHash", result);
        verify(wrapperChainCodeService, times(1)).getWrapperHash();
    }

    @Test
    void testUpdateWrapperHash() {
        String newHash = "newHash";

        wrapperService.updateWrapperHash(newHash);

        verify(wrapperChainCodeService, times(1)).updateWrapperHash(any(Wrapper.class));
    }
}