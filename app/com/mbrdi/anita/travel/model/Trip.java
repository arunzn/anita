package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.vehicle.model.Vehicle;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;

import javax.persistence.Id;
import java.util.*;


@MongoCollection(name = "trip")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Trip {

	@Id
	@ObjectId
	public String _id;

    public String travelGroupId;
	public String vehicle_id;

	public String corp_id;
	public Double start_time, end_time;
    public Long stop_time;
	public String start_place, end_place, origin_address, destination_address;
	
	public Double kms_done, kms_done_gps, garage_pickup, garage_drop;
	public Integer mins_done;
	public String client_name;
	public String site;
	public Integer start_km, end_km;
	public Boolean isNonAC;

	public Double no_of_trips;
	public Double rating;
	public Set<Passenger> passengers;
	
	//==================================================================================================================
	//============================                 DRIVER DETAILS                     ==================================
	//==================================================================================================================
	public String driver_id, driver_name;
	public Long driver_phone;

	//==================================================================================================================
	//============================                 VEHICLE DETAILS                     =================================
	//==================================================================================================================
	public String vehicle_regNo;
	public String vehicle_type;

	//==================================================================================================================
	//============================                 INVOICE FIELDS                     ==================================
	//==================================================================================================================

	public Location start_location, end_location, destination;
	public String message;

	// yyyymmdd
	public Integer date, end_date;
    public Long booking_no, updated_on;
	public Integer status = -1; // 0: On Pre Journey, 1: On Journey, 2:
								// Delayed/Changed, 3: Completed, 4:Cancelled, 5:Didnot Run


	public String route_name;

	public Vehicle vehicle;

	public String history, error;
	public String mapUrl;

	//transient Fields
	public  Integer passanger_travelled;
    public List<TripEvent> tripEvents;


    public Trip() {
	}

    public Passenger getPassenger(String user_id){
        if(user_id == null || passengers == null)
            return null;

        for(Passenger p : passengers) {
            if(user_id.equals(p.user_id))
                return p;
        }
        return null;
    }

}
