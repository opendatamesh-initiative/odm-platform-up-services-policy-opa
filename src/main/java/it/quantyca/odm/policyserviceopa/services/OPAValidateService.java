package it.quantyca.odm.policyserviceopa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import it.quantyca.odm.policyserviceopa.resources.v1.dto.ValidateRequest;

import java.util.Map;

@Service
public class OPAValidateService {

    @Value("${opa.url.data}")
    private String dataUrl;

    @Autowired
    RestTemplate rt;

    public Map validateByPolicyID(String id, ValidateRequest document){

        return rt.postForObject(dataUrl+"/"+id,document, Map.class);

    };

    public Map<String, Object> validate(ValidateRequest document){

        Map<String, Object>   map = rt.postForObject(dataUrl,document, Map.class);

        return map;
    };

}
