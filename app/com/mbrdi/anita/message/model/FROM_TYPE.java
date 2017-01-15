package com.mbrdi.anita.message.model;

public enum FROM_TYPE {
	
	PAIR(0), GROUP(1), CORPORATE(2), SYSTEM(9);
	
	public int value;
	
	private FROM_TYPE(int value){
		this.value = value;
	}
	
	public static FROM_TYPE get(int value){
		if(value == 1)
			return GROUP;
		if(value == 2)
			return CORPORATE;
		if(value == 9)
			return SYSTEM;
		else
			return PAIR;
	}
}
