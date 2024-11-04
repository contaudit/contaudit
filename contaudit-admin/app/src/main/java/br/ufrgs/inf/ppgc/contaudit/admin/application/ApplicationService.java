package br.ufrgs.inf.ppgc.contaudit.admin.application;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ApplicationChainCodeService;

public class ApplicationService {
    private Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private ApplicationChainCodeService applicationChainCodeService;

    public ApplicationService(ApplicationChainCodeService applicationChainCodeService) {
        this.applicationChainCodeService = applicationChainCodeService;
    }

    public List<Application> getApplications() {
        logger.info("Getting Applications...");
        return this.applicationChainCodeService.getApplications();
    }

    public void createApplication(Application application) {
        logger.info("Creating new Application...");
        applicationChainCodeService.insertApplication(application);
    }
}
