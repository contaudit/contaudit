package br.ufrgs.inf.ppgc.contaudit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
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
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WrapperChaincodeTest {
    @Mock
    private ChaincodeStub stub;

    @Mock
    private ClientIdentity clientIdentity;

    @InjectMocks
    private WrapperChaincode wrapperChaincode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInit() {
        Response response = wrapperChaincode.init(stub);
        assertEquals(Status.SUCCESS, response.getStatus());
    }

    @Test
    void testInvokeClientIdentityInitialization() throws Exception {
        when(stub.getFunction()).thenReturn("get");
        when(stub.getParameters()).thenReturn(Arrays.asList());
        ClientIdentity mockIdentity = mock(ClientIdentity.class);
        when(mockIdentity.getMSPID()).thenReturn("auditor-contaudit-ppgc-inf-ufrgs-br");
        wrapperChaincode.clientIdentity = null;
        wrapperChaincode = spy(wrapperChaincode);
        doReturn(mockIdentity).when(wrapperChaincode).createClientIdentity(stub);

        wrapperChaincode.invoke(stub);

        assertNotNull(wrapperChaincode.clientIdentity);
    }

    @Test
    void testInvokeInvalidFunction() {
        when(stub.getFunction()).thenReturn("invalidFunction");

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Invalid invoke function name"));
    }

    @Test
    void testInvokeCatchExceptionHandling() {
        when(stub.getFunction()).thenThrow(new RuntimeException("Simulated Exception"));
 
        Response response = wrapperChaincode.invoke(stub);
    
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Unexpected error"), "A resposta deveria conter 'Unexpected error'");
    }

    @Test
    void testGetHashSuccess() {
        when(stub.getFunction()).thenReturn("getHash");
        when(stub.getStringState(WrapperChaincode.WRAPPER_HASH_KEY)).thenReturn("hashValue");
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(new String(response.getMessage()).contains("Wrapper hash."));
    }

    @Test
    void testGetHasNullHashValue() {
        when(stub.getFunction()).thenReturn("getHash");
        when(stub.getStringState(WrapperChaincode.WRAPPER_HASH_KEY)).thenReturn(null);
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Wrapper hash not found."));
    }

    @Test
    void testGetHashEmptyHashValue() {
        when(stub.getFunction()).thenReturn("getHash");
        when(stub.getStringState(WrapperChaincode.WRAPPER_HASH_KEY)).thenReturn("");
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Wrapper hash not found."));
    }

    @Test
    void testGetHashAccessDenied() {
        when(stub.getFunction()).thenReturn("getHash");
        when(clientIdentity.getMSPID()).thenReturn("unauthorized-org");
    
        Response response = wrapperChaincode.invoke(stub);
    
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Access Denied for organization."));
    }

    @Test
    void testGetHashExceptionHandling() {
        when(stub.getFunction()).thenReturn("getHash");
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);
        doThrow(new RuntimeException("Test Exception")).when(stub).getStringState(WrapperChaincode.WRAPPER_HASH_KEY);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Error during getHash operation"));
    }

    @Test
    void testUpdateHashSuccess() {
        when(stub.getFunction()).thenReturn("updateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList("newHash"));
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.SUCCESS, response.getStatus());
        verify(stub, times(1)).putStringState(WrapperChaincode.WRAPPER_HASH_KEY, "newHash");
    }

    @Test
    void testUpdateHashInvalidArgs() {
        when(stub.getFunction()).thenReturn("updateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList());
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Incorrect number of arguments. Expecting a wrapper hash."));
    }

    @Test
    void testUpdateHashAccessDenied() {
        when(stub.getFunction()).thenReturn("updateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList("newHash"));
        when(clientIdentity.getMSPID()).thenReturn("unauthorized-org");

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Access Denied for organization."));
    }

    @Test
    void testUpdateHashExceptionHandling() {
        when(stub.getFunction()).thenReturn("updateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList("newHash"));
        when(clientIdentity.getMSPID()).thenReturn(WrapperChaincode.ORGANIZATION);
        doThrow(new RuntimeException("Test Exception")).when(stub).putStringState(WrapperChaincode.WRAPPER_HASH_KEY, "newHash");

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Error during updateHash operation"));
    }

    @Test
    void testValidateHashSuccess() {
        when(stub.getFunction()).thenReturn("validateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList("validHash"));
        when(stub.getStringState(WrapperChaincode.WRAPPER_HASH_KEY)).thenReturn("validHash");

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(new String(response.getMessage()).contains("Valid wrapper hash."));
    }

    @Test
    void testValidateHashFailure() {
        when(stub.getFunction()).thenReturn("validateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList("invalidHash"));
        when(stub.getStringState(WrapperChaincode.WRAPPER_HASH_KEY)).thenReturn("validHash");
    
        Response response = wrapperChaincode.invoke(stub);
    
        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(new String(response.getMessage()).contains("Invalid wrapper hash."));
    }

    @Test
    void testValidateHashInvalidArgs() {
        when(stub.getFunction()).thenReturn("validateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList());
    
        Response response = wrapperChaincode.invoke(stub);
    
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Incorrect number of arguments. Expecting only a hash string."));
    }    

    @Test
    void testValidateHashExceptionHandling() {
        when(stub.getFunction()).thenReturn("validateHash");
        when(stub.getParameters()).thenReturn(Arrays.asList("hash"));
        doThrow(new RuntimeException("Test Exception")).when(stub).getStringState(WrapperChaincode.WRAPPER_HASH_KEY);

        Response response = wrapperChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getMessage().contains("Error during validateHash operation"));
    }

    @Test
    void testCreateClientIdentityExceptionHandling() {
        when(stub.getFunction()).thenReturn("getHash");
        doThrow(new RuntimeException("Test Exception")).when(stub).getCreator();
    
        Response response = wrapperChaincode.invoke(stub);
    
        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
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

        ClientIdentity identity = wrapperChaincode.createClientIdentity(stub);

        assertNotNull(identity, "ClientIdentity should not be null");
    }

    @Test
    void testCreateClientIdentityThrowsCertificateException() {
        wrapperChaincode = new WrapperChaincode(null) {
            @Override
            protected ClientIdentity createClientIdentity(ChaincodeStub stub) throws CertificateException {
                throw new CertificateException("Test Certificate Exception");
            }
        };
        assertThrows(CertificateException.class, () -> {
            wrapperChaincode.createClientIdentity(stub);
        });
    }

    @Test
    void testCreateClientIdentityThrowsJSONException() {
        doThrow(new JSONException("Test JSON Exception")).when(stub).getCreator();

        JSONException thrown = assertThrows(JSONException.class, () -> {
            wrapperChaincode.createClientIdentity(stub);
        });

        assertEquals("Test JSON Exception", thrown.getMessage());
    }

    @Test
    void testCreateClientIdentityThrowsIOException() {
        wrapperChaincode = new WrapperChaincode(null) {
            @Override
            protected ClientIdentity createClientIdentity(ChaincodeStub stub) throws IOException {
                throw new IOException("Test IO Exception");
            }
        };
        assertThrows(IOException.class, () -> {
            wrapperChaincode.createClientIdentity(stub);
        });
    }
}