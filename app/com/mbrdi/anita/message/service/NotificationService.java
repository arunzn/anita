package com.mbrdi.anita.message.service;

import com.mbrdi.anita.message.model.*;
import com.mbrdi.anita.travel.model.AppTrip;
import com.mbrdi.anita.travel.model.Trip;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import play.i18n.Messages;

public class NotificationService {

    public static void send(User user, Notification notification) {
        if (user != null && user.device_id != null) {
            DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
            MessageService.sendDirectNotification(user.device_id, device_type, notification);
        }
    }

	public static void notifyMessage(MessageGroup mg, Message msg, String from_device_id, String from_user_id) {

		Notification notification = new Notification();
		notification.action = ACTION.READ_MESSAGE;
		notification.type = NOTIFICATION_TYPE.SHOW_CHAT;
		notification.message = msg;
		for (String device_id : mg.devices) {
			if (!device_id.equals(from_device_id)) {
				User user = UserDAO.getByDeviceId(device_id);
				MessageService.sendDirectNotification(device_id, DEVICE_TYPE.ANDROID, notification);
			}
		}
	}

	public static void notifyMessageGroupDelete(MessageGroup mg) {
		if (mg != null) {
			for (String device_id : mg.devices) {
				User user = UserDAO.getByDeviceId(device_id);
				if (user != null) {
					DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
					Notification notification = new Notification();
					notification.action = ACTION.DELETE_MESSAGE_GROUP;
					notification.type = NOTIFICATION_TYPE.SHOW_INFO;
					Message msg = new Message();
					msg.msg_group_id = mg._id;
					notification.message = msg;

					MessageService.sendDirectNotification(device_id, device_type, notification);
				}
			}
		}
	}


	public static void notifyUpdateProfile(User user) {
		if (user != null && user.device_id != null) {
			String device_id = user.device_id;
			DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
			Notification notification = new Notification();
			notification.action = ACTION.UPDATE_PROFILE;
			notification.type = NOTIFICATION_TYPE.HIDDEN;
			MessageService.sendDirectNotification(device_id, device_type, notification);
		}
	}


	public static void notifyUpdateTrip(User user, Trip trip) {
		if (user != null && user.device_id != null) {
			String device_id = user.device_id;
			Notification notification = new Notification();
			DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
			notification.action = ACTION.UPDATE_TRIP;
			notification.type = NOTIFICATION_TYPE.SHOW_INFO;
			if(trip != null)
				notification.trip = new AppTrip(trip);

			MessageService.sendNotification(user._id, device_id, device_type, notification);
		}
	}

	public static void notifyRequestLocation(User user, String sender_id) {
		if (user != null && user.device_id != null) {
			User sender = UserDAO.get(sender_id);
			String device_id = user.device_id;
			DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
			Notification notification = new Notification();
			notification.action = ACTION.UPDATE_LOCATION;
			notification.type = NOTIFICATION_TYPE.SHOW_INFO;
			Message msg = new Message();
			msg.from_id = sender_id;
			msg.from_name = sender.name;
			msg.text = sender.name + " wants to know your location";
			msg.header = "Share Location";
			notification.message = msg;
			MessageService.sendDirectNotification(device_id, device_type, notification);
		}
	}


	public static void notifyDailyUpdate(User user) {
		if (user != null && user.device_id != null) {
			DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
			Notification notification = new Notification();
			notification.action = ACTION.RUN_DAILY_UPDATE;
			notification.type = NOTIFICATION_TYPE.HIDDEN;
			MessageService.sendDirectNotification(user.device_id, device_type, notification);
		}
	}

    public static void notifyUpdateLocation(User user) {
        if (user != null && user.device_id != null) {
			DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
            Notification notification = new Notification();
            notification.action = ACTION.UPDATE_LOCATION;
            notification.type = NOTIFICATION_TYPE.HIDDEN;
            notification.ensure = false;
            MessageService.sendDirectNotification(user.device_id, device_type, notification);
        }
    }



}
