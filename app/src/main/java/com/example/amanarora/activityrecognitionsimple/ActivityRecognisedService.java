package com.example.amanarora.activityrecognitionsimple;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aman's Laptop on 5/13/2016.
 */
public class ActivityRecognisedService extends IntentService {

    public static final int STATUS_UPDATE = 1;
    public String curActivity;


    public ActivityRecognisedService() {
        super("ActivityRecognisedService");
    }

    public ActivityRecognisedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent))
        {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getMostProbableActivity());

            // BroadCast receiver listens to this intent for the updates when app is in background
            // and passes data to the MainActivity for creation of blocks List.
            Intent localIntent = new Intent("com.google.android.gms.location.activityrecognition.BROADCAST_ACTION");
            localIntent.putExtra("com.google.android.gms.location.activityrecognition.ACTIVITY_EXTRA", curActivity);
            localIntent.putExtra("Updated Time", getFormattedTime(System.currentTimeMillis()));
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        }

    }

    // This function handles the Detected Activity .
    // For this app purpose , only 2 states are used. Still or Moving.
    private void handleDetectedActivities(DetectedActivity mostProbableActivity) {

        switch (mostProbableActivity.getType()) {
            case 3: {
                curActivity = "STILL";

                if (mostProbableActivity.getConfidence() >= 75) {
                    //Log.v("ActivityRecogition", "Still: " + mostProbableActivity.getConfidence());
                    //Log.v("ActivityRecognition", String.valueOf(System.currentTimeMillis()));
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    builder.setContentText("Are you Still?");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle(getString(R.string.app_name));
                    NotificationManagerCompat.from(this).notify(0, builder.build());

                }
                break;
            }

            default: {

                curActivity = "Moving";

                if (mostProbableActivity.getConfidence() >= 60) {
                    //Log.v("ActivityRecogition", count + "Moving: " + mostProbableActivity.getConfidence());
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    builder.setContentText("Are you Moving?");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle(getString(R.string.app_name));
                    NotificationManagerCompat.from(this).notify(0, builder.build());

                }
                break;

            }
        }
    }

    public String getFormattedTime(long time) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(time * 1000);
        return df.format(time)+"";
    }



}
