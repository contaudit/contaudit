package br.ufrgs.inf.ppgc.contaudit.admin;

import br.ufrgs.inf.ppgc.contaudit.admin.application.Application;
import br.ufrgs.inf.ppgc.contaudit.admin.application.ApplicationService;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.admin.application.artifact.ArtifactService;
import br.ufrgs.inf.ppgc.contaudit.admin.wrapper.WrapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.Console;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StartupTest {
    @Mock
    private WrapperService mockWrapperService;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private ArtifactService mockArtifactService;

    @Mock
    private Console mockConsole;

    @InjectMocks
    private Startup startup;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        startup.console = mockConsole;
        when(mockWrapperService.getWrapperHash()).thenReturn("mockedHash");
        when(mockConsole.readLine())
            .thenReturn("1")
            .thenReturn("0"); 
    }

    @Test
    void testMainExecution() throws IOException, URISyntaxException {
        Startup.main(new String[]{});
        Startup spyStartup = spy(new Startup(mockWrapperService, mockApplicationService, mockArtifactService));
        doNothing().when(spyStartup).run();
        
        spyStartup.run();

        verify(spyStartup, times(1)).run();
    }

    @Test
    void testRunMenuOption1GetWrapperHash() {
        when(mockConsole.readLine()).thenReturn("1", "0");
        when(mockWrapperService.getWrapperHash()).thenReturn("mockedHash");

        startup.run();

        verify(mockWrapperService, times(1)).getWrapperHash();
    }

    @Test
    void testRunMenuOption2UpdateWrapperHash() {
        when(mockConsole.readLine()).thenReturn("2", "newMockedHash", "0");

        startup.run();

        verify(mockWrapperService, times(1)).updateWrapperHash("newMockedHash");
    }

    @Test
    void testRunMenuOption3ListApplications() {
        when(mockConsole.readLine()).thenReturn("3", "0");
        when(mockApplicationService.getApplications()).thenReturn(new ArrayList<>());

        startup.run();

        verify(mockApplicationService, times(1)).getApplications();
    }

    @Test
    void testRunMenuOption4CreateNewApplication() {
        when(mockConsole.readLine())
                .thenReturn("4", "TestApp", "1.0", "mockedHash", "0");

        startup.run();

        verify(mockApplicationService, times(1)).createApplication(any(Application.class));
    }

    @Test
    void testRunMenuOption5ListArtifacts() {
        when(mockConsole.readLine()).thenReturn("5", "0");
        when(mockArtifactService.getArtifacts()).thenReturn(new ArrayList<>());

        startup.run();

        verify(mockArtifactService, times(1)).getArtifacts();
    }

    @Test
    void testRunMenuOption6CreateNewArtifact() {
        when(mockConsole.readLine())
                .thenReturn("6", "AppId", "TestArtifact", "mockedHash", "artifactContent", "0");

        startup.run();

        verify(mockArtifactService, times(1)).createArtifact(any(Artifact.class));
    }


    @Test
    void testShowMenuOption0Exit() {
        when(mockConsole.readLine()).thenReturn("0");

        int result = startup.showMenu();

        assertEquals(0, result);
    }

    @Test
    void testShowMenuOption1GetWrapperHash() {
        when(mockConsole.readLine()).thenReturn("1");
        when(mockWrapperService.getWrapperHash()).thenReturn("mockedHash");

        int result = startup.showMenu();

        verify(mockWrapperService, times(1)).getWrapperHash();
        assertEquals(1, result);
    }

    @Test
    void testShowMenuOption2UpdateWrapperHash() {
        when(mockConsole.readLine()).thenReturn("2", "newMockedHash");

        int result = startup.showMenu();

        verify(mockWrapperService, times(1)).updateWrapperHash("newMockedHash");
        assertEquals(1, result);
    }

    @Test
    void testShowMenuOption3ListApplications() {
        when(mockConsole.readLine()).thenReturn("3");
        when(mockApplicationService.getApplications()).thenReturn(new ArrayList<>());

        int result = startup.showMenu();

        verify(mockApplicationService, times(1)).getApplications();
        assertEquals(1, result);
    }

    @Test
    void testShowMenuOption4CreateNewApplication() {
        when(mockConsole.readLine()).thenReturn("4", "TestApp", "1.0", "mockedHash");

        int result = startup.showMenu();

        verify(mockApplicationService, times(1)).createApplication(any(Application.class));
        assertEquals(1, result);
    }

    @Test
    void testShowMenuOption5ListArtifacts() {
        when(mockConsole.readLine()).thenReturn("5");
        when(mockArtifactService.getArtifacts()).thenReturn(new ArrayList<>());

        int result = startup.showMenu();

        verify(mockArtifactService, times(1)).getArtifacts();
        assertEquals(1, result);
    }

    @Test
    void testShowMenuOption6CreateNewArtifact() {
        when(mockConsole.readLine()).thenReturn("6", "AppId", "TestArtifact", "mockedHash", "artifactContent");

        int result = startup.showMenu();

        verify(mockArtifactService, times(1)).createArtifact(any(Artifact.class));
        assertEquals(1, result);
    }

    @Test
    void testShowMenuInvalidOption() {
        when(mockConsole.readLine()).thenReturn("invalidOption");

        assertThrows(InvalidParameterException.class, () -> {
            startup.showMenu();
        });
    }
}