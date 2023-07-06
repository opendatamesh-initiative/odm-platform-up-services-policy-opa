package org.opendatamesh.platform.up.policy.opa.server.services;

import org.opendatamesh.platform.up.policy.opa.client.OpaClient;
import org.opendatamesh.platform.up.policy.opa.client.resources.v1.ValidateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OPAValidateService {

    @Autowired
    OpaClient opaClient;

    public Map validateByPolicyID(String id, ValidateRequest document){
        return opaClient.validateDocumentByPolicyId(id, document);
    }

    public Map<String, Object> validate(ValidateRequest document){
        return opaClient.validateDocument(document);
    }

}
