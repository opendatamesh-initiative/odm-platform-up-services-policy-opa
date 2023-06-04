package it.quantyca.odm.policyserviceopa.repositories;

import it.quantyca.odm.policyserviceopa.entities.SuiteEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuiteRepository extends PagingAndSortingRepository<SuiteEntity, String> {
}