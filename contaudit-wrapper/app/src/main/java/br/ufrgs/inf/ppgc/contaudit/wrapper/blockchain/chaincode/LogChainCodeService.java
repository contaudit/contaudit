package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import com.google.gson.Gson;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.log.Log;

public class LogChainCodeService {
    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCHODE_NAME = "log-chaincode";
    protected BlockchainService blockchainService;

    public LogChainCodeService(BlockchainService blockchain) {
        this.blockchainService = blockchain;
    }

    public void insertLog(Log log){
        this.blockchainService.submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, "insert", new String[] { log.getId(), new Gson().toJson(log) });
    }
}