package com.mbrdi.anita.travel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mbrdi.anita.basic.model.ServerAppRequest;
import com.mbrdi.anita.basic.model.ServerAppResponse;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.AppTrip;
import com.mbrdi.anita.travel.model.Passenger;
import com.mbrdi.anita.travel.model.Rating;
import com.mbrdi.anita.travel.model.Trip;
import com.mbrdi.anita.travel.service.TripService;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import org.apache.commons.lang.time.DateUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * APP Controller For TRIP
 * 
 * @author Rohit
 *
 */
public class TripAppController extends Controller {

	/**
	 * OLD app version
	 *
	 * @param user_id
	 * @return list of trips where user has traveled as driver or passenger
	 */
	@Deprecated
	public Result travelHistory(String user_id) {

		User user = UserDAO.get(user_id);
		List<Trip> trips = new LinkedList<Trip>();
		Integer previousDate = Util.getIntDate(DateUtils.addDays(new Date(), -100));
		
		// trip history as passenger
		previousDate = Util.getIntDate(DateUtils.addDays(new Date(), -100));
		if (user != null) {
			if (trips != null) {
				trips.addAll(TripDAO.getPastTripByPassengerID(user._id, previousDate));
			} else {
				trips = TripDAO.getPastTripByPassengerID(user._id, previousDate);
			}
		}

		List<AppTrip> appTrips = new LinkedList<AppTrip>();

		for (Trip trip : trips) {
			Passenger p = trip.getPassenger(user_id);
			trip.passengers = new HashSet<>();
			trip.passengers.add(p);
			appTrips.add(new AppTrip(trip));
		}
		ServerAppResponse response = new ServerAppResponse("success");
		return ok(Json.toJson(appTrips).toString());
	}

	/**
	 * trip rating send by the user from app
	 *
	 * @param trip_id
	 * @param user_id
	 * @return
	 */
	public Result tripRating(String trip_id, String user_id) {

        ServerAppResponse response = new ServerAppResponse();
		try {
			JsonNode node = request().body().asJson();
			if (node != null) {
				Rating rating = Json.fromJson(node, Rating.class);
				TripService.markPassengerRating(trip_id, user_id, rating);
				response.status = "success";
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.status = "error";
		}

		return ok(Json.toJson(response));
	}

    public Result saveEvents() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        if (request != null && request.trip != null && request.trip.tripEvents != null) {
            Trip t = TripDAO.get(request.trip._id);
            if(t != null){
                t.tripEvents = request.trip.tripEvents;
                TripDAO.updateHard(t);
            }
        }

        ServerAppResponse response = new ServerAppResponse("success");
        return ok(Json.toJson(response));
    }

}
