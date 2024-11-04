package br.ufrgs.inf.ppgc.contaudit;

import java.io.Serializable;
import java.util.UUID;

public class Artifact implements Serializable {
    private UUID id;
    private String application;
    private String name;
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

    public void setApplication(String apllication) {
        this.application = apllication;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "Artifact{id=" + this.id + ", application=" + this.application + ", name = " + this.name + ", hash = " + this.hash + ", content = " + this.content + "}";
    }
}