/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package br.ufrgs.inf.ppgc.contaudit;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import io.netty.handler.ssl.OpenSsl;

public class LogChaincode extends ChaincodeBase {
    protected static final Log logger = LogFactory.getLog(LogChaincode.class);

    @Override
    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Log Chaincode...");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();

            switch (func) {
                case "insert":
                    return insert(stub, params);
                case "queryByRange":
                    return queryByRange(stub, params);
                default:
                    return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"insert\", \"queryByRange\"]");
            }
        } catch (Exception e) {
            logger.error("Error invoking chaincode", e);
            return ResponseUtils.newErrorResponse(e);
        }
    }

    protected Response insert(ChaincodeStub stub, List<String> args) {
        try {
            if (args.size() != 2) {
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting a log key and a log object.");
            }

            logger.info("Saving log...");
            String logKey = args.get(0);
            String logString = args.get(1);
            stub.putStringState(logKey, logString);

            logger.info("Log saved successfully!");
            return ResponseUtils.newSuccessResponse("Log saved successfully.");
        } catch (Exception e) {
            logger.error("Error during insert operation", e);
            return ResponseUtils.newErrorResponse("Error during insert operation.");
        }
    }

    protected Response queryByRange(ChaincodeStub stub, List<String> args) {
        try {
            if (args.size() != 2) {
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting a start log key and an end log key.");
            }

            logger.info("Reading arguments...");
            String startLogKey = args.get(0);
            String endLogKey = args.get(1);
            logger.info(String.format("Start key: %s, End key: %s.", startLogKey, endLogKey));

            logger.info("Searching logs...");
            QueryResultsIterator<KeyValue> results = null;
            JSONArray logsJSON = new JSONArray();

            try {
                results = stub.getStateByRange(startLogKey, endLogKey);
                for (KeyValue result : results) {
                    String logState = result.getStringValue();
                    logsJSON.put(new JSONObject(logState));
                }
            } finally {
                if (results != null) {
                    results.close();
                }
            }

            if (logsJSON.length() > 0) {
                logger.info("Logs found: " + logsJSON);
                return ResponseUtils.newSuccessResponse("Logs found.", logsJSON.toString().getBytes());
            } else {
                return ResponseUtils.newSuccessResponse("No logs found.");
            }
        } catch (Exception e) {
            logger.error("Error during queryByRange operation", e);
            return ResponseUtils.newErrorResponse("Error during queryByRange operation.");
        }
    }

    public static void main(String[] args) {
        logger.info("OpenSSL available: " + OpenSsl.isAvailable());
        new LogChaincode().start(args);
    }
}