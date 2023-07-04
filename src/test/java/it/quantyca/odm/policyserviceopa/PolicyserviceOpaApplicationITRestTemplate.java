package it.quantyca.odm.policyserviceopa;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.PolicyResource;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.SuiteResource;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.ValidateResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class PolicyserviceOpaApplicationITRestTemplate extends TestRestTemplate {

    protected String host = "localhost";
    protected String port = "80";
    private ObjectMapper objectMapper;

    public PolicyserviceOpaApplicationITRestTemplate(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    protected String apiUrl(RoutesV1 route) {
        return apiUrl(route, "");
    }

    protected String apiUrl(RoutesV1 route, String extension) {
        return apiUrlFromString(route.getPath() + extension);
    }

    protected String apiUrlFromString(String routeUrlString) {
        return "http://" + host + ":" + port + routeUrlString;
    }

    protected String apiUrlOfItem(RoutesV1 route) {
        return apiUrl(route, "/{id}");
    }

    protected String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    HttpEntity<PolicyResource> getPolicyFileAsHttpEntity(String file) throws IOException {

        HttpEntity<PolicyResource> entity = null;
        PolicyResource policyResource = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (file != null) {
            String docContet = readFile(file);
            policyResource = objectMapper.readValue(docContet, PolicyResource.class);
        }

        entity = new HttpEntity<>(policyResource, headers);

        return entity;
    }

    HttpEntity<SuiteResource> getSuiteFileAsHttpEntity(String file) throws IOException {

        HttpEntity<SuiteResource> entity = null;
        SuiteResource suiteResource = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (file != null) {
            String docContet = readFile(file);
            suiteResource = objectMapper.readValue(docContet, SuiteResource.class);
        }

        entity = new HttpEntity<>(suiteResource, headers);

        return entity;
    }

    HttpEntity<Object> getDocumentAsHttpEntity(String file) throws IOException {

        HttpEntity<Object> entity = null;
        Object document = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (file != null) {
            String docContet = readFile(file);
            document = objectMapper.readValue(docContet, Object.class);
        }

        entity = new HttpEntity<>(document, headers);

        return entity;
    }

    // ======================================================================================
    // Proxy services
    // ======================================================================================

    // ----------------------------------------
    // Policy
    // ----------------------------------------

    public ResponseEntity<PolicyResource> createPolicy(String filePath) throws IOException {

        HttpEntity<PolicyResource> entity = getPolicyFileAsHttpEntity(filePath);

        ResponseEntity<PolicyResource> postPolicyResponse = postForEntity(
                apiUrl(RoutesV1.POLICY),
                entity,
                PolicyResource.class
        );

        return postPolicyResponse;

    }

    public ResponseEntity<PolicyResource> updatePolicy(String id, String filePath) throws IOException {

        HttpEntity<PolicyResource> entity = getPolicyFileAsHttpEntity(filePath);

        ResponseEntity<PolicyResource> putPolicyResponse = exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.PUT,
                entity,
                PolicyResource.class,
                id
        );

        return putPolicyResponse;

    }

    public ResponseEntity<PolicyResource[]> readAllPolicies() {
        return getForEntity(
                apiUrl(RoutesV1.POLICY),
                PolicyResource[].class);
    }

    public ResponseEntity<PolicyResource> readOnePolicy(String id) {
        return getForEntity(
                apiUrlOfItem(RoutesV1.POLICY),
                PolicyResource.class,
                id
        );
    }

    public ResponseEntity<Void> deletePolicy(String policyId) {
        return exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.DELETE,
                null,
                Void.class,
                policyId
        );
    }
    
    
    // ----------------------------------------
    // Suite
    // ----------------------------------------

    public ResponseEntity<SuiteResource> createSuite(String filePath) throws IOException {

        HttpEntity<SuiteResource> entity = getSuiteFileAsHttpEntity(filePath);

        ResponseEntity<SuiteResource> postSuiteResponse = postForEntity(
                apiUrl(RoutesV1.SUITE),
                entity,
                SuiteResource.class
        );

        return postSuiteResponse;

    }

    public ResponseEntity<SuiteResource[]> readAllSuites() {
        return getForEntity(
                apiUrl(RoutesV1.SUITE),
                SuiteResource[].class);
    }

    public ResponseEntity<SuiteResource> readOneSuite(String id) {
        return getForEntity(
                apiUrlOfItem(RoutesV1.SUITE),
                SuiteResource.class,
                id
        );
    }

    public ResponseEntity<Void> deleteSuite(String suiteId) {
        return exchange(
                apiUrlOfItem(RoutesV1.SUITE),
                HttpMethod.DELETE,
                null,
                java.lang.Void.class,
                suiteId
        );
    }



    // ----------------------------------------
    // Validation
    // ----------------------------------------

    public ResponseEntity<Map> validateDocument(String filePath) throws IOException {

        HttpEntity<Object> document = getDocumentAsHttpEntity(filePath);

        return exchange(
                apiUrl(RoutesV1.VALIDATE),
                HttpMethod.POST,
                document,
                Map.class
        );
    }

    public ResponseEntity<ValidateResponse> validateDocumentByPoliciesIds(String filePath, String id) throws IOException {

        HttpEntity<Object> document = getDocumentAsHttpEntity(filePath);

        return exchange(
                apiUrl(RoutesV1.VALIDATE, "?id={id}"),
                HttpMethod.POST,
                document,
                ValidateResponse.class,
                id
        );
    }

    public ResponseEntity<ValidateResponse> validateDocumentBySuite(String filePath, String suite) throws IOException {

        HttpEntity<Object> document = getDocumentAsHttpEntity(filePath);

        return exchange(
                apiUrl(RoutesV1.VALIDATE, "?suite={suite}"),
                HttpMethod.POST,
                document,
                ValidateResponse.class,
                suite
        );
    }

}