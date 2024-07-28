/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package br.ufrgs.inf.ppgc.contaudit;

import java.util.List;

import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LogChaincode extends ChaincodeBase {

    private static Log logger = LogFactory.getLog(LogChaincode.class);

    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Log Chaincode...");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("insert")) {
                return insert(stub, params);
            }
            if (func.equals("queryByRange")) {
                return queryByRange(stub, params);
            }
            return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"insert\"]");
        } catch (Exception e) {
            return ResponseUtils.newErrorResponse(e);
        }
    }

    // insert a log on blockchain
    private Response insert(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting a log key and a log object.");
        }

        logger.info("Saving object...");
        String logKey = args.get(0);
        String logString = args.get(1);
        stub.putStringState(logKey, logString);

        logger.info("Log saved!");
        return ResponseUtils.newSuccessResponse();
    }

    // query logs on blockchain by a key range
    private Response queryByRange(ChaincodeStub stub, List<String> args) throws Exception {
        if (args.size() != 2) {
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting a start log key and an end log key.");
        }
        logger.info("Reading arguments...");
        String startLogKey = args.get(0);
        String endLogKey = args.get(1);
        logger.info(String.format("Start key: %s. End key: %s.", startLogKey, endLogKey));

        logger.info("Searching logs...");
        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startLogKey, endLogKey);
        
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
        logger.info("Logs founded: " + logsJSON);

        if (logsJSON.length() > 0) {
            return ResponseUtils.newSuccessResponse("Logs founded.", logsJSON.toString().getBytes());
        } else {
            return ResponseUtils.newSuccessResponse("No logs founded.");
        }        
    }

    public static void main(String[] args) {
        logger.info("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new LogChaincode().start(args);
    }

}
