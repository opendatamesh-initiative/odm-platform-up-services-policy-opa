package org.opendatamesh.platform.up.policy.opa.server.services;

import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.opendatamesh.platform.up.policy.opa.client.OpaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OPAPolicyService {

    @Autowired
    OpaClient opaClient; //= new OpaClient(policiesUrl);

    public Iterable<PolicyResource> getAllOPAPolicies(){

        List<PolicyResource> policies = new ArrayList<>();
        Map<String, Object> opaResponse = opaClient.getPolicies();
        List<Map<String, Object>> result = (List<Map<String, Object>>) opaResponse.get("result");
        for (Map<String, Object> policy : result){
            PolicyResource policyResource = new PolicyResource();
            policyResource.setId((String) policy.get("id"));
            policyResource.setRawPolicy((String)policy.get("raw"));
            policies.add(policyResource);
        }
    
        return policies;
    
    }

    public PolicyResource getOPAPolicyByID(String id){

        Map<String, Object> opaResponse = opaClient.getPolicyById(id);
        Map<String, Object> result = (Map<String, Object>) opaResponse.get("result");
        PolicyResource policyResource = new PolicyResource();
        policyResource.setId((String) result.get("id"));
        policyResource.setRawPolicy((String)result.get("raw"));

        return policyResource;
    }

    public void putOPAPolicy(String id, String policy){
        opaClient.updatePolicy(id, policy);
    }

    public void deleteOPAPolicyById(String id){
        opaClient.deletePolicyById(id);
    }

}
