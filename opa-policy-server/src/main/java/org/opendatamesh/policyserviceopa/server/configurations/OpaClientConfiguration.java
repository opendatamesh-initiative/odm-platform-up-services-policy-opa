package org.opendatamesh.policyserviceopa.server.configurations;

import org.opendatamesh.policyserviceopa.client.OpaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpaClientConfiguration {

    @Value("${timeout.seconds}")
    private long timeout;

    @Value("${opa.url.policies}")
    private String policiesUrl;

    @Value("${opa.url.data}")
    private String dataUrl;

    @Bean
    public OpaClient opaClient()
    {
        return new OpaClient(policiesUrl, dataUrl, timeout);
    }

}
