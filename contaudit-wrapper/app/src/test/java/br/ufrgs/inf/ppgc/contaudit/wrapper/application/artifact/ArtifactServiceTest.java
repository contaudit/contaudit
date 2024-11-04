package br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;

class ArtifactServiceTest {
    @Mock
    private HashService hashService;

    @InjectMocks
    private ArtifactService artifactService;

    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        tempDir = Files.createTempDirectory("test-artifacts");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(tempDir)
             .map(Path::toFile)
             .forEach(file -> {
                 if (!file.delete()) {
                     file.deleteOnExit();
                 }
             });
    }

    @Test
    void testLoadData_NoCommandProvided() throws IOException {
        Application application = mock(Application.class);

        List<Artifact> artifacts = artifactService.loadData(application, "");

        assertTrue(artifacts.isEmpty());
    }

    @Test
    void testLoadData_InvalidArtifact() throws IOException {
        Application application = mock(Application.class);

        List<Artifact> artifacts = artifactService.loadData(application, "app invalid_path");

        assertTrue(artifacts.isEmpty());
    }

    @Test
    void testLoadData_ValidFileArtifact() throws IOException {
        Application application = mock(Application.class);
        when(application.getName()).thenReturn("TestApp");
        when(application.getHash()).thenReturn("TestHash");
        Path tempFile = Files.createTempFile(tempDir, "valid_file", ".txt");
        Files.writeString(tempFile, "File content");
        when(hashService.generateSHA3256Hash(tempFile)).thenReturn("hash_value");

        List<Artifact> artifacts = artifactService.loadData(application, "app " + tempFile.toString());

        assertEquals(1, artifacts.size());
        Artifact artifact = artifacts.get(0);
        assertNotNull(artifact.getId());
        assertEquals("TestApp-TestHash", artifact.getApplication());
        assertEquals(tempFile.getFileName().toString(), artifact.getName());
        assertEquals(tempFile.toString(), artifact.getFullPath());
        assertEquals("hash_value", artifact.getHash());
        assertEquals("File content" + System.lineSeparator(), artifact.getContent());
    }

    @Test
    void testLoadData_ValidDirectoryArtifact() throws IOException {
        Application application = mock(Application.class);
        when(application.getName()).thenReturn("TestApp");
        when(application.getHash()).thenReturn("TestHash");
        Path tempDirectory = Files.createTempDirectory(tempDir, "valid_directory");
        when(hashService.generateSHA3256Hash(tempDirectory)).thenReturn("hash_value");

        List<Artifact> artifacts = artifactService.loadData(application, "app " + tempDirectory.toString());

        assertEquals(1, artifacts.size(), "Um artefato deveria ter sido carregado.");
        Artifact artifact = artifacts.get(0);
        assertNotNull(artifact.getId(), "O artefato deveria ter um ID.");
        assertEquals("TestApp-TestHash", artifact.getApplication(), "O nome da aplicação deveria ser 'TestApp-TestHash'.");
        assertEquals(tempDirectory.getFileName().toString(), artifact.getName(), "O nome do artefato deveria ser 'valid_directory'.");
        assertEquals(tempDirectory.toString(), artifact.getFullPath(), "O caminho completo deveria ser o diretório.");
        assertEquals("hash_value", artifact.getHash(), "O hash deveria ser 'hash_value'.");
        assertNull(artifact.getContent(), "Diretórios não deveriam ter conteúdo.");
    }

    @Test
    void testLoadData_InvalidArtifact_NotRegularFileNorDirectory() throws IOException {
        Application application = mock(Application.class);
        Path invalidPath = mock(Path.class);
        when(invalidPath.toRealPath()).thenReturn(invalidPath);
        when(invalidPath.toString()).thenReturn("not_a_file_or_directory");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(invalidPath)).thenReturn(false);
            mockedFiles.when(() -> Files.isDirectory(invalidPath)).thenReturn(false);

            List<Artifact> artifacts = artifactService.loadData(application, "app not_a_file_or_directory");

            assertTrue(artifacts.isEmpty());
        }
    }

    @Test
    void testLoadData_FileIOException() throws IOException {
        Application application = mock(Application.class);
        Path invalidPath = tempDir.resolve("invalid_file.txt");

        List<Artifact> artifacts = artifactService.loadData(application, "app " + invalidPath.toString());

        assertTrue(artifacts.isEmpty());
    }

    @Test
    void testReadFileContent() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "test_file", ".txt");
        Files.writeString(tempFile, "line1\nline2\nline3");

        String content = artifactService.readFileContent(tempFile);

        assertEquals("line1" + System.lineSeparator() + "line2" + System.lineSeparator() + "line3" + System.lineSeparator(), content);
    }

    @Test
    void testLoadData_InvalidPathException() throws IOException {
        Application application = mock(Application.class);
        Path invalidPath = mock(Path.class);
        when(invalidPath.toRealPath()).thenThrow(new InvalidPathException("invalid", "Invalid Path"));

        List<Artifact> artifacts = artifactService.loadData(application, "app invalid_path");

        assertTrue(artifacts.isEmpty());
    }

    @Test
    void testLoadData_IOException() throws IOException {
        Application application = mock(Application.class);
        Path invalidPath = mock(Path.class);
        when(invalidPath.toRealPath()).thenThrow(new IOException("IO Error"));

        List<Artifact> artifacts = artifactService.loadData(application, "app invalid_path");

        assertTrue(artifacts.isEmpty());
    }
}