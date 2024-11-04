package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.Wrapper;

public class WrapperChainCodeService {
    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCHODE_NAME = "wrapper-chaincode";
    protected Logger logger = LoggerFactory.getLogger(WrapperChainCodeService.class);
    protected BlockchainService blockchainService;

    public WrapperChainCodeService(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    public void updateWrapperHash(Wrapper wrapper) {
        this.blockchainService.submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "updateHash", new String[] { wrapper.getHash() });
    }

    public boolean validateWrapper(Wrapper wrapper){
        if (Utils.isDebug()) {
            try {
                if (!this.validateWrapperHash(wrapper)) {
                    logger.info("Debug Mode detected. Updating Wrapper hash...");
                    this.updateWrapperHash(wrapper);
                    return this.validateWrapperHash(wrapper);
                }
                else {
                    return true;
                }
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
        }
        else {
            return this.validateWrapperHash(wrapper);
        }
        return false;
    }

    private boolean validateWrapperHash(Wrapper wrapper){
        logger.info("Validating Wrapper...");
        return Boolean.parseBoolean(this.blockchainService.evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "validateHash", new String[] { wrapper.getHash() }));
    }
}