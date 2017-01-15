package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MatrixApiElements {
	
	public MatrixApiDuration distance;
	public MatrixApiDuration duration;
	public String status;
}
