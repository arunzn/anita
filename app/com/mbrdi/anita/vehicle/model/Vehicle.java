package com.mbrdi.anita.vehicle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.mongojack.ObjectId;

import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Vehicle {

	@Id
	@ObjectId
	public String _id;
	
	public String driver_id, owner_id, corp_id, corp_name;
	public String country;
	public String org;
	public String zone_id;
	public String zone_name;
	public Long app_phone_no;
	public Integer app_country_code;
	public Integer app_synced = 0;

	/**
	 * If vendor is not on Zoyride then client can add vendors vehicle whose corp_id will
	 * be vendors Id and assigned Id will be its own Id 
	 * 
	 */
	public String assigned_to;
	public Integer seats;
	public String reg_no;
	public String color;
	public String vehicle_type;
	public Boolean onJourney;
	public Double fuel_allocation = 0d;
	public Double fuel_consumed = 0d;
	public Double fuel_available = 0d;
	public Double fuel_limit = 0d; 

	public Float amount_ltrs= 0f;
	public Integer kms_reading = 0;
	public Double todayHours = 0.0;
	
	public String sim_no;
	public Long imei;
	public String gps_mfg;
	
	public String purchase_from;
	public String garage_id;
	public String pic_url;
	

	//transient field
	public String client_name;

}
