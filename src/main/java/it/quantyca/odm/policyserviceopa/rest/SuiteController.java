package it.quantyca.odm.policyserviceopa.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.quantyca.odm.policyserviceopa.entities.SuiteEntity;
import it.quantyca.odm.policyserviceopa.enums.PatchModes;
import it.quantyca.odm.policyserviceopa.exceptions.BadRequestException;
import it.quantyca.odm.policyserviceopa.exceptions.NotFoundException;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyserviceOpaAPIStandardError;
import it.quantyca.odm.policyserviceopa.repositories.SuiteRepository;
import it.quantyca.odm.policyserviceopa.resources.v1.mappers.SuiteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;

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
public class SuiteController {

    @Autowired
    private SuiteRepository sr;

    @Autowired
    private SuiteMapper sm;

    @GetMapping
    @Operation(
            summary = "Get all suites",
            description = "Fetch all registered suites",
            tags = { "Suite API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All registered suites",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuiteResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            )
    })
    public ResponseEntity getSuites(){

        Iterable<SuiteEntity> suites = sr.findAll();
        Iterable<SuiteResource> suitesDTO = sm.suiteIterableToSuiteDTOIterable(suites);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(suitesDTO);

    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a suite",
            description = "Fetch a specific registered suite given its ID",
            tags = { "Suite API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "The registered suite",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuiteResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "[Not Found](https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found)"
                            + "\r\n - Error code 40402 - Suite not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            )
    })
    public ResponseEntity getSuitesByID(
            @Parameter(description = "Identifier of the suite")
            @Valid @PathVariable String id
    ){

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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new suite",
            description = "Create and register a new suite (i.e., collection of OPA policies)",
            tags = { "Suite API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Suite created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuiteResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "[Bad Request](https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request)"
                            + "\r\n - Error code 40005 - Suite already exists"
                            + "\r\n - Error code 40006 - OPA Bad Request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            )
    })
    public ResponseEntity postSuite(
            @Parameter(description = "JSON description of the suite object")
            @Valid @RequestBody SuiteResource suite
    ){

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

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a suite",
            description = "Delete a registered suite given its ID",
            tags = { "Suite API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Suite deleted",
                    content = @Content(
                            mediaType = "plain/text",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "[Not Found](https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found)"
                            + "\r\n - Error code 40402 - Suite not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            )
    })
    public ResponseEntity deleteSuiteByID(
            @Parameter(description = "Identifier of the suite")
            @Valid @PathVariable String id
    ){

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

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update a suite",
            description = "Add or remove a policy, through its ID, from a registered suite",
            tags = { "Suite API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Suite patched",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuiteResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "[Not Found](https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found)"
                            + "\r\n - Error code 40402 - Suite not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResource.class)
                    )
            )
    })
    public ResponseEntity patchSuiteByID(
            @Parameter(description = "Identifier of the suite")
            @Valid @PathVariable String id,
            @Parameter(description = "Patch mode - whether it ADD a policy or REMOVE a policy from a registered suite - could only be {\"ADD\", \"REMOVE\"}")
            @Valid @RequestParam PatchModes mode,
            @Parameter(description = "Identifier of the policy to add/remove from the suite")
            @Valid @RequestParam String policyId
    ){

        Optional<SuiteEntity> suiteEntityOpt = sr.findById(id);

        if(!suiteEntityOpt.isPresent()) {
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND,
                    "Suite " + id + " not found on DB"
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