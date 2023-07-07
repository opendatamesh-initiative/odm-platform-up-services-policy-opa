package org.opendatamesh.platform.up.policy.opa.server.rest;

import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.opendatamesh.platform.up.policy.opa.server.services.OPAPolicyService;
import org.opendatamesh.platform.up.policy.server.api.v1.controllers.AbstrPolicyController;
import org.opendatamesh.platform.up.policy.server.database.entities.PolicyEntity;
import org.opendatamesh.platform.up.policy.server.exceptions.BadRequestException;
import org.opendatamesh.platform.up.policy.server.exceptions.InternalServerException;
import org.opendatamesh.platform.up.policy.server.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;

@RestController
public class PolicyController extends AbstrPolicyController {

    @Autowired
    private OPAPolicyService opaPolicyService;

    @Override
    public ResponseEntity createPolicy(PolicyResource policies) {

        // Extract ID from RawPolicy
        String policyId = policyService.getIdFromPolicy(policies.getRawPolicy());
        policies.setId(policyId);

        // Mapping from DTO to Entity
        PolicyEntity policyEntity = pm.toEntity(policies);

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
            PolicyEntity policyEntity = pm.toEntity(policies);

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
