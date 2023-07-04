package it.quantyca.odm.policyserviceopa.resources.v1.mappers;

import it.quantyca.odm.policyserviceopa.entities.PolicyEntity;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.PolicyResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    PolicyResource policyToPolicyDto(PolicyEntity policy);
    PolicyEntity policyDTOToPolicy(PolicyResource policy);
    Iterable<PolicyEntity> policyDTOIterableToPolicyIterable(Iterable<PolicyResource> policies);
    Iterable<PolicyResource> policyIterableToPolicyDTOIterable(Iterable<PolicyEntity> policies);
    
}