package br.ufrgs.inf.ppgc.contaudit.admin.application;

import java.util.List;

import org.slf4j.Logger;

import br.ufrgs.inf.ppgc.contaudit.admin.LoggerInstance;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ApplicationChainCodeService;

public class ApplicationService {
    private Logger logger =  LoggerInstance.get();

    public List<Application> getApplications() {
        logger.info("Getting Applications...");

        ApplicationChainCodeService applicationChainCodeService = new ApplicationChainCodeService();
        return applicationChainCodeService.getApplications();
    }

    public void createApplication(Application application) {
        logger.info("Creating new Application...");
        
        ApplicationChainCodeService applicationChainCodeService = new ApplicationChainCodeService();
        applicationChainCodeService.insertApplication(application);
    }
}
