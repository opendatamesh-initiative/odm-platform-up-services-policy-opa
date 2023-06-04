package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import java.util.List;

import javax.persistence.ElementCollection;

import lombok.Data;

@Data
public class ValidatedSuiteDTO {

  @ElementCollection
  private List<ValidatedPolicyDTO> validatedPolicies;

  private String suite;
  
}