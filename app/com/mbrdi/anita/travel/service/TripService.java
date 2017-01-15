package com.mbrdi.anita.travel.service;

import com.mbrdi.anita.travel.dao.TripDAO;
import com.mbrdi.anita.travel.model.Passenger;
import com.mbrdi.anita.travel.model.Rating;
import com.mbrdi.anita.travel.model.Trip;

public class TripService {


    public static void markPassengerRating(String trip_id, String user_id, Rating rating) {
        Trip t = TripDAO.get(trip_id);
        if (t == null || t.passengers == null || rating == null)
            return;

        Passenger savedPassenger = t.getPassenger(user_id);
        if (savedPassenger != null) {
            savedPassenger.rating = rating;
            try {
                TripDAO.updateHard(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}