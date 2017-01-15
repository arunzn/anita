package com.mbrdi.anita.travel.service;

import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.travel.model.Passenger;
import com.mbrdi.anita.travel.model.TravelType;
import com.mbrdi.anita.travel.model.Trip;
import play.Logger;

import java.util.*;

public class PassengerOrderingService {

    public static void processPassengerOrder(Trip t){

        List<Passenger> shuffle = new LinkedList<>(t.passengers);

        // SET PICKUP ORDER
        List<Passenger> list = new LinkedList<>();
        do {
            Passenger p = getClosest(shuffle, t.start_location.latitude, t.start_location.longitude);
            list.add(p);
            shuffle.remove(p);
        } while(!shuffle.isEmpty());

        int order = 1;
        for(Passenger p :list) {
            t.getPassenger(p.user_id).pickup_order = order++;
        }

        // SET DROP ORDER
        list.clear();
        shuffle.clear();

        Set shuffle1 = new LinkedHashSet<Passenger>(t.passengers);
        do {
            Passenger p = getFarthest(shuffle1, t.start_location, TravelType.FRO, false);

            list.add(p);
            shuffle1.remove(p);
        } while(!shuffle1.isEmpty());

        order = 0;
        for(Passenger p :list) {
            t.getPassenger(p.user_id).drop_order = t.passengers.size() - order++;
        }

        return;
    }


    private static List<Passenger> createOrder(List<Passenger> shuffle, Passenger start) {
        List<Passenger> list = new LinkedList<>();
        do {
            Passenger p = getClosest(shuffle, start.pickup_location.latitude, start.pickup_location.longitude);
            list.add(p);
            shuffle.remove(p);
        } while(!shuffle.isEmpty());
        return list;
    }

    private static Passenger getFarthest(Set<Passenger> passengers, Location officeLocation, TravelType type, boolean mustBeMale) {
        Passenger s = null;
        Double distance = null;
        for(Passenger p:passengers) {
            if(!mustBeMale || p.gender!= null && p.gender.intValue() == 1) {
                Double d = null;
                if(type == TravelType.TO)
                    d = Util.getDistance(p.pickup_location.latitude, p.pickup_location.longitude, officeLocation.latitude, officeLocation.longitude);
                else
                    d = Util.getDistance(p.drop_location.latitude, p.drop_location.longitude, officeLocation.latitude, officeLocation.longitude);

                if (distance == null || d != null && distance < d) {
                    distance = d;
                    s = p;
                }
            }
        }
        return s;
    }

    private static Passenger getClosest(List<Passenger> passengers, Double lat, Double lon) {
        Passenger s = null;
        Double distance = 10000000d;
        for(Passenger p:passengers) {
            Double d = Util.getDistance(p.pickup_location.latitude, p.pickup_location.longitude, lat, lon);
            if (distance > d) {
                distance = d;
                s = p;
            }
        }
        return s;
    }
}

class PassengerOrder {

    Passenger passenger;
    Integer order;
    Double distance;
}