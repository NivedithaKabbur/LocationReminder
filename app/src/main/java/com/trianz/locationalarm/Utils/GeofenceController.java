package com.trianz.locationalarm.Utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
public class GeofenceController implements GoogleApiClient.ConnectionCallbacks {

// region Properties

  private final String TAG = GeofenceController.class.getName();

  Boolean connectedFlag=false;
  private Context context;
  private GoogleApiClient googleApiClient;
  private Gson gson;
  private SharedPreferences prefs;
  private GeofenceControllerListener listener;

  private List<NamedGeofence> namedGeofences;

  public List<NamedGeofence> getNamedGeofences() {
    return namedGeofences;
  }

  private List<NamedGeofence> namedGeofencesToRemove;
  private List<NamedGeofence> namedGeofencesToAdd;

  private Geofence geofenceToAdd;
  private NamedGeofence namedGeofenceToAdd;

// endregion

// region Shared Instance

  private static GeofenceController INSTANCE;

  public static GeofenceController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new GeofenceController();
    }

    return INSTANCE;
  }

// endregion

// region Public

  public void init(Context context) {
    this.context = context.getApplicationContext();

    gson = new Gson();
    namedGeofences = new ArrayList<>();
    namedGeofencesToRemove = new ArrayList<>();
    prefs = this.context.getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
    googleApiClient = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(connectionFailedListener)
            .build();
    googleApiClient.connect();
    loadGeofences();
  }

/*
 public void addGeofences(List<NamedGeofence> namedGeofences, GeofenceControllerListener listener) {
 this.namedGeofencesToAdd = namedGeofences;
 this.geofenceToAdd = namedGeofence.geofence();
 this.listener = listener;
 System.out.println("in addgeofence " + namedGeofence.name);

//connectWithCallbacks(connectionAddListener);
 onCommandAdd();
 }*/

  public void addGeofence(NamedGeofence namedGeofence, GeofenceControllerListener listener) {
    this.namedGeofenceToAdd = namedGeofence;
    this.geofenceToAdd = namedGeofence.geofence();
    this.listener = listener;
    System.out.println("in addgeofence "+ namedGeofence.reminder_msg);

//connectWithCallbacks(connectionAddListener);
    onCommandAdd(namedGeofence);
  }

  public void removeGeofences(List<NamedGeofence> namedGeofencesToRemove, GeofenceControllerListener listener) {
    this.namedGeofencesToRemove = namedGeofencesToRemove;
    this.listener = listener;

//connectWithCallbacks(connectionRemoveListener);
    onCommandRemove();
  }

  public void removeAllGeofences(GeofenceControllerListener listener) {

    namedGeofencesToRemove = new ArrayList<>();
    for (NamedGeofence namedGeofence : namedGeofences) {
      namedGeofencesToRemove.add(namedGeofence);
    }
    this.listener = listener;

//connectWithCallbacks(connectionRemoveListener);
    onCommandRemove();

  }

// endregion

// region Private

  private void loadGeofences() {
    // Loop over all geofence keys in prefs and add to namedGeofences
    Map<String, ?> keys = prefs.getAll();
    for (Map.Entry<String, ?> entry : keys.entrySet()) {
      String jsonString = prefs.getString(entry.getKey(), null);
      NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
      namedGeofences.add(namedGeofence);
    }

// Sort namedGeofences by name
    Collections.sort(namedGeofences);

  }

/*private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
 System.out.println("in making connection");
 googleApiClient = new GoogleApiClient.Builder(context)
 .addApi(LocationServices.API)
 .addConnectionCallbacks(callbacks)
 .addOnConnectionFailedListener(connectionFailedListener)
 .build();
 googleApiClient.connect();
 }*/

  private GeofencingRequest getAddGeofencingRequest(NamedGeofence namedGeofence) {
    List<Geofence> geofencesToAdd = new ArrayList<>();
    geofencesToAdd.add(namedGeofence.geofence());
    System.out.println("in getAdd geofencerequest " + namedGeofence.reminder_msg);
    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
    builder.addGeofences(geofencesToAdd);
    return builder.build();
  }

  private void saveGeofence(NamedGeofence namedGeofence) {
    namedGeofences.add(namedGeofence);
    if (listener != null) {
      listener.onGeofencesUpdated();
    }
    // connectWithCallbacks(connectionAddListener);
    System.out.println("in savegeofence " + namedGeofence.reminder_msg);

    String json = gson.toJson(namedGeofence);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(namedGeofence.id, json);
    editor.apply();
  }

  private void removeSavedGeofences() {
    SharedPreferences.Editor editor = prefs.edit();

    for (NamedGeofence namedGeofence : namedGeofencesToRemove) {
      int index = namedGeofences.indexOf(namedGeofence);
      editor.remove(namedGeofence.id);
      namedGeofences.remove(index);
      editor.apply();
    }

    if (listener != null) {
      listener.onGeofencesUpdated();
    }
  }

  private void sendError() {
    if (listener != null) {
      listener.onError();
    }
  }

