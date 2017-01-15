package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.basic.database.MongoDB;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class TripLocation {

	public static JacksonDBCollection<Location, String> tripLocationCollection = MongoDB.getDBCollection(TripLocation.class, Location.class);
	
	public static void save(Location r) {
		tripLocationCollection.save(r);
	}
	
	public static void save(List<Location> locations) {
	    if(locations != null)
		    tripLocationCollection.insert(locations);
	}

	public static void delete(String trip_id) {
		tripLocationCollection.removeById(trip_id);
	}

    public static List<Location> getAllByTripID(String trip_id) {
        return tripLocationCollection.find(DBQuery.is("trip_id", trip_id)).sort(DBSort.asc("time")).toArray();
    }

    public static List<Location> getAllByTripID(String trip_id, Long fromTime) {
		DBQuery.Query query = DBQuery.is("trip_id", trip_id);
		if(fromTime != null)
			query.greaterThan("time", fromTime);
        return tripLocationCollection.find(query).sort(DBSort.asc("time")).toArray();
    }

    public static Integer countTripLocation(String trip_id) {
        return tripLocationCollection.find(DBQuery.is("trip_id", trip_id)).count();
    }
}