package it.quantyca.odm.policyserviceopa.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.quantyca.odm.policyserviceopa.entities.PolicyEntity;
import it.quantyca.odm.policyserviceopa.exceptions.BadRequestException;
import it.quantyca.odm.policyserviceopa.exceptions.InternalServerException;
import it.quantyca.odm.policyserviceopa.exceptions.NotFoundException;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyserviceOpaAPIStandardError;
import it.quantyca.odm.policyserviceopa.repositories.PolicyRepository;
import it.quantyca.odm.policyserviceopa.resources.v1.mappers.PolicyMapper;
import it.quantyca.odm.policyserviceopa.services.OPAPolicyService;
import it.quantyca.odm.policyserviceopa.services.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;

import javax.validation.Valid;
import java.net.ConnectException;

@RestController
@RequestMapping(
        value = "/policies",
        produces = { "application/json" }
)
@Validated
@Tag(
        name = "Policy API",
        description = "CRUD API for Policy entity"
)
public class PolicyController {

    @Autowired
    private OPAPolicyService opaPolicyService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRepository pr;

    @Autowired
    private PolicyMapper pm;

    @GetMapping
    @Operation(
            summary = "Get all policies",
            description = "Fetch all registered policies",
            tags = { "Policy API" }
    )
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All registered policies",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyResource.class)
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
    public ResponseEntity getPolicies(){

        Iterable<PolicyEntity> policyEntities = pr.findAll();
        Iterable<PolicyResource> policies = pm.policyIterableToPolicyDTOIterable(policyEntities);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(policies);

    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a policy",
            description = "Fetch a specific registered policy given its ID",
            tags = { "Policy API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "The registered policy",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "[Not Found](https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found)"
                            + "\r\n - Error code 40401 - Policy not found",
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
    public ResponseEntity getPolicyByID(
            @Parameter(description = "Identifier of the policy")
            @Valid @PathVariable String id
    ){

        if(!pr.existsById(id)) {
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_POLICY_NOT_FOUND,
                    "Policy " + id + " not found on DB"
            );
        }

        PolicyEntity policyEntity = pr.findById(id).get();
        PolicyResource policy = pm.policyToPolicyDto(policyEntity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(policy);

    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new policy",
            description = "Create and register a new OPA policy",
            tags = { "Policy API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Policy created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "[Bad Request](https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request)"
                            + "\r\n - Error code 40001 - Policy already exists"
                            + "\r\n - Error code 40006 - OPA Server bad request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error"
                            + "\r\n - Error code 50001 - OPA Server internal server error"
                            + "\r\n - Error code 50002 - OPA Server not reachable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            )
    })
    public ResponseEntity postPolicy(
            @Parameter(description = "JSON description of the policy object")
            @Valid @RequestBody PolicyResource policies
    ){

        // Extract ID from RawPolicy
        String policyId = policyService.getIdFromPolicy(policies.getRawPolicy());
        policies.setId(policyId);

        // Mapping from DTO to Entity
        PolicyEntity policyEntity = pm.policyDTOToPolicy(policies);

        // Check if policy with that ID does not already exist
        if (!pr.existsById(policyId)) {

            try {
                // POST on OPA Server
                opaPolicyService.putOPAPolicy(policies.getId(), policies.getRawPolicy());
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode()==400) {
                    throw new BadRequestException(
                            PolicyserviceOpaAPIStandardError.SC400_OPA_SERVER_BAD_REQUEST,
                            e.getMessage()
                    );
                } else if (e.getRawStatusCode()==500) {
                    throw new InternalServerException(
                            PolicyserviceOpaAPIStandardError.SC500_OPA_SERVER_INTERNAL_SERVER_ERROR,
                            e.getMessage()
                    );
                } else {
                    throw e;
                }
            } catch (RestClientException e) {
                if (e.getCause() instanceof ConnectException) {
                    // handle connect exception
                    throw new InternalServerException(
                            PolicyserviceOpaAPIStandardError.SC500_OPA_SERVER_NOT_REACHABLE,
                            e.getMessage()
                    );
                }
                throw e;
            }

            // POST on internal DB
            policyEntity.setId(policies.getId());
            pr.save(policyEntity);

            // Return creation message
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(policies);
        } else {
            throw new BadRequestException(
                    PolicyserviceOpaAPIStandardError.SC400_POLICY_ALREADY_EXISTS,
                    "Policy with ID " + policyId + "already exists on DB"
            );
        }

    }

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Update a policy",
            description = "Update a registered OPA policy",
            tags = { "Policy API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Policy updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "[Bad Request](https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request)"
                            + "\r\n - Error code 40004 - ID conflict"
                            + "\r\n - Error code 40006 - OPA Server bad request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "[Not Found](https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found)"
                            + "\r\n - Error code 40401 - Policy not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error"
                            + "\r\n - Error code 50001 - OPA Server internal server error"
                            + "\r\n - Error code 50002 - OPA Server not reachable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            )
    })
    public ResponseEntity putPolicyByID(
            @Parameter(description = "Identifier of the policy")
            @Valid @PathVariable String id,
            @Parameter(description = "JSON description of the policy object to update")
            @Valid @RequestBody PolicyResource policies
    ){

        // Extract ID from RawPolicy
        String policyId = policyService.getIdFromPolicy(policies.getRawPolicy());
        policies.setId(policyId);

        // Check parameter ID and policy ID
        if (!id.equals(policies.getId())) {
            throw new BadRequestException(
                    PolicyserviceOpaAPIStandardError.SC400_IDS_NOT_MATCHING,
                    "URL parameter ID and policy ID not matching"
            );
        }
        // Check if policy does not exist
        if (!pr.existsById(id)) {
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_POLICY_NOT_FOUND,
                    "Policy " + id + " not found on DB"
            );
        } else {

            try {
                // PUT on OPA Server
                opaPolicyService.putOPAPolicy(id, policies.getRawPolicy());
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode()==400) {
                    throw new BadRequestException(
                            PolicyserviceOpaAPIStandardError.SC400_OPA_SERVER_BAD_REQUEST,
                            e.getMessage()
                    );
                } else if (e.getRawStatusCode()==500) {
                    throw new InternalServerException(
                            PolicyserviceOpaAPIStandardError.SC500_OPA_SERVER_INTERNAL_SERVER_ERROR,
                            e.getMessage()
                    );
                } else {
                    throw e;
                }
            } catch (RestClientException e) {
                if (e.getCause() instanceof ConnectException) {
                    // handle connect exception
                    throw new InternalServerException(
                            PolicyserviceOpaAPIStandardError.SC500_OPA_SERVER_NOT_REACHABLE,
                            e.getMessage()
                    );
                }
                throw e;
            }

