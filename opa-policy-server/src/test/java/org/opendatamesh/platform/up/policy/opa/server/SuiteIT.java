package org.opendatamesh.platform.up.policy.opa.server;

import org.junit.Test;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SuiteIT extends PolicyserviceOpaApplicationIT {

    // Note: Tests NEED a running OPA server listening on Port 8181 to work
    // Note: Edit run configurations and add environment variable SPRING_PROFILES_ACTIVE=dev

    // ----------------------------------------
    // CREATE Suite
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteCreate() throws IOException {

        cleanState();

        // TEST 1: create a Suite with all properties and verify the response
        SuiteResource suiteResource = createSuite1();
        assertThat(suiteResource.getId()).isEqualTo("odm-suite");
        assertThat(suiteResource.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteResource.getDescription()).isEqualTo("Policy collection for odm-suite");
        System.out.println(suiteResource.getPolicies());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteCreateError400() throws IOException {

        cleanState();

        SuiteResource suiteResource = createSuite1();
        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.postForEntity(
                apiUrl(RoutesV1.SUITE),
                suiteResource,
                ErrorResource.class
        );
        verifyResponseError(
                errorResponse,
                HttpStatus.BAD_REQUEST,
                PolicyserviceOpaAPIStandardError.SC400_SUITE_ALREADY_EXISTS
        );

    }

    // ----------------------------------------
    // UPDATE Suite
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteUpdate() throws IOException {

        cleanState();

        createSuite1();

        // TEST 1: ADD a policy to a suite

        ResponseEntity<SuiteResource> entity = null;
        String extensions = "/odm-suite?mode=ADD&policyId=newpolicy";

        entity = rest.exchange(
                apiUrl(RoutesV1.SUITE, extensions),
                HttpMethod.PATCH,
                null,
                SuiteResource.class
        );

        SuiteResource suiteResourceUpdated = entity.getBody();

        assertThat(suiteResourceUpdated.getId()).isEqualTo("odm-suite");
        assertThat(suiteResourceUpdated.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteResourceUpdated.getDescription()).isEqualTo("Policy collection for odm-suite");
        assertThat(suiteResourceUpdated.getPolicies().size()).isEqualTo(3);
        assertThat(suiteResourceUpdated.getPolicies().contains("newpolicy")).isTrue();

        // TEST 2: REMOVE a policy from suite
        extensions = "/odm-suite?mode=REMOVE&policyId=newpolicy";

        entity = rest.exchange(
                apiUrl(RoutesV1.SUITE, extensions),
                HttpMethod.PATCH,
                null,
                SuiteResource.class
        );

        suiteResourceUpdated = entity.getBody();

        assertThat(suiteResourceUpdated.getId()).isEqualTo("odm-suite");
        assertThat(suiteResourceUpdated.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteResourceUpdated.getDescription()).isEqualTo("Policy collection for odm-suite");
        assertThat(suiteResourceUpdated.getPolicies().size()).isEqualTo(2);
        assertThat(suiteResourceUpdated.getPolicies().contains("newpolicy")).isFalse();

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteUpdateError404() throws IOException {

        HttpEntity<SuiteResource> entity = null;
        ResponseEntity<ErrorResource> errorResponse = null;

        String extensions = "/not-a-suite?mode=ADD&policyId=newpolicy";

        errorResponse = rest.exchange(
                apiUrl(RoutesV1.SUITE, extensions),
                HttpMethod.PATCH,
                null,
                ErrorResource.class
        );
        System.out.println(errorResponse.getBody());
        verifyResponseError(
                errorResponse,
                HttpStatus.NOT_FOUND,
                PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND
        );

    }


    // ----------------------------------------
    // READ Suites
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuitesReadAll() throws IOException {

        cleanState();

        createSuite1();

        ResponseEntity<SuiteResource[]> getSuiteResponse = rest.readAllSuites();
        SuiteResource[] suiteResources = getSuiteResponse.getBody();
        verifyResponseEntity(getSuiteResponse, HttpStatus.OK, true);

        assertThat(getSuiteResponse.getBody().length).isEqualTo(1);
        assertThat(suiteResources[0].getId()).isEqualTo("odm-suite");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteReadOne() throws IOException {

        cleanState();

        createSuite1();

        ResponseEntity<SuiteResource> getSuiteResponse = rest.readOneSuite("odm-suite");
        SuiteResource suiteResource = getSuiteResponse.getBody();
        verifyResponseEntity(getSuiteResponse, HttpStatus.OK, true);

        assertThat(suiteResource.getId()).isEqualTo("odm-suite");
        assertThat(suiteResource.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteResource.getDescription()).isEqualTo("Policy collection for odm-suite");
        assertThat(suiteResource.getPolicies().size()).isEqualTo(2);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteReadOneError404() {

        cleanState();

        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.SUITE),
                HttpMethod.GET,
                null,
                ErrorResource.class,
                "odm-suite"
        );

        verifyResponseError(errorResponse, HttpStatus.NOT_FOUND, PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND);

    }


    // ----------------------------------------
    // DELETE Suite
    // ----------------------------------------

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteDelete() throws IOException {

        cleanState();

        SuiteResource suiteResource = createSuite1();

        ResponseEntity<Void> getPolicyResponse = rest.deleteSuite(suiteResource.getId());
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, false);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteDeleteError404() throws IOException {

        cleanState();

        ResponseEntity<ErrorResource> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.SUITE),
                HttpMethod.DELETE,
                null,
                ErrorResource.class,
                "notanid"
        );

        verifyResponseError(
                errorResponse,
                HttpStatus.NOT_FOUND,
                PolicyserviceOpaAPIStandardError.SC404_SUITE_NOT_FOUND
        );

    }


    // ----------------------------------------
    // Clean state for each test: empty DB
    // ----------------------------------------
    private void cleanState() {

        ResponseEntity<SuiteResource[]> suites = rest.readAllSuites();
        SuiteResource[] suiteResources = suites.getBody();
        for (SuiteResource suiteResource : suiteResources) {
            rest.deleteSuite(suiteResource.getId());
        }

    }

}
