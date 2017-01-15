package com.mbrdi.anita.dashboard.controller;


import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.security.Authenticator;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.dashboard.service.DashboardService;
import com.mbrdi.anita.travel.dao.TravelGroupDAO;
import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.TravelGroup;
import com.mbrdi.anita.travel.model.TravelRequestAnalytics;
import com.mbrdi.anita.travel.model.Trip;
import com.mbrdi.anita.user.dao.UserDAO;
import org.mongojack.DBQuery;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.*;

@Security.Authenticated(Authenticator.class)
public class DashboardController extends Controller {

    public Result dashboard(){
        return ok(views.html.user.dashboard.render());
    }


    public Result getStatistics() {

        boolean isVendor = "true".equals(session("TYPE_FLEET"));
        Long time = (System.currentTimeMillis() - 86400000);

        Long total_reg_users = UserDAO.countAll();
        Long total_on_app_users = UserDAO.countAll(DBQuery.exists("device_id"));

        Long total_crash_free_users = 0l;

        Long average_session_length = 0l;

        Long total_crash_reported = 0l;

        DBQuery.Query query = DBQuery.is("", Util.getTodayTimeZeroInteger(TimeZone.getTimeZone("Asia/Kolkata")));
        List<TravelGroup> travelGroups = TravelGroupDAO.fetchAll(query);
        int rides_searched_by_users = DashboardService.totalPassengerGettingRides(travelGroups);

        Long no_of_users_offered_ride = new Long (travelGroups.size());

        Long no_of_users_searching_ride = 0l;

        Long total_searchers_without_match = 0l;

        Long time_betw_offer_match = 0l;

        List<Long> list = new LinkedList<Long>();
        list.add(total_reg_users);
        list.add(total_on_app_users);
        list.add(total_crash_free_users);
        list.add(average_session_length);
        list.add(total_crash_reported);
        list.add(no_of_users_offered_ride);
        list.add(no_of_users_searching_ride);
        list.add(total_searchers_without_match);
        list.add(time_betw_offer_match);

        return ok(Json.toJson(list));
    }

    // Trip page

    public Result trips(){
        if (session().isEmpty() == true)
            return redirect(com.mbrdi.anita.user.controller.routes.LoginController.login());

        return ok(views.html.trip.trips.render());
    }


    public Result tripData() {
        GridRequest gridRequest = Util.extractGridRequest(request());
        String corp_id = null;
        gridRequest.query = DBQuery.empty();
        List<DBQuery.Query> mandatoryChecks = new LinkedList<>();

        String start_date = null;
        if (gridRequest.filterColumns.contains("from_date")) {
            start_date = (String) gridRequest.filterValues.get(gridRequest.filterColumns.indexOf("from_date"));
            gridRequest.filterValues.remove(gridRequest.filterColumns.indexOf("from_date"));
            gridRequest.filterColumns.remove("from_date");
        }

        if (!Util.isNullOrEmpty(start_date) && !"NaN".equals(start_date)) {
            gridRequest.query = gridRequest.query.is("date", Integer.parseInt(start_date));
        }

        gridRequest.sidx = "date";
        gridRequest.sord = "desc";

        GridResponse<Trip> response = TripDAO.fetch(gridRequest);

        return ok(Json.toJson(response));
    }


    public Result downloadTrips() {

        String trip_type = Util.getFormValue(request(), "trip_type", "all");

        String st_date = Util.getFormValue(request(), "st_date");
        String ed_date = Util.getFormValue(request(), "ed_date");

        byte[] bytes = DashboardService.downloadTrips(trip_type, st_date, ed_date);

        response().setHeader("Content-disposition", "attachment; filename=trips.xlsx");

        return ok(bytes).as("application/vnd.ms-excel");
    }

    public Result analyticsSimple(Integer date) {
        if(session("user_id") == null)
            return forbidden();

        TravelRequestAnalytics t = TravelRequestAnalytics.getByDate(date);
        if(t != null) {
            t.totalLiftGivers = t.liftGivers != null ? t.liftGivers.size() : 0;
            t.totalOfferers = t.offerers != null ? t.offerers.size() : 0;
            t.totalPassenger = t.passengers != null ? t.passengers.size() : 0;
            t.totalRequesters = t.requesters != null ? t.requesters.size() : 0;

            HashSet users = new HashSet<>();
            if(t.requesters != null)
                users.addAll(t.requesters);
            if(t.offerers != null)
                users.addAll(t.offerers);

            HashSet activeUsers = new HashSet<>();
            if(t.passengers != null)
                activeUsers.addAll(t.passengers);
            if(t.liftGivers != null)
                activeUsers.addAll(t.liftGivers);

            t.totalUsers = users.size();
            t.totalActiveUsers = activeUsers.size();

            if(t.hourlyRequesters != null) {
                t.hourlyRequestersAnalysis = new HashMap<>();
                for(Integer key : t.hourlyRequesters.keySet()){
                    t.hourlyRequestersAnalysis.put(key +":00 -" + (key+1) +":00" , t.hourlyRequesters.get(key).size());
                }
                t.hourlyRequesters = null;
            }

            if(t.hourlyOfferers != null) {
                t.hourlyOfferersAnalysis = new HashMap<>();
                for(Integer key : t.hourlyOfferers.keySet()){
                    t.hourlyOfferersAnalysis.put(key +":00 -" + (key+1) +":00" , t.hourlyOfferers.get(key).size());
                }
                t.hourlyOfferers = null;
            }

            if(t.hourlyLiftGivers != null) {
                t.hourlyLiftGiversAnalysis = new HashMap<>();
                for(Integer key : t.hourlyLiftGivers.keySet()){
                    t.hourlyLiftGiversAnalysis.put(key +":00 -" + (key+1) +":00" , t.hourlyLiftGivers.get(key).size());
                }
                t.hourlyLiftGivers = null;
            }

            if(t.hourlyPassengers != null) {
                t.hourlyPassengerAnalysis = new HashMap<>();
                for(Integer key : t.hourlyPassengers.keySet()){
                    t.hourlyPassengerAnalysis.put(key +":00 -" + (key+1) +":00" , t.hourlyPassengers.get(key).size());
                }
                t.hourlyPassengers = null;
            }

            return ok(Json.toJson(t).toString());
        }

        return ok("No Activities done today");
    }
}
