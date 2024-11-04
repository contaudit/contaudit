package br.ufrgs.inf.ppgc.contaudit.wrapper.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import br.ufrgs.inf.ppgc.contaudit.wrapper.security.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class ApplicationServiceTest {
    @Mock
    private HashService hashService;

    private ApplicationService applicationService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new ApplicationService(hashService);
    }

    @Test
    void testLoadDataSuccess() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "testExecutableFile", ".exe");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.isExecutable(any(Path.class))).thenReturn(true);

            when(hashService.generateSHA3256Hash(tempFile)).thenReturn("testHash");

            Application application = applicationService.loadData(tempFile.toString());

            assertNotNull(application);
            assertNotNull(application.getId());
            assertEquals(tempFile.getFileName().toString(), application.getName());
            assertEquals(tempFile.toString(), application.getFullPath());
            assertEquals("testHash", application.getHash());
        }
    }

    @Test
    void testLoadDataThrowsExceptionWhenFileNotFound() {
        String nonExistentFilePath = tempDir.resolve("nonExistentFile").toString();

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(false);

            assertThrows(FileNotFoundException.class, () -> applicationService.loadData(nonExistentFilePath));
        }
    }

    @Test
    void testLoadDataThrowsExceptionWhenFileNotExecutable() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "nonExecutableFile", ".txt");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.isExecutable(any(Path.class))).thenReturn(false);

            assertThrows(FileNotFoundException.class, () -> applicationService.loadData(tempFile.toString()));
        }
    }

    @Test
    void testExtractFilePathValid() {
        String commandLine = "filePath";
        String filePath = applicationService.extractFilePath(commandLine);
        assertEquals("filePath", filePath);
    }

    @Test
    void testExtractFilePathInvalid() {
        String commandLine = " ";
        assertThrows(IllegalArgumentException.class, () -> applicationService.extractFilePath(commandLine));
    }

    @Test
    void testValidateAndResolvePathSuccess() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "testExecutableFile", ".exe");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.isExecutable(any(Path.class))).thenReturn(true);

            Path result = applicationService.validateAndResolvePath(tempFile.toString());

            assertNotNull(result);
            assertEquals(tempFile.toRealPath(), result);
        }
    }

    @Test
    void testValidateAndResolvePathFileNotFound() {
        String nonExistentFilePath = tempDir.resolve("nonExistentFile").toString();

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(false);

            assertThrows(FileNotFoundException.class, () -> applicationService.validateAndResolvePath(nonExistentFilePath));
        }
    }

    @Test
    void testValidateAndResolvePathFileNotExecutable() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "nonExecutableFile", ".txt");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.isExecutable(any(Path.class))).thenReturn(false);

            assertThrows(FileNotFoundException.class, () -> applicationService.validateAndResolvePath(tempFile.toString()));
        }
    }

    @Test
    void testBuildApplicationSuccess() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "testExecutableFile", ".exe");

        when(hashService.generateSHA3256Hash(tempFile)).thenReturn("testHash");

        Application application = applicationService.buildApplication(tempFile);

        assertNotNull(application);
        assertEquals(tempFile.getFileName().toString(), application.getName());
        assertEquals(tempFile.toString(), application.getFullPath());
        assertEquals("testHash", application.getHash());
        assertNotNull(application.getId());
    }
}