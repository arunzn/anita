package com.mbrdi.anita.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.travel.model.Passenger;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AppUser {

	public String _id;
	public String name;
	public String email;
	public Integer gender;
	public Integer country_code;
	public String dob;
	public Long phone;
	public Double rating;
	public String verificationCode;
	public Address address = new Address();
	public Address work_address = new Address();
	public String work_location_id;
	public String device_id;
	public Boolean isCorpAdmin;
	public Integer phone_type;
    public String corp_unit;
	public Long code_sent;

    public String dlNo, pan, aadhar;
	public String emp_no, department, designation;
	public AppUser() {
	}

	public AppUser(User user) {
		if (user == null)
			return;
		this._id = user._id;
		this.name = user.name;
		this.email = user.email;
		this.gender = user.gender;
		this.country_code = user.country_code;
		this.phone = user.phone;
		this.dob = user.dob;
		this.address = user.address;
		this.work_address = user.work_address;
		this.work_location_id = user.work_location_id;
		this.device_id = user.device_id;
		this.isCorpAdmin = user.isCorpAdmin;
		this.phone_type = user.phone_type;
        this.corp_unit = user.corp_unit;
        this.emp_no = user.emp_no;
        this.department = user.department;
        this.designation = user.designation;
	}
	
	public AppUser(Passenger p) {
		if (p == null)
			return;
		this._id = p.user_id;
		this.name = p.name;
		this.email = p.email;
		this.gender = p.gender;
		this.country_code = p.country_code;
		this.phone = p.phone;
	}
}
