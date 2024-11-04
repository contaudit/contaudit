package br.ufrgs.inf.ppgc.contaudit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ArtifactChaincodeTest {
    private ArtifactChaincode artifactChaincode;

    @Mock
    private ChaincodeStub stub;

    @Mock
    private ClientIdentity identity;

    @Mock
    private QueryResultsIterator<KeyValue> queryResultsIterator;

    @Mock
    private KeyValue keyValue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        artifactChaincode = new ArtifactChaincode(identity);
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void testInit() {
        Response response = artifactChaincode.init(stub);
        assertEquals(Status.SUCCESS, response.getStatus());
    }

    @Test
    void testInvokeWithInvalidFunction() {
        when(stub.getFunction()).thenReturn("invalidFunction");
        Response response = artifactChaincode.invoke(stub);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testInvokeGetSuccess() throws Exception {
        when(stub.getFunction()).thenReturn("get");
        when(stub.getParameters()).thenReturn(Arrays.asList());
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getStateByRange(anyString(), anyString())).thenReturn(queryResultsIterator);

        Iterator<KeyValue> iterator = mock(Iterator.class);
        when(queryResultsIterator.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(keyValue);
        when(keyValue.getStringValue()).thenReturn(new JSONObject().put("key", "value").toString());

        Response response = artifactChaincode.invoke(stub);
        assertEquals(Status.SUCCESS, response.getStatus());
        verify(queryResultsIterator).close();
    }

    @Test
    void testInvokeGetAccessDenied() {
        when(stub.getFunction()).thenReturn("get");
        when(identity.getMSPID()).thenReturn("invalid-organization");

        Response response = artifactChaincode.get(stub, identity);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void testInvokeCatchBlock() {
        when(stub.getFunction()).thenThrow(new RuntimeException("Simulated exception"));

        Response response = artifactChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertNotNull(response.getMessage(), "A mensagem de erro não deve ser nula.");
        assertTrue(new String(response.getMessage()).contains("Unexpected error"),
                   "A mensagem de erro deve conter 'Unexpected error'. Mensagem atual: " + new String(response.getMessage()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetIteratorClosed() throws Exception {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getStateByRange(anyString(), anyString())).thenReturn(queryResultsIterator);

        Iterator<KeyValue> iterator = mock(Iterator.class);
        when(queryResultsIterator.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);

        Response response = artifactChaincode.get(stub, identity);
        assertEquals(Status.SUCCESS, response.getStatus());
        verify(queryResultsIterator).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetNoArtifactsFound() {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getStateByRange(anyString(), anyString())).thenReturn(queryResultsIterator);

        Iterator<KeyValue> iterator = mock(Iterator.class);
        when(queryResultsIterator.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);

        Response response = artifactChaincode.get(stub, identity);
        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("No artifacts found.", new String(response.getMessage()));
    }

    @Test
    void testGetExceptionHandling() {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getStateByRange(anyString(), anyString())).thenThrow(new RuntimeException("Simulated exception"));

        Response response = artifactChaincode.get(stub, identity);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Error during get operation.", new String(response.getMessage()));
    }

    @Test
    void testInvokeInsertSuccess() {
        when(stub.getFunction()).thenReturn("insert");
        when(stub.getParameters()).thenReturn(Arrays.asList("artifactKey", "{\"name\":\"test\"}"));
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");

        Response response = artifactChaincode.invoke(stub);
        assertEquals(Status.SUCCESS, response.getStatus());
        verify(stub).putStringState("artifactKey", "{\"name\":\"test\"}");
    }

    @Test
    void testInvokeInsertAccessDenied() {
        when(stub.getFunction()).thenReturn("insert");
        when(identity.getMSPID()).thenReturn("invalid-organization");
        when(stub.getParameters()).thenReturn(Arrays.asList("artifactKey", "{\"name\":\"test\"}"));

        Response response = artifactChaincode.insert(stub, stub.getParameters(), identity);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void testInvokeInsertInvalidArguments() {
        when(stub.getFunction()).thenReturn("insert");
        when(stub.getParameters()).thenReturn(Arrays.asList("artifactKey"));
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");

        Response response = artifactChaincode.invoke(stub);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void testInsertExceptionHandling() {
        when(identity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        when(stub.getParameters()).thenReturn(Arrays.asList("artifactKey", "{\"name\":\"test\"}"));
        doThrow(new RuntimeException("Simulated exception")).when(stub).putStringState(anyString(), anyString());

        Response response = artifactChaincode.insert(stub, stub.getParameters(), identity);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Error during insert operation.", new String(response.getMessage()));
    }

    @Test
    void testInvokeClientIdentityInitialization() throws Exception {
        when(stub.getFunction()).thenReturn("get");
        when(stub.getParameters()).thenReturn(Arrays.asList());
        ClientIdentity mockIdentity = mock(ClientIdentity.class);
        when(mockIdentity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        artifactChaincode.clientIdentity = null;
        artifactChaincode = spy(artifactChaincode);
        doReturn(mockIdentity).when(artifactChaincode).createClientIdentity(stub);

        artifactChaincode.invoke(stub);

        assertNotNull(artifactChaincode.clientIdentity);
    }

    @ParameterizedTest
    @CsvSource({
        "'artifactKey', 'null', 'Invalid artifact.'",
        "'artifactKey', '', 'Invalid artifact.'",
        "'artifactKey', '{\"name\":\"test\"}', 'Valid artifact.'"
    })
    void testValidateArtifact(String artifactKey, String artifactValue, String expectedMessage) {
        when(stub.getFunction()).thenReturn("validateArtifact");
        when(stub.getParameters()).thenReturn(Arrays.asList(artifactKey));
        String actualArtifactValue = "null".equals(artifactValue) ? null : artifactValue;
        when(stub.getStringState(artifactKey)).thenReturn(actualArtifactValue);
    
        Response response = artifactChaincode.invoke(stub);
    
        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals(expectedMessage, new String(response.getMessage()));
    }
    
    @Test
    void testValidateArtifactIncorrectArgs() {
        when(stub.getFunction()).thenReturn("validateArtifact");
        when(stub.getParameters()).thenReturn(Arrays.asList());

        Response response = artifactChaincode.invoke(stub);
    
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting only an artifact key.", new String(response.getMessage()));
    }

    @Test
    void testValidateArtifactExceptionHandling() {
        when(stub.getFunction()).thenReturn("validateArtifact");
        when(stub.getParameters()).thenReturn(Arrays.asList("artifactKey"));
        when(stub.getStringState("artifactKey")).thenThrow(new RuntimeException("Simulated exception"));

        Response response = artifactChaincode.invoke(stub);
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Error during validateArtifact operation.", new String(response.getMessage()));
    }

    @Test
    void testCreateClientIdentity() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        X500Name issuer = new X500Name("CN=Test Certificate");
        java.math.BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date startDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24);
        Date endDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365);
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, startDate, endDate, issuer, keyPair.getPublic());
        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(certHolder);
        String mockCertBase64 = Base64.getEncoder().encodeToString(certificate.getEncoded());
        Identities.SerializedIdentity serializedIdentity = Identities.SerializedIdentity.newBuilder()
                .setMspid("auditor-contaudit-ppgc-inf-ufrgs-br")
                .setIdBytes(com.google.protobuf.ByteString.copyFrom(Base64.getDecoder().decode(mockCertBase64)))
                .build();
        when(stub.getCreator()).thenReturn(serializedIdentity.toByteArray());

        ClientIdentity clientIdentity = artifactChaincode.createClientIdentity(stub);

        assertNotNull(clientIdentity, "ClientIdentity should not be null");
    }

    @Test
    void testStartChaincode() {
        ArtifactChaincode spyChaincode = spy(new ArtifactChaincode(null));
        doNothing().when(spyChaincode).start(any(String[].class));

        spyChaincode.startChaincode(new String[]{});

        verify(spyChaincode).start(any(String[].class));
    }
}