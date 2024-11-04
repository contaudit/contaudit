package br.ufrgs.inf.ppgc.contaudit.wrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.Application;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.ApplicationService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.Artifact;
import br.ufrgs.inf.ppgc.contaudit.wrapper.application.artifact.ArtifactService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.ApplicationChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.ArtifactChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain.chaincode.WrapperChainCodeService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.environment.EnvironmentService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.log.LogService;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.Wrapper;
import br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper.WrapperService;

class CommandTest {
    @Mock
    private WrapperService wrapperService;
    
    @Mock
    private WrapperChainCodeService wrapperChainCodeService;
    
    @Mock
    private ApplicationService applicationService;
    
    @Mock
    private ApplicationChainCodeService applicationChainCodeService;
    
    @Mock
    private ArtifactService artifactService;
    
    @Mock
    private ArtifactChainCodeService artifactChainCodeService;
    
    @Mock
    private LogService logService;
    
    @Mock
    private EnvironmentService environmentService;

    @Mock
    private Application application;
    
    @Mock
    private Artifact artifact1;
    
    @Mock
    private Artifact artifact2;

    @InjectMocks
    private Command command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new Command(
            "exampleCommand",
            wrapperService,
            wrapperChainCodeService,
            applicationService,
            applicationChainCodeService,
            artifactService,
            artifactChainCodeService,
            logService,
            environmentService
        );
    }

    @Test
    void testExecuteValidCommand() throws IOException, InterruptedException {
        Wrapper wrapper = mock(Wrapper.class);
        when(wrapperService.loadData()).thenReturn(wrapper);
        when(wrapperChainCodeService.validateWrapper(wrapper)).thenReturn(true);
        when(applicationService.loadData(anyString())).thenReturn(application);
        when(applicationChainCodeService.validateApplication(application)).thenReturn(true);
        List<Artifact> artifacts = Arrays.asList(artifact1, artifact2);
        when(artifactService.loadData(any(Application.class), anyString())).thenReturn(artifacts);
        when(artifactChainCodeService.validateArtifact(artifact1)).thenReturn(true);
        when(artifactChainCodeService.validateArtifact(artifact2)).thenReturn(true);
        when(environmentService.checkStateHash(anyString())).thenReturn("preHash", "postHash");
        when(environmentService.checkDiffState(anyString(), anyString())).thenReturn("diff");
        ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("Command output".getBytes()));
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenReturn(0);

        command.execute();

        verify(wrapperService).loadData();
        verify(wrapperChainCodeService).validateWrapper(wrapper);
        verify(applicationService).loadData(anyString());
        verify(applicationChainCodeService).validateApplication(application);
        verify(artifactService).loadData(any(Application.class), anyString());
        verify(artifactChainCodeService).validateArtifact(artifact1);
        verify(artifactChainCodeService).validateArtifact(artifact2);
        verify(environmentService, times(2)).checkStateHash(anyString());
        verify(environmentService).checkDiffState(anyString(), anyString());
        verify(environmentService).clearStates();
        verify(logService).saveToBlockchain(
                anyString(),
                any(Application.class),
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void testExecuteInvalidWrapper() throws IOException {
        Wrapper wrapper = mock(Wrapper.class);
        when(wrapperService.loadData()).thenReturn(wrapper);
        when(wrapperChainCodeService.validateWrapper(wrapper)).thenReturn(false);

        command.execute();

        verify(wrapperService).loadData();
        verify(wrapperChainCodeService).validateWrapper(wrapper);
        verifyNoMoreInteractions(applicationService, artifactService, logService, environmentService);
    }

    @Test
    void testExecuteInvalidApplication() throws IOException {
        Wrapper wrapper = mock(Wrapper.class);
        when(wrapperService.loadData()).thenReturn(wrapper);
        when(wrapperChainCodeService.validateWrapper(wrapper)).thenReturn(true);
        when(applicationService.loadData(anyString())).thenReturn(application);
        when(applicationChainCodeService.validateApplication(application)).thenReturn(false);

        command.execute();

        verify(wrapperService).loadData();
        verify(wrapperChainCodeService).validateWrapper(wrapper);
        verify(applicationService).loadData(anyString());
        verify(applicationChainCodeService).validateApplication(application);
        verifyNoMoreInteractions(artifactService, logService, environmentService);
    }

    @Test
    void testExecuteInvalidArtifact() throws IOException {
        Wrapper wrapper = mock(Wrapper.class);
        when(wrapperService.loadData()).thenReturn(wrapper);
        when(wrapperChainCodeService.validateWrapper(wrapper)).thenReturn(true);
        when(applicationService.loadData(anyString())).thenReturn(application);
        when(applicationChainCodeService.validateApplication(application)).thenReturn(true);
        List<Artifact> artifacts = Arrays.asList(artifact1, artifact2);
        when(artifactService.loadData(any(Application.class), anyString())).thenReturn(artifacts);
        when(artifactChainCodeService.validateArtifact(artifact1)).thenReturn(true);
        when(artifactChainCodeService.validateArtifact(artifact2)).thenReturn(false);

        command.execute();

        verify(wrapperService).loadData();
        verify(wrapperChainCodeService).validateWrapper(wrapper);
        verify(applicationService).loadData(anyString());
        verify(applicationChainCodeService).validateApplication(application);
        verify(artifactService).loadData(any(Application.class), anyString());
        verify(artifactChainCodeService).validateArtifact(artifact1);
        verify(artifactChainCodeService).validateArtifact(artifact2);
        verifyNoMoreInteractions(logService, environmentService);
    }
}