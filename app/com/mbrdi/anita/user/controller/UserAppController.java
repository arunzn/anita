package com.mbrdi.anita.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.mbrdi.anita.basic.model.ReportBug;
import com.mbrdi.anita.basic.model.ServerAppRequest;
import com.mbrdi.anita.basic.model.ServerAppResponse;
import com.mbrdi.anita.basic.model.ValidationFailureException;
import com.mbrdi.anita.message.model.Email;
import com.mbrdi.anita.message.service.MessageService;
import com.mbrdi.anita.travel.service.TravelGroupService;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.AppUser;
import com.mbrdi.anita.user.model.User;
import com.mbrdi.anita.user.service.UserService;
import com.mbrdi.anita.vehicle.dao.VehicleDAO;
import com.mbrdi.anita.vehicle.model.Vehicle;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Date;


public class UserAppController extends Controller {

    @Inject
    TravelGroupService travelGroupService;

    public Result getUser(String user_id) {

        User user = UserDAO.get(user_id);
        ServerAppResponse sr = new ServerAppResponse();
        sr.user = new AppUser(user);

        sr.vehicle = VehicleDAO.getVehicleByOwner_id(user_id);

        sr.status = "success";
        return ok(Json.toJson(sr).toString());
    }

    public Result updateUser() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        if (request == null) {
            response.status = "error";
            return ok(Json.toJson(response).toString());

        } else {
            AppUser user = request.user;
            if(user != null)
                response.user = UserService.updateUser(user);
            else
                response.status = "error";
        }
        return ok(Json.toJson(response).toString());
    }

    public Result resendCode() {

        JsonNode json = request().body().asJson();
        ServerAppResponse response = new ServerAppResponse();
        String msg = "";
        if (json == null) {
            response.status = "error";
            return ok(Json.toJson(response).toString());

        } else {
            ServerAppRequest request = null;
            try {
                request = Json.fromJson(json, ServerAppRequest.class);
            } catch (RuntimeJsonMappingException e) {
                e.printStackTrace();
            }
            if (request != null) {
                User saved_user = UserDAO.getByEmail(request.user.email);
                boolean code_three_min_ago = ((saved_user != null && saved_user.code_sent != null ? saved_user.code_sent : 0) + (3 * 60 * 1000)) < (new Date().getTime());
                if (saved_user != null && saved_user.code_sent != null && code_three_min_ago) {

                    saved_user.code_sent = Double.valueOf(Math.random() * 1000000).longValue();
                    UserDAO.update(saved_user);
                    Email.sendForgotPasswordMail(saved_user, saved_user.code_sent.toString(), "AnITa Password");
                    response.status = "success";
                    response.msg = "Message sent";
                    return ok(Json.toJson(response).toString());
                } else {
                    response.status = "3minago";
                    return ok(Json.toJson(response).toString());
                }

            } else {
                response.status = "error";
                return ok(Json.toJson(response).toString());
            }
        }
    }


    /*public Result getCountries() {
        Map<Integer, String> countries = Country.getCountryMap();
        return ok(Json.toJson(countries).toString());
    }

    public Result getStates() {
        Integer country_id = Integer.parseInt(request().getQueryString("country"));
        Map<Integer, String> states = State.getStateMap(country_id);
        return ok(Json.toJson(states).toString());
    }

    public Result getCities() {
        Integer state_id = Integer.parseInt(request().getQueryString("state"));
        Map<Integer, String> cities = City.cityMapByStateID(state_id);
        return ok(Json.toJson(cities).toString());
    }

    public Result getAreas() {
        Integer city_id = Integer.parseInt(request().getQueryString("city"));
        Map<Integer, String> areas = Area.getAreaMapByCity(city_id);
        return ok(Json.toJson(areas).toString());
    }*/


    public Result getNotification(String user_id) {
        ServerAppResponse response = new ServerAppResponse("success");
        response.notifications = MessageService.getNotifications(user_id);
        return ok(Json.toJson(response));
    }

    public Result registerByEmail() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        User user = UserDAO.getByEmail(request.user.email);
        if (user != null) {
            user.code_sent = Double.valueOf(Math.random() * 1000000).longValue();
            UserDAO.update(user);
            Email.sendForgotPasswordMail(user, user.code_sent.toString(), "AnITa Password");
            return ok(Json.toJson(response));
        }
        if(request.user.email != null && request.user.email.toLowerCase().endsWith("@gmail.com") || request.user.email.toLowerCase().endsWith("@daimler.com")) {
            try {
                user = new User();
                user.email = request.user.email;
                user = UserService.addEmployee(user);
                if (user != null) {
                    user.code_sent = Double.valueOf(Math.random() * 1000000).longValue();
                    UserDAO.update(user);
                    Email.sendForgotPasswordMail(user, user.code_sent.toString(), "AnITa Password");
                    return ok(Json.toJson(response));
                }
            } catch (ValidationFailureException e) {
                e.printStackTrace();
            }
        }
        response = new ServerAppResponse("error");
        response.msg = "Invalid email id provided";
        return ok(Json.toJson(response));
    }

    public Result verifyByEmail() {
        ServerAppRequest request = Json.fromJson(request().body().asJson(), ServerAppRequest.class);
        ServerAppResponse response = new ServerAppResponse("success");
        User user = UserDAO.getByEmail(request.user.email);
        if (user != null && request.user.code_sent != null && request.user.code_sent.equals(user.code_sent)) {
            user.code_sent = null;
            UserDAO.update(user);
            response.user = new AppUser(user);

            travelGroupService.purgeForNewUser(user._id);
            return ok(Json.toJson(response));
        }
        return ok(Json.toJson(new ServerAppResponse("error")));
    }

    public Result reportBug() {

        JsonNode jsonNode = request().body().asJson();
        if (jsonNode == null) {
            return badRequest("Expecting Json data");
        } else {
            ReportBug reportBug = Json.fromJson(jsonNode, ReportBug.class);

            reportBug.save();
            return ok("success");
        }

    }

}
