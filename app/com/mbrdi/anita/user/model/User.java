package com.mbrdi.anita.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.mongojack.ObjectId;

import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class User {

	@Id
	@ObjectId
	public String _id;

	/**
	 * User Details
	 */
	public String name;

//	@Constraints.Email(message = "Email syntax is incorrect.")
	public String email;

	public String device_id;
	public Integer country_code;
	public Long phone;
	public Integer phone_type;
	public String password;
	public String dob;
	// 0->female 1-> male 2 -> other
	public Integer gender;
	public Address address = new Address();
	public Address work_address = new Address();
	
	public String work_location_id;
	public String department;
	public String designation;
	public String zone_id;
	public String sms_verification_code;

	public Long lastLogin;
	public Integer join_date;
	public Boolean status, notVerified;
	public String corp_unit;
	public String timeZone = "Asia/Kolkata";

	public Boolean isCorpAdmin;

	public String emp_no;

	public Long code_sent;

	public String rights;
	public String business_unit_id;

	public Boolean is_system_generated;
	
    public String business_unit_name;
    public String project_id, project_name;
    public String assign_route;


	public User() {
	}


}