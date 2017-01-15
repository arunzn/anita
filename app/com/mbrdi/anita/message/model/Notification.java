package com.mbrdi.anita.message.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.travel.model.AppTrip;
import com.mbrdi.anita.travel.model.TravelGroup;
import com.mbrdi.anita.travel.model.TravelRequest;
import com.mbrdi.anita.travel.model.TripEvent;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Notification {

    public Notification(){}

    public Notification (ACTION action){
        this.action = action;
    }


	public String header, content;
	public ACTION action;
	public Message message;
	public String corp_id;
	public AppTrip trip;
	public Map<String, String> extra;
    public Boolean ensure;
    public TravelRequest travelRequest;
    public TravelGroup travelGroup;
    public TripEvent tripEvent;
	public NOTIFICATION_TYPE type;


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((trip == null) ? 0 : trip.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notification other = (Notification) obj;
		if (action != other.action)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (trip == null) {
			if (other.trip != null)
				return false;
		} else if (!trip.equals(other.trip))
			return false;
		return true;
	}
	
	
}
