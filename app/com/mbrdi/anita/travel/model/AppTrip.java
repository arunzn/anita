package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.vehicle.dao.VehicleDAO;
import com.mbrdi.anita.vehicle.model.Vehicle;
import play.Logger;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AppTrip {

	public String _id, travelGroupId;
	public Double start_time;
	public Double end_time;
    public Long stop_time;
	public String start_place, end_place, origin_address, destination_address;
	public String ack_user_id;
	public Integer status;
	public Double kms_done, garage_pickup, garage_drop;
	public Integer mins_done;
	public String vehicle_type, client_name;
	public Location start_location, end_location, destination;
	public Integer start_km, end_km;
	public Set<Passenger> passengers;
	/**
	 * ===============================================================================================
	 * ============================TRANSIENT FIELDS===================================================
	 * ===============================================================================================
	 */
	
	public Vehicle vehicle;
	public String route_name;
	public String vehicle_regNo;
	public Integer date, end_date;
	public String corp_id;
	public String driver_id;
    public String driver_name;
	public String vehicle_id;
	public String mapUrl, tripJourneyUrl;

    public Double driver_rating;

    public List<TripEvent> tripEvents;

    public AppTrip() {
	}

	public AppTrip(Trip trip) {
		if (trip == null) {
            Logger.error("~~~~~~~~~~~~~~~~TRIP IS COMING NULL", new RuntimeException("Trip is Null"));
            return;
        }
		this._id = trip._id;
		this.travelGroupId = trip.travelGroupId;
		this.start_time = trip.start_time;
		this.end_time = trip.end_time;
        this.stop_time = trip.stop_time;
		this.start_place = trip.start_place;
		this.end_place = trip.end_place;

        this.origin_address = trip.origin_address;
        this.destination_address = trip.destination_address;

		this.kms_done = trip.kms_done;
		this.vehicle_type = trip.vehicle_type;
		this.start_location = trip.start_location;
		this.end_location = trip.end_location;
        this.destination = trip.destination;
		this.start_km = trip.start_km;
		this.end_km = trip.end_km;
		this.status = trip.status;
		this.vehicle_regNo = trip.vehicle_regNo;
		this.date = trip.date;
		this.end_date = trip.end_date;
		this.corp_id = trip.corp_id;
		this.vehicle = VehicleDAO.get(trip.vehicle_id);
		this.mapUrl = trip.mapUrl;
        this.route_name = trip.route_name;
        if(trip.booking_no != null) {
			this.route_name = trip.booking_no.toString();
		} else {
            this.route_name = trip.driver_name;
        }
        this.client_name = trip.client_name;
        this.passengers = trip.passengers;
        this.tripEvents = trip.tripEvents;
        this.driver_id = trip.driver_id;
        this.driver_name = trip.driver_name;
        this.vehicle_id = trip.vehicle_id;
        this.mins_done = trip.mins_done;
        this.garage_drop = trip.garage_drop;
        this.garage_pickup = trip.garage_pickup;
        this.driver_rating = trip.rating;
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
