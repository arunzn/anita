package com.mbrdi.anita.message.model;

import com.amazonaws.services.devicefarm.model.Device;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.mongojack.ObjectId;

import javax.persistence.Id;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the message group. Each communication will be
 * represented by a group.<br>
 * <br>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MessageGroup {

	@Id
	@ObjectId
	public String _id;
	
	public String name;
	public FROM_TYPE type;

	public Set<String> devices = new LinkedHashSet<>();
}