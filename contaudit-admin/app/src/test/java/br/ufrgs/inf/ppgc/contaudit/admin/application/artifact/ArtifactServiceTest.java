package br.ufrgs.inf.ppgc.contaudit.admin.application.artifact;

import static org.mockito.Mockito.*;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ArtifactChainCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArtifactServiceTest {
    @Mock
    private ArtifactChainCodeService artifactChainCodeService;

    private ArtifactService artifactService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        artifactService = new ArtifactService(artifactChainCodeService);
    }

    @Test
    void testGetArtifacts() {
        List<Artifact> mockArtifacts = Arrays.asList(new Artifact(), new Artifact());
        when(artifactChainCodeService.getArtifacts()).thenReturn(mockArtifacts);

        List<Artifact> artifacts = artifactService.getArtifacts();

        assertEquals(mockArtifacts, artifacts);
        verify(artifactChainCodeService).getArtifacts();
    }

    @Test
    void testCreateArtifact() {
        Artifact artifact = new Artifact();

        artifactService.createArtifact(artifact);

        verify(artifactChainCodeService).insertArtifact(artifact);
    }
}