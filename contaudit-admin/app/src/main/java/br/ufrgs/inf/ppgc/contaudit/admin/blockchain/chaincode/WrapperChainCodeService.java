package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.admin.wrapper.Wrapper;

public class WrapperChainCodeService {
    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCHODE_NAME = "wrapper-chaincode";

    public Wrapper getWrapperHash() {
        String transactionName = "getHash";

        Wrapper wrapper = new Wrapper();
        wrapper.setHash(new BlockchainService().evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] {}));
        return wrapper;
    }

    public void updateWrapperHash(Wrapper wrapper) {
        String transactionName = "updateHash";

        new BlockchainService().submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] { wrapper.getHash() });
    }
}
