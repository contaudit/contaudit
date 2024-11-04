package br.ufrgs.inf.ppgc.contaudit.checker.log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufrgs.inf.ppgc.contaudit.checker.Utils;
import br.ufrgs.inf.ppgc.contaudit.checker.application.Application;
import br.ufrgs.inf.ppgc.contaudit.checker.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.checker.blockchain.chaincode.LogChainCodeService;

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

    public Log getLastLog() {
        List<Log> logs = null;
        
        int attempts = 0;
        int monthsBack = 2;
        while (attempts < 10) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -monthsBack);
            
            String fromDate = Utils.dateFormatter().format(cal.getTime());
            String toDate = Utils.dateFormatter().format(new Date());

            logs = this.queryLogByDateRange(fromDate, toDate);
            if (logs == null || logs.isEmpty()) {
                monthsBack *= monthsBack;
                attempts++;
            } else {
                break;
            }
        }

        if (logs != null && !logs.isEmpty())
            return logs.get(logs.size() - 1);
        else
            return null;
    }

    public List<Log> queryLogByDateRange(String startDate, String endDate) {
        this.logger.info("Searching logs in blockchain...");
        List<Log> logs = this.logChainCodeService.queryLogsByDateRange(startDate, endDate);
        if (logs == null || logs.isEmpty())
            this.logger.info("No logs founded!");
        
        return logs;
    }
}
