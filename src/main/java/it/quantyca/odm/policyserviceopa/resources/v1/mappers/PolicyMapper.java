package it.quantyca.odm.policyserviceopa.resources.v1.mappers;

import it.quantyca.odm.policyserviceopa.entities.PolicyEntity;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.PolicyDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    PolicyDTO policyToPolicyDto(PolicyEntity policy);
    PolicyEntity policyDTOToPolicy(PolicyDTO policy);
    Iterable<PolicyEntity> policyDTOIterableToPolicyIterable(Iterable<PolicyDTO> policies);
    Iterable<PolicyDTO> policyIterableToPolicyDTOIterable(Iterable<PolicyEntity> policies);
    
}