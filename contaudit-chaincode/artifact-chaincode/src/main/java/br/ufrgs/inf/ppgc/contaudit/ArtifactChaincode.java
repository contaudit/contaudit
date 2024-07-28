/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package br.ufrgs.inf.ppgc.contaudit;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ArtifactChaincode extends ChaincodeBase {
    private static Log logger = LogFactory.getLog(ArtifactChaincode.class);

    @Override
    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Artifact Chaincode...");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("get")) {
                return get(stub);
            }
            if (func.equals("insert")) {
                return insert(stub, params);
            }
            if (func.equals("validateArtifact")) {
                return validateArtifact(stub, params);
            }
            return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"insert\", \"validateArtifact\"]");
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e);
        }
    }

    // list the artifacts on blockchain
    private Response get(ChaincodeStub stub) throws Exception {
        String transactionOrganization = new ClientIdentity(stub).getMSPID();
        if (!transactionOrganization.equals("auditor-contaudit-ppgc-inf-ufrgs-br"))
            return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", transactionOrganization));   

        String startKey = "00000000-0000-0000-0000-0000000000";
        String endKey = "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzz";
        logger.info(String.format("Start key: %s. End key: %s.", startKey, endKey));

        logger.info("Searching artifacts...");
        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startKey, endKey);
        
        JSONArray logsJSON = new JSONArray();
        for (KeyValue result : results) {
            try {
                String logState = result.getStringValue();
                JSONObject logJSON = new JSONObject(logState);
                logsJSON.put(logJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        results.close();
        logger.info("Artifacts founded: " + logsJSON);

        if (logsJSON.length() > 0) {
            return ResponseUtils.newSuccessResponse("Artifacts founded.", logsJSON.toString().getBytes());
        } else {
            return ResponseUtils.newSuccessResponse("No artifacts founded.");
        }    
    }

    // insert an artifact on blockchain
    private Response insert(ChaincodeStub stub, List<String> args) throws CertificateException, JSONException, IOException {
        String transactionOrganization = new ClientIdentity(stub).getMSPID();
        if (!transactionOrganization.equals("auditor-contaudit-ppgc-inf-ufrgs-br"))
            return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", transactionOrganization));   

        if (args.size() != 2) {
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting an artifact key and an artifact object.");
        }
        
        logger.info("Saving artifact...");
        String artifactKey = args.get(0);
        String artifactObjString = args.get(1);
        stub.putStringState(artifactKey, artifactObjString);

        logger.info("Artifact saved!");
        return ResponseUtils.newSuccessResponse();
    }

    // validate an artifact on blockchain
    private Response validateArtifact(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting only an artifact key.");
        
        logger.info("Checking artifact...");
        String artifactKey = args.get(0);
        String artifactObjString = stub.getStringState(artifactKey);

        if (!artifactObjString.isEmpty()) {
            return ResponseUtils.newSuccessResponse("Valid artifact.", "true".getBytes());
        } else {
            return ResponseUtils.newSuccessResponse("Invalid artifact.", "false".getBytes());
        }
    }

    public static void main(String[] args) {
        logger.info("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new ArtifactChaincode().start(args);
    }
}
