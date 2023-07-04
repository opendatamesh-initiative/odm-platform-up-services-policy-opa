package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import java.util.List;

import javax.persistence.ElementCollection;

import lombok.Data;

@Data
public class ValidatedSuiteResource {

  @ElementCollection
  private List<ValidatedPolicyResource> validatedPolicies;

  private String suite;
  
}