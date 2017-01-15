package com.mbrdi.anita.travel.service;

import com.mbrdi.anita.basic.exception.DirtyUpdateException;
import com.mbrdi.anita.basic.util.Constants;
import com.mbrdi.anita.basic.util.ExecutorUtil;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.location.service.DirectionService;
import com.mbrdi.anita.location.service.LocationService;
import com.mbrdi.anita.message.model.ACTION;
import com.mbrdi.anita.message.model.NOTIFICATION_TYPE;
import com.mbrdi.anita.message.model.Notification;
import com.mbrdi.anita.message.service.NotificationService;
import com.mbrdi.anita.travel.dao.TravelGroupDAO;
import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.*;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import org.mongojack.DBQuery;

import javax.inject.Singleton;
import java.util.*;

import static com.mbrdi.anita.basic.util.Util.roundCurrency;
import static com.mbrdi.anita.basic.util.Util.subtract;
import static com.mbrdi.anita.basic.util.Util.sum;


@Singleton
public class TravelGroupService {

    /**
     * Find all TravelGroupd according to a TravelRequest where it is starting in -30 min to 15 min range.
     * Also the starting pickup location is around 2 KM from driver start location and drop location is within 5 km range.
     *
     * @param request
     * @return
     */
    public List<TravelGroup> fetchMatchingTravelGroups(TravelRequest request) {
        DBQuery.Query q = DBQuery.is("date", request.date).lessThan("time", Util.getAddedTime(request.time, 15)).greaterThanEquals("time", Util.getAddedTime(request.time, -30));
        q.notExists("status");
        if (request.from != null) {

            Double[] range = LocationService.getLatLngRange(request.from.lat, request.from.lon, 2);
            if (request.from.lat > 0) {
                q = q.lessThan("waypts.latitude", request.from.lat + range[0]);
                q = q.greaterThan("waypts.latitude", request.from.lat - range[0]);
            } else {
                q = q.lessThan("waypts.latitude", request.from.lat - range[0]);
                q = q.greaterThan("waypts.latitude", request.from.lat + range[0]);
            }
            if (request.from.lon > 0) {
                q = q.lessThan("waypts.longitude", request.from.lon + range[1]);
                q = q.greaterThan("waypts.longitude", request.from.lon - range[1]);
            } else {
                q = q.lessThan("waypts.longitude", request.from.lon - range[1]);
                q = q.greaterThan("waypts.longitude", request.from.lon + range[1]);
            }

            if (request.to != null) {
                range = LocationService.getLatLngRange(request.to.lat, request.to.lon, 2);

                if (request.to.lat > 0) {
                    q = q.lessThan("waypts.latitude", request.to.lat + range[0]);
                    q = q.greaterThan("waypts.latitude", request.to.lat - range[0]);
                } else {
                    q = q.lessThan("waypts.latitude", request.to.lat - range[0]);
                    q = q.greaterThan("waypts.latitude", request.to.lat + range[0]);
                }
                if (request.to.lon > 0) {
                    q = q.lessThan("waypts.longitude", request.to.lon + range[1]);
                    q = q.greaterThan("waypts.longitude", request.to.lon - range[1]);
                } else {
                    q = q.lessThan("waypts.longitude", request.to.lon - range[1]);
                    q = q.greaterThan("waypts.longitude", request.to.lon + range[1]);
                }
            }
        }

        return TravelGroupDAO.fetchAll(q);
        //return null;
    }

    public boolean joinTravelGroup(TravelRequest travelRequest, String travelGroup_id) {
        TravelGroup travelGroup = TravelGroupDAO.get(travelGroup_id);
        if (validateCriteria(travelGroup, travelRequest)) {

            TravelRequest t = TravelRequest.get(travelRequest._id);
            if(t != null && !travelGroup_id.equals(t.travelGroup_id)){
                travelRequest._id = null;
            }

            populateDetourDistTimeInTravelRequest(travelGroup, travelRequest);

            travelRequest.travelGroup_id = travelGroup_id;
            travelRequest.saveUpdate();

            Notification n = new Notification();
            n.action = ACTION.TRAVEL_GRP_JOIN_REQ;
            n.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
            n.travelRequest = travelRequest;
            NotificationService.send(UserDAO.get(travelGroup.owner_id), n);
            return true;
        }
        return false;
    }

