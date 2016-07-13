package com.ekeneijeoma.lookup;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Vibrator;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.ijeoma.Ijeoma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import processing.core.PApplet;
import processing.core.PFont;
import processing.data.Table;
import processing.data.TableRow;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainPApplet extends PApplet {
    String TAG = "MainPApplet";

    Context context;

    Vibrator vibrator;

    ReactiveLocationProvider locationProvider;
    Subscription subscription;

    LocationRequest locationRequest;

    GeofencingRequest geofenceRequest;
    PendingIntent geofencePendingIntent;
    IntentFilter geofenceIntentFilter;
    BroadcastReceiver geofenceReceiver;

    ArrayList<Geofence> geofenceList = new ArrayList<>();
    HashMap<String, Intersection> geofenceMap = new HashMap<>();

    int geofenceEnterRadius = 17;

    int locationTimeout = 1000;
    int activityTimeout = 30000;

    Location cleanLocation;
    Location dirtyLocation;

    int cleanLocationTime = 0;
    int dirtyLocationTime = 0;

    float cleanLocationChange = 0;
    float dirtyLocationChange = 0;

    float cleanLocationSpeed = 0;
    float dirtyLocationSpeed = 0;

    DetectedActivity cleanActivity;
    DetectedActivity dirtyActivity;

    Intersection cleanGeofence = null;
    Intersection dirtyGeofence = null;

    boolean useGeofence = false;
    Intersection nearGeofence = null;

    boolean loaded = false;
    boolean visible = true;
    boolean debugVisible = false;

    Face face;

    PFont debugFont;
    float debugFontSize = 64;

    PFont infoFont;
    float infoFontSize = 48;

    List<Intersection> itemList = new ArrayList<>();

    int itemW = 0;
    int itemH = 120;
    int itemCount = 0;
    int itemPaddingTop = 10;
    int itemMarginLeft = 50;
    int itemMarginRight = 50;

    int tapStart = 0;
    int doubleTapTime = 750;
    int tapCount = 0;

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        context = this.surface.getSurfaceView().getContext();

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        setupIntersections();

        setupLocation();
        startLocation();

        setupGeofences();
        startGeofences();

        if (Settings.useActivity)
            startActivity();

        setupFace();

        frameRate(60);

        itemW = width;
        itemH = (int) (height * .04f);

        debugFontSize = height * .025f;
        infoFontSize = height * .0175f;

        debugFont = createFont("WorkSans-Regular.otf", debugFontSize);
        infoFont = createFont("WorkSans-Regular.otf", infoFontSize);

        if (Settings.useDebug)
            textFont(debugFont, debugFontSize);
        else
            textFont(infoFont, infoFontSize);
    }


    public void draw() {
        background(0);

        Ijeoma.update(frameCount);

        if (debugVisible)
            drawDebug();
        else {
            if (face.visible)
                face.draw();
            else
                drawInfo();
        }

        if (millis() - tapStart > doubleTapTime) {
            tapCount = 0;
            tapStart = millis();
        }
    }

    public void resume() {
        visible = true;
    }

    public void pause() {
        visible = false;
    }

    public void onDestroy() {
        stopLocation();
    }

    public void mousePressed() {
        if (Settings.useDebug) {
            tapCount++;

            if (tapCount == 2 && millis() - tapStart < doubleTapTime) {
                debugVisible = !debugVisible;

                tapCount = 0;
                tapStart = millis();
            }
        }

        if (hasGeofence())
            face.randomLook(25, 0);
    }

    public void drawInfo() {
        pushMatrix();
        translate(width / 2, height / 2);

        textFont(infoFont, infoFontSize);
        textAlign(CENTER, CENTER);

        fill(255);

        if (nearGeofence == null) {
            text("Meet me in the city", 0, 0);
        } else {
            int item = 0;
            int step = (int) (infoFontSize + (infoFontSize * .2f));

            text("Meet me at the crossroads...", 0, step * item++);
            text((int) mToFt(nearGeofence.distance) + "ft away from " + nearGeofence.name, 0, step * item++);
        }

        popMatrix();
    }

    public void updateDebug(Collection c) {
        itemList = new ArrayList(c);
        itemCount = PApplet.min(itemList.size(), height / (itemH + itemPaddingTop)) - 2;

//        Log.e(TAG, String.valueOf(itemList));

        Collections.sort(itemList, new Comparator<Intersection>() {
            public int compare(Intersection i1, Intersection i2) {
                return Float.compare(i1.distance, i2.distance);
            }
        });
    }

    public void drawDebug() {
        int j = 2;
        float step1 = (itemH + itemPaddingTop);
        float step2 = itemH / 2;

        textFont(debugFont, debugFontSize);
        textAlign(LEFT, CENTER);

        fill(255);
        rect(0, step1 * ++j, itemW, itemH);

        fill((Settings.sleeping) ? color(255, 0, 0) : 0);
        textAlign(LEFT, CENTER);
        text(dirtyLocationChange + "m in " + dirtyLocationTime / 1000 + "s at " + dirtyLocationSpeed, itemMarginLeft, step1 * j + step2);
        textAlign(RIGHT, CENTER);

        if (dirtyLocation != null)
            text(dirtyLocation.getAccuracy() + "%/" + dirtyLocation.getSpeed(), itemW - itemMarginRight, step1 * j + step2);

        fill(255);
        rect(0, step1 * ++j, itemW, itemH);

        fill((Settings.sleeping) ? color(255, 0, 0) : 0);
        textAlign(LEFT, CENTER);
        text(cleanLocationChange + "m in " + cleanLocationTime / 1000 + "s at " + cleanLocationSpeed + " (clean)", itemMarginLeft, step1 * j + step2);

        textAlign(RIGHT, CENTER);

        if (cleanLocation != null)
            text(cleanLocation.getAccuracy() + "%/" + cleanLocation.getSpeed(), itemW - itemMarginRight, step1 * j + step2);

        if (Settings.useActivity) {
            fill(255);
            rect(0, step1 * ++j, itemW, itemH);
            if (dirtyActivity != null) {
                fill(0);
                textAlign(LEFT, CENTER);
                text(dirtyActivity.getConfidence() + "% " + activityName(dirtyActivity), itemMarginLeft, step1 * j + step2);
            }

            fill(255);
            rect(0, step1 * ++j, itemW, itemH);
            if (cleanActivity != null) {
                fill(0);
                textAlign(LEFT, CENTER);
                text(cleanActivity.getConfidence() + "% " + activityName(cleanActivity) + " (clean)", itemMarginLeft, step1 * j + step2);
            }
        }

        for (int i = 0; i < itemCount; i++) {
            Intersection g = itemList.get(i);

            float itemX = 0;
            float itemY = step1 * ++j;

            pushMatrix();
            translate(itemX, itemY);

            boolean isDirtyGeofence = dirtyGeofence != null && g.equals(dirtyGeofence);
            boolean isCleanGeofence = hasGeofence() && g.equals(cleanGeofence);

            strokeWeight((isDirtyGeofence) ? 5 : 1);
            stroke((isDirtyGeofence) ? color(255, 0, 0) : 255);
            fill((isCleanGeofence) ? color(0, 255, 0) : 255);
            rect(0, 0, itemW, itemH);

            fill(0);
            textAlign(LEFT, CENTER);
            text(g.name, itemMarginLeft, step2);

            textAlign(RIGHT, CENTER);
            text((int) g.distance + ((i == 0) ? "m" : ""), itemW - itemMarginRight, step2);//convert to mi

            popMatrix();
        }
    }

    public void setupFace() {
        float eyeY = height * .45f;
        float eyeSize = width * .85f;
        int eyeC1 = 0;
        int eyeC2 = 255;
        int irisC1 = 0;
        int irisC2 = 255;
        int irisLineW = 5;
        int pupilC = 0;

        Ijeoma.setup(this);

        face = new Face(this, eyeY, eyeSize, eyeC1, eyeC2, irisC1, irisC2, irisLineW, pupilC);
        face.close();
    }

    public void showFace() {
        if (face != null) {
            face.randomC();

            if (hasGeofence()) {
                face.irisScore(cleanGeofence.score);
            } else
                face.irisScore(1);

            face.open();
        }
    }

    public void hideFace() {
        face.close();
    }

    void setupIntersections() {
        Table intersections = loadTable("nyc.csv", "header");

        for (int j = 0; j < intersections.getColumnCount(); j++) {
            TableRow row = intersections.getRow(j);

            Intersection i = createIntersection(row);
            geofenceMap.put(i.name, i);

            Geofence g = createGeofence(row);
            geofenceList.add(g);
        }
    }

    Intersection createIntersection(TableRow row) {
        Intersection i = new Intersection();

        i.name = row.getString(0);

        i.location.setLongitude(row.getDouble(1));
        i.location.setLatitude(row.getDouble(2));

        i.injuries = row.getInt(3);
        i.fatalities = row.getInt(4);

        i.points = row.getInt(5);
        i.score = row.getInt(6);

        return i;
    }

    public void enterIntersection(Intersection i) {
        cleanGeofence = i;

        if (Settings.useAll) {
            useGeofence = true;
        } else if (Settings.useEvery) {
            Settings.everyCount++;

            if (Settings.everyCount % Settings.every == 0) {
                Settings.everyCount = 0;
                useGeofence = true;
            }
        } else if (Settings.useRandom && Math.random() > .5f) {
            useGeofence = true;
        }

        if (useGeofence) {
            showFace();

            if (!visible)
                sendToast("Look Up you're at " + i.name + "!", Toast.LENGTH_LONG);

            sendVibrate(i.score);
        }
    }

    public void exitIntersection(Intersection i) {
        cleanGeofence = null;

        if (useGeofence) {
            hideFace();
            useGeofence = false;
        }
    }

    void setupLocation() {
        locationRequest = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(locationTimeout);

        locationProvider = new ReactiveLocationProvider(context);
    }

    private void startLocation() {
//        if (Settings.useDebug)
//            sendToast("start location", Toast.LENGTH_SHORT);

        subscription = locationProvider.getUpdatedLocation(locationRequest)
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        updateLocation(location);
                    }
                });
    }

    private void stopLocation() {
//        if (Settings.useDebug)
//            sendToast("stop location", Toast.LENGTH_SHORT);

        subscription.unsubscribe();
    }

    void updateLocation(Location location) {
        //        if (useDebug)
//            sendToast("update location", Toast.LENGTH_SHORT);

        dirtyLocation = location;

        dirtyLocationTime = millis() - dirtyLocationTime;
        dirtyLocationChange = (int) location.distanceTo(dirtyLocation);
        dirtyLocationSpeed = (int) (dirtyLocationChange / dirtyLocationTime);

        if (!loaded || checkLocation(location)) {
            cleanLocation = location;

            cleanLocationTime = millis() - cleanLocationTime;
            cleanLocationChange = (int) location.distanceTo(cleanLocation);
            cleanLocationSpeed = (int) (cleanLocationChange / cleanLocationTime);

            loaded = true;
        }
    }

    boolean checkLocation(Location l) {
        return l.getAccuracy() < 25 &&
                (Settings.useOnFoot && l.getSpeed() < 5) ||
                (Settings.useOnBicycle && (l.getSpeed() > 5 && l.getSpeed() < 10)) ||
                (Settings.useInVehicle && l.getSpeed() > 10);
    }

    void setupGeofences() {
//        if (Settings.useDebug)
//            sendToast("setup geofence", Toast.LENGTH_SHORT);

        geofencePendingIntent = createGeofencingPendingIntent();

        geofenceIntentFilter = new IntentFilter("geofenceTransition");

        geofenceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int transition = intent.getIntExtra("transition", 0);

                ArrayList<String> ids = intent.getStringArrayListExtra("ids");
                String id = ids.get(0);

                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    dirtyGeofence = geofenceMap.get(id);

                    enterIntersection(dirtyGeofence);
                } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    exitIntersection(dirtyGeofence);

                    dirtyGeofence = null;
                } else {
                    sendToast("dwelling in " + id, Toast.LENGTH_LONG);
                }
            }
        };

        context.registerReceiver(geofenceReceiver, geofenceIntentFilter);
    }

    void startGeofences() {
//        if (Settings.useDebug)
//            sendToast("start geofence", Toast.LENGTH_SHORT);

        geofenceRequest = createGeofencingRequest();
        if (geofenceRequest == null) return;

        locationProvider
                .removeGeofences(geofencePendingIntent)
                .flatMap(new Func1<Status, Observable<Status>>() {
                    @Override
                    public Observable<Status> call(Status pendingIntentRemoveGeofenceResult) {
                        return locationProvider.addGeofences(geofencePendingIntent, geofenceRequest);
                    }
                })
                .subscribe(new Action1<Status>() {
                    @Override
                    public void call(Status addGeofenceResult) {
                        if (Settings.useDebug)
                            sendToast(geofenceList.size() + " new intersections added", Toast.LENGTH_SHORT);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (Settings.useDebug)
                            sendToast("error adding geofences", Toast.LENGTH_SHORT);
                    }
                });

//        locationProvider
//                .addGeofences(geofencePendingIntent, geofenceRequest)
//                .subscribe(new Action1<Status>() {
//                    @Override
//                    public void call(Status addGeofenceResult) {
//                        if (Settings.useDebug)
//                            sendToast(geofenceList.size() + " new intersections added", Toast.LENGTH_SHORT);
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        if (Settings.useDebug)
//                            sendToast("error adding geofences", Toast.LENGTH_SHORT);
//                    }
//                });
    }

    void stopGeofences() {
        if (Settings.useDebug)
            sendToast("stop geofence", Toast.LENGTH_SHORT);

        locationProvider.removeGeofences(createGeofencingPendingIntent()).subscribe(new Action1<Status>() {
            @Override
            public void call(Status status) {
                if (Settings.useDebug)
                    sendToast("intersections removed", Toast.LENGTH_SHORT);

                startGeofences();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (Settings.useDebug)
                    sendToast("error removing geofences", Toast.LENGTH_SHORT);
            }
        });


        if (Settings.useDebug)
            sendToast("stop geofence", Toast.LENGTH_SHORT);
    }

    Geofence createGeofence(TableRow row) {
        String id = row.getString(0);
        double lat = row.getDouble(1);
        double lng = row.getDouble(2);

        Geofence g = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lng, geofenceEnterRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        return g;
    }

    private GeofencingRequest createGeofencingRequest() {
        try {
            return new GeofencingRequest.Builder()
                    .addGeofences(geofenceList)
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .build();
        } catch (NumberFormatException ex) {
            if (Settings.useDebug)
                sendToast("error parsing geofences", Toast.LENGTH_SHORT);

            return null;
        }
    }

    private PendingIntent createGeofencingPendingIntent() {
        return PendingIntent.getBroadcast(context, 0, new Intent(context, GeofenceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startActivity() {
        locationProvider.getDetectedActivity(1000) //activityTimeout
                .subscribe(new Action1<ActivityRecognitionResult>() {
                    @Override
                    public void call(ActivityRecognitionResult detectedActivity) {
                        dirtyActivity = detectedActivity.getMostProbableActivity();

                        if (checkActivity(dirtyActivity)) {
                            cleanActivity = detectedActivity.getMostProbableActivity();

                            if (Settings.sleeping) {
                                Settings.sleeping = false;

                                startLocation();

                                if (Settings.useDebug)
                                    sendToast("active, wallpaper waking up", Toast.LENGTH_SHORT);
                            }
                        } else if (checkInActivity(dirtyActivity)) {
                            cleanActivity = detectedActivity.getMostProbableActivity();

                            if (!Settings.sleeping) {
                                Settings.sleeping = true;

                                stopLocation();
                                stopGeofences();

                                if (Settings.useDebug)
                                    sendToast("inactive, wallpaper going to sleep", Toast.LENGTH_SHORT);
                            }
                        }
                    }
                });
    }

    private boolean checkActivity(DetectedActivity activity) {
        if (activity.getConfidence() >= 25) {
            if (Settings.useOnFoot && (activity.getType() == DetectedActivity.ON_FOOT || activity.getType() == DetectedActivity.WALKING || activity.getType() == DetectedActivity.RUNNING)) {
                return true;
            } else if (Settings.useOnBicycle && activity.getType() == DetectedActivity.ON_BICYCLE) {
                return true;
            } else
                return Settings.useInVehicle && activity.getType() == DetectedActivity.IN_VEHICLE;
        } else
            return false;
    }

    private boolean checkInActivity(DetectedActivity activity) {
        if (activity.getConfidence() >= 75) {
            return activity.getType() == DetectedActivity.STILL;
        } else
            return false;
    }

    public void sendToast(String text, int duration) {
        Toast t = Toast.makeText(context, text, duration);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();
    }

    public void sendVibrate(int count) {
        long[] pattern = new long[count * 2 + 1];

        pattern[0] = 0;
        for (int i = 1; i < count * 2; i += 2) {
            pattern[i] = 1000;
            pattern[i + 1] = 50;
        }

        vibrator.vibrate(pattern, -1);
    }

    boolean hasGeofence() {
        return cleanGeofence != null;
    }

    public String activityName(DetectedActivity activity) {
        switch (activity.getType()) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "NA";
        }
    }

    float mToFt(float mi) {
        return mi * 3.281f;
    }
}