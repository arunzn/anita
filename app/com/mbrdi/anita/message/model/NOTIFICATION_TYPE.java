package com.mbrdi.anita.message.model;

public enum NOTIFICATION_TYPE {

	/**
	 * Completely Hidden
	 */
	HIDDEN,

	/**
	 * Shows Info without any alert
	 */
	SHOW_INFO, 
	
	/**
	 * Shows Message with Alert ( Bip sound or vibration)
	 */
	SHOW_ALERT_ONLY, 
	
	/**
	 * Shows Chat Message with alert sound or vibration. 
	 */
	SHOW_CHAT, 
	
	/**
	 * Shows Message with Link to Track Location.
	 */
	SHOW_LOCATION,
	
	/**
	 * Play alarm ringtone
	 */
	PLAY_ALARM,
	
	/**
	 * Play default rintone
	 */
	PLAY_RINGTONE;

}
