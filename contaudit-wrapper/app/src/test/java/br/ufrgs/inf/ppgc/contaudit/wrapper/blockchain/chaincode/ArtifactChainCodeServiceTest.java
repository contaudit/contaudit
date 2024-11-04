package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;

class ArtifactChainCodeServiceTest {
    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private ArtifactChainCodeService artifactChainCodeService;

    private Artifact artifact;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        artifact = new Artifact();
        artifact.setHash("hash123");
        artifact.setApplication("TestApplication");
        artifact.setName("TestArtifact");
        artifact.setFullPath("/path/to/artifact");
        artifact.setContent("ArtifactContent");
    }

    @Test
    void testInsertArtifact() {
        this.artifactChainCodeService.insertArtifact(artifact);

        verify(blockchainService, times(1)).submitTransaction(
            "c1", 
            "artifact-chaincode", 
            "insert", 
            new String[] { artifact.getHash(), new Gson().toJson(artifact) }
        );
    }

    @Test
    void testValidateArtifact_ArtifactValid_DebugMode() {
        try (MockedStatic<Utils> mockedStaticUtils = mockStatic(Utils.class)) {
            mockedStaticUtils.when(Utils::isDebug).thenReturn(true);
            when(blockchainService.evaluateTransaction(
                anyString(), 
                anyString(), 
                eq("validateArtifact"), 
                any(String[].class))
            ).thenReturn("true");

            boolean result = this.artifactChainCodeService.validateArtifact(artifact);

            verify(blockchainService, times(1)).evaluateTransaction(
                "c1", 
                "artifact-chaincode", 
                "validateArtifact", 
                new String[] { artifact.getHash() }
            );
            verify(blockchainService, times(0)).submitTransaction(anyString(), anyString(), anyString(), any());
            assertTrue(result);
        }
    }

    @Test
    void testValidateArtifact_ArtifactNotValid_NonDebugMode() {
        try (MockedStatic<Utils> mockedStaticUtils = mockStatic(Utils.class)) {
            mockedStaticUtils.when(Utils::isDebug).thenReturn(false);
            when(blockchainService.evaluateTransaction(
                anyString(), 
                anyString(), 
                eq("validateArtifact"), 
                any(String[].class))
            ).thenReturn("false");

            boolean result = this.artifactChainCodeService.validateArtifact(artifact);

            verify(blockchainService, times(1)).evaluateTransaction(
                "c1", 
                "artifact-chaincode", 
                "validateArtifact", 
                new String[] { artifact.getHash() }
            );
            verify(blockchainService, times(0)).submitTransaction(anyString(), anyString(), anyString(), any());
            assertFalse(result);
        }
    }

    @Test
    void testValidateArtifact_ArtifactValid_NonDebugMode() {
        try (MockedStatic<Utils> mockedStaticUtils = mockStatic(Utils.class)) {
            mockedStaticUtils.when(Utils::isDebug).thenReturn(false);
            when(blockchainService.evaluateTransaction(
                anyString(), 
                anyString(), 
                eq("validateArtifact"), 
                any(String[].class))
            ).thenReturn("true");

            boolean result = this.artifactChainCodeService.validateArtifact(artifact);

            verify(blockchainService, times(1)).evaluateTransaction(
                "c1", 
                "artifact-chaincode", 
                "validateArtifact", 
                new String[] { artifact.getHash() }
            );
            verify(blockchainService, times(0)).submitTransaction(anyString(), anyString(), anyString(), any());
            assertTrue(result);
        }
    }
}