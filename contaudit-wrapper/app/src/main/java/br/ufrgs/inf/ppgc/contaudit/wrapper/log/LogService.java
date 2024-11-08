package br.ufrgs.inf.ppgc.contaudit.wrapper.log;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.LogChainCodeService;

public class LogService {
    protected Logger logger = LoggerFactory.getLogger(LogService.class);
    private LogChainCodeService logChainCodeService;

    public LogService(LogChainCodeService logChainCodeService) {
        this.logChainCodeService = logChainCodeService;
    }

    public void saveToBlockchain(String commandLine, Application application, List<Artifact> artifacts, String environmentPreStateHash, String output, String environmentPostStateHash, String environmentDiff) {
        this.logger.info("Sending to blockchain...");
        Log log = new Log(commandLine, application, artifacts, environmentPreStateHash, output, environmentPostStateHash, environmentDiff);
        this.logChainCodeService.insertLog(log);
    }
}