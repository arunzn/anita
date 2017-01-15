package com.mbrdi.anita.travel.dao;

import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.service.GridSupport;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.travel.model.Trip;
import org.mongojack.*;

import java.util.*;

public class TripDAO {

    private static JacksonDBCollection<Trip, String> TRIP_COLLECTION = MongoDB.getDBCollection(Trip.class);
    private static JacksonDBCollection<Trip, String> TripDeleted_Collection = MongoDB.getDBCollection(Trip.class, "TripDeleted");

    public static void save(Trip trip) {
        // throw excp if any of 4 mandatory field is null
        trip.updated_on = System.currentTimeMillis();

        if (trip._id == null) {
            WriteResult<Trip, String> result = TRIP_COLLECTION.save(trip);
            trip._id = result.getSavedObject()._id;
        } else {
            Trip old_trip = get(trip._id);
            if(old_trip != null){
                Util.smartCopy(trip, old_trip);
                TRIP_COLLECTION.save(old_trip);
            } else {
                TRIP_COLLECTION.save(trip);
            }
        }
    }

    public static void delete(String _id) {
        if (_id != null) {
            TripDeleted_Collection.save(get(_id));
            TRIP_COLLECTION.removeById(_id);
        }
    }

    public static void softDelete(String id) {
        if (id != null && get(id) != null)
            TRIP_COLLECTION.findAndModify(DBQuery.is("_id", id), DBUpdate.set("deleted", true));
    }

    /**
     * This updates values as it is
     */
    public static void updateHard(Trip trip) {
        trip.updated_on = System.currentTimeMillis();
        TRIP_COLLECTION.updateById(trip._id, trip);
    }

    public static Trip getOnJourneyTrip(String route_id) {
        return TRIP_COLLECTION.findOne(DBQuery.is("route_id", route_id).is("status", 1));
    }


    public static void updateEndJourney(String trip_id, Double end_time, Integer end_date) {
        if (trip_id == null)
            return;
        TRIP_COLLECTION.update(DBQuery.is("_id", trip_id).is("status", 1), DBUpdate.set("onJourney", false).set("end_time", end_time)
                .set("end_date", end_date).set("status", 3).set("updated_on", System.currentTimeMillis()));
    }

    public static Trip get(String _id) {
        if(Util.isNullOrEmpty(_id))
            return null;
        return TRIP_COLLECTION.findOneById(_id);
    }

    public static Trip getByTgId(String tg_id) {
        if(Util.isNullOrEmpty(tg_id))
            return null;
        return TRIP_COLLECTION.findOne(DBQuery.is("travelGroupId", tg_id));
    }

    public static GridResponse<Trip> fetch(GridRequest request) {
        return GridSupport.fetch(Trip.class, TRIP_COLLECTION, request);
    }

    public static List<Trip> fetchAll(DBQuery.Query q) {
        return TRIP_COLLECTION.find(q).toArray();
    }

    public static Trip findOne(DBQuery.Query q) {
        return TRIP_COLLECTION.findOne(q);
    }

    public static List<Trip> fetchAll(DBQuery.Query q, Integer batchSize, Integer offset) {
        if(batchSize != null && offset != null && batchSize > 0 && offset >= 0) {
            return TRIP_COLLECTION.find(q).skip(offset).limit(batchSize).toArray();
        }
        return null;
    }

    public static Trip getByRouteNDate(String route_id, String date) {
        return TRIP_COLLECTION.findOne(DBQuery.is("route_id", route_id).is("date", date));
    }

    public static void changeStatus(String id, Integer status) {
        TRIP_COLLECTION.findAndModify(DBQuery.is("_id", id), DBUpdate.set("status", status).set("updated_on", System.currentTimeMillis()));
    }

    public static Long countAll(DBQuery.Query query) {
        return TRIP_COLLECTION.getCount(query);
    }

    public static List<Trip> getAll(DBQuery.Query query) {
        return TRIP_COLLECTION.find(query).sort(DBSort.desc("date")).toArray();
    }

    public static boolean exists(DBQuery.Query query) {
        return TRIP_COLLECTION.findOne(query) != null;
    }

    public static void update(DBQuery.Query query, DBUpdate.Builder update) {
        TRIP_COLLECTION.update(query, update);
    }

    public static List<Trip> getPastTripByPassengerID( String user_id, Integer fromDate) {
        return TRIP_COLLECTION.find(DBQuery.is("status", 3).is("passengers.user_id", user_id).or(DBQuery.greaterThanEquals("date", fromDate), DBQuery.greaterThanEquals("end_date", fromDate))).sort(DBSort.desc("date")).toArray();
    }
}
