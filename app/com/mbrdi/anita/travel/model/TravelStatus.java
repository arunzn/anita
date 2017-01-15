package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by AJ on 1/4/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelStatus {

    public AppTrip trip;
    public TravelGroup travelGroup;
    public TravelRequest travelRequest;
}
