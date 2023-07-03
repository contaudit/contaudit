package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;

public class ArtifactChainCodeService {
    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCHODE_NAME = "artifact-chaincode";

    public List<Artifact> getArtifacts() {
        String transactionName = "get";

        String result = new BlockchainService().evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] {});
        Type listType = new TypeToken<List<Artifact>>() {}.getType();
        return new Gson().fromJson(result, listType);
    }

    public void insertArtifact(Artifact artifact) {
        String transactionName = "insert";

        new BlockchainService().submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] { artifact.getHash(), new Gson().toJson(artifact) });
    }
}
