package com.devcodes.workshopkit.kubernetes.certificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificateSpec implements KubernetesResource {
	private String commonName;
	private String secretName;
	
	private CertificateIssuer issuerRef;
	
	public String getCommonName() {
		return commonName;
	}
	
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
	public String getSecretName() {
		return secretName;
	}
	
	public void setSecretName(String secretName) {
		this.secretName = secretName;
	}

	public CertificateIssuer getIssuerRef() {
		return issuerRef;
	}

	public void setIssuerRef(CertificateIssuer issuerRef) {
		this.issuerRef = issuerRef;
	}
}
