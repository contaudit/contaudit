package br.ufrgs.inf.ppgc.contaudit.wrapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.ApplicationService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.ArtifactService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.BlockchainService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.ApplicationChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.ArtifactChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.LogChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.WrapperChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.environment.EnvironmentService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.log.LogService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.Wrapper;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.WrapperService;

class CommandFactory {
    public Command createCommand(String command) throws IOException, URISyntaxException {
        return new Command(
            command, 
            new WrapperService(new HashService()), 
            new WrapperChainCodeService(new BlockchainService()),
            new ApplicationService(new HashService()),
            new ApplicationChainCodeService(new BlockchainService()),
            new ArtifactService(new HashService()),
            new ArtifactChainCodeService(new BlockchainService()),
            new LogService(new LogChainCodeService(new BlockchainService())),
            new EnvironmentService(new HashService())
        );
    }
}

public class Command {
    protected static Logger logger = LoggerFactory.getLogger(Command.class);
    protected String commandLine;

    protected Map<String, Object> context;
    protected WrapperService wrapperService;
    protected WrapperChainCodeService wrapperChainCodeService;
    protected ApplicationService applicationService;
    protected ApplicationChainCodeService applicationChainCodeService;
    protected ArtifactService artifactService;
    protected ArtifactChainCodeService artifactChainCodeService;
    protected LogService logService;
    protected EnvironmentService environmentService;

    protected Application application;
    protected List<Artifact> artifacts;

    @SuppressWarnings("squid:S107")
    public Command(
        String commandLine, 
        WrapperService wrapperService,
        WrapperChainCodeService wrapperChainCodeService,
        ApplicationService applicationService,
        ApplicationChainCodeService applicationChainCodeService,
        ArtifactService artifactService,
        ArtifactChainCodeService artifactChainCodeService,
        LogService logService,
        EnvironmentService environmentService) {
        this.commandLine = commandLine;
        this.context = new HashMap<>();

        this.wrapperService = wrapperService;
        this.wrapperChainCodeService = wrapperChainCodeService;
        this.applicationService = applicationService;
        this.applicationChainCodeService = applicationChainCodeService;
        this.artifactService = artifactService;
        this.artifactChainCodeService = artifactChainCodeService;
        this.logService = logService;
        this.environmentService = environmentService;
    }

    public void execute() {
        try {
            if (!canExecute()) {
                return;
            }

            runPreCommandActions();
            runCommand();
            runPostCommandActions();
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing command", e);
            Thread.currentThread().interrupt();
        }
    }

    private boolean canExecute() throws IOException {
        return validateWrapper() && validateApplication() && validateArtifacts();
    }

    private boolean validateWrapper() throws IOException {
        Wrapper wrapper = wrapperService.loadData();
        boolean isValid = wrapperChainCodeService.validateWrapper(wrapper);

        if (!isValid) {
            logger.info("Wrapper not valid. Command won't be executed.");
        }

        return isValid;
    }

    private boolean validateApplication() throws IOException {
        application = applicationService.loadData(commandLine);
        boolean isValid = applicationChainCodeService.validateApplication(application);

        if (!isValid) {
            logger.info("Application not valid. Command won't be executed.");
        }

        return isValid;
    }

    private boolean validateArtifacts() throws IOException {
        artifacts = artifactService.loadData(application, commandLine);

        Optional<Artifact> invalidArtifact = artifacts.stream()
                .filter(artifact -> !artifactChainCodeService.validateArtifact(artifact))
                .findFirst();

        invalidArtifact.ifPresent(artifact -> logger.info("Artifact {} not allowed. Command won't be executed.", artifact.getName()));

        return invalidArtifact.isEmpty();
    }

    private void runPreCommandActions() throws InterruptedException, IOException {
        logger.info("Running pre-command actions...");

        String preStateHash = environmentService.checkStateHash(getInstanceName("_pre"));
        context.put("preStateHash", preStateHash);
    }

    private void runCommand() throws IOException, InterruptedException {
        logger.info("Executing command...");

        ProcessBuilder builder = createProcessBuilder();
        builder.command("sh", "-c", commandLine);
        Process process = builder.start();
        String commandOutput = Utils.transformInputStreamToString(process.getInputStream(), false);
        process.waitFor();

        logger.info("Command output: {}", commandOutput);
        context.put("commandOutput", commandOutput);
    }

    protected ProcessBuilder createProcessBuilder() {
        return new ProcessBuilder();
    }

    private void runPostCommandActions() throws InterruptedException, IOException {
        logger.info("Running post-command actions...");

        String postStateHash = environmentService.checkStateHash(getInstanceName("_post"));
        String diffState = environmentService.checkDiffState(getInstanceName("_pre"), getInstanceName("_post"));
        environmentService.clearStates();

        logService.saveToBlockchain(
                commandLine,
                application,
                artifacts,
                (String) context.get("preStateHash"),
                (String) context.get("commandOutput"),
                postStateHash,
                diffState
        );
    }

    private String getInstanceName(String suffix) {
        return Startup.getInstanceName() + suffix;
    }

    @Override
    public String toString() {
        return String.format("Command: %s", commandLine);
    }
}