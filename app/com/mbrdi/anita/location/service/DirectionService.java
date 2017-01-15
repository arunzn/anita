package com.mbrdi.anita.location.service;

import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.model.Location;
import org.json.JSONArray;
import org.json.JSONObject;
import play.Play;

import java.util.ArrayList;
import java.util.List;

public class DirectionService {

    private static final String GOOGLE_SERVER_KEY = Play.application().configuration().getString("GOOGLE_SERVER_KEY");

    public static String getPolylineEncoded(List<Location> locations) {
        if(locations != null && locations.size() > 2) {
            Location from = locations.remove(0);
            Location to = locations.remove(locations.size()-1);
            String url = makeURL(from.latitude, from.longitude, to.latitude, to.longitude, locations.toArray(new Location[locations.size()]));
            String response = Util.sendHTTPRequest(url);
            return extractEncodedLocations(response);
        }
        return null;
    }

    public static String getPolylineEncoded(Location from, Location to, Location ...waypts) {
        if(from != null && to != null) {
             String url = makeURL(from.latitude, from.longitude, to.latitude, to.longitude, waypts);
            String response = Util.sendHTTPRequest(url);
            return extractEncodedLocations(response);
        }
        return null;
    }

    /**
     * Polyline line = mMap.addPolyline(PolylineOptions)
     */
    public static List<Location> getPathLocations(Location ...locations){
        if(locations != null && locations.length >1) {
            String url = makeURL(locations[0].latitude, locations[0].longitude, locations[1].latitude, locations[1].longitude);
            String response = Util.sendHTTPRequest(url);
            return extractLocations(response);
        }
        return null;
    }


    private static String makeURL (double sourcelat, double sourcelog, double destlat, double destlog, Location ...waypts ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));

        if(waypts != null && waypts.length > 0) {
            urlString.append("&waypoints=");
            int i = 0;
            for(Location l :waypts) {
                urlString.append(l.latitude + "," +l.longitude);
                i++;
                if(waypts.length > 1 && i < waypts.length)
                    urlString.append("%7C");// |
            }
        }
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=" + GOOGLE_SERVER_KEY);
        return urlString.toString();
    }

    private static String extractEncodedLocations(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            return overviewPolylines.getString("points");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<Location> extractLocations(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            return decodePolyline(encodedString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static List<Location> decodePolyline(String encoded) {

        List<Location> poly = new ArrayList<Location>();
        if(Util.isNullOrEmpty(encoded))
            return null;
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            Location p = new Location( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }
}

