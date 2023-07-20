package org.opendatamesh.platform.up.policy.opa.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendatamesh.platform.up.policy.api.v1.clients.PolicyServiceClient;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Testcontainers
//@ActiveProfiles("dev")
//@ActiveProfiles("testpostgresql")
//@ActiveProfiles("testmysql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { PolicyserviceOpaApplication.class })
public abstract class PolicyserviceOpaApplicationIT {

	@LocalServerPort
	protected String port;

	protected PolicyServiceClient client;

	protected ResourceBuilder resourceBuilder;

	protected final String POLICY_1 = "src/test/resources/policies/policy1.json";

	protected final String POLICY_1_UPDATED = "src/test/resources/policies/policy1-updated.json";

	protected final String POLICY_2 = "src/test/resources/policies/policy2.json";

	protected final String POLICY_3 = "src/test/resources/policies/policy3.json";

	protected final String POLICY_VERSIONS = "src/test/resources/policies/policy-versions.json";

	protected final String POLICY_SERVICESTYPE = "src/test/resources/policies/policy-servicestype.json";

	protected final String SUITE_1 = "src/test/resources/suites/suite1.json";

	protected final String DOCUMENT_1 = "src/test/resources/documents/document1.json";

	protected final String DOCUMENT_2 = "src/test/resources/documents/document-servicestype.json";

	protected final String DPD = "src/test/resources/documents/dpd.json";

	protected static final String OPA_DOCKER_IMAGE = "src/test/resources/opa/docker-compose.yml";

	// private/protected to make it recreate for each test
	// private/protected static final to make it shared with all tests
	@Container
	protected static final DockerComposeContainer opaServer =
			new DockerComposeContainer(new File(OPA_DOCKER_IMAGE))
					.withExposedService("opa", 8181);

	@DynamicPropertySource
    static void opaServerProperties(DynamicPropertyRegistry registry) {
		String baseUrl = "http://localhost:" + opaServer.getServicePort("opa", 8181);
		registry.add("opa.url.policies", () ->  baseUrl + "/v1/policies");
		registry.add("opa.url.data", () -> baseUrl + "/v1/data");
    }


	@PostConstruct
	public final void init() {
		client = new PolicyServiceClient("http://localhost:" + port);
		resourceBuilder = new ResourceBuilder();
	}

	@BeforeEach
	public void cleanDbState(@Autowired JdbcTemplate jdbcTemplate, @Autowired Environment environment) {
		if(Arrays.stream(environment.getActiveProfiles()).findFirst().get().equals("testpostgresql")) {
			JdbcTestUtils.deleteFromTables(
					jdbcTemplate,
					"\"ODMPOLICY\".\"POLICY\"",
					"\"ODMPOLICY\".\"SUITE\"",
					"\"ODMPOLICY\".\"SuiteEntity_policies\""
			);
		} else if (Arrays.stream(environment.getActiveProfiles()).findFirst().get().equals("testmysql")) {
			JdbcTestUtils.deleteFromTables(
					jdbcTemplate,
					"ODMPOLICY.POLICY",
					"ODMPOLICY.SUITE",
					"ODMPOLICY.SUITEENTITY_POLICIES"
			);
		}
	}

	// ======================================================================================
	// Create test basic resources
	// ======================================================================================

	protected PolicyResource createPolicy1() throws IOException {

		PolicyResource pr = resourceBuilder.readResourceFromFile(POLICY_1,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);

		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicy2() throws IOException {

		PolicyResource pr = resourceBuilder.readResourceFromFile(POLICY_2,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicyVersions() throws IOException {
		PolicyResource pr = resourceBuilder.readResourceFromFile(POLICY_VERSIONS,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicyServicesType() throws IOException {
		PolicyResource pr = resourceBuilder.readResourceFromFile(POLICY_SERVICESTYPE,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource updatePolicy1() throws IOException {
		PolicyResource pr = resourceBuilder.readResourceFromFile(POLICY_1_UPDATED,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.updatePolicy("dataproduct", pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.OK, true);

		return postPolicyResponse.getBody();
	}


	protected SuiteResource createSuite1() throws IOException {

		SuiteResource sr = resourceBuilder.readResourceFromFile(SUITE_1,SuiteResource.class);
		ResponseEntity<SuiteResource> postSuiteResponse = client.createSuite(sr);
		verifyResponseEntity(postSuiteResponse, HttpStatus.CREATED, true);

		return postSuiteResponse.getBody();
	}

	// ======================================================================================
	// Verify test basic resources
	// ======================================================================================

	protected ResponseEntity verifyResponseEntity(
			ResponseEntity responseEntity, HttpStatus statusCode, boolean checkBody
	) {
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(statusCode);
		if (checkBody) {
			assertThat(responseEntity.getBody()).isNotNull();
		}
		return responseEntity;
	}

	protected void verifyResponseError(
			ResponseEntity<ErrorResource> errorResponse,
			HttpStatus status,
			PolicyserviceOpaAPIStandardError error) {
		assertThat(errorResponse.getStatusCode())
				.isEqualByComparingTo(status);
		assertThat(errorResponse.getBody().getCode())
				.isEqualTo(error.code());
		assertThat(errorResponse.getBody().getDescription())
				.isEqualTo(error.description());
	}

}