    /**
     * Validate if a TravelRequest is valid for this travel group
     *
     * @param travelGroup
     * @param travelRequest
     * @return
     */
    private boolean validateCriteria(TravelGroup travelGroup, TravelRequest travelRequest) {
        if (travelGroup.passengers != null && travelGroup.seats != null && travelGroup.seats <= travelGroup.passengers.size())
            return false;

        return true;
    }

    /**
     * This assigns a TravelRequest to a TravelGroup if seats available.
     *
     * @param travelRequest
     * @param travelGroup_id
     * @return
     */
    public TravelGroup acceptTravelGroupRequest(TravelRequest travelRequest, String travelGroup_id) {
        TravelGroup tg = TravelGroupDAO.get(travelGroup_id);
        if (tg != null  && (travelRequest.approved == null || !travelRequest.approved)) {
            if (tg.passengers == null)
                tg.passengers = new HashSet<>();

            User user = UserDAO.get(travelRequest.requester_id);
            Passenger p = new Passenger(user);
            p.pickup_location = new Location(travelRequest.from.lat, travelRequest.from.lon);
            p.drop_location = new Location(travelRequest.to.lat, travelRequest.to.lon);
            p.pickupTime = travelRequest.time;
            p.address_line = travelRequest.from.addressLine;
            p.dest_address_line = travelRequest.to.addressLine;

            tg.passengers.add(p);
            if (tg.seats == tg.passengers.size())
                tg.status = 1;

            try {
                TravelGroupService.populateEncodedPath(tg);
                TravelGroupDAO.update(tg);

                travelRequest.travelGroup_id = tg._id;
                travelRequest.approved = true;
                populateDetourDistTimeInTravelRequest(tg, travelRequest);
                travelRequest.saveUpdate();

                Notification n = new Notification();
                n.action = ACTION.TRAVEL_GRP_JOIN_REQ_ACCEPT;
                n.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
                n.travelGroup = tg;
                n.travelRequest = travelRequest;

                for (Passenger passenger : tg.passengers) {
                    if (user._id.equals(passenger.user_id)) {
                        n.travelGroup = tg;
                        populateDetourDistTimeInTravelGroup(tg, travelRequest);
                        NotificationService.send(UserDAO.get(p.user_id), n);
                    }
                }

                TravelRequest.deleteByRequestorId(travelRequest.requester_id);

                return tg;
            } catch (DirtyUpdateException e) {
                acceptTravelGroupRequest(travelRequest, travelGroup_id);
            }
        }
        return null;
    }

    public List<TravelRequest> fetchMatchingTravelRequest(TravelGroup travelGroup) {

        DBQuery.Query q = DBQuery.is("travelGroup_id", travelGroup._id).or(DBQuery.notExists("status"), DBQuery.notEquals("status", 3));
        List<TravelRequest> rs = TravelRequest.fetchAll(q, 100, 0);

        Comparator<TravelRequest> c = (p, o) ->
                p.approved!= null && p.approved == true ? -1 : 1;

        Collections.sort(rs, c);

        q = DBQuery.is("date", travelGroup.date).lessThan("time", Util.getAddedTime(travelGroup.time, 15)).greaterThanEquals("time", Util.getAddedTime(travelGroup.time, -30));
        q = q.notExists("status").notExists("travelGroup_id");

        if(rs != null && !rs.isEmpty()) {
            List<String> list = new LinkedList<>();
            for(TravelRequest t: rs) {
                list.add(t.requester_id);
            }

            q = q.notIn("requester_id", list);
        }

        if (travelGroup.from != null) {

            Double[] range = LocationService.getLatLngRange(travelGroup.from.lat, travelGroup.from.lon, 5);
            if (travelGroup.from.lat > 0) {
                q = q.lessThan("from.lat", travelGroup.from.lat + range[0]);
                q = q.greaterThan("from.lat", travelGroup.from.lat - range[0]);
            } else {
                q = q.lessThan("from.lat", travelGroup.from.lat - range[0]);
                q = q.greaterThan("from.lat", travelGroup.from.lat + range[0]);
            }
            if (travelGroup.from.lon > 0) {
                q = q.lessThan("from.lon", travelGroup.from.lon + range[1]);
                q = q.greaterThan("from.lon", travelGroup.from.lon - range[1]);
            } else {
                q = q.lessThan("from.lon", travelGroup.from.lon - range[1]);
                q = q.greaterThan("from.lon", travelGroup.from.lon + range[1]);
            }

            if (travelGroup.to != null) {
                range = LocationService.getLatLngRange(travelGroup.from.lat, travelGroup.from.lon, 5);

                if (travelGroup.to.lat > 0) {
                    q = q.lessThan("to.lat", travelGroup.to.lat + range[0]);
                    q = q.greaterThan("to.lat", travelGroup.to.lat - range[0]);
                } else {
                    q = q.lessThan("to.lat", travelGroup.to.lat - range[0]);
                    q = q.greaterThan("to.lat", travelGroup.to.lat + range[0]);
                }
                if (travelGroup.to.lon > 0) {
                    q = q.lessThan("to.lon", travelGroup.to.lon + range[1]);
                    q = q.greaterThan("to.lon", travelGroup.to.lon - range[1]);
                } else {
                    q = q.lessThan("to.lon", travelGroup.to.lon - range[1]);
                    q = q.greaterThan("to.lon", travelGroup.to.lon + range[1]);
                }
            }
        }

        List<TravelRequest> rs2 = TravelRequest.fetchAll(q, 100, 0);

        List<TravelRequest> requests = new LinkedList<>();
        if (rs != null)
            requests.addAll(rs);

        if (rs2 != null)
            requests.addAll(rs2);

        return requests;
    }

