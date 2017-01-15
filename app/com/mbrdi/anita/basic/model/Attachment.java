package com.mbrdi.anita.basic.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attachment {
	public Integer id;
	public String name;
	public String desc;
	public String url, by;
	public Long time;
}
