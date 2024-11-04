package br.ufrgs.inf.ppgc.contaudit.admin.application;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode.ApplicationChainCodeService;
import java.util.Arrays;
import java.util.List;

class ApplicationServiceTest {
    @Mock
    private ApplicationChainCodeService applicationChainCodeService;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetApplications() {
        List<Application> mockApplications = Arrays.asList(new Application(), new Application());
        when(applicationChainCodeService.getApplications()).thenReturn(mockApplications);

        List<Application> applications = applicationService.getApplications();

        verify(applicationChainCodeService).getApplications();
        assertNotNull(applications);
        assertEquals(2, applications.size());
    }

    @Test
    void testCreateApplication() {
        Application newApplication = new Application();

        applicationService.createApplication(newApplication);

        verify(applicationChainCodeService).insertApplication(newApplication);
    }
}