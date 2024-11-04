package br.ufrgs.inf.ppgc.contaudit.checker;

import java.io.Console;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.checker.blockchain.chaincode.LogChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.checker.environment.EnvironmentService;
import br.ufrgs.inf.ppgc.contaudit.checker.log.LogService;
import br.ufrgs.inf.ppgc.contaudit.checker.security.HashService;

public class Startup {
    protected Logger logger = LoggerFactory.getLogger(Startup.class);
    protected Console console;
    protected Scanner scanner;
    protected EnvironmentService envService;

    public Startup(EnvironmentService envService) {
        this.envService = envService;
        this.console = System.console();
        if (this.console == null) {
            this.scanner = new Scanner(System.in);
            this.logger.warn("Console is null. Using Scanner for input.");
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new Startup(
            new EnvironmentService(
                new LogService(
                    new LogChainCodeService()
                ),
                new HashService()
            )
        ).run();    
    }

    public void run() {
        try {
            this.logger.info("Initializing ContAudIT Checker...");
            String logString = String.format("Current directory: %s", Utils.currentDirectory());
            this.logger.info(logString);
            int resultCode = 1;
            while (resultCode == 1) {
                resultCode = showMenu();
            }
        } catch (InterruptedException ex) {
            this.logger.error(ex.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            this.logger.error(ex.getMessage());
        } finally {
            this.logger.info("Finishing ContAudIT Checker...");
        }
    }

    public int showMenu() throws InterruptedException, IOException {
        this.logger.info("---------------------------");
        this.logger.info("Menu:");
        this.logger.info("0 - Exit");
        this.logger.info("1 - Save Environment State");
        this.logger.info("2 - Validate Environment State by last environment hash stored");
        this.logger.info("3 - Validate Environment State by applying environment diffs since the last saved state");
        this.logger.info("---------------------------");
        this.logger.info("Choose an option: ");

        String menuOption;
        if (this.console != null) {
            menuOption = this.console.readLine();
        } else {
            menuOption = this.scanner.nextLine();
        }
        
        switch (menuOption) {
            case "0":
                return 0;
            case "1":
                this.envService.saveCurrentState();
                break;
            case "2":
                this.envService.validateStateByLastHashStored();
                break;
            case "3":
                this.envService.validateStateByCalculatedHash();
                break;
            default:
                throw new InvalidParameterException("Invalid menu option.");
        }

        return 1;
    }
}
