package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;

public class ApplicationChainCodeService {
    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCHODE_NAME = "application-chaincode";
    protected Logger logger = LoggerFactory.getLogger(ApplicationChainCodeService.class);
    protected BlockchainService blockchainService;

    public ApplicationChainCodeService(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    public void insertApplication(Application application) {
        this.blockchainService.submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "insert", new String[] { application.getHash(), new Gson().toJson(application) });
    }

    public boolean validateApplication(Application application){
        if (Utils.isDebug()) {
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
        return Boolean.parseBoolean(this.blockchainService.evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "validateApplication", new String[] { application.getHash() }));
    }
}