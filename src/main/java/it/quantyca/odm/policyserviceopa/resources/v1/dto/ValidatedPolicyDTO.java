package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import lombok.Data;

@Data
public class ValidatedPolicyDTO {

  private Object validationResult;

  private String policy;
  
}