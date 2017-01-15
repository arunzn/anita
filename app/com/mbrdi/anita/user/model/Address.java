package com.mbrdi.anita.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Address {

	public String addressLine;
	public Integer city_id;
	public String city_name;
	public String state_name;
	public Integer state_id;
	public String country_name;
	public Integer country_id;
	public Integer pinCode;
	public String area_name;
	public Integer area_id;
	public Double lat, lon;
    public Boolean notAccurate;
	
}
