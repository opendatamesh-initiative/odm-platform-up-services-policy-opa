package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import java.util.List;
import org.opendatamesh.platform.up.policy.api.v1.resources.ValidatedPolicyResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.ValidatedSuiteResource;

import javax.persistence.ElementCollection;

import lombok.Data;

@Data
public class ValidateResponse {

  @ElementCollection
  private List<ValidatedPolicyResource> validatedPolicies;

  @ElementCollection
  private List<ValidatedSuiteResource> validatedSuites;
  
}