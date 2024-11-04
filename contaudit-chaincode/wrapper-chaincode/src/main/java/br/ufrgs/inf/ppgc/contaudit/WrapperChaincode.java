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
import org.json.JSONException;

public class WrapperChaincode extends ChaincodeBase {
    protected static final Log logger = LogFactory.getLog(WrapperChaincode.class);
    protected static final String WRAPPER_HASH_KEY = "WRAPPER_HASH";
    protected static final String ORGANIZATION = "auditor-contaudit-ppgc-inf-ufrgs-br";
    protected ClientIdentity clientIdentity;

    public WrapperChaincode(ClientIdentity clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    @Override
    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Wrapper Chaincode...");
            String function = stub.getFunction();
            List<String> params = stub.getParameters();

            if (clientIdentity == null) {
                clientIdentity = createClientIdentity(stub);
            }

            switch (function) {
                case "getHash":
                    return getHash(stub, clientIdentity);
                case "updateHash":
                    return updateHash(stub, params, clientIdentity);
                case "validateHash":
                    return validateHash(stub, params);
                default:
                    return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"getHash\", \"updateHash\", \"validateHash\"]");
            }
        } catch (Exception e) {
            logger.error("Error invoking WrapperChaincode", e);
            return ResponseUtils.newErrorResponse(e);
        }
    }

    protected Response getHash(ChaincodeStub stub, ClientIdentity identity) {
        try {
            if (!isAuthorized(identity)) {
                return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", identity.getMSPID()));
            }

            logger.info("Fetching Wrapper hash...");
            String hashValue = stub.getStringState(WRAPPER_HASH_KEY);

            if (hashValue == null || hashValue.isEmpty()) {
                return ResponseUtils.newErrorResponse("Wrapper hash not found.");
            }

            logger.info("Wrapper hash found: " + hashValue);
            return ResponseUtils.newSuccessResponse("Wrapper hash.", hashValue.getBytes());
        } catch (Exception e) {
            logger.error("Error during getHash operation", e);
            return ResponseUtils.newErrorResponse("Error during getHash operation.");
        }
    }

    protected Response updateHash(ChaincodeStub stub, List<String> args, ClientIdentity identity) {
        try {
            if (!isAuthorized(identity)) {
                return ResponseUtils.newErrorResponse("Access Denied for organization.");
            }

            if (args.size() != 1) {
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting a wrapper hash.");
            }

            String wrapperHash = args.get(0);
            logger.info("Updating Wrapper hash: " + wrapperHash);
            stub.putStringState(WRAPPER_HASH_KEY, wrapperHash);

            logger.info("Wrapper hash updated successfully.");
            return ResponseUtils.newSuccessResponse();
        } catch (Exception e) {
            logger.error("Error during updateHash operation", e);
            return ResponseUtils.newErrorResponse("Error during updateHash operation.");
        }
    }

    protected Response validateHash(ChaincodeStub stub, List<String> args) {
        try {
            if (args.size() != 1) {
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting only a hash string.");
            }

            String hashParam = args.get(0);
            String storedHash = stub.getStringState(WRAPPER_HASH_KEY);

            boolean isValid = hashParam.equals(storedHash);
            logger.info("Validation result for hash: " + isValid);
            return ResponseUtils.newSuccessResponse(isValid ? "Valid wrapper hash." : "Invalid wrapper hash.", Boolean.toString(isValid).getBytes());
        } catch (Exception e) {
            logger.error("Error during validateHash operation", e);
            return ResponseUtils.newErrorResponse("Error during validateHash operation.");
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
        new WrapperChaincode(null).start(args);
    }
}