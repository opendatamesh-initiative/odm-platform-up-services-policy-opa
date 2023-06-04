package it.quantyca.odm.policyserviceopa;

import it.quantyca.odm.policyserviceopa.exceptions.PolicyserviceOpaAPIStandardError;
import it.quantyca.odm.policyserviceopa.resources.v1.dto.SuiteDTO;
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
        SuiteDTO suiteDTO = createSuite1();
        assertThat(suiteDTO.getId()).isEqualTo("odm-suite");
        assertThat(suiteDTO.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteDTO.getDescription()).isEqualTo("Suite collection for odm-suite");
        System.out.println(suiteDTO.getPolicies());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteCreateError400() throws IOException {

        cleanState();

        SuiteDTO suiteDTO = createSuite1();
        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.postForEntity(
                apiUrl(RoutesV1.SUITE),
                suiteDTO,
                ErrorRes.class
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

        ResponseEntity<SuiteDTO> entity = null;
        String extensions = "/odm-suite?mode=ADD&policyId=newpolicy";

        entity = rest.exchange(
                apiUrl(RoutesV1.SUITE, extensions),
                HttpMethod.PATCH,
                null,
                SuiteDTO.class
        );

        SuiteDTO suiteDTOUpdated = entity.getBody();

        assertThat(suiteDTOUpdated.getId()).isEqualTo("odm-suite");
        assertThat(suiteDTOUpdated.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteDTOUpdated.getDescription()).isEqualTo("Policy collection for odm-suite");
        assertThat(suiteDTOUpdated.getPolicies().size()).isEqualTo(3);
        assertThat(suiteDTOUpdated.getPolicies().contains("newpolicy")).isTrue();

        // TEST 2: REMOVE a policy from suite
        extensions = "/odm-suite?mode=REMOVE&policyId=newpolicy";

        entity = rest.exchange(
                apiUrl(RoutesV1.SUITE, extensions),
                HttpMethod.PATCH,
                null,
                SuiteDTO.class
        );

        suiteDTOUpdated = entity.getBody();

        assertThat(suiteDTOUpdated.getId()).isEqualTo("odm-suite");
        assertThat(suiteDTOUpdated.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteDTOUpdated.getDescription()).isEqualTo("Policy collection for odm-suite");
        assertThat(suiteDTOUpdated.getPolicies().size()).isEqualTo(2);
        assertThat(suiteDTOUpdated.getPolicies().contains("newpolicy")).isFalse();

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteUpdateError404() throws IOException {

        HttpEntity<SuiteDTO> entity = null;
        ResponseEntity<ErrorRes> errorResponse = null;

        String extensions = "/not-a-suite?mode=ADD&policyId=newpolicy";

        errorResponse = rest.exchange(
                apiUrl(RoutesV1.SUITE, extensions),
                HttpMethod.PATCH,
                null,
                ErrorRes.class
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

        ResponseEntity<SuiteDTO[]> getSuiteResponse = rest.readAllSuites();
        SuiteDTO[] suiteDTOs = getSuiteResponse.getBody();
        verifyResponseEntity(getSuiteResponse, HttpStatus.OK, true);

        assertThat(getSuiteResponse.getBody().length).isEqualTo(1);
        assertThat(suiteDTOs[0].getId()).isEqualTo("odm-suite");

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteReadOne() throws IOException {

        cleanState();

        createSuite1();

        ResponseEntity<SuiteDTO> getSuiteResponse = rest.readOneSuite("odm-suite");
        SuiteDTO suiteDTO = getSuiteResponse.getBody();
        verifyResponseEntity(getSuiteResponse, HttpStatus.OK, true);

        assertThat(suiteDTO.getId()).isEqualTo("odm-suite");
        assertThat(suiteDTO.getDisplayName()).isEqualTo("Suite odm-suite");
        assertThat(suiteDTO.getDescription()).isEqualTo("Policy collection for odm-suite");
        assertThat(suiteDTO.getPolicies().size()).isEqualTo(2);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteReadOneError404() {

        cleanState();

        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.SUITE),
                HttpMethod.GET,
                null,
                ErrorRes.class,
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

        SuiteDTO suiteDTO = createSuite1();

        ResponseEntity<Void> getPolicyResponse = rest.deleteSuite(suiteDTO.getId());
        verifyResponseEntity(getPolicyResponse, HttpStatus.OK, false);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testSuiteDeleteError404() throws IOException {

        cleanState();

        ResponseEntity<ErrorRes> errorResponse = null;

        errorResponse = rest.exchange(
                apiUrlOfItem(RoutesV1.SUITE),
                HttpMethod.DELETE,
                null,
                ErrorRes.class,
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

        ResponseEntity<SuiteDTO[]> suites = rest.readAllSuites();
        SuiteDTO[] suiteDTOS = suites.getBody();
        for (SuiteDTO suiteDTO : suiteDTOS) {
            rest.deleteSuite(suiteDTO.getId());
        }

    }

}
