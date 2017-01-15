package com.mbrdi.anita.travel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.exception.DirtyUpdateException;
import com.mbrdi.anita.basic.util.Constants;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.location.model.Location;
import com.mbrdi.anita.location.service.DirectionService;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.Address;
import com.mbrdi.anita.user.model.User;
import org.mongojack.*;

import java.util.List;
import java.util.TimeZone;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelRequest {

    public static JacksonDBCollection<TravelRequest, String> COLLECTION = MongoDB.getDBCollection(TravelRequest.class);

    @Id
    @ObjectId
    public String _id;

    public String requester_id, travelGroup_id;
    public String name, email, company, designation, company_id;
    public Integer gender;
    public Double user_rating;
    public Integer date;
    public Double time, in_time, out_time;
    public String comment;
    public Address from, to;
    public Integer version;
    public Boolean approved;
    public Double detour_dist, detour_time;
    public String encodedPath;
    public String detourPath;

    public Integer carSegment;
    public Boolean regular, poolColleaguesOnly;
    public TravelType travelType;

    public Boolean su, mo, tu, we, th, fr, sa;

    public static TravelRequest get(String _id){
        if(!Util.isNullOrEmpty(_id))
            return COLLECTION.findOneById(_id);

        return null;
    }

    public void save(){
        if(Util.isNullOrEmpty(_id) || version == null){
            version = 1;
            populateEncodedPath();
            WriteResult<TravelRequest, String> result = COLLECTION.save(this);
            _id = result.getSavedId();
            TravelRequestAnalytics.addRequester(Util.getTodayTimeZeroInteger(Constants.timeZone), UserDAO.get(requester_id).email);
        }
    }

    public void update() throws DirtyUpdateException {
        if(!Util.isNullOrEmpty(_id)){
            this.version++;
            WriteResult<TravelRequest, String> result = COLLECTION.update(DBQuery.is("_id", _id).is("version", version - 1), this);
            if(!result.getWriteResult().isUpdateOfExisting()){
                throw new DirtyUpdateException();
            }
        }
    }

    public static void delete(String _id) {
        COLLECTION.removeById(_id);
    }

    public static List<TravelRequest> fetch(DBQuery.Query q) {
        return COLLECTION.find(q).toArray();
    }

    public static List<TravelRequest> fetchAll(DBQuery.Query q, Integer limit, Integer skip) {
        if(limit == null)
            limit = 100;
        if(skip == null)
            return COLLECTION.find(q).limit(limit).toArray();

        return COLLECTION.find(q).limit(limit).skip(skip).toArray();
    }


    public void saveUpdate() {
        TravelRequestAnalytics.addRequester(Util.getTodayTimeZeroInteger(Constants.timeZone), UserDAO.get(requester_id).email);
        if(_id == null) {
            TravelRequest t = COLLECTION.findOne(DBQuery.is("requester_id", requester_id).is("travelGroup_id", travelGroup_id));
            if(t == null) {
                this.save();
                return;
            } else {
                this._id = t._id;
                this.version = t.version;
            }
        }

        try {
            populateEncodedPath();
            this.update();
        } catch (DirtyUpdateException e) {
            e.printStackTrace();
        }
    }

    private void populateEncodedPath() {

        if(from != null && from.lat != null && from.lon != null && to != null && to.lat != null && to.lon != null) {

            if(Util.isNullOrEmpty(encodedPath)) {
                encodedPath = DirectionService.getPolylineEncoded(new Location(from.lat, from.lon), new Location(to.lat, to.lon));
                return;
            }

            TravelRequest tr = TravelRequest.get(_id);

            if(tr != null && tr.from != null && tr.from.lat != null && tr.from.lon != null && tr.to != null
                    && tr.to.lat != null && tr.to.lon != null && (!from.lat.equals(tr.from.lat) ||
                    !from.lon.equals(tr.from.lon) || !to.lat.equals(tr.to.lat) || !to.lat.equals(tr.to.lat))){

                encodedPath = DirectionService.getPolylineEncoded(new Location(from.lat, from.lon), new Location(to.lat, to.lon));
            }
        }
    }

    public static void deleteByGroupId(String travelGroup_id) {
        COLLECTION.remove(DBQuery.is("travelGroup_id", travelGroup_id));
    }

    public static List<TravelRequest> deleteByRequestorId(String requester_id) {
        List<TravelRequest> list = COLLECTION.find(DBQuery.is("requester_id", requester_id)).toArray();
        COLLECTION.remove(DBQuery.is("requester_id", requester_id));
        return list;
    }

    public static boolean exists(String requester_id, String travelGroup_id) {
        return COLLECTION.findOne(DBQuery.is("requester_id", requester_id).is("travelGroup_id", travelGroup_id)) != null;
    }

    public static TravelRequest findOne(DBQuery.Query query) {
        return COLLECTION.findOne(query);
    }
}
