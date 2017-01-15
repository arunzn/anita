package com.mbrdi.anita.location.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoCodeResponse {
    public String status;
    public List<GResult> results;


    //=====================================================================================
    //======================      ADDITIONAL CLASSES        ===============================
    //=====================================================================================
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GResult {
        public List<GAddressComponent> address_components;
        public String formatted_address;
        public GPGeometry geometry;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GAddressComponent {
        public String long_name, short_name;
        public List<String> types;
    }
}
