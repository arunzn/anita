package com.mbrdi.anita.message.model;

public enum DEVICE_TYPE {
	
	ANDROID(0), IPHONE(1), WINDOWS(2);

	public int value;

	private DEVICE_TYPE(int value) {
		this.value = value;
	}
	
	public static DEVICE_TYPE get(Integer type){
        if(type == null)
            return null;
		switch (type){
			case 0:
				return DEVICE_TYPE.ANDROID;
			case 1:
				return DEVICE_TYPE.IPHONE;
			case 2:
				return DEVICE_TYPE.WINDOWS;
		}
		return null;
	}

}
