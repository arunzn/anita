package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.location.service.LocationService;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;

import javax.persistence.Id;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@MongoCollection(name = "location")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Location {

	private static JacksonDBCollection<Location, String> LocationCollection = MongoDB.getDBCollection(Location.class);

	@Id
	@ObjectId
	public String _id;

	public String user_id, trip_id;
	public Double latitude;
	public Double longitude;
	public Integer speed;
	public Long time;
	public Double distance;

	/**
	 * Location updated XXX minutes ago
	 */
	public Long updatedAgo;

	public transient String name;

	public static Map<String, Location> locationCache = new ConcurrentHashMap<String, Location>();

	static ExecutorService executor = Executors.newFixedThreadPool(10);

	public Location() {
		super();
	}

	public Location(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}


    public void save() {
        this.time = System.currentTimeMillis();

		if (this.user_id != null && !"".equals(this.user_id)) {
			_id = user_id;
			LocationCollection.save(this);
			locationCache.put(_id, this);
		}

//		if (this.trip_id != null && !"".equals(this.trip_id)) {
//			_id = trip_id;
//			LocationCollection.save(this);
//			locationCache.put(_id, this);
//		}
	}

	public static boolean saveAll(List<Location> locations) {
		
		if (locations == null || locations.isEmpty())
			return false;

		executor.execute(new LocationSaver(locations));
		
		return true;
	}

	public static boolean saveAllBlocking(List<Location> locations) {
		
		if(locations == null || locations.isEmpty())
			return false;

		Collections.sort(locations, new Comparator<Location>() {

			@Override
			public int compare(Location o1, Location o2) {
				return (o1.time != null && o1.time != null) ? o1.time.compareTo(o2.time) : 1;
			}
		});
		Location location = locations.get(locations.size() - 1);

		if (location.user_id != null && !"".equals(location.user_id)) {
			location._id = location.user_id;
			LocationCollection.save(location);
			locationCache.put(location._id, location);
		}

		location._id = null;
		LocationHistory.save(locations);

		List<Location> tripLocations = null;
		for(Location l:locations) {
			if(l.trip_id != null) {
				if(tripLocations == null)
					tripLocations = new LinkedList<>();

				tripLocations.add(l);
			}
		}

		TripLocation.save(tripLocations);

		return true;
	}

	public static Location getUserLocation(String user_id) {
        if(user_id == null || "".equals(user_id))
            return null;
		Location location = locationCache.get(user_id);
		if (location == null) {
			location = LocationCollection.findOneById(user_id);
			if (location != null)
				locationCache.put(user_id, location);
		}
		if (location != null && location.time != null)
			location.updatedAgo = (System.currentTimeMillis() - location.time) / (60000);

		return location;
	}

	public static Location getVehicleLocation(String vehicle_id) {
		Location location = locationCache.get(vehicle_id);
		if (location == null) {
			location = LocationCollection.findOneById(vehicle_id);
			if (location != null)
				locationCache.put(vehicle_id, location);
		}

		if (location != null && location.time != null)
			location.updatedAgo = (System.currentTimeMillis() - location.time) / (60000);

		return location;
	}


	/**
	 * This method says if a user or driver or vehicle is online. _id: user._id
	 * or driver._id, or vehicle._id
	 */
	public static boolean isOnline(String _id) {
		Location location = locationCache.get(_id);
		return location != null && (location.time < (System.currentTimeMillis() - 5 * 60 * 1000));
	}

    public static List<Location> getNearByDeliveryBoys(String corp_id, Location pickupLocation, List<String> previous_users) {
        Double[] range = LocationService.getLatLngRange(pickupLocation.latitude, pickupLocation.longitude, 3);

        DBQuery.Query q = DBQuery.is("is_delivery_boy", true).is("corp_id", corp_id);
        if(previous_users != null && !previous_users.isEmpty())
            q = q.notIn("user_id", previous_users);
        if(pickupLocation.latitude > 0) {
            q = q.lessThan("latitude", pickupLocation.latitude + range[0]);
            q = q.greaterThan("latitude", pickupLocation.latitude - range[0]);
        } else {
            q = q.lessThan("latitude", pickupLocation.latitude - range[0]);
            q = q.greaterThan("latitude", pickupLocation.latitude + range[0]);
        }
        if(pickupLocation.longitude > 0) {
            q = q.lessThan("longitude", pickupLocation.longitude + range[1]);
            q = q.greaterThan("longitude", pickupLocation.longitude - range[1]);
        } else {
            q = q.lessThan("longitude", pickupLocation.longitude - range[1]);
            q = q.greaterThan("longitude", pickupLocation.longitude + range[1]);
        }
        return  LocationCollection.find(q).toArray();
    }

	/*static class SendNotification extends Thread {
		Location location;

		public SendNotification(Location location) {
			this.location = location;
		}

		@Override
		public void run() {
			VehicleNearbyNotificationService.handleJourneyNotificationCache(location);
		}
	}*/

	public static class LocationSaver implements Runnable {
		List<Location> locations;

		public LocationSaver(List<Location> locations) {
			this.locations = locations;
		}

		@Override
		public void run() {
			Location.saveAllBlocking(locations);
		}
	}
}
