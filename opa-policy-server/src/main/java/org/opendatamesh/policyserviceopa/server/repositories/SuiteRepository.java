package org.opendatamesh.policyserviceopa.server.repositories;

import org.opendatamesh.policyserviceopa.server.entities.SuiteEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuiteRepository extends PagingAndSortingRepository<SuiteEntity, String> {
}