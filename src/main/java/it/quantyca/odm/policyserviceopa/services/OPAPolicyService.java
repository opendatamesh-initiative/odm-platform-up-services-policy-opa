package it.quantyca.odm.policyserviceopa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import it.quantyca.odm.policyserviceopa.resources.v1.dto.PolicyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OPAPolicyService {

    @Value("${opa.url.policies}")
    private String policiesUrl;

    @Autowired
    RestTemplate rt;

    public Iterable<PolicyDTO> getAllOPAPolicies(){

        List<PolicyDTO> policies = new ArrayList<PolicyDTO>();
        Map<String, Object> opaResponse = rt.getForObject(policiesUrl, Map.class);
        List<Map<String, Object>> result = (List<Map<String, Object>>) opaResponse.get("result");
        for (Map<String, Object> policy : result){
            PolicyDTO policyDTO = new PolicyDTO();
            policyDTO.setId((String) policy.get("id"));
            policyDTO.setRawPolicy((String)policy.get("raw"));
            policies.add(policyDTO);
        }
    
        return policies;
    
    };

    public PolicyDTO getOPAPolicyByID(String id){

        Map<String, Object> opaResponse = rt.getForObject(policiesUrl+"/"+id, Map.class);
        Map<String, Object> result = (Map<String, Object>) opaResponse.get("result");
        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setId((String) result.get("id"));
        policyDTO.setRawPolicy((String)result.get("raw"));

        return policyDTO;
    };

    public void putOPAPolicy(String id, String policy){

        rt.put(policiesUrl+"/"+id,policy);

    }

    public void deleteOPAPolicyById(String id){

        rt.delete(policiesUrl+"/"+id);

    }

}
