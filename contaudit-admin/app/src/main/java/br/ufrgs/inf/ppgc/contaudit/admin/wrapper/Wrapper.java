package br.ufrgs.inf.ppgc.contaudit.admin.wrapper;

public class Wrapper {
    private String hash;

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Wrapper{hash=" + this.hash + "}";
    }
}
