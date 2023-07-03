package br.ufrgs.inf.ppgc.contaudit.checker.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

public class HashService {
    public String generateSHA3256Hash(Path path) throws IOException {
        return this.generateSHA3256Hash(Files.readAllBytes(path));
    }

    public String generateSHA3256Hash(String value) {
        return this.generateSHA3256Hash(value.getBytes());
    }

    private String generateSHA3256Hash(byte[] bytes) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        byte[] digest = digestSHA3.digest(bytes);
        return Hex.toHexString(digest);
    }
}
