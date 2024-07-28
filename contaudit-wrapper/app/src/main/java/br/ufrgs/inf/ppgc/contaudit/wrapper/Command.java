package br.ufrgs.inf.ppgc.contaudit.wrapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.ApplicationService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.ArtifactService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.ApplicationChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.ArtifactChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.WrapperChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.environment.EnvironmentService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.log.LogService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.Wrapper;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.WrapperService;

public class Command {
    private Logger logger = LoggerFactory.getLogger(Command.class);
    private String commandLine;
    private Map<String, Object> context;

    private Application application;
    private List<Artifact> artifacts;

    public Command(String commandLine){
        this.commandLine = commandLine;
        this.context = new HashMap<>();
    }

    public void execute() {
        try {
            if (!this.canExecute()) return;

            this.runPreCommandActions();
            this.runCommand();
            this.runPostCommandActions();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            this.logger.error(e.toString());
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private boolean canExecute() throws IOException, URISyntaxException {
        Wrapper wrapper = new WrapperService().loadData();
        boolean wrapperIsValid = new WrapperChainCodeService().validateWrapper(wrapper);
        if (!wrapperIsValid) {
            this.logger.info("Wrapper not valid. Command won't be executed.");
            return false;
        }

        application = new ApplicationService().loadData(commandLine);
        boolean applicationIsValid = new ApplicationChainCodeService().validateApplication(application);
        if (!applicationIsValid) {
            this.logger.info("Application not valid. Command won't be executed.");
            return false;
        }

        artifacts = new ArtifactService().loadData(application, commandLine);
        for (Artifact artifact : artifacts) {
            boolean artifactIsValid = new ArtifactChainCodeService().validateArtifact(artifact);
            if (!artifactIsValid) {
                String logString = String.format("Artifact %s not allowed. Command won't be executed.", artifact.getName());
                this.logger.info(logString);
                return false;
            }
        }

        return true;
    }

    private void runPreCommandActions() throws InterruptedException, IOException {
        this.logger.info("Running pre-command actions...");

        EnvironmentService envService = new EnvironmentService();
        String preStateHash = envService.checkStateHash(Startup.getInstanceName() + "_pre");

        this.context.put("preStateHash", preStateHash);
    }

    private void runCommand() throws IOException, InterruptedException {
        this.logger.info("Executing command...");
        
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", this.commandLine);
        Process process = builder.start();
        String commandOutput = Utils.transformInputStreamToString(process.getInputStream(), false);
        process.waitFor();
        
        String logString = String.format("Command output: %n%s", commandOutput);
        this.logger.info(logString);

        this.context.put("commandOutput", commandOutput);
    }

    private void runPostCommandActions() throws InterruptedException, IOException, URISyntaxException {
        this.logger.info("Running post-command actions...");

        EnvironmentService envService = new EnvironmentService();
        String postStateHash = envService.checkStateHash(Startup.getInstanceName() + "_post");
        String diffState = envService.checkDiffState(Startup.getInstanceName() + "_pre", Startup.getInstanceName() + "_post");
        envService.clearStates();
        
        new LogService().saveToBlockchain(
            this.commandLine, 
            this.application, 
            this.artifacts, 
            (String) this.context.get("preStateHash"), 
            (String) this.context.get("commandOutput"), 
            postStateHash, 
            diffState);
    }

    @Override
    public String toString() {
        return String.format("Command: %s", this.commandLine);
    }
}