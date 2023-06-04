package it.quantyca.odm.policyserviceopa;

import it.quantyca.odm.policyserviceopa.exceptions.PolicyserviceOpaAPIStandardError;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.PolicyDTO;
import it.quantyca.odm.policyserviceopa.resources.v1.errors.ErrorRes;
import org.junit.Test;
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
        PolicyDTO policyDTO = createPolicy1();
        assertThat(policyDTO.getId()).isEqualTo("dataproduct");
        assertThat(policyDTO.getDisplayName()).isEqualTo("Dataproduct policies");
        assertThat(policyDTO.getDescription()).isEqualTo("Set of policies for the package dataproduct");
        assertThat(policyDTO.getRawPolicy()).isEqualTo("package dataproduct\n\ndefault allow := false\n\nallow := true {                                     \n    startswith(input.name, \"dp-\")\n}");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyCreateError400() throws IOException {

        cleanState();

        PolicyDTO policyDTO = createPolicy1();
        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.postForEntity(
                apiUrl(RoutesV1.POLICY),
                policyDTO,
                ErrorRes.class
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
        PolicyDTO policyDTOUpdated = updatePolicy1();

        assertThat(policyDTOUpdated.getId()).isEqualTo("dataproduct");
        assertThat(policyDTOUpdated.getDisplayName()).isEqualTo("Dataproduct policies - updated");
        assertThat(policyDTOUpdated.getDescription()).isEqualTo("Set of policies for the package dataproduct - updated");
        assertThat(policyDTOUpdated.getRawPolicy()).isEqualTo("package dataproduct\n\ndefault allow := false\n\nallow := true {                                     \n    startswith(input.name, \"dp-\")\n}");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPolicyUpdateError400() throws IOException {

        cleanState();

        HttpEntity<PolicyDTO> entity = rest.getPolicyFileAsHttpEntity(POLICY_3);
        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.PUT,
                entity,
                ErrorRes.class,
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

        HttpEntity<PolicyDTO> entity = rest.getPolicyFileAsHttpEntity(POLICY_3);
        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.PUT,
                entity,
                ErrorRes.class,
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

        ResponseEntity<PolicyDTO[]> getPolicyResponse = rest.readAllPolicies();
        PolicyDTO[] policyDTOs = getPolicyResponse.getBody();
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, true);

        assertThat(getPolicyResponse.getBody().length).isEqualTo(2);
        assertThat(policyDTOs[0].getId()).isEqualTo("dataproduct");
        assertThat(policyDTOs[1].getId()).isEqualTo("xpolicy");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesReadOne() throws IOException {

        cleanState();

        createPolicy1();

        ResponseEntity<PolicyDTO> getPolicyResponse = rest.readOnePolicy("dataproduct");
        PolicyDTO policyDTO = getPolicyResponse.getBody();
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, true);

        assertThat(policyDTO.getId()).isEqualTo("dataproduct");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesReadOneError404() {

        cleanState();

        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.GET,
                null,
                ErrorRes.class,
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

        PolicyDTO policy = createPolicy1();

        ResponseEntity<Void> getPolicyResponse = rest.deletePolicy(policy.getId());
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, false);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPoliciesDeleteError404() throws IOException {

        cleanState();

        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.POLICY),
                HttpMethod.DELETE,
                null,
                ErrorRes.class,
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

        ResponseEntity<PolicyDTO[]> policies = rest.readAllPolicies();
        PolicyDTO[] policyDTOS = policies.getBody();
        for (PolicyDTO policyDTO : policyDTOS) {
            rest.deletePolicy(policyDTO.getId());
        }

    }

}
