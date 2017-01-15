package com.mbrdi.anita.basic.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.location.model.Location;
import org.apache.http.util.ByteArrayBuffer;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import play.Logger;
import play.Play;
import play.mvc.Http.Request;
import play.mvc.Http.Session;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

public class Util {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyMMdd");
	private static SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat dateFormat4 = new SimpleDateFormat("MM/dd/yyyy");
	private static SimpleDateFormat dateFormat5 = new SimpleDateFormat("yyyy-MM-dd HH.mm a");
	private static String GOOGLE_SERVER_KEY = Play.application().configuration().getString("GOOGLE_SERVER_KEY");

	private static final DecimalFormat df = new DecimalFormat("#.##");

	public static GridRequest extractGridRequest(Request request) {
		GridRequest gridRequest = new GridRequest();

		Map<String, String[]> querymap = new HashMap<>(request.queryString());

		Integer rows = getReqParam(querymap, "rows", 10);
		Integer page = getReqParam(querymap, "page", 1);

		gridRequest.rows = rows != null ? rows : 100;
		gridRequest.page = page != null ? page : 1;

		gridRequest.sidx = getReqParam(querymap, "sidx", "id");
		gridRequest.filterOp = getReqParam(querymap, "filterOp", "eq");
		gridRequest.sord = getReqParam(querymap, "sord", "asc");

		String[] filterColumns = new String[0];
		String[] filterValues = new String[0];

		if ("true".equalsIgnoreCase(getReqParam(querymap, "_search", "false"))) {
			Set<String> keySet = querymap.keySet();
			keySet.remove("sord");
			keySet.remove("page");
			keySet.remove("nd");
			keySet.remove("sidx");
			keySet.remove("_search");
			keySet.remove("rows");
			keySet.remove("filters");

			gridRequest = populateValueIn(gridRequest, querymap);

			filterColumns = new String[keySet.size()];
			filterValues = new String[keySet.size()];
			for (String key : keySet) {
				gridRequest.filterColumns.add(key);
				gridRequest.filterValues.add(querymap.get(key)[0]);
			}
		}

		return gridRequest;
	}

	private static <T> T getReqParam(Map<String, String[]> querymap, String key, T defaultValue) {
		if (querymap.get(key) != null && querymap.get(key)[0] != null)
			if (defaultValue instanceof Integer) {
				if("undefined".equals(querymap.get(key)[0])){
					return defaultValue;
				}
				return (T) new Integer(querymap.get(key)[0]);
			} else
				return (T) querymap.get(key)[0];

		return null;
	}

	private static GridRequest populateValueIn(GridRequest gridRequest, Map<String, String[]> reqMap) {
		Set<String> keySet = reqMap.keySet();
		if (keySet.contains("notinfield")) {
			if (reqMap.get("notinvalues") != null) {
				gridRequest.notinfield = reqMap.get("notinfield")[0];
				gridRequest.notinvalues = Util.removeLastOrFirstComma(reqMap.get("notinvalues")[0]);
			}
			keySet.remove("notinfield");
			keySet.remove("notinvalues");
		}
		if (keySet.contains("infield")) {

			gridRequest.infield = reqMap.get("infield")[0];
			gridRequest.invalues = Util.removeLastOrFirstComma(reqMap.get("invalues")[0]);

			keySet.remove("invalues");
			keySet.remove("infield");
			gridRequest.invalues = "".equals(gridRequest.invalues) ? "-1" : gridRequest.invalues;
		}
		if (keySet.contains("btwfield")) {
			gridRequest.btwfield = reqMap.get("btwfield")[0];
			gridRequest.btwvalues = Arrays.asList(Util.removeLastOrFirstComma(reqMap.get("btwvalues")[0]).split(","));
			keySet.remove("btwfield");
			keySet.remove("btwvalues");
		}

		if (keySet.contains("ltfield")) {
			gridRequest.ltfield = reqMap.get("ltfield")[0];
			gridRequest.ltvalue = reqMap.get("ltvalue")[0];
			keySet.remove("ltfield");
			keySet.remove("ltvalue");
		}
		if (keySet.contains("gtfield")) {
			gridRequest.gtfield = reqMap.get("gtfield")[0];
			gridRequest.gtvalue = reqMap.get("gtvalue")[0];
			keySet.remove("gtfield");
			keySet.remove("gtvalue");
		}
		return gridRequest;
	}

