
package com.mbrdi.anita.message.service;

import com.mbrdi.anita.basic.util.ExecutorUtil;
import com.mbrdi.anita.message.model.ACTION;
import com.mbrdi.anita.message.model.DEVICE_TYPE;
import com.mbrdi.anita.message.model.FCMessage;
import com.mbrdi.anita.message.model.Notification;
import play.Logger;
import play.Play;
import play.libs.Json;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageService {

    private static String GOOGLE_SERVER_KEY = Play.application().configuration().getString("GOOGLE_SERVER_KEY", "AIzaSyB3n9_-iSMEHykxBwCeDl-Enq6t8261iEM");

    public static Map<String, Set<Notification>> notificationMap = new HashMap<String, Set<Notification>>();

    /**
     * System sends notification by GCM/iOS.
     *
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public static boolean sendDirectNotification(String device_id, DEVICE_TYPE device_type, Notification notification) {

        if (device_id == null )
            return false;

        try {
            sendFireBaseMessage(device_id, notification);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * System sends notification by GCM/iOS.
     *
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public static boolean sendNotification(String user_id, String device_id, DEVICE_TYPE device_type, Notification notification) {

        String jsonString = Json.toJson(notification).toString();

        //if(notification.ensure == null )

		/*
         * If Device Id or type is null dont send any GCM notification, as the
		 * user is probably not registered. Once User is registered the
		 * notifications will go to him.
		 */
        if (device_id == null )
            return false;

        try {

            if (jsonString.length() > 2048) {
                notificationMap.get(user_id).add(notification);
                notification = new Notification();
                notification.action = ACTION.GET_NOTIFICATION;
                sendFireBaseMessage(device_id, notification);
            } else {
                sendFireBaseMessage(device_id, notification);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Set<Notification> getNotifications(String user_id) {
        if (notificationMap.get(user_id) == null)
            return null;

        return notificationMap.remove(user_id);
    }

    public static void sendFireBaseMessage(String device_id, Notification notification) {
        ExecutorUtil.executeNow(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "key=" + GOOGLE_SERVER_KEY);

                    conn.setDoOutput(true);

                    FCMessage fcMessage = new FCMessage();
                    fcMessage.to = device_id;
                    fcMessage.data.put("notification", notification);

                    String input = Json.toJson(fcMessage).toString();
                    OutputStream os = conn.getOutputStream();
                    os.write(input.getBytes());
                    os.flush();
                    os.close();

//                    if (conn.getResponseCode() != 200) {
                        System.out.println("\nSending 'POST' request to URL : " + url);
                        System.out.println("Post parameters : " + input);
                        System.out.println("Response Code : " + conn.getResponseCode());

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        System.out.println(response.toString());
//                    }

                    Logger.error("Sent Successfully.");
                } catch (Exception e) {
                    Logger.error(e.getMessage(), e);
                }
            }
        });

    }

}
