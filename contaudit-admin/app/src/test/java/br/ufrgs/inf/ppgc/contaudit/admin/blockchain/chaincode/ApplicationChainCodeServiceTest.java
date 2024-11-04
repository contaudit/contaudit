package br.ufrgs.inf.ppgc.contaudit.admin.blockchain.chaincode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;
import br.ufrgs.inf.ppgc.contaudit.admin.application.Application;
import br.ufrgs.inf.ppgc.contaudit.admin.blockchain.BlockchainService;

class ApplicationChainCodeServiceTest {
    @Mock
    private BlockchainService blockchainService;

    private ApplicationChainCodeService applicationChainCodeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationChainCodeService = new ApplicationChainCodeService(blockchainService);
    }

    @Test
    void testGetApplications() {
        Application app1 = new Application();
        app1.setId(UUID.randomUUID());
        app1.setName("appName1");
        app1.setVersion("1.0");
        app1.setFullPath("/path/to/app1");
        app1.setHash("hash1");

        Application app2 = new Application();
        app2.setId(UUID.randomUUID());
        app2.setName("appName2");
        app2.setVersion("2.0");
        app2.setFullPath("/path/to/app2");
        app2.setHash("hash2");

        String jsonResult = new Gson().toJson(Arrays.asList(app1, app2));

        when(blockchainService.evaluateTransaction(
                eq("c1"),
                eq("application-chaincode"),
                eq("get"),
                any(String[].class)
        )).thenReturn(jsonResult);

        List<Application> applications = applicationChainCodeService.getApplications();

        assertNotNull(applications);
        assertEquals(2, applications.size());
        Application firstApp = applications.get(0);
        assertEquals(app1.getId(), firstApp.getId());
        assertEquals(app1.getName(), firstApp.getName());
        assertEquals(app1.getVersion(), firstApp.getVersion());
        assertEquals(app1.getFullPath(), firstApp.getFullPath());
        assertEquals(app1.getHash(), firstApp.getHash());
        Application secondApp = applications.get(1);
        assertEquals(app2.getId(), secondApp.getId());
        assertEquals(app2.getName(), secondApp.getName());
        assertEquals(app2.getVersion(), secondApp.getVersion());
        assertEquals(app2.getFullPath(), secondApp.getFullPath());
        assertEquals(app2.getHash(), secondApp.getHash());
        verify(blockchainService).evaluateTransaction(
                eq("c1"),
                eq("application-chaincode"),
                eq("get"),
                any(String[].class)
        );
    }

    @Test
    void testInsertApplication() {
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setName("appName1");
        application.setVersion("1.0");
        application.setFullPath("/path/to/app1");
        application.setHash("hash1");

        applicationChainCodeService.insertApplication(application);

        verify(blockchainService).submitTransaction(
            "c1",
            "application-chaincode",
            "insert",
            new String[] { application.getHash(), new Gson().toJson(application) }
        );
    }
}