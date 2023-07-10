package org.opendatamesh.platform.up.policy.opa.server;

import lombok.Data;
import org.opendatamesh.platform.up.policy.api.v1.clients.PolicyServiceClient;

@Data
public class TestPolicyServiceClient extends PolicyServiceClient {
    public TestPolicyServiceClient(String address) {
        super(address);
    }

    // New methods will be added here:
    // TODO...

}
