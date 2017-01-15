package com.mbrdi.anita.basic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtil {

    public static final SimpleDateFormat ZOYRIDE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public static final SimpleDateFormat UI_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static Date sqlDate(String date) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = null;
		try {
			d = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// java.sql.Date sqlDate = new java.sql.Date(d.getTime());
		return d;
	}
	
	public static TimeZone getTimeZone(String timeZone) {
		return TimeZone.getTimeZone(timeZone);
	}
	
	public static Date parseDate(String date, String timeZone) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(getTimeZone(timeZone));
		Date d = null;
		try {
			d = formatter.parse(date);
		} catch (ParseException e) {
			d = null;
			e.printStackTrace();
		}
		return d;
	}

    public static String formatDateTime(Long date, TimeZone tz){
        if(date == null)
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        formatter.setTimeZone(tz);
        String date_string = formatter.format(new Date(date));
        return date_string;
    }
	
	public static String formatDate(Long date, String timeZone){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(getTimeZone(timeZone));
		String date_string = formatter.format(new Date(date));
		return date_string;
	}
	
	public static String formatDate(Date date, String timeZone){
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(getTimeZone(timeZone));
		String date_string = formatter.format(date);
		return date_string;
	}
	
	public static String currentFormatDate(String timeZone){
		Date date = new Date(); 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setTimeZone(getTimeZone(timeZone));
		String date_string = formatter.format(date);
		return date_string;
	}
	
	/**
	 * This gives todays date without Hour, Minute, Second.
	 * @return
	 */
	public static Date getTodayOnly(TimeZone tz){
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.setTimeZone(tz);
		return c.getTime();
	}
	
	/**
	 * This return Date as per time zone.
	 * 
	 * @param timeZone
	 * @return
	 */
	public static Date getNow(String timeZone) {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone(timeZone));
		return c.getTime();
	}

	public static String HHmmfromDate(Long date){
		
		SimpleDateFormat ddMMYYFormatter = new SimpleDateFormat("HH-mm-ss");
		String time_string = ddMMYYFormatter.format(new Date(date));
		return time_string;
	}
	
	public static String HHmm2fromDate(Long date){
		
		SimpleDateFormat ddMMYYFormatter = new SimpleDateFormat("HHmm");
		String time_string = ddMMYYFormatter.format(new Date(date));
		return time_string;
	}

	public static int getYear(int date) {
		return date/10000;
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static int getDayOfYear(int date) {
		try {
		Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(""+date));
			return c.get(Calendar.DAY_OF_YEAR);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static int getYear(){
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR);
	}
	
	public static int getDayOfYear(){
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.DAY_OF_YEAR);
	}

    public static Long getTimeDifferenceInMinutes(Integer start_date, Integer end_date, Double start_time, Double end_time) {
        if(start_time == null || end_time == null)
            return 0l;
        if(end_date == null || end_date.equals(start_date)){
            end_date = start_date;
        }
        try {

            Calendar c1 = Calendar.getInstance();
            c1.setTime(ZOYRIDE_DATE_FORMAT.parse(start_date.toString()));
            c1.set(Calendar.HOUR_OF_DAY, start_time.intValue());
            c1.set(Calendar.MINUTE, Double.valueOf(start_time  * 100).intValue() % 100);

            Calendar c2 = Calendar.getInstance();
            c2.setTime(ZOYRIDE_DATE_FORMAT.parse(end_date.toString()));
            c2.set(Calendar.HOUR_OF_DAY, end_time.intValue());
            c2.set(Calendar.MINUTE, Double.valueOf(end_time  * 100).intValue() % 100);

            long diff = c2.getTimeInMillis() - c1.getTimeInMillis();
            return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0l;
    }
    
    public static String formatMinutes(Number mins) {
    	if(mins != null && mins.intValue() != 0){
            int hrs = (int) Math.floor(mins.intValue()/60);
            if(hrs == 0)
                return (mins.intValue() - (hrs * 60)) + " Mins";
            if(hrs == 1)
                return hrs + " Hr " + (mins.intValue() - (hrs * 60)) + " Mins";
            return hrs + " Hrs " + (mins.intValue() - (hrs * 60)) + " Mins";
	    }
    	return "";
    }

    public static String formatTime(Double mins) {
        if(mins != null && mins != 0){
            int hrs = (int) Math.floor(mins/60);
            int minutes = Double.valueOf(mins - (hrs * 60)).intValue();
            return hrs + " Hrs " + (minutes > 10 ? minutes : ("0" + minutes))+ " Mins";
        }
        return "";
    }

    public static Integer incrementDate(Integer date) {
        try{
            Date d = ZOYRIDE_DATE_FORMAT.parse(date.toString());
            d.setTime(d.getTime() + 86400000);
            return Integer.valueOf(ZOYRIDE_DATE_FORMAT.format(d));
        }catch (Exception e){
        }
        return null;
    }

    public static Integer decreaseDate(Integer date) {
        try{
            Date d = ZOYRIDE_DATE_FORMAT.parse(date.toString());
            d.setTime(d.getTime() - 86400000);
            return Integer.valueOf(ZOYRIDE_DATE_FORMAT.format(d));
        }catch (Exception e){
        }
        return null;
    }

    public static int getDayOfWeek(Integer date){
        if(date == null || date < 19470815)
            return -1;
        try{
            Calendar calendar = Calendar.getInstance();
            Date d = ZOYRIDE_DATE_FORMAT.parse(date.toString());
            calendar.setTime(d);
            return calendar.get(Calendar.DAY_OF_WEEK);
        }catch (Exception e){

        }
        return -1;
    }

    public static Long getTimeinMillis(Integer date, TimeZone timeZone) {
        try {
            Calendar c = Calendar.getInstance(timeZone);
            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.YEAR, date/ 10000);
            c.set(Calendar.MONTH, ((date/ 100)%100) -1 );
            c.set(Calendar.DATE, date%100);
            return c.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Long getTimeDifferenceInDays(Integer start_date, Integer end_date) {
        if(end_date == null || end_date.equals(start_date)){
            end_date = start_date;
        }
        try {

            Calendar c1 = Calendar.getInstance();
            c1.setTime(ZOYRIDE_DATE_FORMAT.parse(start_date.toString()));

            Calendar c2 = Calendar.getInstance();
            c2.setTime(ZOYRIDE_DATE_FORMAT.parse(end_date.toString()));

            long diff = c2.getTimeInMillis() - c1.getTimeInMillis();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer getDateFromNoOfDays(Integer date, int days) {
        return date + days;
    }

    public static String getDateInUI(Integer date) {
        if(date == null)
            return null;
        try{
            Date d = ZOYRIDE_DATE_FORMAT.parse(date.toString());
            return UI_DATE_FORMAT.format(d);
        } catch (Exception e){

        }
        return "";
    }

    public static Long getTime(Double time, Integer date, TimeZone tz) {
        if(time == null)
            return null;

        Calendar c = Calendar.getInstance(tz);
        c.set(Calendar.MILLISECOND, 0);
        if (date != null){
            c.set(Calendar.YEAR, date/ 10000);
            c.set(Calendar.MONTH, ((date/ 100)%100) -1 );
            c.set(Calendar.DATE, date%100);
        }
        c.set(Calendar.HOUR_OF_DAY, time.intValue());
        c.set(Calendar.MINUTE, Double.valueOf(time*100).intValue() % 100);
        c.set(Calendar.SECOND, 0);

        return c.getTimeInMillis();
    }

    public static Long getTime(Double time, Integer date, TimeZone tz, Integer added_minutes) {
        Long t  = getTime(time, date, tz);
        return t + added_minutes *60000;
    }


    public static Integer addDays(Integer date, int days) {
        LocalDate dt = LocalDate.of(date/10000, (date%10000)/100, date%100 ).plusDays(days);
        return dt.getYear() * 10000 + dt.getMonthValue() * 100 + dt.getDayOfMonth();
    }



    public static String getLongToDate(Long date) {
        if(date == null)
            return "";
        TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(tz);
        return formatter.format(new Date(date));
    }
}
