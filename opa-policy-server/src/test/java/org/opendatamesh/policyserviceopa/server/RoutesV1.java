package org.opendatamesh.policyserviceopa.server;

public enum RoutesV1 {

    POLICY("/api/v1/planes/utility/policy-services/opa/policies"),
    SUITE("/api/v1/planes/utility/policy-services/opa/suites"),
    VALIDATE("/api/v1/planes/utility/policy-services/opa/validate");

    private final String path;

    private RoutesV1(String path) { this.path = path; }

    @Override
    public String toString() {
        return this.path;
    }

    public String getPath() {
        return path;
    }

}
