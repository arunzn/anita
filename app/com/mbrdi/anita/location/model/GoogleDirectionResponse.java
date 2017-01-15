package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleDirectionResponse {

    public String status;
    public List<GWayPoint> geocoded_waypoints;
    public List<GRoute> routes;

    //=====================================================================================
    //======================      ADDITIONAL CLASSES        ===============================
    //=====================================================================================
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GWayPoint {
        public String geocoder_status, place_id;
        public List<String> types;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GRoute {
        public String summary;
        public List<GLeg> legs;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GLeg {
        public List<GStep> steps;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GStep {
        public String travel_mode;
        public GPLocation start_location, end_location;
        public GPolyline polyline;
        public GValue duration, distance;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GPolyline {
        public String points;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GValue {
        public Long value;
        public String text;
    }
}
