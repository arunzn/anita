package com.mbrdi.anita.user.dao;

import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.model.ValidationFailureException;
import com.mbrdi.anita.basic.service.GridSupport;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.user.model.User;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.List;
import java.util.Set;

public class UserDAO {

    public static JacksonDBCollection<User, String> UserCollection = MongoDB.getDBCollection(User.class);

    public static void setLastLoginDate(String _id) {
        UserCollection.updateById(_id, DBUpdate.set("lastLogin", System.currentTimeMillis()));
    }

    public static GridResponse<User> fetch(GridRequest request) {
        GridResponse<User> response = GridSupport.fetch(User.class, UserCollection, request);
        return response;
    }

    public static User save(User usernew) throws ValidationFailureException {
        if(usernew.email != null && !"".equals(usernew.email)) {
            usernew.email = usernew.email.trim().toLowerCase();
        }

        validateUpdate(usernew);
        User user = getByEmail(usernew.email);

        if (user == null) {
            /**
             * if user is assigned to corporate as a employee
             */
            WriteResult<User, String> result = UserCollection.save(usernew);
            usernew._id = result.getSavedObject()._id;

            user = result.getSavedObject();
        } else {
            Util.smartCopy(usernew, user);
            UserCollection.save(user);
        }

        return user;
    }

    public static void validateUpdate(User user) throws ValidationFailureException {
        User oldUser = get(user._id);
        if(user._id == null || oldUser == null)
            return;

        //validate email
        if(!Util.isNullOrEmpty(user.email) && !user.email.equalsIgnoreCase(oldUser.email)) {
            if(UserCollection.findOne(DBQuery.is("email", user.email).notEquals("_id", user._id)) != null)
                throw new ValidationFailureException("A user with same email already exists");
        }
    }

    public static void delete(String _id) {
        if (_id != null) {
            UserCollection.removeById(_id);
        }
    }

    public static User get(String _id) {
        try {
            if (_id == null || "".equals(_id))
                return null;
            else
                return UserCollection.findOneById(_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User update(User u) {
        User user = get(u._id);
        Util.smartCopy(u, user);
        UserCollection.updateById(u._id, user);
        return user;
    }

    public static void updateHard(User user) {
        UserCollection.updateById(user._id, user);
    }

    public static User updateEmployee(User user) {
        UserCollection.save(user);
        return user;
    }

    public static User getByDeviceId(String device_id) {
        return UserCollection.findOne(DBQuery.is("device_id", device_id));
    }

    public static List<User> getAllByDeviceId(String device_id) {
        return UserCollection.find(DBQuery.is("device_id", device_id)).toArray();
    }

    public static User getAdminUser(String email, String password) {
        return UserCollection.findOne(DBQuery.is("email", email).is("password", password).exists("admin_of"));
    }

    public static List<User> get(Set<String> ids) {
        return UserCollection.find(DBQuery.in("_id", ids)).toArray();
    }

    public static List<User> fetch(DBQuery.Query query) {
        return UserCollection.find(query).toArray();
    }

    public static User getByEmail(String email) {
        return UserCollection.findOne(DBQuery.is("email", email));
    }


    public static void deleteEmployee(User user) {
        UserCollection.updateById(user._id, DBUpdate.unset("employee_of").unset("emp_no"));
    }


    /*public static List<User> downloadEmployees(String business_unit_id, String work_location_id, String dutyhrs_id) {
        DBQuery.Query query = DBQuery.empty();
        if(!"".equals(business_unit_id) && business_unit_id != null && !"all".equals(business_unit_id)){
            query = query.is("zone_id", business_unit_id);
        }

        if(!"".equals(work_location_id) && work_location_id != null && !"all".equals(work_location_id)){
            query = query.is("work_location_id", work_location_id);
        }

        if(!"".equals(dutyhrs_id) && dutyhrs_id != null && !"all".equals(dutyhrs_id)){
            query = query.is("dutyhrs_id", dutyhrs_id);
        }

        return UserCollection.find(query).toArray();
    }*/

    public static List<User> getAll(){
        return UserCollection.find().toArray();
    }

    public static List<User> getAllWhereDeviceIdNotExists() {
        return UserCollection.find( DBQuery.notExists("device_id") ).toArray();
    }


    public static User getUserbyEmailPassword(String email, String password) {
        return UserCollection.findOne(DBQuery.is("email", email).is("password", password));
    }

    public static Long countAll(DBQuery.Query query) {
        return UserCollection.getCount(query);
    }

    public static Long countAll() {
        return UserCollection.getCount();
    }

}
