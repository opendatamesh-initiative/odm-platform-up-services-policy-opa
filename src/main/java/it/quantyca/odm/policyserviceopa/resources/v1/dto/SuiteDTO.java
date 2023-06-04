package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import lombok.Data;

import javax.persistence.ElementCollection;
import java.util.Date;
import java.util.Set;

@Data
public class SuiteDTO {

  private String id;

  private String displayName;

  private String description;

  private Date createdAt;

  private Date updatedAt;

  @ElementCollection
  private Set<String> policies;

}