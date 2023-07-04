package it.quantyca.odm.policyserviceopa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OPAPolicyService {

    @Value("${opa.url.policies}")
    private String policiesUrl;

    @Autowired
    RestTemplate rt;

    public Iterable<PolicyResource> getAllOPAPolicies(){

        List<PolicyResource> policies = new ArrayList<>();
        Map<String, Object> opaResponse = rt.getForObject(policiesUrl, Map.class);
        List<Map<String, Object>> result = (List<Map<String, Object>>) opaResponse.get("result");
        for (Map<String, Object> policy : result){
            PolicyResource policyResource = new PolicyResource();
            policyResource.setId((String) policy.get("id"));
            policyResource.setRawPolicy((String)policy.get("raw"));
            policies.add(policyResource);
        }
    
        return policies;
    
    };

    public PolicyResource getOPAPolicyByID(String id){

        Map<String, Object> opaResponse = rt.getForObject(policiesUrl+"/"+id, Map.class);
        Map<String, Object> result = (Map<String, Object>) opaResponse.get("result");
        PolicyResource policyResource = new PolicyResource();
        policyResource.setId((String) result.get("id"));
        policyResource.setRawPolicy((String)result.get("raw"));

        return policyResource;
    };

    public void putOPAPolicy(String id, String policy){

        rt.put(policiesUrl+"/"+id,policy);

    }

    public void deleteOPAPolicyById(String id){

        rt.delete(policiesUrl+"/"+id);

    }

}
