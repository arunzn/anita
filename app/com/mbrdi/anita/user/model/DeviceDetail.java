package com.mbrdi.anita.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDetail {

    public Integer codeVersion;
    public String model;
    public String brand;
    public String device;
    public String manufacturer;
    public String display;
    public String dtype;
}