    public TravelGroup saveTravelGroup(TravelGroup travelGroup) {
        Integer today = Util.getTodayTimeZeroInteger(TimeZone.getDefault());
        travelGroup._id = travelGroup.owner_id;
        TravelGroup tg = TravelGroupDAO.findOne(DBQuery.is("_id", travelGroup.owner_id)/*.is("date", travelGroup.date).or(DBQuery.notExists("status"), DBQuery.notEquals("status", 3))*/);
        if(tg != null) {
//            travelGroup._id = tg._id;
            travelGroup.version = tg.version;
            try {
                TravelGroupService.populateEncodedPath(travelGroup);
                TravelGroupDAO.update(travelGroup);
            } catch (DirtyUpdateException e) {
                e.printStackTrace();
            }
        } else {
            TravelGroupService.populateEncodedPath(travelGroup);
            TravelGroupDAO.save(travelGroup);
        }
        notifySyncTravelGroup(travelGroup);
        return travelGroup;
    }

    public void notifySyncTravelGroup(final TravelGroup travelGroup) {
        ExecutorUtil.executeNow(new Runnable() {
            @Override
            public void run() {
                List<TravelRequest> travelRequests = fetchMatchingTravelRequest(travelGroup);
                for(TravelRequest tr:travelRequests) {
                    NotificationService.send(UserDAO.get(tr.requester_id), new Notification(ACTION.TRAVEL_GRP_SYNC));
                }
            }
        });
    }

