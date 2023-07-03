package br.ufrgs.inf.ppgc.contaudit.admin.wrapper;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.admin.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.WrapperChainCodeService;

public class WrapperService {
    private Logger logger = LoggerInstance.get();

    public String getWrapperHash() {
        logger.info("Getting Wrapper Hash...");
        
        WrapperChainCodeService wrapperChainCodeService = new WrapperChainCodeService();
        Wrapper wrapper = wrapperChainCodeService.getWrapperHash();
        return wrapper.getHash();
    }

    public void updateWrapperHash(String wrapperHash) {
        logger.info("Updating Wrapper Hash...");
        
        Wrapper wrapper = new Wrapper();
        wrapper.setHash(wrapperHash);

        WrapperChainCodeService wrapperChainCodeService = new WrapperChainCodeService();
        wrapperChainCodeService.updateWrapperHash(wrapper);
    }
}