            // Mapping from DTO to Entity
            PolicyEntity policyEntity = pm.policyDTOToPolicy(policies);

            // Update policy on internal DB
            pr.save(policyEntity);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(policies);

        }

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Delete a policy",
            description = "Delete a registered OPA policy given its ID",
            tags = { "Policy API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "400",
                    description = "[Bad Request](https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request)"
                            + "\r\n - Error code 40006 - OPA Server bad request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "[Not Found](https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found)"
                            + "\r\n - Error code 40401 - Policy not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "[Internal Server Error](https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error)"
                            + "\r\n - Error code 50000 - Generic internal server error"
                            + "\r\n - Error code 50001 - OPA Server internal server error"
                            + "\r\n - Error code 50002 - OPA Server not reachable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResource.class))
            )
    })
    public void deletePolicyByID(
            @Parameter(description = "Identifier of the policy")
            @Valid @PathVariable String id
    ){

        // Check if policy does not exist on DB
        if (!pr.existsById(id)) {
            throw new NotFoundException(
                    PolicyserviceOpaAPIStandardError.SC404_POLICY_NOT_FOUND,
                    "Policy " + id + " not found on DB"
            );
        }

        try {
            // Delete policy on OPA server
            opaPolicyService.deleteOPAPolicyById(id);
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode()==400) {
                throw new BadRequestException(
                        PolicyserviceOpaAPIStandardError.SC400_OPA_SERVER_BAD_REQUEST,
                        e.getMessage()
                );
            } else if (e.getRawStatusCode()==500) {
                throw new InternalServerException(
                        PolicyserviceOpaAPIStandardError.SC500_OPA_SERVER_INTERNAL_SERVER_ERROR,
                        e.getMessage()
                );
            } else {
                throw e;
            }
        } catch (RestClientException e) {
            if (e.getCause() instanceof ConnectException) {
                // handle connect exception
                throw new InternalServerException(
                        PolicyserviceOpaAPIStandardError.SC500_OPA_SERVER_NOT_REACHABLE,
                        e.getMessage()
                );
            }
            throw e;
        }

        // Delete policy on internal DB
        pr.deleteById(id);

    }

}
