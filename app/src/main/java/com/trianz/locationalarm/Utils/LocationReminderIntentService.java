package com.trianz.locationalarm.Utils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;
import com.trianz.locationalarm.MainActivity;
import com.trianz.locationalarm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationReminderIntentService extends IntentService {

  // region Properties

  private final String TAG = LocationReminderIntentService.class.getName();

  private SharedPreferences prefs;
  private Gson gson;

  // endregion

  // region Constructors

  public LocationReminderIntentService() {
    super("AreWeThereIntentService");
  }

  // endregion

  // region Overrides

  @Override
  protected void onHandleIntent(Intent intent) {
    prefs = getApplicationContext().getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
    gson = new Gson();

    GeofencingEvent event = GeofencingEvent.fromIntent(intent);
    if (event != null) {
      if (event.hasError()) {
        onError(event.getErrorCode());
      } else {
        int transition = event.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
          List<String> geofenceIds = new ArrayList<>();
          for (Geofence geofence : event.getTriggeringGeofences()) {
            geofenceIds.add(geofence.getRequestId());
          }
          if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            onEnteredGeofences(geofenceIds);
          }
        }
      }
    }
  }

  // endregion

  // region Private

  private void onEnteredGeofences(List<String> geofenceIds) {
    for (String geofenceId : geofenceIds) {
      String reminder_message = "";
      String reminder_place = "";

      // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
      Map<String, ?> keys = prefs.getAll();
      for (Map.Entry<String, ?> entry : keys.entrySet()) {
        String jsonString = prefs.getString(entry.getKey(), null);
        NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
        if (namedGeofence.id.equals(geofenceId)) {
          reminder_message = namedGeofence.reminder_msg;
          reminder_place = namedGeofence.reminder_place;
          break;
        }
      }

      // Set the notification text and send the notification
      String contextMsg =  reminder_message;
      String contextPlace = reminder_place;

      NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

      // Sound for notification
      Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      Notification notification = new NotificationCompat.Builder(this)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setContentTitle(contextPlace)
              .setContentText(contextMsg)
              .setContentIntent(pendingNotificationIntent)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(contextMsg))
              .setSound(alarmSound)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)
              .build();
      notificationManager.notify(0, notification);

    }
  }

  private void onError(int i) {
    Log.e(TAG, "Geofencing Error: " + i);
  }

  // endregion
}

