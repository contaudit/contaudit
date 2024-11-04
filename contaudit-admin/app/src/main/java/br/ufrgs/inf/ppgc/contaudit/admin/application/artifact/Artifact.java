package br.ufrgs.inf.ppgc.contaudit.admin.application.artifact;

import java.util.UUID;

public class Artifact {
    private UUID id;
    private String application;
    private String name;
    private String fullPath;
    private String hash;
    private String content;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getApplication() {
        return this.application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Artifact{id=" + this.id + ", applicationVersion=" + this.application + ", name = " + this.name + ", fullPath = " + this.fullPath + ", hash = " + this.hash + ", content = " + this.content + "}";
    }
}