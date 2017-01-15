package com.mbrdi.anita.travel.controller;

import com.mbrdi.anita.basic.exception.DirtyUpdateException;
import com.mbrdi.anita.basic.model.ServerAppRequest;
import com.mbrdi.anita.basic.model.ServerAppResponse;
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
import com.mbrdi.anita.travel.service.TravelGroupService;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import org.mongojack.DBQuery;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.mbrdi.anita.basic.util.Util.*;


public class
TravelGroupAppController extends Controller {

    @Inject
    FormFactory formFactory;

    @Inject
    TravelGroupService travelGroupService;

//    public Result saveTravelRequest() {
//        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
//        ServerAppResponse response = new ServerAppResponse("success");
//
//        if (request.travelRequest.version == null)
//            request.travelRequest.save();
//        else
//            request.travelRequest.saveUpdate();
//
//        response.travelRequest = request.travelRequest;
//        return ok(Json.toJson(response).toString());
//    }

    public Result initData(String user_id) {

        TravelStatus t = new TravelStatus();
        Integer date = Util.getTodayTimeZeroInteger(Constants.timeZone);

        Trip trip = TripDAO.findOne(DBQuery.or(DBQuery.is("travelGroupId", user_id), DBQuery.is("passengers.user_id", user_id))
                .is("date", date).notEquals("status", 3));

        if (trip != null){
            t.trip = new AppTrip(trip);
        }
        else {
            t.travelGroup = TravelGroupDAO.get(user_id);
            if (t.travelGroup == null) {
                t.travelRequest = TravelRequest.findOne(DBQuery.is("requester_id", user_id).notExists("travelGroup_id"));
                if (t.travelRequest == null)
                    t.travelRequest = TravelRequest.findOne(DBQuery.is("requester_id", user_id));
            }
        }
        ServerAppResponse response = new ServerAppResponse("success");
        response.travelStatus = t;
        return ok(Json.toJson(response));
    }

    public Result allCoPassengers() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);

        ServerAppResponse response = new ServerAppResponse("success");
        response.travelRequests = travelGroupService.viewCoPassengers(request.travelRequest);

        response.travelRequests.add(request.travelRequest);
        response.travelRequests.add(request.travelRequest);
        response.travelRequests.add(request.travelRequest);

        return ok(Json.toJson(response).toString());
    }

    public Result updateTravelRequest(String from_user_id) {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        request.travelRequest.saveUpdate();
        Notification n = new Notification();
        n.action = ACTION.TRAVEL_REQ_UPDATE;
        n.travelRequest = request.travelRequest;
        User u = null;
        TravelGroup tg = TravelGroupDAO.get(request.travelRequest.travelGroup_id);
        if (from_user_id.equals(request.travelRequest.requester_id))
            u = UserDAO.get(tg.owner_id);
        else
            u = UserDAO.get(from_user_id);
        NotificationService.send(u, n);

        ServerAppResponse response = new ServerAppResponse("success");
        response.travelRequest = request.travelRequest;

        return ok(Json.toJson(response).toString());
    }

    public Result deleteTravelRequest() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        travelGroupService.deleteTravelRequest(request.travelRequest);
        return ok(Json.toJson(new ServerAppResponse("success")));
    }

    public Result saveTravelGroup() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        if(request.travelGroup.time == null) {
            request.travelGroup.time = Util.getCurrentTime(Constants.timeZone);
        }
        response.travelGroup = travelGroupService.saveTravelGroup(request.travelGroup);
        response.travelRequests = travelGroupService.fetchMatchingTravelRequest(response.travelGroup);

        if (response.travelRequests != null && !response.travelRequests.isEmpty()) {
            populateTravelRequestsForOwner(response.travelGroup, response.travelRequests);
            for (TravelRequest t : response.travelRequests) {
                NotificationService.send(UserDAO.get(t.requester_id), new Notification(ACTION.TRAVEL_GRP_SYNC));
            }
        }
        return ok(Json.toJson(response));
    }

    private void populateTravelRequestsForOwner(TravelGroup travelGroup, List<TravelRequest> travelRequests) {

        for (TravelRequest tr : travelRequests) {
            Location from = new Location(travelGroup.from.lat, travelGroup.from.lon);
            Location to = new Location(travelGroup.to.lat, travelGroup.to.lon);
            Location p_from = new Location(tr.from.lat, tr.from.lon);
            Location p_to = new Location(tr.to.lat, tr.to.lon);

            Double[] val1 = LocationService.getDistanceTimeOnlyByAPI(from, p_from);
            Double[] val2 = LocationService.getDistanceTimeOnlyByAPI(p_from, p_to);
            Double[] val3 = LocationService.getDistanceTimeOnlyByAPI(p_to, to);
            Double[] val4 = LocationService.getDistanceTimeOnlyByAPI(from, to);
            tr.detour_dist = roundCurrency(subtract(sum(val1[0], val2[0], val3[0]), val4[0]));
            tr.detour_time = roundCurrency(subtract(sum(val1[1], val2[1], val3[1]), val4[1]));

            tr.detourPath = DirectionService.getPolylineEncoded(from, to, p_from, p_to);
        }

        Comparator<TravelRequest> c = (p, o) -> (
                p.approved == null && p.detour_dist != null ? p.detour_dist.compareTo(o.detour_dist) : (p.approved != null ? -1 : 1));

        Collections.sort(travelRequests, c);
    }


    public Result saveTravelReqestNViewOptions() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        if(request.travelRequest.time == null) {
            request.travelRequest.time = Util.getCurrentTime(Constants.timeZone);
        }
        if (request != null && request.travelRequest != null && request.travelRequest.to != null) {
            request.travelRequest.saveUpdate();
        }

        ServerAppResponse response = new ServerAppResponse("success");
        if (request.travelRequest != null && request.travelRequest._id != null)
            response.travelRequest = request.travelRequest;

        TravelGroup travelGroup = TravelGroupDAO.getByPassenger(request.travelRequest.requester_id);

        if (travelGroup != null) {
            Notification n = new Notification();
            n.action = ACTION.TRAVEL_REQ_UPDATE;
            n.travelRequest = response.travelRequest;
            NotificationService.send(UserDAO.get(travelGroup.owner_id), n);

            response.travelGroup = travelGroup;
            travelGroupService.populateDetourDistTimeInTravelGroup(travelGroup, response.travelRequest);

        } else if(request != null && request.travelRequest != null){

            response.travelGroups = travelGroupService.fetchMatchingTravelGroups(request.travelRequest);

            for (TravelGroup tg : response.travelGroups) {
                if (TravelRequest.exists(request.travelRequest.requester_id, tg._id))
                    tg.requested = true;

                tg.currentLocation = travelGroupService.requestLocation(tg.owner_id);
                travelGroupService.populateDetourDistTimeInTravelGroup(tg, response.travelRequest);
                NotificationService.send(UserDAO.get(tg.owner_id), new Notification(ACTION.TRAVEL_REQ_SYNC));
            }
        }

        return ok(Json.toJson(response));
    }

