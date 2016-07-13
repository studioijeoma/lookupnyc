package com.ekeneijeoma.lookup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent1) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent1);
        int transition = event.getGeofenceTransition();

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                transition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> geofences = event.getTriggeringGeofences();
            ArrayList<String> geofenceIdsList = new ArrayList();

            for (Geofence geofence : geofences)
                geofenceIdsList.add(geofence.getRequestId());

            Intent intent2 = new Intent("geofenceTransition");
            intent2.putExtra("transition", transition);
            intent2.putStringArrayListExtra("ids", geofenceIdsList);

            context.sendBroadcast(intent2);
        }
    }
}