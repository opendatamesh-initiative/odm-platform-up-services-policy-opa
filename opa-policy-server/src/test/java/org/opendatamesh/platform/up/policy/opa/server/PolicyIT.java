package org.opendatamesh.platform.up.policy.opa.server;

import org.junit.Test;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class PolicyIT extends PolicyserviceOpaApplicationIT {

    // Note: Tests NEED a running OPA server listening on Port 8181 to work
    // Note: Edit run configurations and add environment variable SPRING_PROFILES_ACTIVE=dev

    // ----------------------------------------
    // CREATE Policy
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyCreate() throws IOException {

        cleanState();

        // Create a Policy with all properties and verify the response
        PolicyResource policyResource = createPolicy1();
        assertThat(policyResource.getId()).isEqualTo("dataproduct");
        assertThat(policyResource.getDisplayName()).isEqualTo("Dataproduct policies");
        assertThat(policyResource.getDescription()).isEqualTo("Set of policies for the package dataproduct");
        assertThat(policyResource.getRawPolicy()).isEqualTo("package dataproduct\n\ndefault allow := false\n\nallow := true {                                     \n    startswith(input.name, \"dp-\")\n}");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyCreateError400() throws IOException {

        cleanState();

        PolicyResource policyResource = createPolicy1();
        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.postForEntity(
                apiUrl(RoutesV1.POLICY),
                policyResource,
                ErrorResource.class
        );
        verifyResponseError(
                errorResponse,
                HttpStatus.BAD_REQUEST,
                PolicyserviceOpaAPIStandardError.SC400_POLICY_ALREADY_EXISTS
        );

    }

    // ----------------------------------------
    // UPDATE Policy
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyUpdate() throws IOException {

        cleanState();

        createPolicy1();
        PolicyResource policyResourceUpdated = updatePolicy1();

        assertThat(policyResourceUpdated.getId()).isEqualTo("dataproduct");
        assertThat(policyResourceUpdated.getDisplayName()).isEqualTo("Dataproduct policies - updated");
        assertThat(policyResourceUpdated.getDescription()).isEqualTo("Set of policies for the package dataproduct - updated");
        assertThat(policyResourceUpdated.getRawPolicy()).isEqualTo("package dataproduct\n\ndefault allow := false\n\nallow := true {                                     \n    startswith(input.name, \"dp-\")\n}");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyUpdateError400() throws IOException {

        cleanState();

        HttpEntity<PolicyResource> entity = rest.getPolicyFileAsHttpEntity(POLICY_3);
        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.PUT,
                entity,
                ErrorResource.class,
                "wrongid"
        );
        verifyResponseError(
                errorResponse,
                HttpStatus.BAD_REQUEST,
                PolicyserviceOpaAPIStandardError.SC400_IDS_NOT_MATCHING
        );

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyUpdateError404() throws IOException {

        cleanState();

        HttpEntity<PolicyResource> entity = rest.getPolicyFileAsHttpEntity(POLICY_3);
        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.PUT,
                entity,
                ErrorResource.class,
                "test"
        );
        verifyResponseError(
                errorResponse,
                HttpStatus.NOT_FOUND,
                PolicyserviceOpaAPIStandardError.SC404_POLICY_NOT_FOUND
        );

    }

    // ----------------------------------------
    // READ Policies
    // ----------------------------------------
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesReadAll() throws IOException {

        cleanState();

        createPolicy1();
        createPolicy2();

        /*
        // OLD WAY:
        ResponseEntity<PolicyResource[]> getPolicyResponse = rest.readAllPolicies();
        PolicyResource[] policyResources = getPolicyResponse.getBody();
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, true);
        */
        //NEW WAY:
        PolicyResource[] policyResources = client.readPolicies().getBody();

        assertThat(policyResources.length).isEqualTo(2);
        assertThat(policyResources[0].getId()).isEqualTo("dataproduct");
        assertThat(policyResources[1].getId()).isEqualTo("xpolicy");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesReadOne() throws IOException {

        cleanState();

        createPolicy1();

        ResponseEntity<PolicyResource> getPolicyResponse = rest.readOnePolicy("dataproduct");
        PolicyResource policyResource = getPolicyResponse.getBody();
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, true);

        assertThat(policyResource.getId()).isEqualTo("dataproduct");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesReadOneError404() {

        cleanState();

        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.GET,
                null,
                ErrorResource.class,
                "dataproduct"
        );

        verifyResponseError(errorResponse, HttpStatus.NOT_FOUND, PolicyserviceOpaAPIStandardError.SC404_POLICY_NOT_FOUND);

    }


    // ----------------------------------------
    // DELETE Policies
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesDelete() throws IOException {

        cleanState();

        PolicyResource policy = createPolicy1();

        ResponseEntity<Void> getPolicyResponse = rest.deletePolicy(policy.getId());
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, false);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesDeleteError404() throws IOException {

        cleanState();

        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.DELETE,
                null,
                ErrorResource.class,
                "notanid"
        );

        verifyResponseError(
                errorResponse,
                HttpStatus.NOT_FOUND,
                PolicyserviceOpaAPIStandardError.SC404_POLICY_NOT_FOUND
        );

    }


    // ----------------------------------------
    // Clean state for each test: empty DB
    // ----------------------------------------
    private void cleanState() {

        ResponseEntity<PolicyResource[]> policies = rest.readAllPolicies();
        PolicyResource[] policyResources = policies.getBody();
        for (PolicyResource policyResource : policyResources) {
            rest.deletePolicy(policyResource.getId());
        }

    }

}
