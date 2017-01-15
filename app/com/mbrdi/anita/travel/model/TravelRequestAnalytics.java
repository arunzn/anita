package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.util.Constants;
import com.mbrdi.anita.basic.util.Util;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.ObjectId;
import org.mongojack.WriteResult;

import javax.persistence.Id;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AJ on 1/4/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelRequestAnalytics {

    public static JacksonDBCollection<TravelRequestAnalytics, String> COLLECTION = MongoDB.getDBCollection(TravelRequestAnalytics.class);

    static Executor executor = Executors.newSingleThreadExecutor();

    @Id
    @ObjectId
    public String _id;

    public Integer date;
    public Map<Integer, Set<String>> hourlyRequesters;
    public Map<Integer, Set<String>> hourlyOfferers;
    public Map<Integer, Set<String>> hourlyPassengers;
    public Map<Integer, Set<String>> hourlyLiftGivers;

    public Set<String> requesters;
    public Set<String> offerers;

    public Set<String> passengers;
    public Set<String> liftGivers;

    public Integer totalRequesters, totalOfferers, totalPassenger, totalLiftGivers;
    public Map<String, Integer> hourlyRequestersAnalysis, hourlyOfferersAnalysis, hourlyPassengerAnalysis, hourlyLiftGiversAnalysis;
    public Integer totalUsers;
    public Integer totalActiveUsers;


    public static TravelRequestAnalytics getByDate(Integer date) {
        return COLLECTION.findOne(DBQuery.is("date", date));
    }

    private static void save(TravelRequestAnalytics t) {
        WriteResult<TravelRequestAnalytics, String> result = COLLECTION.save(t);
        t._id = result.getSavedId();
    }

    public static void addRequester(Integer date, String email) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                TravelRequestAnalytics t = getByDate(date);
                if (t == null) {
                    t = new TravelRequestAnalytics();
                    t.date = date;
                    save(t);
                }
                if (t.requesters == null) {
                    t.requesters = new HashSet<>();
                }
                t.requesters.add(email);

                Integer hr = Util.getCurrentHour(Constants.timeZone);
                if(t.hourlyRequesters == null){
                    t.hourlyRequesters = new TreeMap<>();
                }
                if(t.hourlyRequesters.get(hr) == null){
                    t.hourlyRequesters.put(hr, new HashSet<>());
                }
                t.hourlyRequesters.get(hr).add(email);
                save(t);
            }
        });
    }

    public static void addOfferers(Integer date, String email) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                TravelRequestAnalytics t = getByDate(date);
                if (t == null) {
                    t = new TravelRequestAnalytics();
                    t.date = date;
                    save(t);
                }
                if (t.offerers == null) {
                    t.offerers = new HashSet<>();
                }
                t.offerers.add(email);

                Integer hr = Util.getCurrentHour(Constants.timeZone);
                if(t.hourlyOfferers == null){
                    t.hourlyOfferers = new TreeMap<>();
                }
                if(t.hourlyOfferers.get(hr) == null){
                    t.hourlyOfferers.put(hr, new HashSet<>());
                }
                t.hourlyOfferers.get(hr).add(email);
                save(t);
            }
        });
    }

    public static void addPassengers(Integer date, String email) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                TravelRequestAnalytics t = getByDate(date);
                if (t == null) {
                    t = new TravelRequestAnalytics();
                    t.date = date;
                    save(t);
                }
                if (t.passengers == null) {
                    t.passengers = new HashSet<>();
                }
                t.passengers.add(email);

                Integer hr = Util.getCurrentHour(Constants.timeZone);
                if(t.hourlyPassengers == null){
                    t.hourlyPassengers = new TreeMap<>();
                }
                if(t.hourlyPassengers.get(hr) == null){
                    t.hourlyPassengers.put(hr, new HashSet<>());
                }
                t.hourlyPassengers.get(hr).add(email);
                save(t);
            }
        });
    }

    public static void addLiftGivers(Integer date, String email) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                TravelRequestAnalytics t = getByDate(date);
                if (t == null) {
                    t = new TravelRequestAnalytics();
                    t.date = date;
                    save(t);
                }
                if (t.liftGivers == null) {
                    t.liftGivers = new HashSet<>();
                }
                t.liftGivers.add(email);

                Integer hr = Util.getCurrentHour(Constants.timeZone);
                if(t.hourlyLiftGivers == null){
                    t.hourlyLiftGivers = new TreeMap<>();
                }
                if(t.hourlyLiftGivers.get(hr) == null){
                    t.hourlyLiftGivers.put(hr, new HashSet<>());
                }
                t.hourlyLiftGivers.get(hr).add(email);
                save(t);
            }
        });
    }
}
