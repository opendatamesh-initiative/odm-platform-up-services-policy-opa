package org.opendatamesh.policyserviceopa.client;

import org.opendatamesh.policyserviceopa.client.resources.v1.ValidateRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OpaClient {

    RestTemplate restTemplate;

    String policiesUrl;

    String dataUrl;

    Long timeout;

    public OpaClient(String policiesUrl, String dataUrl, Long timeout) {
        this.policiesUrl = policiesUrl;
        this.dataUrl = dataUrl;
        this.timeout = timeout;
        this.restTemplate = new OpaRestTemplate(timeout).buildRestTemplate();
    }

    public Map<String, Object> getPolicies() {
        return restTemplate.getForObject(policiesUrl, Map.class);
    }

    public Map<String, Object> getPolicyById(String id) {
        return restTemplate.getForObject(policiesUrl+"/"+id, Map.class);
    }

    public void updatePolicy(String id, String policy) {
        restTemplate.put(policiesUrl+"/"+id, policy);
    }

    public void deletePolicyById(String id) {
        restTemplate.delete(policiesUrl+"/"+id);
    }

    public Map validateDocumentByPolicyId(String id, ValidateRequest document) {
        return restTemplate.postForObject(dataUrl+"/"+id, document, Map.class);
    }

    public Map<String, Object> validateDocument(ValidateRequest document) {
        return restTemplate.postForObject(dataUrl, document, Map.class);
    }

}
