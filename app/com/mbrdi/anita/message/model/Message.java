package com.mbrdi.anita.message.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.mongojack.ObjectId;

import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Message {

	@Id
	@ObjectId
	public String _id;
	
	public String msg_group_id;
	public String text;
	public String header;
	public String from_name;
	public String from_id;
	public FROM_TYPE from_type;
	public Long sent;

	public MessageGroup messageGroup;
	
	public Message() {
	}
	
	public Message(String msg_group_id) {
		this.msg_group_id = msg_group_id;
	}

}
