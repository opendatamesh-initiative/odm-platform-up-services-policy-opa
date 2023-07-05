package org.opendatamesh.policyserviceopa.server.repositories;

import org.opendatamesh.policyserviceopa.server.entities.PolicyEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends PagingAndSortingRepository<PolicyEntity, String> {
}