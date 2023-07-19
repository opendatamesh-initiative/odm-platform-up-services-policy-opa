package org.opendatamesh.platform.up.policy.opa.server;

import org.junit.runner.RunWith;
import org.opendatamesh.platform.up.policy.api.v1.clients.PolicyServiceClient;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("testmysql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { PolicyserviceOpaApplication.class })
public abstract class PolicyserviceOpaApplicationIT {

	@LocalServerPort
	protected String port;

	protected PolicyServiceClient client;

	protected ResourceBuilder rb;

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

	@PostConstruct
	public final void init() {

		client = new PolicyServiceClient("http://localhost:" + port);

		rb = new ResourceBuilder();
	}

	// ======================================================================================
	// Create test basic resources
	// ======================================================================================

	protected PolicyResource createPolicy1() throws IOException {

		PolicyResource pr = rb.readResourceFromFile(POLICY_1,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);

		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicy2() throws IOException {

		PolicyResource pr = rb.readResourceFromFile(POLICY_2,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicyVersions() throws IOException {
		PolicyResource pr = rb.readResourceFromFile(POLICY_VERSIONS,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicyServicesType() throws IOException {
		PolicyResource pr = rb.readResourceFromFile(POLICY_SERVICESTYPE,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.createPolicy(pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource updatePolicy1() throws IOException {
		PolicyResource pr = rb.readResourceFromFile(POLICY_1_UPDATED,PolicyResource.class);
		ResponseEntity<PolicyResource> postPolicyResponse = client.updatePolicy("dataproduct", pr);
		verifyResponseEntity(postPolicyResponse, HttpStatus.OK, true);

		return postPolicyResponse.getBody();
	}


	protected SuiteResource createSuite1() throws IOException {

		SuiteResource sr = rb.readResourceFromFile(SUITE_1,SuiteResource.class);
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
