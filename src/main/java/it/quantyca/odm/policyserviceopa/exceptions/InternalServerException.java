package it.quantyca.odm.policyserviceopa.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerException extends PolicyserviceOpaAPIException {
	public InternalServerException() {
	}

	public InternalServerException(PolicyserviceOpaAPIStandardError error, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(error, message, cause, enableSuppression, writableStackTrace);
	}

	public InternalServerException(PolicyserviceOpaAPIStandardError error, String message, Throwable cause) {
		super(error, message, cause);
	}

	public InternalServerException(PolicyserviceOpaAPIStandardError error, String message) {
		super(error, message);
	}

	public InternalServerException(Throwable cause) {
		super(cause);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}
