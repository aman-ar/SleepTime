package com.example.amanarora.activityrecognitionsimple;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    public GoogleApiClient mApiClient;


    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private TextView activitytextView;
    private TextView updateTimeTextView;
    private ListView blockListView;
    private TextView timeTextView;
    private Button mButton;
    private Button detailsButton;
    private String STATUS = "Start";
    public final static String DATA_ACTIVITY = "Data Activity";
    private ArrayList<Block> blocksList;
    private ArrayList<Block> mergeList;
    ArrayList<Block> list;
    private int seconds = 0;
    private boolean running;
    public Handler handler;
    public Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialising the UI components //
        activitytextView = (TextView) findViewById(R.id.activityTextView);
        //updateTimeTextView = (TextView) findViewById(R.id.updateTimeTextView);
        mButton = (Button) findViewById(R.id.startButton);
        detailsButton = (Button) findViewById(R.id.showButton);
        //Declaring the Broadcast Receiver //
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        //Building the Google API client to connect to Google Play Services //
        mApiClient = new GoogleApiClient.Builder(this).addApi(ActivityRecognition.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mApiClient.connect();

        // Initialising the blockList and the adapters //
        blocksList = new ArrayList<Block>();

        //blockListView = (ListView) findViewById(R.id.blocksList);
        //mBlockAdapter = new BlockAdapter(this, blocksList);
        //blockListView.setAdapter(mBlockAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        //Register the broadcast receiver to get Sensor Updates in background from ActivityRecognisedService.java//
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter("com.google.android.gms.location.activityrecognition.BROADCAST_ACTION"));
        super.onStart();
    }

    @Override
    public void onStop() {

        // Unregister the broadcast receiver that was registered during onStart().
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    // Implementing the GoogleAPI connection methods.

    @Override
    public void onConnected(Bundle bundle) {

        // Inform the user that Services are now connected //
        Toast.makeText(this, "Google Client Connected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // Inform the user that coneection to GooglePlay has failed //
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();

    }

    // This method is called when the Start/Stop button is pressed.
    // If the Button says "Start", then on clicked it will that Activity Recognition Service
    // and will start detecting the state of the User (Still or Moving)
    public void buttonClicked(View v) throws ParseException {

        if (STATUS == "Start") {
            if (!mApiClient.isConnected()) {
                // Check if the Client is connected before requesting Sensor Updates //
                Toast.makeText(this, "Google Client is not Connected", Toast.LENGTH_SHORT).show();
            }

            // If the services are connected, then Update the Current UI and request Activity Updates using A
            // ActivityRecognitionApi ..

            else {
                mButton.setText("Stop");
                STATUS = "Stop";
                detailsButton.setEnabled(false);
                //On pressing start, reset the timer, update the UI and start requesting for updates again //

                activitytextView.setText("");
                activitytextView.setHint("Activity");


                // Start getting updates and timer //

                running = true;
                if (handler != null) {
                    handler.removeCallbacks(runnable);
                }
                runTimer();

                // Requesting for updates in every 5 minutes //

                ActivityRecognition.
                        ActivityRecognitionApi.requestActivityUpdates(mApiClient, 300000, getActivityDetectionPendingIntent()).setResultCallback(this);


            }

        }
        else
        {
            mButton.setText("Start");
            STATUS = "Start";

            // startGetting Physical Data..

            String endTime = getFormattedTime(System.currentTimeMillis());
            mergeList = getPhysicalData(blocksList, endTime);
            displayList(mergeList);
            // To be Removed //
            /*list = new ArrayList<Block>();
            Block b1 = new Block("7:30", "8:00", "STILL");
            Block b2 = new Block("8:00", "8:09", "Moving");
            Block b3 = new Block("8:30", "9:00", "STILL");
            Block b4 = new Block("9:00", "9:08", "Moving");
            list.add(b1);
            list.add(b2);
            list.add(b3);
            list.add(b4);
            // Till Here //
            */

            // Stop Requesting Activity Updates //
            ActivityRecognition.
                    ActivityRecognitionApi.removeActivityUpdates(mApiClient, getActivityDetectionPendingIntent());

            running = false;
            handler.removeCallbacks(runnable);
            timeTextView.setText("00:00:00");
            detailsButton.setEnabled(true);

        }

    }


    public void showClicked(View v)
    {

            // Put the list in the Intent and start Details Activity class for displaying data
            // and sleep duration.
        Intent intent = new Intent(this, detailsActivity.class);
        intent.putParcelableArrayListExtra(DATA_ACTIVITY, mergeList); // Here list to be changed to mergeList //
        startActivity(intent);


    }


    // This Method works on the activity updates list and merges consecutive blocks with same states
    // to one block.
    // Ex : Still 08:00 - 08:05
    //      Still 08:05 - 08:10
    //      will be changed to Still 08:00 - 08:10.
    //  This is done till the next block is of a different State.
    private ArrayList<Block> getPhysicalData(ArrayList<Block> blocksList, String endTime) {

        ArrayList<Block> mergeBlock = new ArrayList<Block>();
        //Current Element Data
        String curState = "";
        String curStartTime = "";

        // Merged Block Data
        String blockStart = "";
        String blockEnd = "";
        String blockState = null;


        for (int i = 0; i < blocksList.size(); i++)
        {

            curState = blocksList.get(i).getState();
            curStartTime = blocksList.get(i).getStartTime();
            if (blocksList.size() <= 1) {
                return blocksList;
            }
            // blockState is the state of Current Merging block//
            // If it is null , that means we are on the first element in list.
            if (blockState == null) {

                blockStart = curStartTime;
                blockState = curState;
            }
            else if (i == blocksList.size() - 1)
            {
                if (curState == blockState)
                {
                    //End the block with endTime as ActivityEndTime //
                    blockEnd = endTime;
                    Block block = new Block(blockStart, blockEnd, blockState);
                    mergeBlock.add(block);
                }

            }

             else if (curState != blockState) {
                // End the current Block and add it into the list.
                blockEnd = curStartTime;
                Block block = new Block(blockStart, blockEnd, blockState);
                mergeBlock.add(block);

                // Start a new Block with startTime as curStartTime and blockState as curState;

                blockStart = curStartTime;
                blockState = curState;

            }

        }

        return mergeBlock;
    }

    // Initialising the PendingIntent for getting Activity updates
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, ActivityRecognisedService.class);


        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // The broadCastReceiver Class
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "activity-detection-response-receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String curActivity =
                    intent.getStringExtra("com.google.android.gms.location.activityrecognition.ACTIVITY_EXTRA");
            String updatedTime = intent.getStringExtra("Updated Time");
            updateDetectedActivitiesList(curActivity, updatedTime);
        }
    }


    // This function is passed with Activity State and the Start Time for that activity.
    // For each Activity , a block is created in the list
    protected void updateDetectedActivitiesList(String curActivity, String updatedTime) {

        // Update the View //
        activitytextView.setText(curActivity);

        // Update the Block List //
        String updateTime = updatedTime + "";

        Block block = new Block(updateTime, curActivity);
        blocksList.add(block);


    }

    @Override
    public void onResult(Status status) {

        if (status.isSuccess()) {

        }

    }

    // Return the seconds in HH:mm:ss format
    public String getFormattedTime(long time) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(time * 1000);
        return df.format(time) + "";
    }

    // To be Removed
    public void displayList(ArrayList<Block> list) {

        for (int i = 0; i < list.size(); i++) {
            Log.v("List", list.get(i).getState() + " " + list.get(i).startTime + " " + list.get(i).endTime);
        }

    }

    // Timer Function. Starts a new handler to keep count of the total time of the Session
    // for which user state was tracked.
    private void runTimer() {

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                timeTextView = (TextView) findViewById(R.id.timerTextView);
                timeTextView.setText(time);
                timeTextView.setTypeface(Typeface.SANS_SERIF);
                if (running) {
                    seconds++;
                }
                handler.postDelayed(runnable, 1000);
            }

        };
        handler.post(runnable);


    }
}


