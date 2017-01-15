package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.user.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Passenger {

	public String user_id;
	public String name;
	public Integer country_code;
	public Long phone;
	public Integer gender;
	public String emp_no, employee_of, email;
	public Integer pickup_order, drop_order;
	public Double pickupTime, dropTime; 
	public Location pickup_location, drop_location;
	public Integer otp;
	public String address_line, area, area_id, city;
    public String dest_address_line, dest_area, dest_city;
	public String sign_url;
    public Boolean isStudent;
    public Double distance;
    public String business_unit_id;
	public Rating rating;

    public Passenger() {
	}
	
	public Passenger(User user) {
		if(user == null)
			return;
		
		this.user_id = user._id;
		this.name = user.name;
		this.country_code = user.country_code;
		this.phone = user.phone;
		this.gender = user.gender;
		this.emp_no = user.emp_no;
		this.email = user.email;

		if(user.address != null) {
			this.address_line = "";
			this.address_line += user.address.addressLine != null ? user.address.addressLine : "";
			this.address_line += user.address.area_name != null ? (" " +user.address.area_name) : "";
			this.address_line += user.address.city_name != null ? (" " + user.address.city_name) : "";
            this.area = user.address.area_name;
		}
	}


    public Passenger(Passenger passenger) {
        if(passenger == null)
            return;

        this.user_id = passenger.user_id;
        this.name = passenger.name;
        this.country_code = passenger.country_code;
        this.phone = passenger.phone;
        this.gender = passenger.gender;
        this.emp_no = passenger.emp_no;
        this.employee_of = passenger.employee_of;
        this.email = passenger.email;
        this.address_line = passenger.address_line;
    }


    public static Set<Passenger> getPassengers(List<User> users) {
        if(users == null)
            return null;

        Set<Passenger> passengers = new HashSet<>();
        for(User u:users) {
            passengers.add(new Passenger(u));
        }
        return passengers;
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((business_unit_id == null) ? 0 : business_unit_id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Passenger other = (Passenger) obj;
		if (business_unit_id == null) {
			if (other.business_unit_id != null)
				return false;
		} else if (!business_unit_id.equals(other.business_unit_id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (user_id == null) {
			if (other.user_id != null)
				return false;
		} else if (!user_id.equals(other.user_id))
			return false;
		return true;
	}
    
}
