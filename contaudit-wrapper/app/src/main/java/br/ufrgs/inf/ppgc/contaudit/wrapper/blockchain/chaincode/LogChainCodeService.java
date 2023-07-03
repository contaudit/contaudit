package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.gson.Gson;

import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.log.Log;

public class LogChainCodeService {
    private BlockchainService blockchain;

    public LogChainCodeService() throws IOException, URISyntaxException{
        blockchain = new BlockchainService();
    }

    public void insertLog(Log log){
        String channelName = "mychannel";
        String chaincodeName = "log-chaincode";
        String transactionName = "insert";

        blockchain.submitTransaction(channelName, chaincodeName, transactionName, new String[] { log.getId(), new Gson().toJson(log) });
    }
}
