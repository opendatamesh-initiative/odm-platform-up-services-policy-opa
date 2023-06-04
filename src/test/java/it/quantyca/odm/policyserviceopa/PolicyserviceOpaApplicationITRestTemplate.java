package it.quantyca.odm.policyserviceopa;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.PolicyDTO;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.SuiteDTO;
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

    HttpEntity<PolicyDTO> getPolicyFileAsHttpEntity(String file) throws IOException {

        HttpEntity<PolicyDTO> entity = null;
        PolicyDTO policyDTO = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (file != null) {
            String docContet = readFile(file);
            policyDTO = objectMapper.readValue(docContet, PolicyDTO.class);
        }

        entity = new HttpEntity<>(policyDTO, headers);

        return entity;
    }

    HttpEntity<SuiteDTO> getSuiteFileAsHttpEntity(String file) throws IOException {

        HttpEntity<SuiteDTO> entity = null;
        SuiteDTO suiteDTO = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (file != null) {
            String docContet = readFile(file);
            suiteDTO = objectMapper.readValue(docContet, SuiteDTO.class);
        }

        entity = new HttpEntity<>(suiteDTO, headers);

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

    public ResponseEntity<PolicyDTO> createPolicy(String filePath) throws IOException {

        HttpEntity<PolicyDTO> entity = getPolicyFileAsHttpEntity(filePath);

        ResponseEntity<PolicyDTO> postPolicyResponse = postForEntity(
                apiUrl(RoutesV1.POLICY),
                entity,
                PolicyDTO.class
        );

        return postPolicyResponse;

    }

    public ResponseEntity<PolicyDTO> updatePolicy(String id, String filePath) throws IOException {

        HttpEntity<PolicyDTO> entity = getPolicyFileAsHttpEntity(filePath);

        ResponseEntity<PolicyDTO> putPolicyResponse = exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.PUT,
                entity,
                PolicyDTO.class,
                id
        );

        return putPolicyResponse;

    }

    public ResponseEntity<PolicyDTO[]> readAllPolicies() {
        return getForEntity(
                apiUrl(RoutesV1.POLICY),
                PolicyDTO[].class);
    }

    public ResponseEntity<PolicyDTO> readOnePolicy(String id) {
        return getForEntity(
                apiUrlOfItem(RoutesV1.POLICY),
                PolicyDTO.class,
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

    public ResponseEntity<SuiteDTO> createSuite(String filePath) throws IOException {

        HttpEntity<SuiteDTO> entity = getSuiteFileAsHttpEntity(filePath);

        ResponseEntity<SuiteDTO> postSuiteResponse = postForEntity(
                apiUrl(RoutesV1.SUITE),
                entity,
                SuiteDTO.class
        );

        return postSuiteResponse;

    }

    public ResponseEntity<SuiteDTO[]> readAllSuites() {
        return getForEntity(
                apiUrl(RoutesV1.SUITE),
                SuiteDTO[].class);
    }

    public ResponseEntity<SuiteDTO> readOneSuite(String id) {
        return getForEntity(
                apiUrlOfItem(RoutesV1.SUITE),
                SuiteDTO.class,
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