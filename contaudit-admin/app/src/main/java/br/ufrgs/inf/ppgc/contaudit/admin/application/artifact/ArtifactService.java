package br.ufrgs.inf.ppgc.contaudit.admin.application.artifact;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ArtifactChainCodeService;

public class ArtifactService {
    private Logger logger = LoggerFactory.getLogger(ArtifactService.class);
    private ArtifactChainCodeService artifactChainCodeService;

    public ArtifactService(ArtifactChainCodeService artifactChainCodeService) {
        this.artifactChainCodeService = artifactChainCodeService;
    }

    public List<Artifact> getArtifacts() {
        logger.info("Getting Artifacts...");
        return this.artifactChainCodeService.getArtifacts();
    }

    public void createArtifact(Artifact artifact) {
        logger.info("Creating new Artifact...");
        this.artifactChainCodeService.insertArtifact(artifact);
    }
}