    public AppTrip deployTravelGroup(TravelGroup travelGroup) {
        TravelGroup tg = TravelGroupDAO.get(travelGroup._id);
        if (tg != null) {
            Trip t = TripDAO.get(tg.trip_id);
            if(t == null) {
                t = new Trip();
            }
            t.travelGroupId = tg._id;
            t.driver_id = tg._id;
            t.driver_name = tg.owner_name;
            User driver = UserDAO.get(tg.owner_id);
            t.driver_phone = driver.phone;
            t.start_place = tg.from.addressLine;
            t.end_place = tg.to.addressLine;
            t.start_time = tg.time;
            t.date = tg.date;
            t.vehicle_regNo = tg.vehicle_reg_no;
            t.vehicle_type = tg.vehicle_name;

            t.start_location = new Location(tg.from.lat, tg.from.lon);
            t.end_location = new Location(tg.to.lat, tg.to.lon);

            t.status = 0;

            if (tg.passengers != null && !tg.passengers.isEmpty()) {
                t.passengers = tg.passengers;
                PassengerOrderingService.processPassengerOrder(t);
            }

            t.tripEvents = populateEvents(t);

            populatePath(t);

            try {
                TripDAO.save(t);

                if (tg.passengers != null && !tg.passengers.isEmpty() && tg.seats == tg.passengers.size()) {
                    tg.status = 3;
                }

                try {
                    tg.trip_id = t._id;
                    TravelGroupDAO.update(tg);
                } catch (DirtyUpdateException e) {
                    e.printStackTrace();
                }

                TravelRequest.deleteByGroupId(tg._id);
                AppTrip trip = new AppTrip(t);
                Notification n = new Notification();

                n.action = ACTION.TRAVEL_GRP_JOURNEY_START;
                n.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
                n.trip = trip;

                if (tg.passengers != null && !tg.passengers.isEmpty()) {
                    for (Passenger p : t.passengers) {
                        User user = UserDAO.get(p.user_id);
                        NotificationService.send(user, n);
                        TravelRequestAnalytics.addPassengers(Util.getTodayTimeZeroInteger(Constants.timeZone), user.email);
                    }
                }

                TravelRequestAnalytics.addLiftGivers(Util.getTodayTimeZeroInteger(Constants.timeZone), driver.email);

                return trip;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new AppTrip(t);
        }
        return null;
    }

    private List<TripEvent> populateEvents(Trip t) {
        List<TripEvent> events = new LinkedList<>();

        TripEvent te = new TripEvent();
        te.isPickUp = true;
        te.address_line = t.start_place;
        te.completed = true;
        te.location = t.start_location;
        events.add(te);

        if (t.passengers != null && !t.passengers.isEmpty()) {
            List<Passenger> passengers = new LinkedList<Passenger>(t.passengers);
            passengers.sort((Passenger p1, Passenger p2) -> p1.pickup_order != null ? p1.pickup_order.compareTo(p2.pickup_order) : 1);
            for (Passenger p : passengers) {
                events.add(new TripEvent(p, true));
            }
            passengers.sort((Passenger p1, Passenger p2) -> p1.drop_order != null ? p1.drop_order.compareTo(p2.drop_order) : 1);
            for (Passenger p : passengers) {
                events.add(new TripEvent(p, false));
            }
        }

        te = new TripEvent();
        te.trip_id = t._id;
        te.isPickUp = false;
        te.passenger_id = "";
        te.address_line = t.end_place;
        te.location = t.end_location;
        events.add(te);

        return events;
    }

    public void populateDetourDistTimeInTravelGroup(TravelGroup tg, TravelRequest tr) {
        Location from = new Location(tr.from.lat, tr.from.lon);
        Location to = new Location(tr.to.lat, tr.to.lon);
        Location p_from = new Location(tg.from.lat, tg.from.lon);
        Location p_to = new Location(tg.to.lat, tg.to.lon);

        Double[] val1 = LocationService.getDistanceTimeOnlyByAPI(from, p_from);
        Double[] val2 = LocationService.getDistanceTimeOnlyByAPI(p_from, p_to);
        Double[] val3 = LocationService.getDistanceTimeOnlyByAPI(p_to, to);
        Double[] val4 = LocationService.getDistanceTimeOnlyByAPI(from, to);
        tg.detour_dist = roundCurrency(subtract(sum(val1[0], val2[0], val3[0]), val4[0]));
        tg.detour_time = roundCurrency(subtract(sum(val1[1], val2[1], val3[1]), val4[1]));
        tg.currentLocation = Location.getUserLocation(tg._id);
    }

    public void populateDetourDistTimeInTravelRequest(TravelGroup tg, TravelRequest tr) {
        Location from = new Location(tr.from.lat, tr.from.lon);
        Location to = new Location(tr.to.lat, tr.to.lon);
        Location p_from = new Location(tg.from.lat, tg.from.lon);
        Location p_to = new Location(tg.to.lat, tg.to.lon);

        Double[] val1 = LocationService.getDistanceTimeOnlyByAPI(from, p_from);
        Double[] val2 = LocationService.getDistanceTimeOnlyByAPI(p_from, p_to);
        Double[] val3 = LocationService.getDistanceTimeOnlyByAPI(p_to, to);
        Double[] val4 = LocationService.getDistanceTimeOnlyByAPI(from, to);
        tr.detour_dist = roundCurrency(subtract(sum(val1[0], val2[0], val3[0]), val4[0]));
        tr.detour_time = roundCurrency(subtract(sum(val1[1], val2[1], val3[1]), val4[1]));
    }


    public static void populatePath(Trip t){

        List<String> path = new LinkedList<>();

        Location from = t.start_location;

        for(TripEvent e:t.tripEvents) {
            e.path = DirectionService.getPolylineEncoded(from, e.location);
            from = e.location;
        }

        /*Passenger prev = getPassengerByOrder(t, 1, true);
        if(prev != null) {
            path.add(DirectionService.getPolylineEncoded(t.start_location, prev.pickup_location));
            for (int i = 1; i < t.passengers.size(); i++) {
                Passenger p = getPassengerByOrder(t, i + 1, true);
                if (p != null) {
                    path.add(DirectionService.getPolylineEncoded(prev.pickup_location, p.pickup_location));
                    prev = p;
                }
            }
        }

        Passenger drop = getPassengerByOrder(t, 1, false);
        if(drop != null) {
            path.add(DirectionService.getPolylineEncoded(prev.pickup_location, drop.drop_location));
            for (int i = 1; i < t.passengers.size(); i++) {
                Passenger p = getPassengerByOrder(t, i + 1, false);
                path.add(DirectionService.getPolylineEncoded(drop.drop_location, p.drop_location));
                drop = p;
            }
        }
        if(drop != null) {
            path.add(DirectionService.getPolylineEncoded(drop.pickup_location, t.end_location));
        } else {
            path.add(DirectionService.getPolylineEncoded(t.start_location, t.end_location));
        }

        return path;*/
    }

    private static Passenger getPassengerByOrder(Trip t, int order_no, boolean pickup){
        for(Passenger p :t.passengers) {
            if (pickup) {
                if (p.pickup_order == order_no)
                    return p;
            } else {
                if (p.drop_order == order_no)
                    return p;
            }
        }
        return null;
    }

    public void notifyTripEvent(TripEvent tripEvent) {
        Trip trip = TripDAO.get(tripEvent.trip_id);
        if(trip != null && trip.corp_id == null) {
            Notification n = new Notification();
            n.action = ACTION.UPDATE_TRIP_EVENT;
            n.tripEvent = tripEvent;
            NotificationService.send(UserDAO.get(trip.driver_id), n);
        }
    }

    public Location requestLocation(String owner_id) {
        return Location.getUserLocation(owner_id);
    }

    public List<TravelRequest> viewCoPassengers(TravelRequest travelRequest) {
        DBQuery.Query q = DBQuery.notEquals("_id", travelRequest._id);
        if(travelRequest.regular != null && travelRequest.regular) {
            q.is("regular", true);
        }

//        q = DBQuery.lessThan("in_time", Util.getAddedTime(travelRequest.in_time, 15)).greaterThanEquals("in_time", Util.getAddedTime(travelRequest.in_time, -30));

        if (travelRequest.from != null) {

            Double[] range = LocationService.getLatLngRange(travelRequest.from.lat, travelRequest.from.lon, 5);
            if (travelRequest.from.lat > 0) {
                q = q.lessThan("from.lat", travelRequest.from.lat + range[0]);
                q = q.greaterThan("from.lat", travelRequest.from.lat - range[0]);
            } else {
                q = q.lessThan("from.lat", travelRequest.from.lat - range[0]);
                q = q.greaterThan("from.lat", travelRequest.from.lat + range[0]);
            }
            if (travelRequest.from.lon > 0) {
                q = q.lessThan("from.lon", travelRequest.from.lon + range[1]);
                q = q.greaterThan("from.lon", travelRequest.from.lon - range[1]);
            } else {
                q = q.lessThan("from.lon", travelRequest.from.lon - range[1]);
                q = q.greaterThan("from.lon", travelRequest.from.lon + range[1]);
            }

            if (travelRequest.to != null) {
                range = LocationService.getLatLngRange(travelRequest.from.lat, travelRequest.from.lon, 5);

                if (travelRequest.to.lat > 0) {
                    q = q.lessThan("to.lat", travelRequest.to.lat + range[0]);
                    q = q.greaterThan("to.lat", travelRequest.to.lat - range[0]);
                } else {
                    q = q.lessThan("to.lat", travelRequest.to.lat - range[0]);
                    q = q.greaterThan("to.lat", travelRequest.to.lat + range[0]);
                }
                if (travelRequest.to.lon > 0) {
                    q = q.lessThan("to.lon", travelRequest.to.lon + range[1]);
                    q = q.greaterThan("to.lon", travelRequest.to.lon - range[1]);
                } else {
                    q = q.lessThan("to.lon", travelRequest.to.lon - range[1]);
                    q = q.greaterThan("to.lon", travelRequest.to.lon + range[1]);
                }
            }
        }

        return TravelRequest.fetchAll(q, 100, 0);
    }

    public static void populateEncodedPath(TravelGroup travelGroup) {

        if(travelGroup.from != null && travelGroup.from.lat != null && travelGroup.from.lon != null
                && travelGroup.to != null && travelGroup.to.lat != null && travelGroup.to.lon != null) {

            Location[] waypts = prepareWayPts(travelGroup);

            if(Util.isNullOrEmpty(travelGroup.encodedPath)) {
                travelGroup.encodedPath = DirectionService.getPolylineEncoded(new Location(travelGroup.from.lat, travelGroup.from.lon),
                        new Location(travelGroup.to.lat, travelGroup.to.lon), waypts);
                populateWaypts(travelGroup, travelGroup.encodedPath);
                return;
            }

            TravelGroup tg = TravelGroupDAO.get(travelGroup._id);
            int passenger_size = tg.passengers == null ? 0 : tg.passengers.size();
            int new_passenger_size = travelGroup.passengers == null ? 0 : travelGroup.passengers.size();

            if(tg != null && tg.from != null && tg.from.lat != null && tg.from.lon != null && tg.to != null
                    && tg.to.lat != null && tg.to.lon != null && (!travelGroup.from.lat.equals(tg.from.lat) ||
                    !travelGroup.from.lon.equals(tg.from.lon) || !travelGroup.to.lat.equals(tg.to.lat)
                    || !travelGroup.to.lat.equals(tg.to.lat))){

                travelGroup.encodedPath = DirectionService.getPolylineEncoded(new Location(travelGroup.from.lat, travelGroup.from.lon),
                        new Location(travelGroup.to.lat, travelGroup.to.lon), waypts);
                populateWaypts(travelGroup, travelGroup.encodedPath);
            }
            else if ( passenger_size != new_passenger_size ) {
                travelGroup.encodedPath = DirectionService.getPolylineEncoded(new Location(travelGroup.from.lat, travelGroup.from.lon),
                        new Location(travelGroup.to.lat, travelGroup.to.lon), waypts);
                populateWaypts(travelGroup, travelGroup.encodedPath);
            }
        }

        //populateWaypts();
    }

    private static void populateWaypts(TravelGroup travelGroup, String encodedPath) {
        List<Location> locs = DirectionService.decodePolyline(encodedPath);
        if(locs == null){
            locs = new LinkedList<>();
        }

        travelGroup.waypts = new LinkedList<>();
        travelGroup.waypts.add(new Location(travelGroup.from.lat, travelGroup.from.lon));

        // no decoded polyline path found
        if(locs.isEmpty()) {
            travelGroup.waypts.add(new Location(travelGroup.to.lat, travelGroup.to.lon));
            return;
        }

        Location pivot = travelGroup.waypts.get(0);
        while(!locs.isEmpty()) {
            if(Util.getDistance(pivot, locs.get(0)) >= 2d) {
                pivot = locs.get(0);
                travelGroup.waypts.add(pivot);
            }
            locs.remove(0);
        }

        travelGroup.waypts.add(new Location(travelGroup.to.lat, travelGroup.to.lon));
    }

    private static Location[] prepareWayPts(TravelGroup tg) {

        if(tg.passengers == null || tg.passengers.isEmpty())
            return null;

        Location[] waypts = new Location[tg.passengers.size()*2];
        List<Location> locs = new LinkedList<>();
        for(Passenger p : tg.passengers) {
            locs.add(p.pickup_location);
        }

        int index = 0;
        Location from = new Location(tg.from.lat, tg.from.lon);

        while(!locs.isEmpty()) {
            Location p = getClosest(locs, from);
            waypts[index] = p;
            locs.remove(p);
            from = p;
            index++;
        }

        for(Passenger p : tg.passengers) {
            locs.add(p.drop_location);
        }

        while(!locs.isEmpty()) {
            Location p = getClosest(locs, from);
            waypts[index] = p;
            from = p;
            locs.remove(p);
            index++;
        }

        return waypts;
    }

    private static Location getClosest(List<Location> passengers, Location from) {
        Location s = null;
        Double distance = 10000000d;
        for(Location p:passengers) {
            Double d = Util.getDistance(p.latitude, p.longitude, from.latitude, from.longitude);
            if (distance > d) {
                distance = d;
                s = p;
            }
        }
        return s;
    }

    public void deleteTravelGroup(TravelGroup travelGroup) {
        if (travelGroup != null) {
            TravelGroup tg = TravelGroupDAO.get(travelGroup._id);
            TravelGroupDAO.delete(travelGroup._id);
            TravelRequest.deleteByGroupId(travelGroup._id);
            if (tg != null && tg.passengers != null) {
                for (Passenger p : tg.passengers) {
                    Notification n = new Notification();
                    n.action = ACTION.TRAVEL_GRP_DELETED;
                    NotificationService.send(UserDAO.get(p.user_id), n);
                }
            }
        }
        notifySyncTravelGroup(travelGroup);
    }

    public void deleteTravelRequest(final TravelRequest travelRequest) {
        if (travelRequest != null) {
            TravelRequest.delete(travelRequest._id);
            List<TravelRequest> list = TravelRequest.deleteByRequestorId(travelRequest.requester_id);

            TravelGroup tg = TravelGroupDAO.get(travelRequest.travelGroup_id);
            if (tg != null && tg.passengers != null) {
                for (Passenger p : tg.passengers) {
                    if (travelRequest.requester_id.equals(p.user_id)) {
                        tg.passengers.remove(p);
                        try {
                            TravelGroupService.populateEncodedPath(tg);
                            TravelGroupDAO.update(tg);
                        } catch (DirtyUpdateException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }

            ExecutorUtil.executeNow(new Runnable() {
                @Override
                public void run() {
                    List<TravelGroup> tgs = fetchMatchingTravelGroups(travelRequest);
                    if (tgs != null || !tgs.isEmpty()) {
                        for (TravelGroup t : tgs) {
                            NotificationService.send(UserDAO.get(t._id), new Notification(ACTION.TRAVEL_REQ_SYNC));
                        }
                    }
                }
            });
        }
    }

    /**
     * On user sign up clean up his old data.
     * @param user_id
     */
    public void purgeForNewUser(String user_id) {
        Integer date = Util.getTodayTimeZeroInteger(Constants.timeZone);

        Trip trip = TripDAO.findOne(DBQuery.or(DBQuery.is("travelGroupId", user_id), DBQuery.is("passengers.user_id", user_id))
                .is("date", date).notEquals("status", 3));

        if (trip != null){
            if(trip.travelGroupId.equals(user_id)) {
                trip.status = 3;
                TripDAO.updateHard(trip);

                TravelGroupDAO.delete(trip.travelGroupId);

                if (trip.passengers != null) {
                    for (Passenger p : trip.passengers) {
                        Notification n = new Notification();
                        n.trip = new AppTrip(trip);
                        n.action = ACTION.TRAVEL_GRP_JOURNEY_STOP;
                        n.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
                        NotificationService.send(UserDAO.get(p.user_id), n);
                    }
                }
            } else {
                Passenger p = trip.getPassenger(user_id);
                trip.passengers.remove(p);
                TripDAO.updateHard(trip);
//                Notification n = new Notification();
//                n.trip = new AppTrip(trip);
//                n.action = ACTION.UPDATE_TRIP;
//                n.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
//                NotificationService.send(UserDAO.get(p.user_id), n);
            }
        }
        else {
            TravelGroup travelGroup = TravelGroupDAO.get(user_id);
            if(travelGroup != null) {
                deleteTravelGroup(travelGroup);
            }
            else {
                TravelRequest travelRequest = TravelRequest.findOne(DBQuery.is("requester_id", user_id).notExists("travelGroup_id"));
                if (travelRequest == null)
                    deleteTravelRequest(travelRequest);
            }
        }
    }
}
