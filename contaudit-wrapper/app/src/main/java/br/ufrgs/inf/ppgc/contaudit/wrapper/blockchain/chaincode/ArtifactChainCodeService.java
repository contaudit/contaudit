package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;

public class ArtifactChainCodeService {
    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCHODE_NAME = "artifact-chaincode";
    protected Logger logger = LoggerFactory.getLogger(ArtifactChainCodeService.class);
    protected BlockchainService blockchainService;

    public ArtifactChainCodeService(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    public void insertArtifact(Artifact artifact) {
        this.blockchainService.submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "insert", new String[] { artifact.getHash(), new Gson().toJson(artifact) });
    }

    public boolean validateArtifact(Artifact artifact){
        if (Utils.isDebug()) {
            try {
                if (!this.validateArtifactHash(artifact)) {
                    logger.info("Debug Mode detected. Enabling Artifact...");
                    this.insertArtifact(artifact);
                    return this.validateArtifactHash(artifact);
                } else {
                    return true;
                }
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
        }
        else {
            return this.validateArtifactHash(artifact);
        }
        return false;
    }

    protected boolean validateArtifactHash(Artifact artifact){
        logger.info("Validating Artifact...");
        return Boolean.parseBoolean(this.blockchainService.evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "validateArtifact", new String[] { artifact.getHash() }));
    }
}