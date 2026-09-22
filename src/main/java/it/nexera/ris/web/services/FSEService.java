package it.nexera.ris.web.services;


import io.jsonwebtoken.Jwts;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.FSEValidationStatus;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.common.FileValidation;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class FSEService {

    private static final Logger log = LogManager.getLogger(FSEService.class);

    private static final String VALIDATION_ENDPOINT = "/documents/validation";

    private static final String KEYSTORE_PASSWORD = "";

    private static final String ALGORITHM = "RSA";

    private static final String VALIDATION_REQUEST_BODY = "{\"healthDataFormat\": \"CDA\", \"mode\": \"ATTACHMENT\", \"activity\": \"VALIDATION\"}";

    private Properties JWTProperties;

    private String baseUrl;

    public boolean createValidationRequest(String path, String fiscalCode, Long radiologyExamRequestId, Long fileEntityId) throws PersistenceBeanException {
        String response = null;
        try {
            loadJWTProperty();
            setBaseUrl(ApplicationSettingsHolder.getInstance().getByKey(
                    ApplicationSettingsKeys.FSE_GATEWAY_ENDPOINT).getValue());
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + generateAuthorizationJWT());
            headers.put("FSE-JWT-Signature", generateSignJWT(fiscalCode));
            headers.put("Accept", "application/json");
            HttpPostMultipart multipart = new HttpPostMultipart(getBaseUrl() + VALIDATION_ENDPOINT, "utf-8", headers, getSocketFactory());
            multipart.addFormField("requestBody", VALIDATION_REQUEST_BODY);
            multipart.addFilePart("file", new File(path));
            response = multipart.finish();
        } catch (Exception e) {
            log.error("An error occurred during the validation of the pdf document", e);
        }
        return saveResult(response, path, radiologyExamRequestId, fileEntityId);
    }

    private boolean saveResult(String response, String path, Long radiologyExamRequestId, Long fileEntityId) throws PersistenceBeanException {
        log.info("Response: " + response);
        FileValidation fileValidation = new FileValidation();
        fileValidation.setStatus(FSEValidationStatus.ERROR);
        if (!ValidationHelper.isNullOrEmpty(response)) {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("workflowInstanceId")) {
                fileValidation.setWorkflowInstanceId(jsonObject.get("workflowInstanceId").toString());
            }
            if (!jsonObject.has("status")) {
                fileValidation.setStatus(FSEValidationStatus.SUCCESS);
            }
        }
        fileValidation.setResponce(response);
        fileValidation.setPath(path);
        fileValidation.setFileEntityId(fileEntityId);
        fileValidation.setRadiologyExamRequestId(radiologyExamRequestId);
        DaoManager.save(fileValidation);
        return fileValidation.getStatus() == FSEValidationStatus.SUCCESS;
    }

    private String generateAuthorizationJWT() throws Exception {
        String[] x5cArray = {FileHelper.getFileBase64(getJWTProperties().getProperty("PATH_TO_DER_FILE"))};
        String authToken = Jwts.builder()
                .signWith(getSignPrivateKey(), Jwts.SIG.RS256).header()
                .add("typ", getJWTProperties().getProperty("TYPE"))
                .add("x5c", Arrays.stream(x5cArray).toArray())
                .and()
                .claim("iss", getJWTProperties().getProperty("COMMON_NAME_AUTH"))
                .claim("iat", getCurrentTime())
                .claim("exp", datePlusOneDay())
                .claim("jti", UUID.randomUUID().toString())
                .claim("aud", getBaseUrl())
                .claim("sub", getJWTProperties().getProperty("SUB"))
                .compact();
        log.info("Auth token: " + authToken);
        return authToken;
    }

    private String generateSignJWT(String fiscalCode) throws Exception {
        String[] x5cArray = {FileHelper.getFileBase64(getJWTProperties().getProperty("PATH_TO_DER_FILE"))};
        String signToken = Jwts.builder()
                .signWith(getSignPrivateKey(), Jwts.SIG.RS256).header()
                .add("typ", getJWTProperties().getProperty("TYPE"))
                .add("x5c", Arrays.stream(x5cArray).toArray())
                .and()
                .claim("iss", getJWTProperties().getProperty("COMMON_NAME_INT"))
                .claim("iat", getCurrentTime())
                .claim("exp", datePlusOneDay())
                .claim("jti", UUID.randomUUID().toString())
                .claim("aud", getBaseUrl())
                .claim("sub", getJWTProperties().getProperty("SUB"))
                .claim("subject_organization_id", getJWTProperties().getProperty("SUBJECT_ORGANIZATION_ID"))
                .claim("subject_organization", getJWTProperties().getProperty("SUBJECT_ORGANIZATION"))
                .claim("locality", getJWTProperties().getProperty("LOCALITY"))
                .claim("subject_role", getJWTProperties().getProperty("SUBJECT_ROLE"))
                .claim("person_id", fiscalCode + "^^^&2.16.840.1.113883.2.9.4.3.2&ISO")
                .claim("patient_consent", getJWTProperties().getProperty("PATIENT_CONSENT"))
                .claim("purpose_of_use", getJWTProperties().getProperty("PURPOSE_OF_USE"))
                .claim("resource_hl7_type", "('" + "68604-8" + "^^2.16.840.1.113883.6.1')")
                .claim("action_id", getJWTProperties().getProperty("ACTION_ID"))
                .claim("subject_application_id", getJWTProperties().getProperty("APPLICATION_ID"))
                .claim("subject_application_vendor", getJWTProperties().getProperty("APPLICATION_VENDOR"))
                .claim("subject_application_version", "V." + getJWTProperties().getProperty("APPLICATION_VERSION"))
                .compact();
        log.info("Sign token: " + signToken);
        return signToken;
    }

    private SSLSocketFactory getSocketFactory() throws Exception {
        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(new FileInputStream(getJWTProperties().getProperty("PATH_TO_P_12_FILE")), KEYSTORE_PASSWORD.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientStore, KEYSTORE_PASSWORD.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(new FileInputStream(System.getProperty("java.home") + "/lib/security/cacerts"), "changeit".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager[] tms = trustManagerFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, tms, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            log.error("Socket factory not loaded", e);
            throw new Exception();
        }
    }

    private PrivateKey getSignPrivateKey() throws Exception {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(getJWTProperties().getProperty("PATH_TO_SIGN_KEY")));
            String keyContent = new String(keyBytes)
                    .replaceAll("-----BEGIN .*-----\n", "")
                    .replaceAll("-----END .*-----\n", "")
                    .replaceAll("\n", "");
            byte[] keyDecoded = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyDecoded);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Sign key not loaded", e);
            throw new Exception();
        }
    }

    private void loadJWTProperty() throws Exception {
        String resourceName = "JWT_config.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        setJWTProperties(new Properties());
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            getJWTProperties().load(resourceStream);
        } catch (IOException e) {
            log.error("No properties loaded", e);
            throw new Exception();
        }
    }

    private static String getCurrentTime() {
        return String.valueOf(new Date().getTime() / 1000);
    }

    private static Long datePlusOneDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime().getTime() / 1000;
    }

    public Properties getJWTProperties() {
        return JWTProperties;
    }

    public void setJWTProperties(Properties JWTProperties) {
        this.JWTProperties = JWTProperties;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
