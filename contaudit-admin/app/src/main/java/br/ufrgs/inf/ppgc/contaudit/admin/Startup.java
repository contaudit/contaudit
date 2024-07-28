package br.ufrgs.inf.ppgc.contaudit.admin;

import java.io.Console;
import java.security.InvalidParameterException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufrgs.inf.ppgc.contaudit.admin.application.Application;
import br.ufrgs.inf.ppgc.contaudit.admin.application.ApplicationService;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.ArtifactService;
import br.ufrgs.inf.ppgc.contaudit.admin.wrapper.WrapperService;

public class Startup {
    private static Logger logger = LoggerFactory.getLogger(Startup.class);
 
    public static void main(String[] args) {
        try {
            logger.info("Initializing ContAudIT Admin...");
            String logString = String.format("Current directory: %s", Utils.getCurrentDirectory());
            logger.info(logString);
            int resultCode = 1;
            while (resultCode == 1) {
                resultCode = showMenu();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        finally {
            logger.info("Finishing ContAudIT Admin...");
        }
    }

    private static int showMenu() {
        logger.info("---------------------------");
        logger.info("Menu:");
        logger.info("0 - Exit");
        logger.info("1 - Get Wrapper Hash");
        logger.info("2 - Update Wrapper Hash");
        logger.info("3 - List Applications");
        logger.info("4 - Create New Application");
        logger.info("5 - List Artifacs");
        logger.info("6 - Create New Artifact");
        logger.info("---------------------------");
        logger.info("Choose an option: ");

        Console console = System.console();
        String menuOption = console.readLine();

        switch (menuOption) {
            case "0":
                return 0;
            case "1":
                String wrapperHash = new WrapperService().getWrapperHash();
                logger.info(wrapperHash);
                break;
            case "2":
                logger.info("Enter the new Wrapper Hash: ");
                String newWrapperHash = console.readLine();
                new WrapperService().updateWrapperHash(newWrapperHash);
                break;
            case "3":
                String applications = new ApplicationService().getApplications().toString();
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
                new ApplicationService().createApplication(application);
                break;
            case "5":
                String artifacts = new ArtifactService().getArtifacts().toString();
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
                new ArtifactService().createArtifact(artifact);
                break;
            default:
                throw new InvalidParameterException("Invalid menu option.");
        }

        return 1; 
    }
}
