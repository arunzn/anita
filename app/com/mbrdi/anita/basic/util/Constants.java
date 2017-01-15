package com.mbrdi.anita.basic.util;

import play.Play;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

public class Constants {
	
    public static final String GOOGLE_BROWSER_KEY = Play.application().configuration().getString("GOOGLE_BROWSER_KEY", "AIzaSyCFEQ2r56vEn21wSsbrWNQKcubkxyDTXCo");

    public static TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");

	public static Integer svnversion;
	
	public static final Integer PHONE_TYPE_ANDROID = 0;
	public static final Integer PHONE_TYPE_IPHONE = 1;
	public static final Integer PHONE_TYPE_WINDOWS = 2;
	public static final Integer USER_TYPE_USER = 0;
	public static final Integer USER_TYPE_DRIVER = 1;
	public static final Integer USER_TYPE_CORP = 2;
	public static final Integer USER_TYPE_ADMIN = 9;
	public static final Integer USER_TYPE_SCHOOL = 3;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	public static final Map<Integer, String> GenderMap = new HashMap<>();
	public static final Map<Integer, String> CorporationType = new HashMap<Integer, String>();
	public static final Map<Integer, String> SurveillanceInterval = new LinkedHashMap<Integer, String>();
	public static final Map<Integer, String> CountryMap = new HashMap<Integer, String>();
	public static final Map<Integer, String> StateMap = new LinkedHashMap<Integer, String>();
	public static final Map<Integer, String> CityMap = new HashMap<Integer, String>();
	public static final Map<Integer, Integer> YearMap = new LinkedHashMap<Integer, Integer>();
	public static final Map<Integer, Integer> MonthMap = new LinkedHashMap<Integer, Integer>();
	
	static{
		
		GenderMap.put(0, "Female");
		GenderMap.put(1, "Male");
		GenderMap.put(2, "Other");
		
		CorporationType.put(1, "Educational Institutions");
		CorporationType.put(2, "Company");
		CorporationType.put(3, "Tour Operators");



	}
	
}
