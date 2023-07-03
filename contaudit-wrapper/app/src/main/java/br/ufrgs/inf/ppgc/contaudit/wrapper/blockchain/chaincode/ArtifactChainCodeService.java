package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.wrapper.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;

public class ArtifactChainCodeService {
    private BlockchainService blockchain;
    private Logger logger = LoggerInstance.get();
    private boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;

    public ArtifactChainCodeService() throws IOException, URISyntaxException{
        blockchain = new BlockchainService();
    }

    public void insertArtifact(Artifact artifact) {
        String channelName = "mychannel";
        String chaincodeName = "artifact-chaincode";
        String transactionName = "insert";

        blockchain.submitTransaction(channelName, chaincodeName, transactionName, new String[] { artifact.getHash(), new Gson().toJson(artifact) });
    }

    public boolean validateArtifact(Artifact artifact){
        if (this.isDebug) {
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

    private boolean validateArtifactHash(Artifact artifact){
        logger.info("Validating Artifact...");
        String channelName = "mychannel";
        String chaincodeName = "artifact-chaincode";
        String transactionName = "validateArtifact";

        return Boolean.parseBoolean(blockchain.evaluateTransaction(channelName, chaincodeName, transactionName, new String[] { artifact.getHash() }));
    }
}
