package com.mbrdi.anita.basic.model;

public class ValidationFailureException extends Exception {
	
	private static final long serialVersionUID = -1l;
	
	public String param;
	public String error;
	
	public ValidationFailureException(String error){
		super(error);
		this.error = error;
	}

	public ValidationFailureException(String error, String param) {
		super(error);
		this.param = param;
		this.error = error;
	}
}
