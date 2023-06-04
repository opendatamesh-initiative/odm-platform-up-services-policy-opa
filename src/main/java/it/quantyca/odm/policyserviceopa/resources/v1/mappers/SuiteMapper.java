package it.quantyca.odm.policyserviceopa.resources.v1.mappers;

import it.quantyca.odm.policyserviceopa.entities.SuiteEntity;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.SuiteDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface  SuiteMapper {

    SuiteDTO suiteToSuiteDto(SuiteEntity suite);
    SuiteEntity suiteDTOToSuite(SuiteDTO suite);
    Iterable<SuiteEntity> suiteDTOIterableToSuiteIterable(Iterable<SuiteDTO> suites);
    Iterable<SuiteDTO> suiteIterableToSuiteDTOIterable(Iterable<SuiteEntity> suites);

}
