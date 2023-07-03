package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.wrapper.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;

public class ApplicationChainCodeService {
    private BlockchainService blockchain;
    private Logger logger = LoggerInstance.get();
    private boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;

    public ApplicationChainCodeService() throws IOException, URISyntaxException{
        blockchain = new BlockchainService();
    }

    public void insertApplication(Application application) {
        String channelName = "mychannel";
        String chaincodeName = "application-chaincode";
        String transactionName = "insert";

        blockchain.submitTransaction(channelName, chaincodeName, transactionName, new String[] { application.getHash(), new Gson().toJson(application) });
    }

    public boolean validateApplication(Application application){
        if (this.isDebug) {
            try {
                if (!this.validateApplicationHash(application)) {
                    logger.info("Debug Mode detected. Enabling Application...");
                    this.insertApplication(application);
                    return this.validateApplicationHash(application);
                } else {
                    return true;
                }
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
        }
        else {
            return this.validateApplicationHash(application);
        }
        return false;
    }

    private boolean validateApplicationHash(Application application){
        logger.info("Validating Application...");
        String channelName = "mychannel";
        String chaincodeName = "application-chaincode";
        String transactionName = "validateApplication";

        return Boolean.parseBoolean(blockchain.evaluateTransaction(channelName, chaincodeName, transactionName, new String[] { application.getHash() }));
    }
}
