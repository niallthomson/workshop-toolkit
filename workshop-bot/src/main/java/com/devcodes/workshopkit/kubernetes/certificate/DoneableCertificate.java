package com.devcodes.workshopkit.kubernetes.certificate;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableCertificate extends CustomResourceDoneable<Certificate> {
	public DoneableCertificate(Certificate resource, Function function) {
		super(resource, function);
	}
}
