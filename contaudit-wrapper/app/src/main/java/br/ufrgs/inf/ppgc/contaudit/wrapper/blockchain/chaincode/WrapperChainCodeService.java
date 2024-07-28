package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.Wrapper;

public class WrapperChainCodeService {
    private Logger logger = LoggerFactory.getLogger(WrapperChainCodeService.class);
    private boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;

    public void updateWrapperHash(Wrapper wrapper) {
        String channelName = "c1";
        String chaincodeName = "wrapper-chaincode";
        String transactionName = "updateHash";

        new BlockchainService().submitTransaction(channelName, chaincodeName, transactionName, new String[] { wrapper.getHash() });
    }

    public boolean validateWrapper(Wrapper wrapper){
        if (this.isDebug) {
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
        String channelName = "c1";
        String chaincodeName = "wrapper-chaincode";
        String transactionName = "validateHash";

        return Boolean.parseBoolean(new BlockchainService().evaluateTransaction(channelName, chaincodeName, transactionName, new String[] { wrapper.getHash() }));
    }
}
