package br.ufrgs.inf.ppgc.contaudit.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerInstance {
    private static Logger logger;

    private LoggerInstance() {}

    public static void start(String instanceName) {
        logger = LoggerFactory.getLogger(instanceName);
    }

    public static Logger get() {
        if (logger == null)
            start("Admin#1");

        return logger;
    }
}