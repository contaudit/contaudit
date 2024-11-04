package br.ufrgs.inf.ppgc.contaudit.wrapper;

import java.io.IOException;
import java.net.URISyntaxException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Startup {
    protected static Logger logger = LoggerFactory.getLogger(Startup.class);
    protected static String instanceName;
    protected CommandFactory commandFactory;

    public Startup(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public static void main(String[] args) {
        new Startup(
            new CommandFactory()
        ).run(args);    
    }

    public void run(String[] args) {
        args = configureArgsForDebug(args);

        try {
            validateArgs(args);
            setInstanceName(args[0]);
            MDC.put("prefix", instanceName);

            String currentDirectory = String.format("Current directory: %s", Utils.currentDirectory());
            logger.info("Initializing ContAudIT Wrapper...");
            logger.info(currentDirectory);

            executeCommand(args[1], this.commandFactory);
        } catch (InvalidArgumentException e) {
            logger.error("Invalid argument exception: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
        } finally {
            logger.info("Finishing ContAudIT Wrapper...");
        }
    }

    public static String getInstanceName() {
        return instanceName;
    }

    protected void setInstanceName(String name) {
        instanceName = name;
    }

    protected static String[] configureArgsForDebug(String[] args) {
        if (Utils.isDebug()) {
            return new String[] {
                "Debug",
                "./../contaudit-samples/executor/executor ./../contaudit-samples/executor/artifacts/apache2.workflow"
            };
        }
        return args;
    }

    protected static void validateArgs(String[] args) throws InvalidArgumentException {
        if (args == null || args.length < 2) {
            throw new InvalidArgumentException("Incorrect number of arguments. Expecting an instance identifier and a command line text.");
        }
    }

    protected static void executeCommand(String command, CommandFactory commandFactory) throws IOException, URISyntaxException {
        Command cmd = commandFactory.createCommand(command);
        cmd.execute();
    }
}