package com.heart2heart.be_app.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.heart2heart.be_app.ArrhythmiaReport.dto.SOSRequestDTO;
import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.auth.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
    public String sendNotificationToTopic(String topic, String title, String body) throws FirebaseMessagingException {

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .putData("category", topic)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);

        return response; // Success response is the message ID
    }

    public Boolean sendSOSNotificationToTopic(String topic, User user, SOSRequestDTO sosRequestDTO) throws FirebaseMessagingException {
        String title = String.format("%s has sent SOS Signal", user.getName());
        String body = String.format("Last known location, latitude: %f, longitude: %f", sosRequestDTO.getLat(), sosRequestDTO.getLongitude());
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .putData("type", "SOS")
                .putData("userId", user.getId().toString())
                .putData("name", user.getName())
                .putData("report", "SOS")
                .build();

        String response = FirebaseMessaging.getInstance().send(message);


        return true;
    }

    public Boolean sendSOSNotificationToTopic2(String username, String id) throws FirebaseMessagingException {
        String title = String.format("%s has sent SOS Signal", username);
        String body = String.format("Please reach the person immediately");
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic("SOS")
                .setNotification(notification)
                .putData("type", "SOS")
                .putData("userId", id)
                .putData("name", username)
                .putData("report", "SOS")
                .build();

        String response = FirebaseMessaging.getInstance().send(message);


        return true;
    }

    public String sendReportNotification(String topic, User user, String report) throws FirebaseMessagingException {
        String title = String.format("%s has been detected with %s", user.getName(), report);
        String body = String.format("Please check on the person.");

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .putData("type", report)
                .putData("userId", user.getId().toString())
                .putData("name", user.getName())
                .putData("report", report)
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }

    public String sendReportNotification2(String username, String userId, String report) throws FirebaseMessagingException {
        String title = String.format("%s has been detected with %s", userId, report);
        String body = String.format("Please check on the person");

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic("report")
                .setNotification(notification)
                .putData("type", report)
                .putData("userId", username)
                .putData("name", userId)
                .putData("report", report)
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }
}