	public static void processConstraintViolationException(BindingResult result, ConstraintViolationException e) {
		Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
		for (ConstraintViolation v : violations) {
			result.addError(new FieldError(result.getObjectName(), v.getPropertyPath().toString(), v.getMessage()));
		}
	}

	public static String removeLastOrFirstComma(String str) {
		if (str != null) {
			str = str.trim();
			if (str.endsWith(","))
				str = str.substring(0, str.length() - 1);
			if (str.startsWith(","))
				str = str.substring(1, str.length());
		}
		return str;
	}

	public static Set<String> csvToSet(String csv) {
		Set set = new HashSet();
		String[] sa = csv.split(",");
		for (String s : sa)
			set.add(s);

		return set;
	}

	public static Long longValue(String value) {
        if(value == null)
            return null;
		try {
			return new Long(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Integer intValue(String value) {
	    if(value == null)
	        return null;
		try {
			return new Integer(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Double doubleValue(String value) {
        if(value == null)
            return null;
		try {
			return new Double(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String shuffleNo(String ori_no) {

		String no_array = "";
		if (ori_no.length() == 10) {
			no_array = "" + ori_no.charAt(6) + ori_no.charAt(8) + ori_no.charAt(2) + ori_no.charAt(5) + ori_no.charAt(1);
		}
		return no_array;
	}

	private static final List<String> prohibited_fields = Arrays.asList(new String[] { "id", "_id" });

	/**
	 * This will copy all fields value to target object, null and _id field
	 * values are omitted.
	 *
	 * @param fromObj
	 * @param toObj
	 */
    @Deprecated
	public static void smartCopy(Object fromObj, Object toObj) {

		if (fromObj == null || toObj == null)
			return;

		for (Field field : fromObj.getClass().getFields()) {
			if (prohibited_fields.contains(field.getName()))
				continue;

			if (Modifier.isFinal(field.getModifiers()))
				continue;

			try {
				if (field.get(fromObj) != null) {

					if (Arrays.asList(field.getAnnotations()).contains(JsonIgnore.class))
						continue;
					Field toField = toObj.getClass().getField(field.getName());
					toField.set(toObj, field.get(fromObj));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * This will copy all fields value to target object, provided the field
	 * names are same.
	 *
	 * @param fromObj
	 * @param toObj
	 */
	public static void hardCopy(Object fromObj, Object toObj) {

		if (fromObj == null || toObj == null)
			return;

		for (Field field : fromObj.getClass().getFields()) {

			if (Modifier.isFinal(field.getModifiers()))
				continue;

			try {
				if (field.get(fromObj) != null) {

					if (Arrays.asList(field.getAnnotations()).contains(JsonIgnore.class))
						continue;

					Field toField = toObj.getClass().getField(field.getName());
					if (toField != null) {
						toField.set(toObj, field.get(fromObj));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String OS = null;

	public static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	public static Map<String, String> getAllFilesInDirectory(String directory) {

		Map<String, String> files = new HashMap<String, String>();
		File folder = new File(directory);

		try {
			File[] listOfFiles = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return (name != null && (name.endsWith(".json")));
				}
			});
			int i = 1;
			if (listOfFiles != null)
				for (File file : listOfFiles) {
					if (file.isFile()) {

						if (isWindows()) {
							files.put(file.getName(), directory + "\\" + file.getName());
						} else {
							files.put(file.getName(), directory + "/" + file.getName());
						}
					}
				}

			return files;
		} catch (Exception e) {
			e.printStackTrace();
			return files;
		}
	}


	public static Double getTimeInDouble(double hour, double min) {
		return hour + (min / 100d);
	}

	public static Integer getDifferenceTimeInMin(Double time1, Double time2) {

		Integer min1 = getFloatTimeInMin(time1);
		Integer min2 = getFloatTimeInMin(time2);

		if (min1 == null || min2 == null)
			return null;
		return min1 - min2;
	}

	private static Integer getFloatTimeInMin(Double time) {

		if (time != null)
			return null;

		Double hours = Math.round(time) * 1.0d;
		Double min = (time - hours) * 100d;

		return (int) Math.round(min);
	}

	public static double getAddedTime(double time, int add_min) {
		String time_st = String.format("%.2f", time);
		time_st = time_st.replace(".", "@");
		String[] time_arr = time_st.split("@");
		String hour = time_arr[0];
		String min = time_arr[1];
		if (!min.startsWith("0") && min.length() == 1) {
			min = min + "0";
		}

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
		cal.set(Calendar.MINUTE, Integer.valueOf(min));

		cal.add(Calendar.MINUTE, add_min);
		Integer final_hour = cal.get(Calendar.HOUR_OF_DAY);
		Integer fianl_min = cal.get(Calendar.MINUTE);
		return getTimeInDouble(final_hour.doubleValue(), fianl_min.doubleValue());
	}

    public static double subtractMinutes(double time, int minutes, TimeZone tz) {
        String time_st = String.format("%.2f", time);
        time_st = time_st.replace(".", "@");
        String[] time_arr = time_st.split("@");
        String hour = time_arr[0];
        String min = time_arr[1];
        if (!min.startsWith("0") && min.length() == 1) {
            min = min + "0";
        }

        Calendar cal = Calendar.getInstance(tz);
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        cal.set(Calendar.MINUTE, Integer.valueOf(min));

        cal.add(Calendar.MINUTE, minutes * -1);
        Integer final_hour = cal.get(Calendar.HOUR_OF_DAY);
        return getTimeInDouble(final_hour.doubleValue(), cal.get(Calendar.MINUTE));
    }

	public static Date getTodayTimeZero(TimeZone tz) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(tz);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static Date getTodayTime(TimeZone tz) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(tz);
		return cal.getTime();
	}

	public static int getTomorrowdateTimeZero(TimeZone tz) {
		Calendar cal = Calendar.getInstance(tz);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DATE);
	}

	public static Double getCurrentTime(TimeZone tz) {
		Calendar cal = Calendar.getInstance(tz);
		Integer hour = cal.get(Calendar.HOUR_OF_DAY);
		Integer min = cal.get(Calendar.MINUTE);
		return getTimeInDouble(hour.floatValue(), min.floatValue());
	}

    public static Integer getCurrentHour(TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

	/*
	 * ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	 * ::
	 */
	/* :: : */
	/* :: This routine calculates the distance between two points (given the : */
	/* :: latitude/longitude of those points). : */
	/* :: : */
	/* :: Definitions: : */
	/* :: South latitudes are negative, east longitudes are positive : */
	/* :: : */
	/* :: unit = 'M' is statute miles (default) : */
	/* :: K = M * 1.609344 'K' is kilometers : */
	/*
	 * ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	 * ::
	 */

	public static Double distanceBetween2GeoPoints(Location location, Location location2) {
		if (location == null || location2 == null || location.latitude == null || location.longitude == null || location2.latitude == null
				|| location2.longitude == null)
			return null;

		if (Math.abs(Util.subtract(location.latitude, location2.latitude) + Math.abs(Util.subtract(location.longitude, location2.longitude))) < 0.0000003)
			return 0d;

		double theta = location.longitude - location2.longitude;
		double dist = Math.sin(deg2rad(location.latitude)) * Math.sin(deg2rad(location2.latitude)) + Math.cos(deg2rad(location.latitude))
				* Math.cos(deg2rad(location2.latitude)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 111189.57696 /* 60 * 1.1515 * 1.609344 * 1000 */; // Dist in Meters
		return Math.abs(dist);

	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public static Date toDateObject(String date, TimeZone tz) {
		Date date_obj = null;
		try {
			date_obj = dateFormat.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(tz);
		cal.setTime(date_obj);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}
	
	/**
	 * if date is in the formate mm/dd/yyy or mm-dd-yyyy
	 * change date to integer formate
	 * @param date
	 * @return
	 */
	public static Integer toDateInteger(String date) {
		if (date == null || "".equals(date))
			return null;
		try {
			if(date.contains("/")){
				return Integer.parseInt(date.replaceAll("/", ""));
			}
			if(date.contains("-")){
				return Integer.parseInt(date.replaceAll("-", ""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	public static int[] getHourMin(Double a) {
		int[] x = new int[2];
		String f1_st = a.toString().replace(".", "-");
		String[] a_st = f1_st.split("-");
		if (a_st.length == 0) {
			x[0] = 0;
			x[1] = 0;
		} else if (a_st.length == 1) {
			x[0] = Integer.valueOf(a_st[0]);
			x[1] = 0;
		} else if (a_st.length == 2) {
			x[0] = Integer.valueOf(a_st[0]);
			if (a_st[1].length() == 1 && a_st[1].charAt(0) != '0') {
				x[1] = Integer.valueOf(a_st[1] + "0");
			} else {
				try{
					x[1] = Integer.valueOf(a_st[1]);
				}catch(NumberFormatException ex){ // handle your exception
					ex.printStackTrace();
				}

			}

		}

		return x;
	}

	public static int minutesBetweenTimes(Double start, Double end) {

		int[] start_arr = getHourMin(start);
		int[] end_arr = getHourMin(end);

		int start_mins = (start_arr[0] * 60) + (start_arr[1]);
		int end_mins = (end_arr[0] * 60) + (end_arr[1]);

		return end_mins - start_mins;
	}

	public static double hourdotmin(int minutes) {

		int hours = minutes / 60;
		int min = minutes - (hours * 60);
		double mins = (double) min / 100d;
		double result = hours + mins;
		return result;
	}

	public static String bookingIdDateFormat(TimeZone tz) {
		return dateFormat2.format(Calendar.getInstance(tz).getTime());
	}

	public static Calendar getCalandarTime(Integer month, Integer year, TimeZone tz) {
		Calendar c = Calendar.getInstance(tz);
		c.set(Calendar.DATE, 1);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	// get date in yyyy-MM-dd format
	public static String getDateString(Date date) {

		return dateFormat.format(date);
	}

	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	static {
		numberFormat.setMaximumFractionDigits(2);
	}

	public static double sum(Double... amounts) {
		double amount = 0;	for (Double amt : amounts)
			if (amt != null) {
				amount += amt;
			}
		return amount;
	}

    public static double sum(Double amt, Number ... amounts) {
        double amount = amt != null ? amt : 0d;
        for (Number a : amounts)
            if (a != null) {
                amount += a.doubleValue();
            }
        return amount;
    }

	public static float sumFloat(Float... floats) {
		Float amount = 0f;
		for (Float amt : floats)
			if (amt != null) {
				amount += amt;
			}
		return amount;
	}

	public static String getDateFromInteger(Integer date) {
		if (date == null)
			return "";
		return (date / 10000) + "-" + ((date % 10000) / 100) + "-" + (date % 100);
	}

	public static Integer getTodayTimeZeroInteger(TimeZone tz) {
		Calendar cal = Calendar.getInstance(tz);
		return (cal.get(Calendar.YEAR) * 10000) + (cal.get(Calendar.MONTH) * 100) + 100 + cal.get(Calendar.DATE);
	}


	public static Integer getPreviousdayTimeZeroInteger(TimeZone tz) {
		Calendar cal = Calendar.getInstance(tz);
		cal.setTimeInMillis(cal.getTimeInMillis() - 86400000);
		return (cal.get(Calendar.YEAR) * 10000) + (cal.get(Calendar.MONTH) * 100) + 100 + cal.get(Calendar.DATE);
	}

	public static Integer getTimeZeroInteger(TimeZone tz, Date date) {
		Calendar cal = Calendar.getInstance(tz);
		if (date != null)
			cal.setTime(date);
		return (cal.get(Calendar.YEAR) * 10000) + (cal.get(Calendar.MONTH) * 100) + 100 + cal.get(Calendar.DATE);
	}

	public static boolean isEmpty(List<String> _ids) {
		if (_ids == null || _ids.isEmpty())
			return true;
		Iterator<String> itr = _ids.iterator();
		while (itr.hasNext()) {
			String s = itr.next();
			if (s == null || "".equals(s))
				itr.remove();
		}

		if (_ids.isEmpty())
			return true;

		return false;
	}

	/**
	 * This returns start date and end date of current month in integer format.
	 * 
	 * @param tz
	 * @return
	 */
	public static Integer[] getFromToDateRange(TimeZone tz) {
		Calendar cal = Calendar.getInstance(tz);
		Integer[] returnVal = new Integer[2];
		returnVal[0] = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + 00;
		returnVal[1] = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + 32;
		return returnVal;
	}

	public static int dayDiffBwDates(Date from, Date to) {

		int diff = (int) (to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24);
		return diff;
	}

	public static Date getDateFrmInt(Integer date_int) {

		// date_int in yyyyMMdd
		try {
			return dateFormat3.parse(date_int.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static String currentDateToString(Date date) {
		return dateFormat3.format(date);
	}

	public static int getMaximumDaysInMonth(int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		return calendar.getActualMaximum(Calendar.DATE);
	}

	public static Double divide(Number numerator, Number denominator) {
		if (denominator == null || numerator == null || denominator.doubleValue() == 0)
			return 0d;

		return numerator.doubleValue() / denominator.doubleValue();
	}

	public static Map<Integer, String> monthConstants() {

		Map<Integer, String> month_const = new LinkedHashMap<Integer, String>();
		month_const.put(0, "Jan");
		month_const.put(1, "Feb");
		month_const.put(2, "Mar");
		month_const.put(3, "Apr");
		month_const.put(4, "May");
		month_const.put(5, "Jun");
		month_const.put(6, "Jul");
		month_const.put(7, "Aug");
		month_const.put(8, "Sep");
		month_const.put(9, "Oct");
		month_const.put(10, "Nov");
		month_const.put(11, "Dec");

		return month_const;
	}

	public static Map<Integer, String> yearlyMap(String timezone) {
		Map<Integer, String> mon_cnst = monthConstants();
		Map<Integer, String> monthmap = new LinkedHashMap<Integer, String>();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
		int in = cal.get(Calendar.MONTH);

		List<Integer> list = new LinkedList<Integer>();

		for (int i = 0; i < 12; i++)
			list.add(in++ % 12);

		for (int j : list) {
			monthmap.put(j, mon_cnst.get(j));
		}

		return monthmap;
	}


	public static Integer getIntDateFromExcelDate(String date) {
		Date date_obj = null;
		try {
			date_obj = dateFormat4.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String int_date = dateFormat3.format(date_obj);
		return Integer.valueOf(int_date);
	}

	public static Integer getIntDate(Date date) {
		return (date.getYear() + 1900) * 10000 + (date.getMonth() + 1) * 100 + date.getDate();
	}

	public static Integer getIntDate(String date) {
		Date d;
		try {
			d = dateFormat.parse(date);
			return (d.getYear() + 1900) * 10000 + (d.getMonth() + 1) * 100 + d.getDate();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Integer getIntDate(Long time, TimeZone tz) {
		Calendar c = Calendar.getInstance(tz);
		c.setTimeInMillis(time);
		return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
	}

	public static String durHHMMSS(int sec) {

		int h = sec / 3600;
		int ms = sec - (h * 3600);
		int m = ms / 60;
		int s = ms - (m * 60);

		String dur = h + ":" + m + ":" + s;
		return dur;
	}

	public static float percentage(int a, int b) {

		return Math.round((a * 10000f) / b) / 100f;
	}

	public static Long getDateInLong(String date) {

		Date date_obj = null;
		try {
			date_obj = dateFormat5.parse(date);
			return date_obj.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Double subtract(Double from_value, Number... subtract_amount) {
		if (from_value == null)
			from_value = 0d;
		for (Number n : subtract_amount) {
			if (n != null)
				from_value -= n.doubleValue();
		}
		return from_value;
	}

    public static Double subtract(Number from, Number... subtract_amount) {
        double from_value = from != null ? from.doubleValue() : 0d;
        for (Number n : subtract_amount) {
            if (n != null)
                from_value -= n.doubleValue();
        }
        return from_value;
    }

	public static Integer subtract(Integer from_value, Number subtract_amount) {
		if (from_value == null)
			from_value = 0;
		if (subtract_amount == null)
			subtract_amount = 0;
		return from_value - subtract_amount.intValue();
	}
	
	public static Double multiply(Number... values) {
		Double v = 1d;
		if (values != null && values.length > 0)
			for(Number n:values)
				if(n != null)
					v *= n.doubleValue();
				else
					return 0d;
		return v;
	}

	public static Integer sum(Integer... amounts) {
		int amount = 0;
		for (Integer amt : amounts)
			if (amt != null) {
				amount += amt;
			}
		return amount;
	}

	public static Integer getMinutes(Long time, TimeZone tz) {
		Calendar c = Calendar.getInstance(tz);
		c.setTimeInMillis(time);
		return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
	}

	public static Integer getMinutes(Float time) {
		if (time != null)
			return time.intValue() * 60 + (Float.valueOf(time * 100).intValue() % 100);

		return 999999999;
	}

	// Restrict number of digits after dcimal to 2
	public static Double getDouble(Double number) {
		DecimalFormat numberFormat = new DecimalFormat("#.00");
		return Double.parseDouble(numberFormat.format(number));
	}

	public static String calculateHash(String hashAlgorithm, String payload) {
		byte[] hashseq = payload.getBytes();
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest algorithm = MessageDigest.getInstance(hashAlgorithm);
			algorithm.reset();
			algorithm.update(hashseq);
			byte messageDigest[] = algorithm.digest();

			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append("0");
				hexString.append(hex);
			}

		} catch (NoSuchAlgorithmException nsae) {
		}

		return hexString.toString();
	}

	public static String unGzip(File file) {
		// StringBuffer fout = new StringBuffer();
		ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(0);
		try {
			GZIPInputStream zin = new GZIPInputStream(new FileInputStream(file));
			try {
				ZipEntry ze = null;
				while (zin.available() > 0) {
					for (int c = zin.read(); c != -1; c = zin.read()) {
						// fout.append(c);
						byteArrayBuffer.append(c);
					}
				}
			} finally {
				zin.close();
			}
		} catch (Exception e) {
			// Log.e(TAG, "Unzip exception", e);
		}
		return new String(byteArrayBuffer.buffer());
	}

	public static String unzip(File file) {
		// StringBuffer fout = new StringBuffer();
		ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(0);
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
			try {
				ZipEntry ze = null;
				while (zin.available() > 0) {
					for (int c = zin.read(); c != -1; c = zin.read()) {
						// fout.append(c);
						byteArrayBuffer.append(c);
					}
				}
			} finally {
				zin.close();
			}
		} catch (Exception e) {
			// Log.e(TAG, "Unzip exception", e);
		}
		return new String(byteArrayBuffer.buffer());
	}

	public static Float getFloatValue(String string) {
		try {
			return Float.parseFloat(string);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static Long getLongValue(String string) {
		try {
			return Long.parseLong(string);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static Double getDoubleValue(String string) {
		try {
			return Double.parseDouble(string);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static String unzip(String zipFilePath) {
		String data = "";
		try {
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			if (entry != null) {
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					data = extractFile(zipIn);
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	private static String extractFile(ZipInputStream zipIn) throws IOException {
		ByteArrayBuffer bos = new ByteArrayBuffer(1);
		byte[] bytesIn = new byte[4096];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.append(bytesIn, 0, read);
		}
		// bos.close();
		// JsonNode json = bos;
		return new String(bos.buffer());
	}

	public static Integer getuniqueRandNum(List<Integer> currentlist) {
		Random generator = new Random();
		Integer randNum = 100 + generator.nextInt(900);
		if (currentlist == null || currentlist.size() == 0 || !currentlist.contains(randNum)) {
            if(currentlist == null)
                currentlist = new LinkedList<>();
            currentlist.add(randNum);
			return randNum;
		} else {
            return getuniqueRandNum(currentlist);
        }
	}

	public static String getTimeformatFromDouble(Double time) {
		if (time == null)
			return "";
		int hr = time > 12 ? (time.intValue() - 12) : time.intValue();
		int min = Double.valueOf(time * 100).intValue() % 100;
		String ampm = time > 12 ? "PM" : "AM";
		return hr + ":" + (min > 10 ? min : ("0" + min)) + " " + ampm;
	}

	public static String decompress(byte[] bytes) {
		InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = in.read(buffer)) > 0)
				baos.write(buffer, 0, len);
			return new String(baos.toByteArray(), "UTF-8");
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public static byte[] compress(String text) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			OutputStream out = new DeflaterOutputStream(baos);
			out.write(text.getBytes("UTF-8"));
			out.close();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		return baos.toByteArray();
	}

	public static String getDateTimeFromTime(Long timeInLong, String timeZone) {
        if(timeInLong == null)
            return "";
		try {
			TimeZone tz = TimeZone.getTimeZone(timeZone);
			Calendar c = Calendar.getInstance(tz);
			c.setTimeInMillis(timeInLong);
			return dateFormat5.format(c.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getDateTimeFromTime(Long timeInLong, TimeZone timeZone) {
		if(timeInLong == null || timeZone == null)
			return "";
		try {
			Calendar c = Calendar.getInstance(timeZone);
			c.setTimeInMillis(timeInLong);
			return dateFormat5.format(c.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	public static String getNow(TimeZone tz) {
		try {
			Calendar c = Calendar.getInstance(tz);
			int hr;
			if((c.get(Calendar.AM_PM) == 0 ? "AM" : "PM") == "AM"){
				 hr = c.get(Calendar.HOUR);
			}
			else {
				 hr = (c.get(Calendar.HOUR) + 12);
			}
				String a = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + " " + hr + "." + c.get(Calendar.MINUTE) + " " + (c.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
			return a;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Double roundCurrency(Double value) {
		if (value == null)
			return null;
		return Math.round(value * 100) / 100d;
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || "".equals(str) ? true : false;
	}

    /**
     * Find distance between two geo points
     * @param loc1
     * @param loc2
     * @return distance between them
     */
    public static Double getDistance(Location loc1, Location loc2) {
        if(loc1 == null || loc2 == null)
            return null;

        return getDistance(loc1.latitude, loc1.longitude,loc2.latitude, loc2.longitude);
    }

	/**
	 * Find distance between two geo points
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance between them
	 */
	public static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if(lat1 == null || lon1 == null || lat2 == null || lon2 == null)
            return null;

		int R = 6371; // km
		double dLat = (lat2 - lat1) * Math.PI / 180;
		double dLon = (lon2 - lon1) * Math.PI / 180;
		lat1 = lat1 * Math.PI / 180;
		lat2 = lat2 * Math.PI / 180;

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;

		return d;
	}

	/**
	 * Call the url and send back the response received.
	 * @param url
	 * @return
	 */
	public static String sendHTTPRequest(String url) {
		
		StringBuilder result = new StringBuilder();
		
		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) new URI(url).toURL().openConnection();

			Logger.info("Response code for url", httpURLConnection.getResponseCode());
			
			Scanner scanner = null;

			if (httpURLConnection.getResponseCode() == 200) {
				scanner = new Scanner(httpURLConnection.getInputStream());
			}

			while (scanner.hasNext()) {
				result.append(scanner.next());
			}

			scanner.close();
			httpURLConnection.disconnect();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		} 
		
		return result.toString();
		
	}
	
	public static String getAdmin(Session session){
        String uname = session.get("user_name");
        String emp_no = session.get("emp_no");
        String dsg = session.get("designation");
        String dpt = session.get("department");
		return (uname != null ? (uname +"-") : "") + (emp_no != null ? (emp_no +"-") : "") + (dsg != null ? (dsg +"-"): "") + (dpt != null ? dpt: "");
	}

    public static String shorten(String value){
        if(value == null)
            return "";
        return value.length() > 10? value.substring(0, 10)  : value;
    }

    public static Long getTimeinMillis(Integer date) {
        if(date == null)
            return null;
        Date dt = null;
        try {
            dt = dateFormat3.parse(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dt.getTime();
    }


    public static Long getTimeinMillis(TimeZone tz, Integer date, Double time) {
        if(date == null)
            return null;
        try {
            Calendar c = Calendar.getInstance(tz);
            c.set(Calendar.YEAR, date/10000 );
            c.set(Calendar.MONTH, (date% 10000)/100 );
            c.set(Calendar.DATE, date% 100);

            c.set(Calendar.HOUR_OF_DAY, time.intValue());
            c.set(Calendar.MINUTE, Double.valueOf(time * 100d).intValue()%100);
            return c.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * returns back any date in mills format
     * @param someDate
     * @return
     */
    public static long getTimeinMillis(String someDate) {
    	Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy");
			date = sdf.parse(someDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date.getTime();
    }

    public static Integer getRoundInt(Double value){
        if(value == null)
            return null;
        return Math.toIntExact(Math.round(value));
    }

    public static String trim(String string, Integer length) {
        if(string == null || string.length() <= length)
            return string;
        return string.substring(0, length);
    }

    public static String getFormValue(Request request, String field, String ...defaultValue){
		String value = null;
        if(request != null && request.body() != null && request.body().asFormUrlEncoded() != null && request.body().asFormUrlEncoded().get(field) != null && !"".equals(request.body().asFormUrlEncoded().get(field)[0]))
			value =  request.body().asFormUrlEncoded().get(field)[0];

		if(Util.isNullOrEmpty(value) && defaultValue != null && defaultValue.length > 0)
			return defaultValue[0];
        return value;
    }

	public static String getMultipartFormValue(Request request, String field){
		if(request != null && request.body() != null && request.body().asMultipartFormData() != null && request.body().asMultipartFormData().asFormUrlEncoded().get(field) != null)
			return request.body().asMultipartFormData().asFormUrlEncoded().get(field)[0];
		return null;
	}

	public static Long toLong(String searchText) {
		try {
			return Long.parseLong(searchText);
		}catch (Exception e) {
			return null;
		}
	}

    public static Integer toInteger(String string) {
        try {
            return Integer.parseInt(string.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean getBoolean(String bool) {
        try {
            return Boolean.parseBoolean(bool.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

	public static String removeAllSpecialCharacters(String str) {
		if (str != null) {
			str = str.replaceAll("[-+^:,_]","");
		}
		return str;
	}

	public static String removeAllSpaces(String str) {
		if (str != null) {
			str = str.replaceAll(" ","");
		}
		return str;
	}

}
