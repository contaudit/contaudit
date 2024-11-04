package br.ufrgs.inf.ppgc.contaudit.admin.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.WrapperChainCodeService;

public class WrapperService {
    private Logger logger = LoggerFactory.getLogger(WrapperService.class);
    private WrapperChainCodeService wrapperChainCodeService;

    public WrapperService(WrapperChainCodeService wrapperChainCodeService) {
        this.wrapperChainCodeService = wrapperChainCodeService;
    }

    public String getWrapperHash() {
        logger.info("Getting Wrapper Hash...");
        Wrapper wrapper = this.wrapperChainCodeService.getWrapperHash();

        return wrapper.getHash();
    }

    public void updateWrapperHash(String wrapperHash) {
        logger.info("Updating Wrapper Hash...");
        Wrapper wrapper = new Wrapper();
        wrapper.setHash(wrapperHash);

        this.wrapperChainCodeService.updateWrapperHash(wrapper);
    }
}