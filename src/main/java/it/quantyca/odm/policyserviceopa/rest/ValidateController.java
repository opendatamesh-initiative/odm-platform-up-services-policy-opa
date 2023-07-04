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
import it.quantyca.odm.policyserviceopa.exceptions.InternalServerException;
import it.quantyca.odm.policyserviceopa.exceptions.PolicyserviceOpaAPIStandardError;
import it.quantyca.odm.policyserviceopa.repositories.SuiteRepository;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.*;
import it.quantyca.odm.policyserviceopa.resources.v1.errors.ErrorRes;
import it.quantyca.odm.policyserviceopa.services.OPAValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import javax.validation.Valid;
import java.net.ConnectException;
import java.util.*;

@RestController
@RequestMapping(
        value = "/validate",
        produces = { "application/json" }
)
@Validated
@Tag(
        name = "Policies validation API",
        description = "API to validate one or more policies for a document"
)
public class ValidateController {

    @Autowired
    private OPAValidateService opaValidateService;

    @Autowired
    private SuiteRepository suiteRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Validate a document",
            description = "Validate a document with a single policy, multiple policies, all the policies or the policies in a suite"
                    + "\n\n If neither the optional parameters \"id\" or \"suite\" will be used the document"
                    + "will be validated through all the policies sotred on OPA Server",
            tags = { "Policies validation API" }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Document validated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error code 40006 - OPA Server bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorRes.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error code 50000 - Generic internal server error"
                            + "\n\nError code 50001 - OPA Server internal server error"
                            + "\n\nError code 50002 - OPA Server not reachable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorRes.class)
                    )
            )
    })
    public ResponseEntity validate(
            @Parameter(description = "Optional - identifier/s of the policy or the policies to use in the validation of the document")
            @Valid @RequestParam(name = "id", required = false) String[] ids,
            @Parameter(description = "Optional - identifier/s of the suite or the suites to use in the validation of the document")
            @Valid @RequestParam(name = "suite", required = false) String[] suites,
            @Parameter(description = "JSON object of the document to be validated")
            @Valid @RequestBody Object document
    ){

        try {
            ValidateRequest validateDocument = new ValidateRequest();
            validateDocument.setInput(document);
            ValidateResponse validateResponse = new ValidateResponse();

            if (ids == null && suites == null) {
                // Caso in cui voglio valutare su tutte le policy presenti in OPA
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(opaValidateService.validate(validateDocument));
            }
            else {
                if (ids != null) {
                    // Valido le singole policy by ID
                    List<ValidatedPolicyResource> validatedPolicies = new ArrayList<>();
                    for (String id : ids) {
                        ValidatedPolicyResource validatedPolicy = new ValidatedPolicyResource();
                        validatedPolicy.setPolicy(id);
                        validatedPolicy.setValidationResult(opaValidateService.validateByPolicyID(id, validateDocument));
                        validatedPolicies.add(validatedPolicy);
                    }
                    validateResponse.setValidatedPolicies(validatedPolicies);
                }
                if (suites != null) {
                    // Valido le suite
                    List<ValidatedSuiteResource> validatedSuites = new ArrayList<>();
                    for (String suite : suites) {
                        ValidatedSuiteResource validatedSuite = new ValidatedSuiteResource();
                        validatedSuite.setSuite(suite);
                        List<ValidatedPolicyResource> validatedPolicies = new ArrayList<>();
                        Optional<SuiteEntity> suiteEntity = suiteRepository.findById(suite);
                        if (suiteEntity.isPresent()){
                            // Se la suite esiste valido ogni policy che contiene
                            List<String> policyIDs = suiteEntity.get().getPolicies();
                            for (String policyID : policyIDs) {
                                ValidatedPolicyResource validatedPolicy = new ValidatedPolicyResource();
                                validatedPolicy.setPolicy(policyID);
                                validatedPolicy.setValidationResult(opaValidateService.validateByPolicyID(policyID, validateDocument));
                                validatedPolicies.add(validatedPolicy);
                            }
                        }
                        validatedSuite.setValidatedPolicies(validatedPolicies);
                        validatedSuites.add(validatedSuite);
                    }
                    validateResponse.setValidatedSuites(validatedSuites);
                }
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(validateResponse);
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

    }

}
