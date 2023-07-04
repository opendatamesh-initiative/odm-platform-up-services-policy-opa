package it.quantyca.odm.policyserviceopa.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.quantyca.odm.policyserviceopa.entities.SuiteEntity;
import it.quantyca.odm.policyserviceopa.exceptions.BadRequestException;
import it.quantyca.odm.policyserviceopa.exceptions.NotFoundException;
import it.quantyca.odm.policyserviceopa.repositories.SuiteRepository;
import it.quantyca.odm.policyserviceopa.resources.v1.mappers.SuiteMapper;
import org.opendatamesh.platform.up.policy.api.v1.controllers.AbstractSuiteController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.enums.PatchModes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/suites",
        produces = { "application/json" }
)
@Validated
@Tag(
        name = "Suite API",
        description = "CRUD API for Suite entity"
)
public class SuiteController extends AbstractSuiteController {

    @Autowired
    private SuiteRepository sr;

    @Autowired
    private SuiteMapper sm;

    @Override
    public ResponseEntity readSuites() {

        Iterable<SuiteEntity> suites = sr.findAll();
        Iterable<SuiteResource> suitesDTO = sm.suiteIterableToSuiteDTOIterable(suites);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(suitesDTO);

    }

    @Override
    public ResponseEntity readOneSuite(String id) {

        Optional<SuiteEntity> suite = sr.findById(id);
        if (!suite.isPresent()){
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND,
                    "Suite " + id + " not found on DB"
            );
        }

        SuiteResource suiteResource = sm.suiteToSuiteDto(suite.get());

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(suiteResource);

    }

    @Override
    public ResponseEntity createSuite(SuiteResource suite) {

        SuiteEntity suiteEntity = sm.suiteDTOToSuite(suite);

        // Check if policy with that ID does not already exist
        if (!sr.existsById(suiteEntity.getId())) {

            // POST on internal DB
            sr.save(suiteEntity);

            // Return creation message
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(suite);
        } else {
            throw new BadRequestException(
                    PolicyserviceOpaAPIStandardError.SC400_SUITE_ALREADY_EXISTS,
                    "Policy with ID " + suite.getId() + "already exists on DB"
            );
        }

    }

    @Override
    public ResponseEntity deleteSuite(String id) {

        // Check if policy does not exist on DB
        if (!sr.existsById(id)) {
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND,
                    "Suite " + id + " not found on DB"
            );
        } else {

            // Delete policy on internal DB
            sr.deleteById(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Suite " + id + " correctly deleted");

        }

    }

    @Override
    public ResponseEntity updateSuite(String suiteId, PatchModes mode, String policyId) {

        Optional<SuiteEntity> suiteEntityOpt = sr.findById(suiteId);

        if(!suiteEntityOpt.isPresent()) {
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND,
                    "Suite " + suiteId + " not found on DB"
            );
        }

        SuiteEntity suiteEntity = suiteEntityOpt.get();
        List<String> policyIds = suiteEntity.getPolicies();
        if (mode.equals(PatchModes.ADD)) {
            policyIds.add(policyId);
        } else {
            policyIds.remove(policyId);
        }
        suiteEntity.setPolicies(policyIds);
        sr.save(suiteEntity);

        SuiteResource suite = sm.suiteToSuiteDto(suiteEntity);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(suite);

    }

}