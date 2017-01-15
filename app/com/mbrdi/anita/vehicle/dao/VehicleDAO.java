package com.mbrdi.anita.vehicle.dao;

import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.service.GridSupport;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.vehicle.model.Vehicle;
import com.mongodb.BasicDBObject;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.util.*;

/**
 * Created by AJ on 10/14/2016.
 */
public class VehicleDAO {

    private static JacksonDBCollection<Vehicle, String> VehicleCollection = MongoDB.getDBCollection(Vehicle.class);

    private static JacksonDBCollection<Vehicle, String> VehicleDeletedCollection = MongoDB.getDBCollection(Vehicle.class, "VehicleDeleted");

    public static List<Vehicle> all() {
        return VehicleCollection.find().toArray();
    }

    public static List<Vehicle> allVehicleByCorporate(String corp_id){
        return VehicleCollection.find(DBQuery.is("corp_id", corp_id)).toArray();
    }

    public static void save(Vehicle vehicle) {
        vehicle.reg_no = vehicle.reg_no.replaceAll(" ", "").toUpperCase();

        if(vehicle._id == null){
            WriteResult<Vehicle, String> result = VehicleCollection.save(vehicle);
            vehicle._id = result.getSavedObject()._id;
        } else {
            Vehicle old = get(vehicle._id);
            Util.smartCopy(vehicle, old);
            VehicleCollection.updateById(vehicle._id, old);
        }
    }

    public static void delete(String id) {
        if (id != null) {
            VehicleDeletedCollection.save(get(id));
            VehicleCollection.removeById(id);
        }
    }

    public static void removeAll() {
        VehicleCollection.drop();
    }

    public static Vehicle get(String id) {
        if(id == null || "".equals(id))
            return null;
        return VehicleCollection.findOneById(id);
    }

    public static boolean exist(Vehicle vehicle) {
        if (vehicle._id == null) {
            return VehicleCollection.find(DBQuery.is("reg_no", vehicle.reg_no.replaceAll(" ", "").toUpperCase())).is("corp_id",vehicle.corp_id).count() > 0;
        } else {
            return VehicleCollection.find(DBQuery.is("reg_no", vehicle.reg_no.replaceAll(" ", "").toUpperCase()).is("corp_id", vehicle.corp_id).notEquals("_id", vehicle._id)).count() > 0;
        }
    }

    public static Vehicle getVehicleByReg_no(String reg_no) {
        return VehicleCollection.findOne(DBQuery.is("reg_no", reg_no.replaceAll(" ", "").toUpperCase()));
    }

    public static List<Vehicle> getVehicleByDriverId(String driver_id) {
        return VehicleCollection.find(DBQuery.is("driver_id", driver_id)).toArray();
    }

    public static Vehicle getVehicleByOwner_id(String owner_id){

        return VehicleCollection.findOne(DBQuery.or(DBQuery.is("owner_id", owner_id)));
    }

    public static List<Vehicle> getVehicleByCorpId(String corp_id,List<String> zones) {
        DBQuery.Query query = DBQuery.or(DBQuery.is("corp_id", corp_id), DBQuery.is("assigned_to", corp_id));
        if(zones.size() > 0)
            query = query.in("zone_id", zones);
        return VehicleCollection.find(query).toArray();
    }

    public static int getVehicleCountByCorpId(String corp_id) {
        return VehicleCollection.find(DBQuery.is("corp_id", corp_id)).count();
    }

    public static Map<String, String> getVehicleByCorp(String corp_id) {
        Map<String, String> map = new LinkedHashMap<>();
        List<Vehicle> vehicles;
        map.put("-1", "Select Vehicle");
        BasicDBObject query = new BasicDBObject("corp_id", corp_id);
        vehicles = VehicleCollection.find(query).toArray();
        for (Vehicle vehicle : vehicles) {
            map.put(vehicle._id, vehicle.reg_no);
        }
        return map;
    }

    public static void updateVehicle(String id, Vehicle vehicle) {
        VehicleCollection.updateById(id, vehicle);
    }

    public static GridResponse<Vehicle> fetch(GridRequest request) {
        GridResponse<Vehicle> response = GridSupport.fetch(Vehicle.class, VehicleCollection, request);
        return response;
    }

    public static Map<String, String> getVehiclesMap(String corp_id) {

        Map<String, String> map = new HashMap<String, String>();
        List<Vehicle> vehicles;
        if (corp_id == null)
            vehicles = new LinkedList<>();
        else {
            BasicDBObject query = new BasicDBObject("corp_id", corp_id);
            vehicles = VehicleCollection.find(query).toArray();
        }
        for (Vehicle vehicle : vehicles) {
            map.put(vehicle._id, vehicle.reg_no);
        }
        return map;
    }

    public static void update(Vehicle vehicle) {
        if (vehicle._id == null)
            throw new RuntimeException("Object is not saved");
        Vehicle old = get(vehicle._id);
        if (old != null) {
            vehicle.reg_no = vehicle.reg_no.replaceAll(" ", "").toUpperCase();
            Util.smartCopy(vehicle, old);
            VehicleCollection.updateById(vehicle._id, old);
        }
    }

    public static void updateHard(Vehicle vehicle) {
        if(vehicle._id != null) {
            VehicleCollection.updateById(vehicle._id, vehicle);
        }
    }



    public static List<Vehicle> getAll(String corp_id, String vendor_id, String zone_id, String vehicle_type) {
        DBQuery.Query query = DBQuery.empty();

        if (vendor_id != null && corp_id.equals(vendor_id)) {
            query = query.or(DBQuery.is("corp_id", vendor_id),DBQuery.is("assigned_to",vendor_id));
        }

        if(vendor_id != null && !corp_id.equals(vendor_id)) {
            query = query.and(DBQuery.is("corp_id", vendor_id),DBQuery.is("assigned_to", corp_id));
        }


        if(vehicle_type != null){
            query = query.is("vehicle_type", vehicle_type);
        }


        if(!Util.isNullOrEmpty(zone_id)){
            query = query.is("zone_id", zone_id);
        }

        if(query != null)
            return VehicleCollection.find(query).toArray();
        else
            return null;
    }
}