//    public Result fetchTravelRequestMatches() {
//        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
//
//        ServerAppResponse response = new ServerAppResponse("success");
//        TravelGroup travelGroup = TravelGroupDAO.getByPassenger(request.travelRequest.requester_id);
//
//        if(travelGroup != null) {
//            Notification n = new Notification();
//            n.action = ACTION.TRAVEL_REQ_UPDATE;
//            n.travelRequest = response.travelRequest;
//            NotificationService.send(UserDAO.get(travelGroup.owner_id), n);
//
//            response.travelGroup = travelGroup;
//            //travelGroupService.populateDetourDistTimeInTravelGroup(travelGroup, response.travelRequest);
//
//        } else {
//
//            response.travelGroups = travelGroupService.fetchMatchingTravelGroups(request.travelRequest);
//
//            for(TravelGroup tg : response.travelGroups) {
//                if(TravelRequest.exists(request.travelRequest.requester_id, tg._id))
//                    tg.requested = true;
//
//                tg.currentLocation = travelGroupService.requestLocation(tg.owner_id);
//                //travelGroupService.populateDetourDistTimeInTravelGroup(tg, response.travelRequest);
//            }
//        }
//
//        return ok(Json.toJson(response));
//    }

    public Result fetchTravelGroupMatches() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        response.travelGroup = TravelGroupDAO.get(request.travelGroup._id);
        response.travelRequests = travelGroupService.fetchMatchingTravelRequest(response.travelGroup);
        populateTravelRequestsForOwner(request.travelGroup, response.travelRequests);
        return ok(Json.toJson(response));
    }


    public Result fetchTravelRequestMatches() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);

        ServerAppResponse response = new ServerAppResponse("success");
        TravelGroup travelGroup = TravelGroupDAO.getByPassenger(request.travelRequest.requester_id);
        response.travelRequest = request.travelRequest;

        if (travelGroup != null) {
            Trip trip = TripDAO.get(travelGroup.trip_id);
            if (trip != null) {
                response.trip = new AppTrip(trip);
            } else {
                response.travelGroup = travelGroup;
                travelGroupService.populateDetourDistTimeInTravelGroup(travelGroup, response.travelRequest);
            }
        } else {
            response.travelGroups = travelGroupService.fetchMatchingTravelGroups(request.travelRequest);
            for (TravelGroup tg : response.travelGroups) {
                if (TravelRequest.exists(request.travelRequest.requester_id, tg._id))
                    tg.requested = true;
                travelGroupService.populateDetourDistTimeInTravelGroup(tg, response.travelRequest);
            }
        }

        return ok(Json.toJson(response));
    }

    public Result joinTravelGroup() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        response.status = travelGroupService.joinTravelGroup(request.travelRequest, request.travelRequest.travelGroup_id) ? "success" : "error";

        return ok(Json.toJson(response));
    }

    public Result joinAddTravelGroup() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        TravelRequest tr = request.travelRequest;
        if (tr._id == null) {
            tr.save();
        }

        ServerAppResponse response = new ServerAppResponse("success");
        response.status = travelGroupService.joinTravelGroup(request.travelRequest, request.travelRequest.travelGroup_id) ? "success" : "error";
        response.travelRequest = tr;

        return ok(Json.toJson(response));
    }

    public Result acceptTravelRequest() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        TravelGroup tg = travelGroupService.acceptTravelGroupRequest(request.travelRequest, request.travelRequest.travelGroup_id);
        ServerAppResponse response = new ServerAppResponse("success");
        response.travelGroup = tg;
        response.travelRequests = travelGroupService.fetchMatchingTravelRequest(response.travelGroup);
        populateTravelRequestsForOwner(tg, response.travelRequests);
        return ok(Json.toJson(response));
    }


    public Result acceptTravelRequestAndUpdateTrip() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        TravelGroup tg = travelGroupService.acceptTravelGroupRequest(request.travelRequest, request.travelRequest.travelGroup_id);
        ServerAppResponse response = new ServerAppResponse("success");
        response.travelGroup = tg;
        response.travelRequests = travelGroupService.fetchMatchingTravelRequest(response.travelGroup);
        populateTravelRequestsForOwner(tg, response.travelRequests);

        response.trip = travelGroupService.deployTravelGroup(tg);
        response.status = response.trip != null ? "success" : "error";

        return ok(Json.toJson(response));
    }


    public Result deleteTravelGroup() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        travelGroupService.deleteTravelGroup(request.travelGroup);
        return ok(Json.toJson(new ServerAppResponse("success")));
    }

    public Result deployTravelGroup() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse();
        response.trip = travelGroupService.deployTravelGroup(request.travelGroup);
        response.status = response.trip != null ? "success" : "error";
        travelGroupService.notifySyncTravelGroup(request.travelGroup);
        return ok(Json.toJson(response));
    }

    public Result notifyTripEvent() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        travelGroupService.notifyTripEvent(request.tripEvent);
        return ok(Json.toJson(response));
    }


    public Result stopTravelGroupJourney() {

        ServerAppResponse response = new ServerAppResponse();
        response.status = "error";

        try {
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart<File> part = body.getFile("uploadedfile");
            File file = part.getFile();

            String json_data = decompress(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            ServerAppRequest request = Json.fromJson(Json.parse(json_data), ServerAppRequest.class);

            AppTrip trip = request.trip;

            if (trip != null) {
                Trip t = TripDAO.get(trip._id);

                Location.saveAllBlocking(request.locations);

                t.status = 3;
                TripDAO.updateHard(t);

                TravelGroupDAO.delete(t.travelGroupId);

                if (t.passengers != null) {
                    for (Passenger p : t.passengers) {
                        Notification n = new Notification();
                        n.trip = trip;
                        n.action = ACTION.TRAVEL_GRP_JOURNEY_STOP;
                        n.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
                        NotificationService.send(UserDAO.get(p.user_id), n);
                    }
                }
                response.status = "success";

            } else {
                response.status = "error";
                response.msg = "Invalid parameter";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok(Json.toJson(response).toString());
    }


    public Result myDetails(String user_id) {
//        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");

        response.travelRequest = TravelRequest.get(user_id);
        response.travelGroup = TravelGroupDAO.get(user_id);
        return ok(Json.toJson(response));
    }

}
