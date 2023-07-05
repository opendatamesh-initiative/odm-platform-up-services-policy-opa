package org.opendatamesh.policyserviceopa.server.rest;

import org.opendatamesh.platform.up.policy.api.v1.controllers.AbstractValidateController;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.ValidateResponse;
import org.opendatamesh.platform.up.policy.api.v1.resources.ValidatedPolicyResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.ValidatedSuiteResource;
import org.opendatamesh.policyserviceopa.client.resources.v1.ValidateRequest;
import org.opendatamesh.policyserviceopa.server.entities.SuiteEntity;
import org.opendatamesh.policyserviceopa.server.exceptions.BadRequestException;
import org.opendatamesh.policyserviceopa.server.exceptions.InternalServerException;
import org.opendatamesh.policyserviceopa.server.repositories.SuiteRepository;
import org.opendatamesh.policyserviceopa.server.services.OPAValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ValidateController extends AbstractValidateController {

    @Autowired
    private OPAValidateService opaValidateService;

    @Autowired
    private SuiteRepository suiteRepository;

    @Override
    public ResponseEntity validateDocument(String[] ids, String[] suites, Object document) {

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
