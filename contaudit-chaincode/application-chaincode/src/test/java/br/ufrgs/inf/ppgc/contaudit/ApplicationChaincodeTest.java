package br.ufrgs.inf.ppgc.contaudit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import io.netty.handler.ssl.OpenSsl;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

class ApplicationChaincodeTest {
    private ApplicationChaincode chaincode;
    private ChaincodeStub stub;
    private ClientIdentity identity;
    private QueryResultsIterator<KeyValue> mockResultsIterator;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        chaincode = new ApplicationChaincode();
        stub = mock(ChaincodeStub.class);
        identity = mock(ClientIdentity.class);
        mockResultsIterator = mock(QueryResultsIterator.class);
    }

    @Test
    void testInit() {
        Response response = chaincode.init(stub);
        assertEquals(Status.SUCCESS, response.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGet_SuccessWithApplications() throws Exception {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        KeyValue mockKeyValue = mock(KeyValue.class);
        when(mockKeyValue.getStringValue()).thenReturn(new JSONObject().put("name", "Test Application").toString());
        Iterator<KeyValue> mockIterator = mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(mockKeyValue);
        when(mockResultsIterator.iterator()).thenReturn(mockIterator);
        when(stub.getStateByRange(ApplicationChaincode.START_KEY, ApplicationChaincode.END_KEY)).thenReturn(mockResultsIterator);

        Response response = chaincode.get(stub, identity);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(new String(response.getPayload()).contains("Test Application"));
    }

    @Test
    void testGet_AccessDenied() {
        when(identity.getMSPID()).thenReturn("unauthorized-organization");

        Response response = chaincode.get(stub, identity);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Access Denied for organization.", response.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGet_NoApplicationsFound() {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        Iterator<KeyValue> mockIterator = mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(false);
        when(mockResultsIterator.iterator()).thenReturn(mockIterator);
        when(stub.getStateByRange(ApplicationChaincode.START_KEY, ApplicationChaincode.END_KEY)).thenReturn(mockResultsIterator);

        Response response = chaincode.get(stub, identity);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("No applications found.", response.getMessage());
    }

    @Test
    void testGet_ExceptionThrown() {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getStateByRange(ApplicationChaincode.START_KEY, ApplicationChaincode.END_KEY)).thenThrow(new RuntimeException("Test Exception"));

        Response response = chaincode.get(stub, identity);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Error during get operation.", response.getMessage());
    }

    @Test
    void testInsert_Success() {
        String applicationKey = "application-key";
        String applicationValue = "{\"name\": \"Test Application\"}";
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getParameters()).thenReturn(Arrays.asList(applicationKey, applicationValue));

        Response response = chaincode.insert(stub, Arrays.asList(applicationKey, applicationValue), identity);

        verify(stub).putStringState(applicationKey, applicationValue);
        assertEquals(Status.SUCCESS, response.getStatus());
    }

    @Test
    void testInsert_Unauthorized() {
        String applicationKey = "application-key";
        String applicationValue = "{\"name\": \"Test Application\"}";
        when(identity.getMSPID()).thenReturn("unauthorized-organization");

        Response response = chaincode.insert(stub, Arrays.asList(applicationKey, applicationValue), identity);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Access Denied for organization.", response.getMessage());
    }

    @Test
    void testInsert_IncorrectNumberOfArguments() {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");

        Response response = chaincode.insert(stub, Collections.singletonList("application-key"), identity);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting an application key and an application object.", response.getMessage());
    }

    @Test
    void testValidateApplication_ValidApplication() {
        String applicationKey = "valid-application-key";
        String applicationValue = "{\"name\": \"Valid Application\"}";
        when(stub.getStringState(applicationKey)).thenReturn(applicationValue);

        Response response = chaincode.validateApplication(stub, Collections.singletonList(applicationKey));

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("Valid application.", response.getMessage());
        assertEquals("true", new String(response.getPayload()));
    }

    @Test
    void testValidateApplication_InvalidApplication() {
        String applicationKey = "invalid-application-key";
        when(stub.getStringState(applicationKey)).thenReturn(null);

        Response response = chaincode.validateApplication(stub, Collections.singletonList(applicationKey));

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("Invalid application.", response.getMessage());
        assertEquals("false", new String(response.getPayload()));
    }

    @Test
    void testValidateApplication_EmptyApplication() {
        String applicationKey = "empty-application-key";
        when(stub.getStringState(applicationKey)).thenReturn("");

        Response response = chaincode.validateApplication(stub, Collections.singletonList(applicationKey));

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("Invalid application.", response.getMessage());
        assertEquals("false", new String(response.getPayload()));
    }

    @Test
    void testValidateApplication_IncorrectNumberOfArguments() {
        String applicationKey = "some-key";

        Response response = chaincode.validateApplication(stub, Arrays.asList(applicationKey, "extra-argument"));

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting only an application key.", response.getMessage());
    }

    @Test
    void testIsAuthorized_Authorized() throws Exception {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");

        boolean isAuthorized = chaincode.isAuthorized(identity);

        assertTrue(isAuthorized);
    }

    @Test
    void testIsAuthorized_Unauthorized() throws Exception {
        when(identity.getMSPID()).thenReturn("unauthorized-organization");

        boolean isAuthorized = chaincode.isAuthorized(identity);

        assertFalse(isAuthorized);
    }

    @Test
    void testMain_OpenSslAvailable() {
        try (MockedStatic<OpenSsl> mockedOpenSsl = mockStatic(OpenSsl.class)) {
            mockedOpenSsl.when(OpenSsl::isAvailable).thenReturn(true);

            chaincode = new ApplicationChaincode() {
                @Override
                public void start(String[] args) {
                    // Skip actual start logic in tests
                }
            };

            try {
                ApplicationChaincode.main(new String[]{});
            } catch (Exception ignored) {
                // We are deliberately ignoring the call to start, so catch and ignore exceptions
            }

            mockedOpenSsl.verify(OpenSsl::isAvailable);
        }
    }

    @Test
    void testMain_OpenSslNotAvailable() {
        try (MockedStatic<OpenSsl> mockedOpenSsl = mockStatic(OpenSsl.class)) {
            mockedOpenSsl.when(OpenSsl::isAvailable).thenReturn(false);

            chaincode = new ApplicationChaincode() {
                @Override
                public void start(String[] args) {
                    // Skip actual start logic in tests
                }
            };

            try {
                ApplicationChaincode.main(new String[]{});
            } catch (Exception ignored) {
                // We are deliberately ignoring the call to start, so catch and ignore exceptions
            }

            mockedOpenSsl.verify(OpenSsl::isAvailable);
        }
    }
}