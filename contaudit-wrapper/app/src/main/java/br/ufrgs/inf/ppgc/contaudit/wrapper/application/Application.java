package br.ufrgs.inf.ppgc.contaudit.wrapper.application;

import java.util.UUID;

public class Application {
    private UUID id;
    private String name;
    private String version;
    private String fullPath;
    private String hash;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFullPath() {
        return this.fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Application{id=" + this.id + ", name = " + this.name + ", version = " + this.version + ", fullPath = " + this.fullPath + ", hash = " + this.hash + "}";
    }
}
