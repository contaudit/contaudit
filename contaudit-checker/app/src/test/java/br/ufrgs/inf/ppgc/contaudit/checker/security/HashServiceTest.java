package br.ufrgs.inf.ppgc.contaudit.checker.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class HashServiceTest {
    @Test
    void testGenerateSHA3256Hash_withFilePath() throws IOException {
        Path mockPath = mock(Path.class);
        byte[] mockBytes = "test file content".getBytes();
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(mockPath)).thenReturn(mockBytes);
            HashService hashService = new HashService();

            String hash = hashService.generateSHA3256Hash(mockPath);
            SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
            byte[] expectedDigest = digestSHA3.digest(mockBytes);
            String expectedHash = Hex.toHexString(expectedDigest);

            assertEquals(expectedHash, hash);
        }
    }

    @Test
    void testGenerateSHA3256Hash_withStringValue() {
        String testValue = "test string content";
        HashService hashService = new HashService();

        String hash = hashService.generateSHA3256Hash(testValue);
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        byte[] expectedDigest = digestSHA3.digest(testValue.getBytes());
        String expectedHash = Hex.toHexString(expectedDigest);

        assertEquals(expectedHash, hash);
    }

    @Test
    void testGenerateSHA3256Hash_withByteArray() {
        byte[] testBytes = "test byte array".getBytes();
        HashService hashService = new HashService();

        String hash = callGenerateSHA3256Hash(hashService, testBytes);
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        byte[] expectedDigest = digestSHA3.digest(testBytes);
        String expectedHash = Hex.toHexString(expectedDigest);

        assertEquals(expectedHash, hash);
    }

    private String callGenerateSHA3256Hash(HashService hashService, byte[] bytes) {
        try {
            java.lang.reflect.Method method = HashService.class.getDeclaredMethod("generateSHA3256Hash", byte[].class);
            method.setAccessible(true);
            return (String) method.invoke(hashService, bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}