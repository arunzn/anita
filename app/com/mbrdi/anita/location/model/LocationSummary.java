package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.vehicle.model.Vehicle;
import org.mongojack.*;

import javax.persistence.Id;
import java.util.List;
import java.util.TimeZone;

@MongoCollection(name = "location")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class LocationSummary {

	private static JacksonDBCollection<LocationSummary, String> LocationSummary = MongoDB.getDBCollection(LocationSummary.class);

	@Id
	@ObjectId
	public String _id;

	public String route_id;
	public String trip_id;
	public String vehicle_id;
	public String reg_no;
	public Double latitude;
	public Double longitude;
	public Integer speed;
	public Integer altitude;
	public Long time;

	// yyyymmdd
	public Integer date;

	// minutes since midnight
	public Integer minutes;

	public Double distance;
	public Integer ant;
	public Integer lac;
	public Integer ss;
	public Double temp;
	public Integer fuel;
	public Boolean ignition;
	// in seconds
	public Integer duration;

	public Boolean onJourney;

	public LocationSummary() {
		super();
	}

	public LocationSummary(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public LocationSummary(List<Location> locations, Integer minutes, Vehicle v) {
		this.vehicle_id = v._id;
		this.reg_no = v.reg_no;
		this.minutes = minutes;
		Location prevLocation = null;
		for (Location l : locations) {

			if (l.trip_id != null && !"".equals(l.trip_id)) {
				this.trip_id = l.trip_id;
			}
			
			if (l.trip_id != null && !"".equals(l.trip_id)) {
				this.trip_id = l.trip_id;
			}

			this.distance = Util.sum(this.distance, l.distance);
			this.speed = Util.sum(this.speed, l.speed);
			this.speed = this.speed == null || this.speed < l.speed ? l.speed : this.speed;

			// TODO this.distance = Util.sum(this.distance , l.distance);
			this.distance = sum(l, prevLocation);
			

			if (this.time == null || this.time < l.time) {
				this.latitude = l.latitude;
				this.longitude = l.longitude;
			}
			prevLocation = l;
		}
	}

	private Double sum(Location l, Location prevLocation) {
		return prevLocation == null ? 0d : findDistance(l, prevLocation);
	}

	private double findDistance(Location presLocation, Location prevLocation) {
		double earthRadius = 6371; // KM
		double dLat = Math.toRadians(prevLocation.latitude - presLocation.latitude);
		double dLng = Math.toRadians(prevLocation.longitude - presLocation.longitude);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(presLocation.latitude))
				* Math.cos(Math.toRadians(presLocation.latitude)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (earthRadius * c);

		return dist;
	}

	public void save() {
		WriteResult<LocationSummary, String> result = LocationSummary.save(this);
	}


	public static List<LocationSummary> getByVehicleID(String trip_id, String vehicle_id, Integer from) {
		return LocationSummary.find(DBQuery.is("trip_id", trip_id).is("vehicle_id", vehicle_id).is("date", from)).toArray();
	}

}
