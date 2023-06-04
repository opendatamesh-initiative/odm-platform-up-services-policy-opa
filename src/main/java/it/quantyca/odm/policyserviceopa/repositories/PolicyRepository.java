package it.quantyca.odm.policyserviceopa.repositories;

import it.quantyca.odm.policyserviceopa.entities.PolicyEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends PagingAndSortingRepository<PolicyEntity, String> {
}