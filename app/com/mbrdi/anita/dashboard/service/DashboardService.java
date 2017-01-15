package com.mbrdi.anita.dashboard.service;

import com.mbrdi.anita.basic.util.ExcelUtil;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.Passenger;
import com.mbrdi.anita.travel.model.TravelGroup;
import com.mbrdi.anita.travel.model.TravelType;
import com.mbrdi.anita.travel.model.Trip;
import org.mongojack.DBQuery;

import java.util.*;

public class DashboardService {

    public static int totalPassengerGettingRides(List<TravelGroup> travelGroups) {
        Set<String> ids = new HashSet<>();
        for (TravelGroup tg : travelGroups) {
            if (tg.passengers != null && tg.passengers.size() > 0){
                for(Passenger p :tg.passengers)
                    ids.add(p.user_id);
            }
        }

        return ids.size();
    }

    public static byte[] downloadTrips(String trip_type, String st_date, String ed_date) {

        int from_date = 0, to_date = 0;
        TravelType travelType = null;

        trip_type = "all".equalsIgnoreCase(trip_type) ? null : trip_type;

        if (st_date != null && ed_date != null && !"".equals(st_date) && !"".equals(ed_date) && !"mm/dd/yyyy".equals(st_date) && !"mm/dd/yyyy".equals(ed_date)) {
            from_date = Util.toDateInteger(st_date);
            to_date = Util.toDateInteger(ed_date);
        }

        if("TO".equalsIgnoreCase(trip_type)){
            travelType = TravelType.TO;
        } else if("FRO".equalsIgnoreCase(trip_type)){
            travelType = TravelType.FRO;
        }
        DBQuery.Query query = DBQuery.greaterThanEquals("date", from_date).lessThanEquals("date", to_date);


        List<Trip> trips = TripDAO.getAll(query);
        trips = processBeforeDownloadTrips(trips);

        String[] documentHeaders = { "Trips Details", "From " + st_date + " to " + ed_date };

        String[] dataColumnHeaders = { "Date", "Start time", "End time", "Start Place", "End Place", "Driver", "Driver Phone", "Veh Reg No." ,"Total Travelled Passanger" };

        String[] fields = { "date", "start_time", "end_time", "start_place", "end_place", "driver_name", "driver_phone", "vehicle_regNo", "passanger_travelled"};


        return ExcelUtil.writeXLS(trips, documentHeaders, dataColumnHeaders, fields, "trips");
    }


    public static List<Trip> processBeforeDownloadTrips(List<Trip> trips) {
        for (Trip trip : trips) {
            trip.passanger_travelled = trip.passengers != null ? trip.passengers.size() : 0;
        }

        if(trips.size() > 0) {
            int date = -1;
            int i =0;
            while(i< trips.size()) {
                if(date == -1 || date != trips.get(i).date){
                    date= trips.get(i).date;
                    Trip t = new Trip();
                    t.date = date;
                    trips.add(i, t);
                    i++;
                }
                i++;
            }
        }

        return trips;
    }
}
