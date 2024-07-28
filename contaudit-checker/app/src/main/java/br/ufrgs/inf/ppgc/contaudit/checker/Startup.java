package br.ufrgs.inf.ppgc.contaudit.checker;

import java.io.Console;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufrgs.inf.ppgc.contaudit.checker.environment.EnvironmentService;

public class Startup {
    private static Logger logger = LoggerFactory.getLogger(Startup.class);
 
    public static void main(String[] args) {
        try {
            logger.info("Initializing ContAudIT Checker...");
            String logString = String.format("Current directory: %s", Utils.currentDirectory());
            logger.info(logString);
            int resultCode = 1;
            while (resultCode == 1) {
                resultCode = showMenu();
            }
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        finally {
            logger.info("Finishing ContAudIT Checker...");
        }
    }

    private static int showMenu() throws InterruptedException, IOException, URISyntaxException {
        logger.info("---------------------------");
        logger.info("Menu:");
        logger.info("0 - Exit");
        logger.info("1 - Save Environment State");
        logger.info("2 - Validate Environment State by last environment hash stored");
        logger.info("3 - Validate Environment State by applying environment diffs since the last saved state");
        logger.info("---------------------------");
        logger.info("Choose an option: ");

        Console console = System.console();
        String menuOption = console.readLine();

        EnvironmentService envService = new EnvironmentService();
        switch (menuOption) {
            case "0":
                return 0;
            case "1":
                envService.saveCurrentState();
                break;
            case "2":
                envService.validateStateByLastHashStored();
                break;
            case "3":
                envService.validateStateByCalculatedHash();
                break;
            default:
                throw new InvalidParameterException("Invalid menu option.");
        } 

        return 1;
    }
}
