package br.ufrgs.inf.ppgc.contaudit.admin.application.artifact;

import java.util.List;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.admin.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ArtifactChainCodeService;

public class ArtifactService {
    private Logger logger = LoggerInstance.get();
    
    public List<Artifact> getArtifacts() {
        logger.info("Getting Artifacts...");

        ArtifactChainCodeService artifactChainCodeService = new ArtifactChainCodeService();
        return artifactChainCodeService.getArtifacts();
    }

    public void createArtifact(Artifact artifact) {
        logger.info("Creating new Artifact...");
        
        ArtifactChainCodeService artifactChainCodeService = new ArtifactChainCodeService();
        artifactChainCodeService.insertArtifact(artifact);
    }
}
