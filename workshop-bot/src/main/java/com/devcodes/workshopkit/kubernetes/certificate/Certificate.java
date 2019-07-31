package com.devcodes.workshopkit.kubernetes.certificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.fabric8.kubernetes.client.CustomResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Certificate extends CustomResource {
	private CertificateSpec spec;

	public CertificateSpec getSpec() {
		return spec;
	}

	public void setSpec(CertificateSpec spec) {
		this.spec = spec;
	}
}
