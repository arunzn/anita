package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class GMapLocation {
	
	public GMapLocation() {
	}
	
	public GMapLocation(LocationSummary ls) {
		this.lat = ls.latitude;
		this.lng = ls.longitude;
	}

	public double lat, lng;
}
