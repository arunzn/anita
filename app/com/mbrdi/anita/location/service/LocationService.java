package com.mbrdi.anita.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mbrdi.anita.basic.service.StorageService;
import com.mbrdi.anita.basic.util.ExecutorUtil;
import com.mbrdi.anita.basic.util.PolylineEncoding;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.model.*;
import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.Passenger;
import com.mbrdi.anita.travel.model.Trip;
import com.mbrdi.anita.user.model.Address;
import io.netty.util.internal.ConcurrentSet;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
public class LocationService {

    /**
     * Following drivers have time not synced
     */
    static Set<String> async_drivers = new ConcurrentSet<>();

    public LocationService() {
        purgeHistory();
    }

    private static final String GOOGLE_SERVER_KEY = Play.application().configuration().getString("GOOGLE_SERVER_KEY");

    private static ExecutorService executor = Executors.newFixedThreadPool(4);

    private static Integer ONLINE_CHECK_DURATION_MINS = 90;

    public static synchronized void purgeHistory() {
        int hrs = 25 - Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).get(Calendar.HOUR_OF_DAY);
        Akka.system().scheduler().schedule(Duration.create(hrs, TimeUnit.HOURS), Duration.create(24, TimeUnit.HOURS),
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            LocationHistory.purge();
                            Logger.debug("Location History service started.");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, Akka.system().dispatcher());
    }

    /**
     * This method calculated the distance traveled in a journey. This find all
     * locations in TripLocation + locations provided. Then processes it.
     *
     * @param trip
     */
    public static void processTravelDistance(Trip trip) {
        try {
            List<Location> tripLocations = calculateGPSDistance(trip);

            trip.kms_done = trip.kms_done_gps;

            if (trip.start_km != null && trip.end_km != null && trip.start_km > 0 && trip.end_km > trip.start_km) {
                trip.kms_done = Integer.valueOf(trip.end_km - trip.start_km).doubleValue();
            }

            if (trip.origin_address == null && trip.end_location.latitude != null && trip.end_location.longitude != null) {
                Object[] values = getPathDistanceTimeByAPI(trip.start_location, trip.end_location);
                trip.origin_address = (String) values[2];
                trip.destination_address = (String) values[3];
            }

//			executor.execute(new UploadTripJourneyMap(trip, tripLocations));
            new UploadTripJourneyMap(trip, tripLocations).run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<Location> calculateGPSDistance(Trip trip) {
        List<Location> tripLocations = TripLocation.getAllByTripID(trip._id);
        Location pre_loc = null;

        if (tripLocations == null)
            tripLocations = new LinkedList<Location>();

        if (trip.start_location != null)
            tripLocations.add(0, trip.start_location);

        if (trip.end_location != null)
            tripLocations.add(trip.end_location);

        if (!tripLocations.isEmpty()) {
            Double travelDist = 0.0;
            for (Location location : tripLocations) {
                Double d = Util.distanceBetween2GeoPoints(location, pre_loc);
                // if((location.time - pre_loc.time)* 0.04 > d)
                travelDist = Util.sum(travelDist, d);
                pre_loc = location;
            }
            if (travelDist > 0) {
                trip.kms_done_gps = Util.roundCurrency(travelDist / 1000d); // in
                // KMs
            }
        }
        return LocationService.getTripLocationsCondensed(trip._id);
    }

    /**
     * distance caluculate via standard method
     *
     * @param trip
     * @return gps_km
     */

    public static double calculateGPSDistanceTest(Trip trip, int accuracy) {
        List<Location> tripLocations = TripLocation.getAllByTripID(trip._id);
        Location pre_loc = null;

        if (tripLocations == null)
            tripLocations = new LinkedList<Location>();

        if (trip.start_location != null)
            tripLocations.add(0, trip.start_location);

        if (trip.end_location != null)
            tripLocations.add(trip.end_location);

        Double travelDist = 0.0;
        if (!tripLocations.isEmpty()) {
            for (Location location : tripLocations) {
                if (/*location.accuracy != null && location.accuracy <= accuracy*/true) {
                    if (pre_loc != null) {
                        Double d = Util.distanceBetween2GeoPoints(location, pre_loc);
                        travelDist = Util.sum(travelDist, d);
                    }
                    pre_loc = location;
                }
            }
        }
        return Util.roundCurrency(travelDist / 1000d);
    }

    /**
     * calculate distance via API
     *
     * @param trip
     * @return
     */

    public static double calculateGPSDistanceViaAPI(Trip trip, int accuracy) {
        List<Location> tripLocations = getTripLocationsCondensedTest(trip._id, accuracy);
        Location pre_loc = null;

        if (tripLocations == null)
            tripLocations = new LinkedList<Location>();

        if (trip.start_location != null)
            tripLocations.add(0, trip.start_location);

        if (trip.end_location != null)
            tripLocations.add(trip.end_location);

        Double travelDist = 0.0;

        if (!tripLocations.isEmpty()) {
            travelDist = getPathDistanceTimeByAPIPolylineEncoder(tripLocations.get(0), PolylineEncoding.encode(tripLocations))[0];
        }

        return Util.roundCurrency(travelDist / 1000d);
    }

    public static boolean reuploadJourneyMap(Trip trip) {
        try {
            if (trip == null)
                return false;

            List<Location> tripLocations = LocationService.getTripLocationsCondensed(trip._id);
            Location pre_loc = null;

            if (tripLocations == null)
                tripLocations = new LinkedList<Location>();

            if (trip.start_location != null)
                tripLocations.add(0, trip.start_location);

            if (tripLocations.size() < 3)
                return false;

            executor.execute(new UploadTripJourneyMap(trip, tripLocations));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    public static boolean reuploadJourneyMapTest(String trip_id, int accuracy) {
//        try {
//            Trip trip = TripDAO.getByTgId(trip_id);
//            if (trip == null)
//                return false;
//
//            List<Location> tripLocations = LocationService.getTripLocationsCondensedTest(trip._id, accuracy);
//            Location pre_loc = null;
//
//            if (tripLocations == null)
//                tripLocations = new LinkedList<Location>();
//
//            if (trip.start_location != null)
//                tripLocations.add(0, trip.start_location);
//
//            if (tripLocations.size() < 3)
//                return false;
//
//            executor.execute(new UploadTripJourneyMap(trip, tripLocations));
//
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public static void populateJourneyPath(Trip trip) {
        if (trip == null || trip.passengers == null || trip.passengers.isEmpty())
            return;
        List<Location> locations = new LinkedList<>();
        Location origin, destination;
        for (Passenger p : trip.passengers) {
            if (p.drop_location != null) {
                locations.add(p.drop_location);
            }
        }
        for (Passenger p : trip.passengers) {
            if (p.pickup_location != null) {
                locations.add(p.pickup_location);
            }
        }
        // Not required as it is created on browser
        // r.polyLinePath.path_locations = getMapPolylinePath(locations);
    }

    public static List<Location> purge(List<Location> locations) {
        List<Location> finalLocations = new LinkedList<>();
        Collections.sort(locations, LocationSorterByTime);
        Location prev = null;
        for (Location l : locations) {

            if (prev == null)
                prev = l;
            else if (((l.time - prev.time) > 300000)
                    || Util.getDistance(prev.latitude, prev.longitude, l.latitude, l.longitude) > 0.05) {

                finalLocations.add(l);
                prev = l;
            }
        }

        return finalLocations;
    }

    /**
     * This create Journey Map and Uploads to S3
     */
    private static class UploadTripJourneyMap implements Runnable {

        Trip trip;
        List<Location> locations;

        public UploadTripJourneyMap(Trip trip, List<Location> locations) {
            this.trip = trip;
            this.locations = locations;
        }

        @Override
        public void run() {

            if (locations == null || locations.isEmpty() || locations.size() < 3)
                return;

            int width = 600, height = 450;
            try {
                Location start_location = locations.get(0), end_location = locations.get(locations.size() - 1);

                StringBuffer url = new StringBuffer("https://maps.googleapis.com/maps/api/staticmap?");
                url.append("&size=").append(width).append("x").append(height).append("&maptype=roadmap|weight:1");

                // ========ADD START LOCATION============
                url.append("&markers=color:green|label:S|").append(start_location.latitude).append(",")
                        .append(start_location.longitude);

                // ========ADD PASSENGER PICKUP LOCATION============
                if (trip.passengers != null) {
                    for (Passenger p : trip.passengers) {
                        if (p != null && p.name != null && p.name.length() > 2 && p.pickup_location != null) {
                            url.append("&markers=color:orange|label:" + p.name.substring(0, 3).replaceAll(" ", "") + "|")
                                    .append(p.pickup_location.latitude).append(",").append(p.pickup_location.longitude);
                        }
                    }
                }
                // ========ADD PATH============

                Logger.error("Trip Locations Size : " + locations.size());


                if (locations.size() > 2) {
                    StringBuffer locationBuffer = new StringBuffer("&path=color:0x0000ff|weight:5|");

                    /**
                     * add polyline encoded path
                     */
                    String urlpath = locationBuffer.toString() + "enc:" + PolylineEncoding.encode(locations);
                    if (urlpath.lastIndexOf("|") == urlpath.length() - 1)
                        urlpath = urlpath.substring(0, urlpath.length() - 1);
                    url.append(urlpath);
                }


                // ========ADD END LOCATION============
                url.append("&markers=color:red|label:E|").append(end_location.latitude).append(",")
                        .append(end_location.longitude);

                // ADD KEY
                url.append("&key=").append(GOOGLE_SERVER_KEY);

                Logger.error("Url of the map : " + url);


                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] chunk = new byte[4096];
                int bytesRead;
                InputStream stream = new URL(url.toString()).openStream();

                while ((bytesRead = stream.read(chunk)) > 0) {
                    outputStream.write(chunk, 0, bytesRead);
                }

                byte[] image = outputStream.toByteArray();

                String path = StorageService.uploadTripMap(91, trip.corp_id, trip._id, trip.date, image);
                TripDAO.update(DBQuery.is("_id", trip._id), DBUpdate.set("mapUrl", path));

                Logger.error("Map Path : " + path);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * fetches street address based on lat-lng i.e. reverse geocoding
     *
     * @param latitude
     * @param longitude
     * @return address name
     */
    public static String getStreetAddress(double latitude, double longitude) {

        String address = "";
        try {
            String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude;
            JsonNode jsonNode = Json.parse(Util.sendHTTPRequest(url));

            if (jsonNode != null) {

                GPModel gpm = Json.fromJson(jsonNode, GPModel.class);

                for (GPResults gpResults : gpm.results) {

                    if (gpResults.formatted_address != null
                            && gpResults.formatted_address.length() > address.length()) {
                        address = gpResults.formatted_address;
                    }
                }
            } else {
                Logger.error("getStreetAddress() : Response couldn't be parsed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return address;
    }

    public static Double processNightTravelDistanceKMs(Trip trip, Double shift_start, Double shift_end) {
        // TODO implement
        return 0d;
    }


    /**
     * Returns Value[0] = kmDone, Value[1] = duration
     *
     * @param location
     * @param location2
     * @return
     */
    public static Double[] getDistanceTimeOnlyByAPI(Location location, Location location2) {
        Double distance = Util.getDistance(location.latitude, location.longitude, location2.latitude, location2.longitude);
        if(distance < 1.8)
            return new Double[]{distance, distance> 1? 5d/distance: 2d};
        else {
            Object[] val = getPathDistanceTimeByAPI(location, location2);
            return new Double[]{Double.valueOf(val[0].toString()), Double.valueOf(val[1].toString())};
        }
    }

    /**
     * Returns Value[0] = kmDone, Value[1] = duration, Value[2] = Origin
     * Address, Value[3] = Destination Address
     *
     * @param location
     * @param location2
     * @return
     */
    public static Object[] getPathDistanceTimeByAPI(Location location, Location location2) {

        Object[] values = new Object[]{0d, 0, null, null};
        Double kmDone = 0.0;
        Integer duration = 0;
        if (location == null || location2 == null)
            return values;

        try {
            String path = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + location.latitude + ","
                    + location.longitude + "&destinations=" + location2.latitude + "," + location2.longitude + "&key="
                    + GOOGLE_SERVER_KEY;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String line, outputString;

            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            outputString = buffer.toString().replaceAll("\\r|\\n", "").replaceAll("  ", "");

            JsonNode json = Json.parse(outputString);
            if (json != null) {
                MapMatrixAPIResponse report = Json.fromJson(json, MapMatrixAPIResponse.class);
                if(report.rows != null && report.rows.size() > 0) {
                    if (report.rows.get(0).elements.get(0).distance != null)
                        kmDone = report.rows.get(0).elements.get(0).distance.value / 1000d;

                    if (report.rows.get(0).elements.get(0).duration != null)
                        duration = report.rows.get(0).elements.get(0).duration.value / 60;

                    values[0] = kmDone;
                    values[1] = duration;
                    values[2] = report.origin_addresses[0];
                    values[3] = report.destination_addresses[0];
                }
            }

            return values;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;

    }

    /**
     * Returns Value[0] = kmDone, Value[1] = duration, Value[2] = Origin
     * Address, Value[3] = Destination Address
     *
     * @param location
     * @return
     */
    public static Double[] getPathDistanceTimeByAPIPolylineEncoder(Location location, String encoder) {

        Double[] values = new Double[]{0d, 0d};
        Double kmDone = 0.0;
        Integer duration = 0;
        if (location == null)
            return values;

        try {
            String path = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + location.latitude + ","
                    + location.longitude + "&destinations=enc:" + encoder + ":" + "&key=" + GOOGLE_SERVER_KEY;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String line, outputString;

            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            outputString = buffer.toString().replaceAll("\\r|\\n", "").replaceAll("  ", "");

            JsonNode json = Json.parse(outputString);
            if (json != null) {
                MapMatrixAPIResponse report = Json.fromJson(json, MapMatrixAPIResponse.class);
                /*if (report.rows.get(0).elements.get(0).distance != null)
					kmDone = report.rows.get(0).elements.get(0).distance.value / 1000d;

				if (report.rows.get(0).elements.get(0).duration != null)
					duration = report.rows.get(0).elements.get(0).duration.value / 60;*/

                for (MatrixApiElements matrixApiElements : report.rows.get(0).elements) {
                    kmDone += matrixApiElements.distance.value;
                }
                for (MatrixApiElements matrixApiElements : report.rows.get(0).elements) {
                    duration += matrixApiElements.duration.value;
                }
            }

            return new Double[]{kmDone, duration.doubleValue()};

        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;

    }

    /**
     * @param locations
     * @return
     */
    public static List<GPLocation> getMapPolylinePath(List<Location> locations) {

        List<GPLocation> locs = new LinkedList<>();
        if (locations == null || locations.size() < 2)
            return locs;

        // remove blank and zero values
        Location start = locations.get(0);
        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            if (l.latitude == null || l.latitude == 0d || l.longitude == null || l.longitude == 0d) {
                locations.remove(i);
                i--;
            } else if (i != 0 && l.latitude.equals(start.latitude) && l.longitude.equals(start.longitude)) {
                locations.remove(i);
                i--;
            } else {
                start = l;
            }
        }

        if (locations.size() < 2)
            return locs;

        Location origin = locations.get(0);
        Location destination = locations.get(locations.size() - 1);
        try {
            String path = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + ","
                    + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude;

            // ADD WAY POINTS
            if (locations.size() > 2) {
                path += "&waypoints=";
                for (int i = 1; i < locations.size() - 1; i++) {
                    Location loc = locations.get(i);
                    path += origin.latitude + "," + origin.longitude;
                    if (i != locations.size() - 2)
                        path += "|";
                }
            }

            path += "&key=" + GOOGLE_SERVER_KEY;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String line, outputString;

            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            outputString = buffer.toString().replaceAll("\\r|\\n", "").replaceAll("  ", "");

            JsonNode json = Json.parse(outputString);
            if (json != null) {
                GoogleDirectionResponse direction = Json.fromJson(json, GoogleDirectionResponse.class);
                if ("REQUEST_DENIED".equals(direction.status)) {
                    Logger.error("--------------This IP, site or mobile application is not authorized to use this API key");
                }
                for (GoogleDirectionResponse.GRoute gRoute : direction.routes) {
                    for (GoogleDirectionResponse.GLeg leg : gRoute.legs) {
                        for (GoogleDirectionResponse.GStep step : leg.steps) {
                            locs.add(step.start_location);
                            locs.add(step.end_location);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locs;
    }

    public static Location getLocationFromAddress(Address address) {
        String adr = (address.addressLine != null ? address.addressLine : "")
                + (address.area_name != null ? " , " + address.area_name : "")
                + (address.city_name != null ? " , " + address.city_name : "")
                + (address.country_name != null ? " , " + address.country_name : "");

        return getLocationFromAddress(adr);
    }

    public static Location getLocationFromAddress(String address) {

        try {
            String path = "https://maps.googleapis.com/maps/api/geocode/json?key=" + GOOGLE_SERVER_KEY + "&address="
                    + URLEncoder.encode(address, "UTF-8");

            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String line, outputString;

            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            outputString = buffer.toString().replaceAll("\\r|\\n", "").replaceAll("  ", "");
            JsonNode json = Json.parse(outputString);
            if (json != null) {
                GeoCodeResponse response = Json.fromJson(json, GeoCodeResponse.class);
                if ("REQUEST_DENIED".equals(response.status)) {
                    Logger.error("--------------This IP, site or mobile application is not authorized to use this API key");
                    System.out.println("--------------This IP, site or mobile application is not authorized to use this API key");
                }
                GPGeometry geometry = response.results != null && !response.results.isEmpty()
                        ? response.results.get(0).geometry : null;
                if (geometry != null && geometry.location != null) {
                    return new Location(geometry.location.lat, geometry.location.lng);
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Location> getTripLocationsCondensed(String trip_id) {
        List<Location> tls = TripLocation.getAllByTripID(trip_id);
        if (tls != null && !tls.isEmpty()) {
            Iterator<Location> itr = tls.iterator();
            Location prev = itr.next();
            while (itr.hasNext()) {
                Location l = itr.next();
                if (Util.distanceBetween2GeoPoints(l, prev) < 50)
                    itr.remove();
                else
                    prev = l;
            }
        }
        return tls;
    }

    public static List<Location> getTripLocationsCondensedTest(String trip_id, int accuracy) {
        List<Location> tls = TripLocation.getAllByTripID(trip_id);
        if (tls != null && !tls.isEmpty()) {
            Iterator<Location> itr = tls.iterator();
            Location prev = itr.next();
            while (itr.hasNext()) {
                Location l = itr.next();

                if (/*l.accuracy != null && l.accuracy > accuracy || */(Util.distanceBetween2GeoPoints(l, prev) < 50)) {
                    itr.remove();
                } else
                    prev = l;
            }
        }
        return tls;
    }

    public static List<GPLocation> convertToWaypts(List<Location> locations) {
        List<GPLocation> locs = new LinkedList<>();
        if (locations != null) {
            for (Location l : locations) {
                locs.add(new GPLocation(l));
            }
        }
        return locs;
    }

    public static boolean isUserOnline(String user_id) {
        long duration = ONLINE_CHECK_DURATION_MINS * 60000;

        Location location = Location.getUserLocation(user_id);
        if (location != null && location.time + duration > System.currentTimeMillis())
            return true;

        return false;
    }

    public static boolean isUserOnline(String user_id, Duration duration) {

        Location location = Location.getUserLocation(user_id);
        if (location != null && location.time + duration.toMillis() > System.currentTimeMillis())
            return true;

        return false;
    }

    public static void populateTimeNotSynced(Long time, String user_id) {
        ExecutorUtil.executeNow(new Runnable() {
            @Override
            public void run() {
                if (time != null && user_id != null) {
                    Long now = System.currentTimeMillis();
                    if (Math.abs(now - time) > 1800000)
                        async_drivers.add(user_id);
                    else
                        async_drivers.remove(user_id);
                }
            }
        });

    }

    public static boolean timeNotSynced(String user_id) {
        return async_drivers.contains(user_id);
    }

    public static String getNearByDeliveryBoys(String corp_id, Location pickupLocation, List<String> previous_users) {
        List<Location> locs = Location.getNearByDeliveryBoys(corp_id, pickupLocation, previous_users);
        Location l = null;
        Double distance = Double.MAX_VALUE;
        for (Location loc : locs) {
            Double d = Util.getDistance(pickupLocation.latitude, pickupLocation.longitude, loc.latitude, loc.longitude);
            if (d < distance) {
                l = loc;
                distance = d;
            }
        }
        return l != null ? l.user_id : null;
    }

    /**
     * This returns latitude and longitude range from a given location for a given distance in KM
     *
     * @param lat
     * @param lng
     * @param distance_km
     * @return
     */
    public static Double[] getLatLngRange(Double lat, Double lng, Integer distance_km) {
        if (lat == null || lng == null || distance_km == null)
            return null;

        return new Double[]{0.008983 * distance_km, Math.abs(distance_km * (360 / (Math.cos(lat) * 40075))) };
    }

    public static final Comparator<Location> LocationSorterByTime = new Comparator<Location>() {
        @Override
        public int compare(Location o1, Location o2) {
            if (o1 != null && o1 != null && o1.time != null)
                return o1.time.compareTo(o2.time);
            return 0;
        }
    };
}
