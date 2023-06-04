package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import java.util.List;

import javax.persistence.ElementCollection;

import lombok.Data;

@Data
public class ValidateResponse {

  @ElementCollection
  private List<ValidatedPolicyDTO> validatedPolicies;

  @ElementCollection
  private List<ValidatedSuiteDTO> validatedSuites;
  
}