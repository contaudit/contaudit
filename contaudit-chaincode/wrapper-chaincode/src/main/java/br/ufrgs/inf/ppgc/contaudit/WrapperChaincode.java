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
    private static Log logger = LogFactory.getLog(WrapperChaincode.class);
    private static final String WRAPPER_HASH_KEY = "WRAPPER_HASH";

    @Override
    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Wrapper Chaincode...");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("getHash")) {
                return getHash(stub);
            }
            if (func.equals("updateHash")) {
                return updateHash(stub, params);
            }
            if (func.equals("validateHash")) {
                return validateHash(stub, params);
            }
            return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"updateHash\", \"validateHash\"]");
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e);
        }
    }

    // get the wrapper hash on blockchain
    private Response getHash(ChaincodeStub stub) throws CertificateException, JSONException, IOException {
        String transactionOrganization = new ClientIdentity(stub).getMSPID();
        if (!transactionOrganization.equals("auditor-contaudit-ppgc-inf-ufrgs-br"))
            return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", transactionOrganization));   

        logger.info("Checking hash...");
        String hashValue = stub.getStringState(WRAPPER_HASH_KEY);

        return ResponseUtils.newSuccessResponse("Wrapper hash.", hashValue.getBytes());
    }

    // Update the wrapper hash on blockchain
    private Response updateHash(ChaincodeStub stub, List<String> args) throws CertificateException, JSONException, IOException {
        String transactionOrganization = new ClientIdentity(stub).getMSPID();
        if (!transactionOrganization.equals("auditor-contaudit-ppgc-inf-ufrgs-br"))
            return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", transactionOrganization));   

        if (args.size() != 1)
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting a wrapper hash.");

        logger.info("Updating Wrapper hash...");
        String wrapperHash = args.get(0);
        stub.putStringState(WRAPPER_HASH_KEY, wrapperHash);

        logger.info("Wrapper Hash updated!");
        return ResponseUtils.newSuccessResponse();
    }

    // validate an wrapper hash on blockchain
    private Response validateHash(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting only a hash string.");
        
        logger.info("Checking hash...");
        String hashParam = args.get(0);
        String hashValue = stub.getStringState(WRAPPER_HASH_KEY);

        if (hashParam.equals(hashValue)) {
            return ResponseUtils.newSuccessResponse("Valid wrapper hash.", "true".getBytes());
        } else {
            return ResponseUtils.newSuccessResponse("Invalid wrapper hash.", "false".getBytes());
        }
    }

    public static void main(String[] args) {
        logger.info("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new WrapperChaincode().start(args);
    }
}
