package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.service.GridSupport;
import com.mbrdi.anita.basic.util.DateUtil;
import com.mbrdi.anita.basic.util.ExecutorUtil;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.service.LocationService;
import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.Trip;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.MongoCollection;

import java.util.*;

@MongoCollection(name = "location_history")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class LocationHistory {

    private static JacksonDBCollection<Location, String> LH_COLLECTION = MongoDB.getDBCollection(LocationHistory.class, Location.class);

    public static void save(Location location) {
        LH_COLLECTION.save(location);
    }

    public static void save(List<Location> locations) {
        LH_COLLECTION.insert(locations);
    }

    public static List<Location> getDriverLocation(String driver_id, Date formTime, Date toTime) {
        return LH_COLLECTION.find(DBQuery.is("driver_id", driver_id).greaterThan("time", formTime).lessThan("time", toTime)).toArray();
    }

    public static List<Location> getUserLocation(String user_id, Date formTime, Date toTime) {
        return LH_COLLECTION.find(DBQuery.is("user_id", user_id)).toArray();
    }

    public static GridResponse<Location> fetch(GridRequest request) {
        return GridSupport.fetch(Location.class, LH_COLLECTION, request);
    }

    public static List<Location> pathLocVeh(String veh_id, long from, long to) {

        return LH_COLLECTION.find(DBQuery.is("vehicle_id", veh_id).greaterThan("time", from).lessThan("time", to)).toArray();
    }

    private static Integer getSummaryTime(Long time, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(time);
        // 10 -> 15 == 15
        // 15.01 -> 19.59 = 20
        int minute = (c.get(Calendar.MINUTE) / 5) * 5 + 5;
        return c.get(Calendar.HOUR_OF_DAY) * 60 + minute;
    }

    public static void deleteByTrip(String trip_id) {
        ExecutorUtil.executeNow(new Runnable() {
            @Override
            public void run() {
                LH_COLLECTION.remove(DBQuery.is("trip_id", trip_id));
            }
        });
    }

    public static void delete(String _id) {
        LH_COLLECTION.removeById(_id);
    }


    public static void purge() {

        Integer toDate = Util.getTodayTimeZeroInteger(TimeZone.getDefault());
        Integer fromDate = DateUtil.addDays(toDate, -2);
        List<Trip> trips = TripDAO.fetchAll(DBQuery.in("status", 3, 4).lessThanEquals("date", toDate).greaterThanEquals("date", fromDate));
        if (trips != null) {
            for (Trip trip : trips) {

                //TODO TripService.uploadPathMapIfAbsent(trip);
                List<Location> locs = LH_COLLECTION.find(DBQuery.is("trip_id", trip._id)).toArray();
                Collections.sort(locs, LocationService.LocationSorterByTime);
                Location prev = null;
                for (Location l : locs) {

                    if (prev == null)
                        prev = l;
                    else if (((l.time - prev.time) > 300000)
                            || Util.getDistance(prev.latitude, prev.longitude, l.latitude, l.longitude) > 0.05) {
                        prev = l;
                    } else {
                        LocationHistory.delete(l._id);
                        TripLocation.delete(l._id);
                    }
                }
            }

            //remove3monthOld();
        }
    }

    private static void remove3monthOld() {
        Integer date = Util.getTodayTimeZeroInteger(TimeZone.getDefault());
        date = DateUtil.addDays(date, -90);
        LH_COLLECTION.remove(DBQuery.lessThan("time", Util.getTimeinMillis(date)));
    }

    public static List<Location> getAllLocationByQuery(DBQuery.Query query) {
        return LH_COLLECTION.find(query).sort(org.mongojack.DBSort.desc("time")).toArray();
    }
}
