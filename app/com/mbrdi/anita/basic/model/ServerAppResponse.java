package com.mbrdi.anita.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.message.model.ACTION;
import com.mbrdi.anita.message.model.Message;
import com.mbrdi.anita.message.model.Notification;
import com.mbrdi.anita.travel.model.AppTrip;
import com.mbrdi.anita.travel.model.TravelGroup;
import com.mbrdi.anita.travel.model.TravelRequest;
import com.mbrdi.anita.travel.model.TravelStatus;
import com.mbrdi.anita.user.model.AppUser;
import com.mbrdi.anita.vehicle.model.Vehicle;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ServerAppResponse {


    public ServerAppResponse(){}

    public ServerAppResponse(String status){
        this.status = status;
    }

    public Boolean updateApp;
	public String status;
	public String msg;
	public AppUser user;
	public Location location;
    public List<Location> locations;
	public ACTION action;
	public List<Message> msgs;
	public AppTrip trip;
	public String trip_id;
	public Map<Integer, String> picList;
	public Integer hasNotification;
	public Set<Notification> notifications;
	public Boolean onJourney;
	public Vehicle vehicle;

    public TravelRequest travelRequest;
    public TravelGroup travelGroup;
	public List<TravelRequest> travelRequests;
    public List<TravelGroup> travelGroups;
	public List<AppTrip> trips;
    public TravelStatus travelStatus;
}
