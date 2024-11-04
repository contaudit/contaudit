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
    protected static final Log logger = LogFactory.getLog(ArtifactChaincode.class);
    protected static final String ORGANIZATION = "auditor-contaudit-ppgc-inf-ufrgs-br";
    protected static final String START_KEY = "00000000-0000-0000-0000-0000000000";
    protected static final String END_KEY = "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzz";
    protected ClientIdentity clientIdentity;

    public ArtifactChaincode(ClientIdentity clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    @Override
    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Artifact Chaincode...");
            String function = stub.getFunction();
            List<String> params = stub.getParameters();

            if (clientIdentity == null) {
                clientIdentity = createClientIdentity(stub);
            }

            switch (function) {
                case "get":
                    return get(stub, clientIdentity);
                case "insert":
                    return insert(stub, params, clientIdentity);
                case "validateArtifact":
                    return validateArtifact(stub, params);
                default:
                    return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"get\", \"insert\", \"validateArtifact\"]");
            }
        } catch (Exception e) {
            logger.error("Error invoking chaincode", e);
            return ResponseUtils.newErrorResponse(e);
        }
    }

    protected Response get(ChaincodeStub stub, ClientIdentity identity) {
        try {
            if (!isAuthorized(identity)) {
                return ResponseUtils.newErrorResponse("Access Denied for organization.");
            }

            logger.info(String.format("Start key: %s. End key: %s.", START_KEY, END_KEY));

            JSONArray artifacts = new JSONArray();
            QueryResultsIterator<KeyValue> results = null;

            try {
                results = stub.getStateByRange(START_KEY, END_KEY);

                for (KeyValue result : results) {
                    artifacts.put(new JSONObject(result.getStringValue()));
                }
            } finally {
                if (results != null) {
                    results.close();
                }
            }

            if (artifacts.length() > 0) {
                logger.info("Artifacts found: " + artifacts);
                return ResponseUtils.newSuccessResponse("Artifacts found.", artifacts.toString().getBytes());
            } else {
                return ResponseUtils.newSuccessResponse("No artifacts found.");
            }

        } catch (Exception e) {
            logger.error("Error during get operation", e);
            return ResponseUtils.newErrorResponse("Error during get operation.");
        }
    }

    protected Response insert(ChaincodeStub stub, List<String> args, ClientIdentity identity) {
        try {
            if (!isAuthorized(identity)) {
                return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", identity.getMSPID()));
            }

            if (args.size() != 2) {
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting an artifact key and an artifact object.");
            }

            String artifactKey = args.get(0);
            String artifactObjString = args.get(1);
            stub.putStringState(artifactKey, artifactObjString);

            logger.info("Artifact saved!");
            return ResponseUtils.newSuccessResponse();
        } catch (Exception e) {
            logger.error("Error during insert operation", e);
            return ResponseUtils.newErrorResponse("Error during insert operation.");
        }
    }

    protected Response validateArtifact(ChaincodeStub stub, List<String> args) {
        try {
            if (args.size() != 1) {
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting only an artifact key.");
            }
    
            String artifactKey = args.get(0);
            String artifactObjString = stub.getStringState(artifactKey);
    
            boolean isValid = (artifactObjString != null && !artifactObjString.isEmpty());
            return ResponseUtils.newSuccessResponse(isValid ? "Valid artifact." : "Invalid artifact.", Boolean.toString(isValid).getBytes());
        } catch (Exception e) {
            logger.error("Error during validateArtifact operation", e);
            return ResponseUtils.newErrorResponse("Error during validateArtifact operation.");
        }
    }   

    protected boolean isAuthorized(ClientIdentity identity) throws JSONException {
        return ORGANIZATION.equals(identity.getMSPID());
    }

    protected ClientIdentity createClientIdentity(ChaincodeStub stub) throws CertificateException, JSONException, IOException {
        return new ClientIdentity(stub);
    }

    public static void main(String[] args) {
        logger.info("OpenSSL available: " + OpenSsl.isAvailable());
        new ArtifactChaincode(null).startChaincode(args);
    }

    protected void startChaincode(String[] args) {
        this.start(args);
    }
}