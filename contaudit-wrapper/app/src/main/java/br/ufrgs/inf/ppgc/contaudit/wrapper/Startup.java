package br.ufrgs.inf.ppgc.contaudit.wrapper;

import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Startup {
    private static String instanceName;
    private static Logger logger = LoggerFactory.getLogger(Startup.class);

    public static String getInstanceName() {
        return instanceName;
    }

    public static void main(String[] args) throws InvalidArgumentException {
        if (Utils.isDebug()) 
            args = new String[] { "Debug", "./../contaudit-samples/executor/executor ./../contaudit-samples/executor/artifacts/apache2.workflow" };
        if (args.length < 2) 
            throw new InvalidArgumentException("Incorrect number of arguments. Expecting a instance identifier and a command line text.");
        
        instanceName = args[0];
        MDC.put("prefix", instanceName);

        try {
            logger.info("Initializing ContAudIT Wrapper...");

            String currentDirectory = String.format("Current directory: %s", Utils.getCurrentDirectory());
            logger.info(currentDirectory);
            
            new Command(args[1]).execute();
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        finally {
            logger.info("Finishing ContAudIT Wrapper...");
        }
    }
}
