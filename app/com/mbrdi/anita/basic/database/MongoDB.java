package com.mbrdi.anita.basic.database;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.MongoJackModule;
import play.Play;

import java.util.LinkedList;
import java.util.List;

public final class MongoDB {
    private static MongoClient mongoClient = null, mongoMasterClient = null, mongoAnalyticsClient = null;

    public static void shutdown() {
        if (mongoClient != null)
            mongoClient.close();
        mongoClient = null;

        if (mongoMasterClient != null)
            mongoMasterClient.close();
        mongoMasterClient = null;

        if (mongoAnalyticsClient != null)
            mongoAnalyticsClient.close();
        mongoAnalyticsClient = null;
    }

    private static MongoClient getMongoClient() {
        if (mongoClient == null) {
            try {
                String host = Play.application().configuration().getString("mongodb.host");
                Integer port = Play.application().configuration().getInt("mongodb.port");

                String username = Play.application().configuration().getString("mongodb.username");
                String password = Play.application().configuration().getString("mongodb.password");
                if (username != null) {
                    List<MongoCredential> list = new LinkedList<>();
                    list.add(MongoCredential.createCredential(username, "zoyride", password.toCharArray()));
                    mongoClient = new MongoClient(new ServerAddress(host, port), list);
                } else {
                    mongoClient = new MongoClient(host, port);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mongoClient;
    }

    public static DBCollection getCollection(Class<?> classOf) {
        return getCollection(classOf.getSimpleName());
    }

    private static DBCollection getCollection(String collectionName) {
        DB db = getMongoClient().getDB(Play.application().configuration().getString("mongodb.database"));
        DBCollection dbCollection = db.getCollection(collectionName);
        return dbCollection;
    }


    public static <T> JacksonDBCollection<T, String> getAuditDBCollection(String dbName, Class mapperClass) {
        JacksonDBCollection<T, String> collection = null;
        collection = JacksonDBCollection.wrap(MongoDB.getCollection(dbName), mapperClass, String.class);
        return collection;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> JacksonDBCollection<T, String> getDBCollection(Class klass, Class... mapperClass) {
        JacksonDBCollection<T, String> collection = null;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        MongoJackModule.configure(objectMapper);
        Class pojoClass = (mapperClass != null && mapperClass.length > 0) ? mapperClass[0] : klass;
        collection = JacksonDBCollection.wrap(MongoDB.getCollection(klass), pojoClass, String.class, objectMapper);

        return collection;
    }

    public static <T> JacksonDBCollection<T, String> getDBCollection(Class klass, String collectionName) {
        JacksonDBCollection<T, String> collection = null;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        MongoJackModule.configure(objectMapper);
        collection = JacksonDBCollection.wrap(MongoDB.getCollection(collectionName), klass, String.class, objectMapper);

        return collection;
    }

}
