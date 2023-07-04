package it.quantyca.odm.policyserviceopa.resources.v1.mappers;

import it.quantyca.odm.policyserviceopa.entities.SuiteEntity;
import org.mapstruct.Mapper;
import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;

@Mapper(componentModel = "spring")
public interface  SuiteMapper {

    SuiteResource suiteToSuiteDto(SuiteEntity suite);
    SuiteEntity suiteDTOToSuite(SuiteResource suite);
    Iterable<SuiteEntity> suiteDTOIterableToSuiteIterable(Iterable<SuiteResource> suites);
    Iterable<SuiteResource> suiteIterableToSuiteDTOIterable(Iterable<SuiteEntity> suites);

}
