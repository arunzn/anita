package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class GPLocation {

	public Double lat;
	public Double lng;

    public GPLocation(){}

    public GPLocation(Location l) {
        if(l != null) {
            this.lat = l.latitude;
            this.lng = l.longitude;
        }
    }
    
    public GPLocation (double lat, double lng) {
    	this.lat = lat;
    	this.lng = lng;
    }
}
