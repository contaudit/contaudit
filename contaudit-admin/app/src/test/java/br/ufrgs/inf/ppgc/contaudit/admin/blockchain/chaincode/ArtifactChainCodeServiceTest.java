package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;

class ArtifactChainCodeServiceTest {
    private ArtifactChainCodeService artifactChainCodeService;
    private BlockchainService blockchainService;
    private Gson gson;

    @BeforeEach
    void setUp() {
        blockchainService = mock(BlockchainService.class);
        artifactChainCodeService = new ArtifactChainCodeService(blockchainService);
        gson = new Gson();
    }

    @Test
    void testGetArtifacts() {
        List<Artifact> mockArtifacts = new ArrayList<>();
        Artifact artifact1 = new Artifact(); 
        artifact1.setHash("hash1"); 
        mockArtifacts.add(artifact1);
        String jsonResponse = gson.toJson(mockArtifacts);
        when(blockchainService.evaluateTransaction(anyString(), anyString(), anyString(), any(String[].class)))
                .thenReturn(jsonResponse);

        List<Artifact> artifacts = artifactChainCodeService.getArtifacts();

        verify(blockchainService).evaluateTransaction("c1", "artifact-chaincode", "get", new String[] {});
        assertNotNull(artifacts);
        assertEquals(1, artifacts.size());
        assertEquals("hash1", artifacts.get(0).getHash());
    }

    @Test
    void testInsertArtifact() {
        Artifact artifact = new Artifact();
        artifact.setHash("hash1");

        artifactChainCodeService.insertArtifact(artifact);

        verify(blockchainService).submitTransaction("c1", "artifact-chaincode", "insert", 
                new String[] { "hash1", gson.toJson(artifact) });
    }
}