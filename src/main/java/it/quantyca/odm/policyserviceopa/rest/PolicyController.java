package it.quantyca.odm.policyserviceopa.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.quantyca.odm.policyserviceopa.entities.PolicyEntity;
import it.quantyca.odm.policyserviceopa.exceptions.BadRequestException;
import it.quantyca.odm.policyserviceopa.exceptions.InternalServerException;
import it.quantyca.odm.policyserviceopa.exceptions.NotFoundException;
import it.quantyca.odm.policyserviceopa.repositories.PolicyRepository;
import it.quantyca.odm.policyserviceopa.resources.v1.mappers.PolicyMapper;
import it.quantyca.odm.policyserviceopa.services.OPAPolicyService;
import it.quantyca.odm.policyserviceopa.services.PolicyService;
import org.opendatamesh.platform.up.policy.api.v1.controllers.AbstractPolicyController;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyserviceOpaAPIStandardError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

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
public class PolicyController extends AbstractPolicyController {

    @Autowired
    private OPAPolicyService opaPolicyService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRepository pr;

    @Autowired
    private PolicyMapper pm;

    @Override
    public ResponseEntity readPolicies() {

        Iterable<PolicyEntity> policyEntities = pr.findAll();
        Iterable<PolicyResource> policies = pm.policyIterableToPolicyDTOIterable(policyEntities);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(policies);

    }

    @Override
    public ResponseEntity readOnePolicy(String id) {

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

    @Override
    public ResponseEntity createPolicy(PolicyResource policies) {

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

    @Override
    public ResponseEntity updatePolicy(String id, PolicyResource policies) {

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

    @Override
    public void deletePolicy(String id) {
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
