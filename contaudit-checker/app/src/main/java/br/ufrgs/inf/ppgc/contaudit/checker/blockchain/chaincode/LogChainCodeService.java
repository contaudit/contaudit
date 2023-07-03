package br.ufrgs.inf.ppgc.contaudit.checker.blockchain.chaincode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import br.ufrgs.inf.ppgc.contaudit.checker.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.checker.log.Log;

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

    public List<Log> queryLogsByDateRange(String startDate, String endDate) {
        String channelName = "mychannel";
        String chaincodeName = "log-chaincode";
        String transactionName = "queryByRange";

        String result = blockchain.evaluateTransaction(channelName, chaincodeName, transactionName, new String[] { startDate, endDate });
        try {
            return new Gson().fromJson(result, new TypeToken<List<Log>>(){}.getType());
        }
        catch (JsonSyntaxException ex) {
            return new ArrayList<>();
        }
    }
}
