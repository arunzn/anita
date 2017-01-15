package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.user.model.Address;
import org.mongojack.ObjectId;

import javax.persistence.Id;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelGroup {

    @Id
    @ObjectId
    public String _id;

    public String trip_id;

    public String owner_id, vehicle_id, corp_id;
    public String owner_name, vehicle_name, vehicle_reg_no, corp_name;
    public Set<Passenger> passengers;
    public Address from, to;

    public Integer date, seats;
    public Double time;
    public Integer version;
    public Double detour_dist, detour_time;
    public String encodedPath;
    public TravelType travelType;
    public List<Location> waypts;

    public Boolean regular;
    public Boolean requested;

    /**
     * null = nothing
     * 1 = Formed
     * 2 = Started Journey
     * 3 = Completed
     * 4 = Cancelled
     */
    public Integer status;

    public Location currentLocation;
}
