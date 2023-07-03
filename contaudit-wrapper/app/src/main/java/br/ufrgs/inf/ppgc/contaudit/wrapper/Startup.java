package br.ufrgs.inf.ppgc.contaudit.wrapper;

import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.slf4j.Logger;

public class Startup {
    private static String instanceName;

    public static String getInstanceName() {
        return instanceName;
    }

    public static void main(String[] args) throws InvalidArgumentException {
        if (Utils.isDebug()) 
            args = new String[] { "Debug#1", "./../contaudit-samples/executor/executor ./../contaudit-samples/executor/artifacts/hexedit.workflow" };
        if (args.length < 2) 
            throw new InvalidArgumentException("Incorrect number of arguments. Expecting a instance identifier and a command line text.");
        
        instanceName = args[0];
        Logger logger = LoggerInstance.get();
        logger.info("Initializing ContAudIT Wrapper...");
        logger.info(String.format("Current directory: %s", Utils.getCurrentDirectory()));

        new Command(args[1]).execute();

        logger.info("Finishing ContAudIT Wrapper...");
    }
}
