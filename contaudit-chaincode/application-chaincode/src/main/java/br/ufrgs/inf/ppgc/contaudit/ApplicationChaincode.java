package br.ufrgs.inf.ppgc.contaudit;

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

public class ApplicationChaincode extends ChaincodeBase {
    protected static final Log logger = LogFactory.getLog(ApplicationChaincode.class);
    protected static final String ORGANIZATION = "auditor-contaudit-ppgc-inf-ufrgs-br";
    protected static final String START_KEY = "00000000-0000-0000-0000-0000000000";
    protected static final String END_KEY = "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzz";

    @Override
    public Response init(ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            logger.info("Invoking Application Chaincode...");
            String function = stub.getFunction();
            List<String> params = stub.getParameters();
            ClientIdentity identity = new ClientIdentity(stub);

            switch (function) {
                case "get":
                    return get(stub, identity);
                case "insert":
                    return insert(stub, params, identity);
                case "validateApplication":
                    return validateApplication(stub, params);
                default:
                    return ResponseUtils.newErrorResponse("Invalid invoke function name. Expecting one of: [\"get\", \"insert\", \"validateApplication\"]");
            }
        } catch (Exception e) {
            logger.error("Error invoking chaincode", e);
            return ResponseUtils.newErrorResponse(e);
        }
    }

    protected Response get(ChaincodeStub stub, ClientIdentity identity) {
        try {
            if (!isAuthorized(identity)) {
                return ResponseUtils.newErrorResponse(String.format("Access Denied for %s.", identity.getMSPID()));
            }

            logger.info(String.format("Start key: %s. End key: %s.", START_KEY, END_KEY));

            JSONArray applications = new JSONArray();
            QueryResultsIterator<KeyValue> results = null;

            try {
                results = stub.getStateByRange(START_KEY, END_KEY);

                for (KeyValue result : results) {
                    applications.put(new JSONObject(result.getStringValue()));
                }
            } finally {
                if (results != null) {
                    results.close();
                }
            }

            if (applications.length() > 0) {
                logger.info("Applications found: " + applications);
                return ResponseUtils.newSuccessResponse("Applications found.", applications.toString().getBytes());
            } else {
                return ResponseUtils.newSuccessResponse("No applications found.");
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
                return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting an application key and an application object.");
            }

            String applicationKey = args.get(0);
            String applicationObjString = args.get(1);
            stub.putStringState(applicationKey, applicationObjString);

            logger.info("Application saved!");
            return ResponseUtils.newSuccessResponse();
        } catch (Exception e) {
            logger.error("Error during insert operation", e);
            return ResponseUtils.newErrorResponse("Error during insert operation.");
        }
    }

    protected Response validateApplication(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            return ResponseUtils.newErrorResponse("Incorrect number of arguments. Expecting only an application key.");
        }

        String applicationKey = args.get(0);
        String applicationObjString = stub.getStringState(applicationKey);

        boolean isValid = (applicationObjString != null && !applicationObjString.isEmpty());
        return ResponseUtils.newSuccessResponse(isValid ? "Valid application." : "Invalid application.", Boolean.toString(isValid).getBytes());
    }

    protected boolean isAuthorized(ClientIdentity identity) throws JSONException {
        return ORGANIZATION.equals(identity.getMSPID());
    }

    public static void main(String[] args) {
        logger.info("OpenSSL available: " + OpenSsl.isAvailable());
        new ApplicationChaincode().start(args);
    }
}