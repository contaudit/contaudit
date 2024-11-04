package br.ufrgs.inf.ppgc.contaudit.admin;

import java.io.Console;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.Scanner;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.admin.application.Application;
import br.ufrgs.inf.ppgc.contaudit.admin.application.ApplicationService;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.ArtifactService;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ApplicationChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ArtifactChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.WrapperChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.admin.wrapper.WrapperService;

public class Startup {
    protected Logger logger = LoggerFactory.getLogger(Startup.class);
    protected Console console;
    protected Scanner scanner;
    protected WrapperService wrapperService;
    protected ApplicationService applicationService;
    protected ArtifactService artifactService;

    public Startup(WrapperService wrapperService, ApplicationService applicationService, ArtifactService artifactService) {
        this.wrapperService = wrapperService;
        this.applicationService = applicationService;
        this.artifactService = artifactService;
        this.console = System.console();
        if (this.console == null) {
            this.scanner = new Scanner(System.in);
            this.logger.warn("Console is null. Using Scanner for input.");
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new Startup(
            new WrapperService(
                new WrapperChainCodeService(
                    new BlockchainService())),
            new ApplicationService(
                new ApplicationChainCodeService(
                    new BlockchainService())),
            new ArtifactService(
                new ArtifactChainCodeService(
                    new BlockchainService()))
        ).run();   
    }

    public void run() {
        try {
            this.logger.info("Initializing ContAudIT Admin...");
            String logString = String.format("Current directory: %s", Utils.currentDirectory());
            this.logger.info(logString);
            int resultCode = 1;
            while (resultCode == 1) {
                resultCode = showMenu();
            }
        } catch (Exception ex) {
            this.logger.error(ex.getMessage());
        }
        finally {
            this.logger.info("Finishing ContAudIT Admin...");
        }
    }

    public int showMenu() {
        this.logger.info("---------------------------");
        this.logger.info("Menu:");
        this.logger.info("0 - Exit");
        this.logger.info("1 - Get Wrapper Hash");
        this.logger.info("2 - Update Wrapper Hash");
        this.logger.info("3 - List Applications");
        this.logger.info("4 - Create New Application");
        this.logger.info("5 - List Artifacts");
        this.logger.info("6 - Create New Artifact");
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
                String wrapperHash = this.wrapperService.getWrapperHash();
                logger.info(wrapperHash);
                break;
            case "2":
                logger.info("Enter the new Wrapper Hash: ");
                String newWrapperHash = console.readLine();
                this.wrapperService.updateWrapperHash(newWrapperHash);
                break;
            case "3":
                String applications = this.applicationService.getApplications().toString();
                logger.info(applications);
                break;
            case "4":
                logger.info("Enter the application name: ");
                String applicationName = console.readLine();
                logger.info("Enter the application version: ");
                String applicationVersion = console.readLine();
                logger.info("Enter the application hash: ");
                String applicationHash = console.readLine();

                Application application = new Application();
                application.setId(UUID.randomUUID());
                application.setName(applicationName);
                application.setVersion(applicationVersion);
                application.setHash(applicationHash);
                this.applicationService.createApplication(application);
                break;
            case "5":
                String artifacts = this.artifactService.getArtifacts().toString();
                logger.info(artifacts);
                break;
            case "6":
                logger.info("Enter the artifact application id: ");
                String artifactApplicationId = console.readLine();
                logger.info("Enter the artifact name: ");
                String artifactName = console.readLine();
                logger.info("Enter the artifact hash: ");
                String artifactHash = console.readLine();
                logger.info("Enter the artifact content: ");
                String artifactContent = console.readLine();

                Artifact artifact = new Artifact();
                artifact.setId(UUID.randomUUID());
                artifact.setApplication(artifactApplicationId);
                artifact.setName(artifactName);
                artifact.setHash(artifactHash);
                artifact.setContent(artifactContent);
                this.artifactService.createArtifact(artifact);
                break;
            default:
                throw new InvalidParameterException("Invalid menu option.");
        }

        return 1; 
    }
}