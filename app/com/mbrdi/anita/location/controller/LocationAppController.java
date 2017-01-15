package com.mbrdi.anita.location.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mbrdi.anita.basic.model.ServerAppRequest;
import com.mbrdi.anita.basic.model.ServerAppResponse;
import com.mbrdi.anita.basic.util.Json1MbParser;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.location.model.TripLocation;
import com.mbrdi.anita.location.service.DirectionService;
import com.mbrdi.anita.location.service.LocationService;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Singleton
public class LocationAppController extends Controller {

    /**
     * Accept data upto 1 MB
     * @return
     */
    @BodyParser.Of(value = Json1MbParser.class)
	public Result saveLocation() {

		JsonNode json_node = request().body().asJson();
		if (json_node == null) {
			return badRequest("Expecting Json data");
		}
		ServerAppRequest serverRequest = Json.fromJson(json_node, ServerAppRequest.class);
		ServerAppResponse res = new ServerAppResponse();
		Location location = serverRequest.locations != null && !serverRequest.locations.isEmpty() ? serverRequest.locations.get(0): null;

		if (location == null || location.user_id == null || location.latitude == null || location.longitude == null) {
			res.status = "error";
			res.msg = "Info is missing";
			return ok(Json.toJson(res).toString());
		} else {
			Location.saveAll(serverRequest.locations);
			res.status = "success";
			return ok(Json.toJson(res).toString());
		}
	}

    public Result saveBulkLocation() {

		ServerAppResponse response = new ServerAppResponse();
		response.status = "error";
		
		try {
			MultipartFormData body = request().body().asMultipartFormData();
			FilePart<File> filepart = body.getFile("uploadedfile");
			File file = filepart.getFile();

			String json_data = Util.decompress(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
			
			ServerAppRequest request = Json.fromJson(Json.parse(json_data), ServerAppRequest.class);
			
			if (request.locations != null && request.locations.size() > 0) {

				Location.saveAll(request.locations);
				response.status = "success";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ok(Json.toJson(response).toString());
	}

    public Result vehicleLocation() {

        ServerAppResponse sr = new ServerAppResponse();
        String vehicle_id = request().getQueryString("vehicle_id");
        if (vehicle_id != null) {
            sr.location = Location.getVehicleLocation(vehicle_id);
            sr.status = "success";
            if (sr.location == null) {
                sr.status = "error";
                sr.msg = "Location not found";
            }
        }
        return ok(Json.toJson(sr).toString());
    }

	public Result fetchLocation() {

		ServerAppResponse sr = new ServerAppResponse();

        String trip_id = request().getQueryString("trip_id");
        Long fromTime = Util.getLongValue(request().getQueryString("from_time"));

        String user_id = request().getQueryString("user_id");
        if (!Util.isNullOrEmpty(user_id)) {
            sr.location = Location.getUserLocation(user_id);
            sr.status = "success";
            if (sr.location == null) {
                sr.status = "error";
                sr.msg = "Location not found";
            }
            if(!Util.isNullOrEmpty(trip_id) ) {
                sr.locations = LocationService.purge(TripLocation.getAllByTripID(trip_id, fromTime));

                // REMOVE UNNECESSARY DATA TO REDUCE LOAD
                for(Location l:sr.locations) {
                    l.trip_id = l.user_id = l.trip_id = null;
                    l.speed = null;
                    l._id = null;
                }
            }
            return ok(Json.toJson(sr).toString());
        }

		return ok(Json.toJson(sr).toString());
	}

	public Result passengerLocation(String user_id) {

		ServerAppResponse sr = new ServerAppResponse();
		if (user_id != null) {
			sr.status = "success";
			sr.location = Location.getUserLocation(user_id);
			if (sr.location != null) {
				sr.status = "success";
			} else {
				sr.status = "error";
			}
		} else {
			sr.status = "error";
		}

		return ok(Json.toJson(sr).toString());
	}


    public Result getPathLocations(){
        JsonNode json_node = request().body().asJson();
        if (json_node == null) {
            return badRequest("Expecting Json data");
        }
        ServerAppRequest serverRequest = Json.fromJson(json_node, ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse();
        if(serverRequest.locations != null && serverRequest.locations.size() > 1) {
            Location l1 = serverRequest.locations.get(0);
            Location l2 = serverRequest.locations.get(1);
            response.locations = DirectionService.getPathLocations(l1, l2);
            response.status = "success";
        }

        return ok(Json.toJson(response));
    }

    public Result geoLoginRoute(String user_id, String type, String route_id){
        return ok("success");
    }
}