// endregion

// region ConnectionCallbacks

  private void onCommandAdd(final NamedGeofence namedGeofence){
    while(connectedFlag==false);
    try {
      Intent intent = new Intent(context, LocationReminderIntentService.class);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(googleApiClient, getAddGeofencingRequest(namedGeofence), pendingIntent);

      result.setResultCallback(new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
          if (status.isSuccess()) {
            System.out.println("in success");
            saveGeofence(namedGeofence);
          } else {
            Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() + " : " + status.getStatusCode());
            sendError();
          }
        }
      });
    }catch(SecurityException se){
      se.printStackTrace();
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  private void onCommandRemove(){
    while(connectedFlag==false);
    List<String> removeIds = new ArrayList<>();
    for (NamedGeofence namedGeofence : namedGeofencesToRemove) {
      removeIds.add(namedGeofence.id);
    }

    if (removeIds.size() > 0) {
      try {
        PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(googleApiClient, removeIds);
        result.setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(Status status) {
            if (status.isSuccess()) {
              removeSavedGeofences();
            } else {
              Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
              sendError();
            }
          }
        });
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }

/* private GoogleApiClient.ConnectionCallbacks connectionAddListener = new GoogleApiClient.ConnectionCallbacks() {
 @Override
 public void onConnected(Bundle bundle) {
 System.out.println("in onConnected");
 try {
 Intent intent = new Intent(context, AreWeThereIntentService.class);
 PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(googleApiClient, getAddGeofencingRequest(), pendingIntent);
 result.setResultCallback(new ResultCallback<Status>() {
 @Override
 public void onResult(Status status) {
 if (status.isSuccess()) {
 System.out.println("in success");
 saveGeofence();
 } else {
 Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() + " : " + status.getStatusCode());
 sendError();
 }
 }
 });
 }catch(SecurityException se){
 se.printStackTrace();
 }catch (Exception e){
 e.printStackTrace();
 }
 }

@Override
 public void onConnectionSuspended(int i) {
 Log.e(TAG, "Connecting to GoogleApiClient suspended.");
 sendError();
 }
 };*/

  private GoogleApiClient.ConnectionCallbacks connectionRemoveListener = new GoogleApiClient.ConnectionCallbacks() {
    @Override
    public void onConnected(Bundle bundle) {
      List<String> removeIds = new ArrayList<>();
      for (NamedGeofence namedGeofence : namedGeofencesToRemove) {
        removeIds.add(namedGeofence.id);
      }

      if (removeIds.size() > 0) {
        try {
          PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(googleApiClient, removeIds);
          result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
              if (status.isSuccess()) {
                removeSavedGeofences();
              } else {
                Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
                sendError();
              }
            }
          });
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }

    @Override
    public void onConnectionSuspended(int i) {
      Log.e(TAG, "Connecting to GoogleApiClient suspended.");
      sendError();
    }
  };

// endregion

// region OnConnectionFailedListener

  private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
      Log.e(TAG, "Connecting to GoogleApiClient failed.");
      sendError();
    }
  };

  @Override
  public void onConnected(Bundle bundle) {
    Handler handler=new Handler();
    Runnable maketrue=new Runnable() {
      @Override
      public void run() {
        connectedFlag=true;
        System.out.println("connected now");
      }
    };
    handler.postDelayed(maketrue,1);
  }

  @Override
  public void onConnectionSuspended(int i) {

  }

// endregion

// region Interfaces

  public interface GeofenceControllerListener {
    void onGeofencesUpdated();
    void onError();
  }

// end region

}