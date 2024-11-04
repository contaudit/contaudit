package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;

public class ArtifactChainCodeService {
    private static final String CHANNEL_NAME = "c1";
    private static final String CHAINCHODE_NAME = "artifact-chaincode";
    private BlockchainService blockchainService;

    public ArtifactChainCodeService(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    public List<Artifact> getArtifacts() {
        String transactionName = "get";
        String result = this.blockchainService.evaluateTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] {});
        if (result.isEmpty())
            return Collections.emptyList();
        
        Type listType = new TypeToken<List<Artifact>>() {}.getType();
        return new Gson().fromJson(result, listType);
    }

    public void insertArtifact(Artifact artifact) {
        String transactionName = "insert";
        this.blockchainService.submitTransaction(CHANNEL_NAME, CHAINCHODE_NAME, transactionName, new String[] { artifact.getHash(), new Gson().toJson(artifact) });
    }
}