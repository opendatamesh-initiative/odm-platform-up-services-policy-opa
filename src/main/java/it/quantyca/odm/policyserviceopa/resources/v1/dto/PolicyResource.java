package it.quantyca.odm.policyserviceopa.resources.v1.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PolicyResource {

  private String id;

  private String displayName;

  private String description;
  
  private String rawPolicy;

  private Date createdAt;

  private Date updatedAt;

}