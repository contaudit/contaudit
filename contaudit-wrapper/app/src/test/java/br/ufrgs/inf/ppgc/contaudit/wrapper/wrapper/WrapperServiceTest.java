package br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

class WrapperServiceTest {
    @TempDir
    Path tempDir;

    @Mock
    HashService hashService;

    @Mock
    Logger logger;

    @InjectMocks
    @Spy
    WrapperService wrapperService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.closeable != null) {
            this.closeable.close();
        }
    }

    @Test
    void testLoadData() throws IOException {
        String expectedHash = "mockedHash";
        when(this.hashService.generateSHA3256Hash(any(Path.class))).thenReturn(expectedHash);

        Wrapper wrapper = this.wrapperService.loadData();

        assertEquals(expectedHash, wrapper.getHash());
    }

    @Test
    void testGenerateWrapperHash() throws IOException {
        String expectedHash = "mockedHash";
        Path tempFile = Files.createTempFile(tempDir, "tempFile", ".tar.gz");
        Files.write(tempFile, "Test data".getBytes(), StandardOpenOption.CREATE);
        when(hashService.generateSHA3256Hash(any(Path.class))).thenReturn(expectedHash);
        doReturn(tempFile).when(wrapperService).createTarFile(anyString());

        String result = wrapperService.generateWrapperHash();

        assertEquals(expectedHash, result);
        verify(hashService).generateSHA3256Hash(tempFile);
        verify(wrapperService).createTarFile(anyString());

        Files.deleteIfExists(tempFile);
    }

    @Test
    void testCreateTarFileAndCleanUp() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "tempFile", ".txt");
        Files.write(tempFile, "Test data".getBytes());

        Path tarFilePath = wrapperService.createTarFile(tempDir.toString());

        assertNotNull(tarFilePath);
        assertTrue(Files.exists(tarFilePath));
        assertTrue(tarFilePath.toString().endsWith(".tar.gz"));

        Files.deleteIfExists(tarFilePath);
    }

    @Test
    void testCreateTarFileIOException() throws IOException {
        WrapperService spyWrapperService = spy(wrapperService);

        String invalidFilePath = tempDir.resolve("invalid.tar.gz").toString();
        doThrow(new IOException("Simulated IOException")).when(spyWrapperService).createTarArchiveOutputStream(invalidFilePath);

        assertThrows(IOException.class, () -> spyWrapperService.createTarFile(invalidFilePath));
    }

    @Test
    void testAddFilesToTarGZWithFile() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "tempFile", ".txt");
        Files.write(tempFile, "Test data".getBytes());
        Path tarFilePath = Files.createTempFile(tempDir, "test", ".tar.gz");

        try (TarArchiveOutputStream tarOs = new TarArchiveOutputStream(Files.newOutputStream(tarFilePath))) {
            wrapperService.addFilesToTarGZ(tempFile.toFile(), "", tarOs);
        }

        assertTrue(Files.exists(tarFilePath));
        assertTrue(Files.size(tarFilePath) > 0);
    }

    @Test
    void testAddFilesToTarGZWithDirectory() throws IOException {
        Path tempDirectory = Files.createTempDirectory(tempDir, "tempDirectory");
        Path tempFile = Files.createFile(tempDirectory.resolve("tempFile.txt"));
        Files.write(tempFile, "Test data".getBytes());

        Path tarFilePath = Files.createTempFile(tempDir, "test", ".tar.gz");

        try (TarArchiveOutputStream tarOs = new TarArchiveOutputStream(Files.newOutputStream(tarFilePath))) {
            wrapperService.addFilesToTarGZ(tempDirectory.toFile(), "", tarOs);
        }

        assertTrue(Files.exists(tarFilePath));
        assertTrue(Files.size(tarFilePath) > 0);
    }
}