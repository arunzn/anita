package com.mbrdi.anita.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.travel.model.AppTrip;
import com.mbrdi.anita.travel.model.TravelGroup;
import com.mbrdi.anita.travel.model.TravelRequest;
import com.mbrdi.anita.travel.model.TripEvent;
import com.mbrdi.anita.user.model.AppUser;
import com.mbrdi.anita.user.model.DeviceDetail;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ServerAppRequest {
	
	public List<Location> locations;
    public AppUser user;
	public AppTrip trip;
    public Long time;
    public DeviceDetail deviceDetail;
    public TravelRequest travelRequest;
    public TravelGroup travelGroup;
    public TripEvent tripEvent;
    public String user_id;
}
