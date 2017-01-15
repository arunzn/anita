package com.mbrdi.anita.travel.dao;

import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.exception.DirtyUpdateException;
import com.mbrdi.anita.basic.util.Constants;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.travel.model.TravelGroup;
import com.mbrdi.anita.travel.model.TravelRequestAnalytics;
import com.mbrdi.anita.user.dao.UserDAO;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.List;

/**
 * Created by AJ on 10/3/2016.
 */
public class TravelGroupDAO {

    private static JacksonDBCollection<TravelGroup, String> COLLECTION = MongoDB.getDBCollection(TravelGroup.class);

    public static void save(TravelGroup travelGroup){
//        if(Util.isNullOrEmpty(travelGroup._id)){
            travelGroup.version = 1;
            WriteResult<TravelGroup, String> result = COLLECTION.save(travelGroup);
            travelGroup._id = result.getSavedId();
        TravelRequestAnalytics.addOfferers(Util.getTodayTimeZeroInteger(Constants.timeZone), UserDAO.get(travelGroup.owner_id).email);
//        }
    }

    public static void update(TravelGroup travelGroup) throws DirtyUpdateException {
        if(!Util.isNullOrEmpty(travelGroup._id)){
            travelGroup.version = ++travelGroup.version;
            WriteResult<TravelGroup, String> result = COLLECTION.update(DBQuery.is("_id", travelGroup._id).is("version", travelGroup.version - 1), travelGroup);
            if(result.getWriteResult() == null){
                throw new DirtyUpdateException();
            }
            TravelRequestAnalytics.addOfferers(Util.getTodayTimeZeroInteger(Constants.timeZone), UserDAO.get(travelGroup.owner_id).email);
        }
    }

    public static List<TravelGroup> fetchAll(DBQuery.Query q) {
        return COLLECTION.find(q).toArray();
    }

    public static TravelGroup get(String _id) {
        return COLLECTION.findOneById(_id);
    }

    public static void delete(String _id) {
        COLLECTION.removeById(_id);
    }

    public static TravelGroup findByDateOwner(Integer date, String owner_id) {
        return COLLECTION.findOne(DBQuery.is("date", date).is("owner_id", owner_id).notExists("status"));
    }


    public static TravelGroup findOne(DBQuery.Query query) {
        return COLLECTION.findOne(query);
    }

    public static TravelGroup getByPassenger(String requester_id) {
        return COLLECTION.findOne(DBQuery.is("passengers._id", requester_id));
    }
}
