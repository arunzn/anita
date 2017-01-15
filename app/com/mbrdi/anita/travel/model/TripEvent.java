package com.mbrdi.anita.travel.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mbrdi.anita.location.model.Location;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripEvent {

    public String trip_id, passenger_id;
    public Boolean completed;
    public Long time;
    public Boolean isPickUp;
    public Location location;
    public String address_line;
    public String path;

    public TripEvent(){}

    public TripEvent(Passenger p, boolean isPickUp){
        this.isPickUp = isPickUp;
        this.passenger_id = p.user_id;
        if(isPickUp) {
            this.address_line = p.address_line;
            this.location = p.pickup_location;
        }
        else {
            this.address_line = p.dest_address_line;
            this.location = p.drop_location;
        }
    }
}
