package br.ufrgs.inf.ppgc.contaudit.wrapper.log;

import java.util.Date;
import java.util.List;
import br.ufrgs.inf.ppgc.contaudit.wrapper.Utils;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;

public class Log {
    private String id;
    private String command;
    private Application application;
    private List<Artifact> artifacts;
    private String environmentPreStateHash;
    private String output;
    private String environmentPostStateHash;
    private String environmentDiff;

    public Log(String command, Application hashes, List<Artifact> artifacts, String environmentPreStateHash, String output, String environmentPostStateHash, String environmentDiff) {
        this.id = Utils.dateFormatter().format(new Date());
        this.command = command;
        this.application = hashes;
        this.artifacts = artifacts;
        this.environmentPreStateHash = environmentPreStateHash;
        this.output = output;
        this.environmentPostStateHash = environmentPostStateHash;
        this.environmentDiff = environmentDiff;
    }

    public String getId() {
        return this.id;
    }

    public String getCommand() {
        return this.command;
    }

    public Application getApplication() {
        return this.application;
    }

    public List<Artifact> getArtifacts() {
        return this.artifacts;
    }

    public String getEnvironmentPreStateHash() {
        return this.environmentPreStateHash;
    }

    public String getOutput() {
        return this.output;
    }

    public String getEnvironmentPostStateHash() {
        return this.environmentPostStateHash;
    }

    public String getEnvironmentDiff() {
        return this.environmentDiff;
    }
}