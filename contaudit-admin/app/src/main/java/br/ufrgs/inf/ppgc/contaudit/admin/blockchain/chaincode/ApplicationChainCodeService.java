package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import br.ufrgs.inf.ppgc.contaudit.admin.application.Application;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;

public class ApplicationChainCodeService {
    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCHODE_NAME = "application-chaincode";

    public List<Application> getApplications() {
        String transactionName = "get";

        String result = new BlockchainService().evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] {});
        Type listType = new TypeToken<List<Application>>() {}.getType();
        return new Gson().fromJson(result, listType);
    }

    public void insertApplication(Application application) {
        String transactionName = "insert";

        new BlockchainService().submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] { application.getHash(), new Gson().toJson(application) });
    }
}
