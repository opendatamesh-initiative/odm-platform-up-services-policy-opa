package org.opendatamesh.policyserviceopa.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.up.policy.api.v1.errors.PolicyserviceOpaAPIStandardError;
import org.opendatamesh.platform.up.policy.api.v1.resources.ErrorResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.SuiteResource;
import org.opendatamesh.platform.up.policy.api.v1.resources.PolicyResource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { PolicyserviceOpaApplication.class })
public abstract class PolicyserviceOpaApplicationIT {

	@LocalServerPort
	protected String port;

	protected PolicyserviceOpaApplicationITRestTemplate rest;

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


	@Autowired
	protected ObjectMapper mapper;

	@PostConstruct
	public final void init() {
		rest = new PolicyserviceOpaApplicationITRestTemplate(mapper);
		rest.setPort(port);
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		rest.getRestTemplate().setRequestFactory(requestFactory);
		// add uri template handler because '+' of iso date would not be encoded
		DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
		defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);
		rest.setUriTemplateHandler(defaultUriBuilderFactory);
	}

	// ======================================================================================
	// Url builder utils
	// ======================================================================================
	protected String apiUrl(RoutesV1 route) {
		return apiUrl(route, "");
	}

	protected String apiUrl(RoutesV1 route, String extension) {
		return apiUrlFromString(route.getPath() + extension);
	}

	protected String apiUrlFromString(String routeUrlString) {
		return "http://localhost:" + port + routeUrlString;
	}

	protected String apiUrlOfItem(RoutesV1 route) {
		return apiUrl(route, "/{id}");
	}

	// ======================================================================================
	// Create test basic resources
	// ======================================================================================

	protected PolicyResource createPolicy1() throws IOException {
		ResponseEntity<PolicyResource> postPolicyResponse = rest.createPolicy(POLICY_1);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicy2() throws IOException {
		ResponseEntity<PolicyResource> postPolicyResponse = rest.createPolicy(POLICY_2);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicyVersions() throws IOException {
		ResponseEntity<PolicyResource> postPolicyResponse = rest.createPolicy(POLICY_VERSIONS);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource createPolicyServicesType() throws IOException {
		ResponseEntity<PolicyResource> postPolicyResponse = rest.createPolicy(POLICY_SERVICESTYPE);
		verifyResponseEntity(postPolicyResponse, HttpStatus.CREATED, true);

		return postPolicyResponse.getBody();

	}

	protected PolicyResource updatePolicy1() throws IOException {
		ResponseEntity<PolicyResource> postPolicyResponse = rest.updatePolicy("dataproduct", POLICY_1_UPDATED);
		verifyResponseEntity(postPolicyResponse, HttpStatus.OK, true);

		return postPolicyResponse.getBody();
	}


	protected SuiteResource createSuite1() throws IOException {
		ResponseEntity<SuiteResource> postSuiteResponse = rest.createSuite(SUITE_1);
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
