package org.opendatamesh.policyserviceopa.server;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import org.opendatamesh.platform.up.policy.api.v1.resources.ValidateResponse;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ValidateIT extends PolicyserviceOpaApplicationIT {

    // Note: Tests NEED a running OPA server listening on Port 8181 to work
    // Note: Edit run configurations and add environment variable SPRING_PROFILES_ACTIVE=dev

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testValidate() throws IOException {

        createPolicy1();
        createPolicy2();
        createPolicyVersions();
        createPolicyServicesType();
        createSuite1();

        // TEST 1: validate document1 with all policies
        ResponseEntity<Map> validationResponse = rest.validateDocument(DOCUMENT_1);
        assertThat(validationResponse.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(validationResponse.getBody().get("decision_id")).isNotNull();
        assertThat(validationResponse.getBody().get("result")).isNotNull();

        // TEST 2: validate document1 with a single policy
        ResponseEntity<ValidateResponse> validationResponse2 = rest.validateDocumentByPoliciesIds(DOCUMENT_1, "dataproduct");
        assertThat(validationResponse2.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(validationResponse2.getBody().getValidatedPolicies().size()).isEqualTo(1);
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(0).getPolicy()).isEqualTo("dataproduct");

        // TEST 2: validate document1 with 2 policies
        validationResponse2 = rest.validateDocumentByPoliciesIds(DOCUMENT_1, "dataproduct,xpolicy");
        assertThat(validationResponse2.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(validationResponse2.getBody().getValidatedPolicies().size()).isEqualTo(2);
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(0).getPolicy()).isEqualTo("dataproduct");
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(1).getPolicy()).isEqualTo("xpolicy");

        // TEST 3: validate document1 with a suite of policies
        validationResponse2 = rest.validateDocumentBySuite(DOCUMENT_1, "odm-suite");
        assertThat(validationResponse2.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(validationResponse2.getBody().getValidatedSuites().size()).isEqualTo(1);
        assertThat(validationResponse2.getBody().getValidatedSuites().get(0).getSuite()).isEqualTo("odm-suite");

        // TEST 4: validate document "dpd" with policy "policy-versions" comparing major version and outputPorts.version
        validationResponse2 = rest.validateDocumentByPoliciesIds(DPD, "versions");
        assertThat(validationResponse2.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(validationResponse2.getBody().getValidatedPolicies().size()).isEqualTo(1);
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(0).getPolicy()).isEqualTo("versions");
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(0).getValidationResult().toString()).contains("allow=true");

        // TEST 5: validate document "document-servicestype" with policy "policy-servicestype" checking servicesType of promises
        validationResponse2 = rest.validateDocumentByPoliciesIds(DOCUMENT_2, "servicestype");
        assertThat(validationResponse2.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(validationResponse2.getBody().getValidatedPolicies().size()).isEqualTo(1);
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(0).getPolicy()).isEqualTo("servicestype");
        assertThat(validationResponse2.getBody().getValidatedPolicies().get(0).getValidationResult().toString()).contains("allow=true");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testValidateError500() throws IOException {

    }

}