package com.example.amanarora.activityrecognitionsimple;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class detailsActivity extends AppCompatActivity {

    public TableLayout dataTable;
    public ArrayList<Block> blockList;
    public TextView sleepTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        sleepTextView = (TextView) findViewById(R.id.sleepTextView);

        initTable();

        Intent intent = getIntent();
        blockList = intent.getParcelableArrayListExtra("Data Activity");

        // This function fills the data in the table.
        // Dynamically TableRow is created for each block with
        // State , Interval and Duration information
        fillTable(blockList);
        try {
            long sleepTime = getSleepDuration(blockList);
            sleepTextView.setText(findDuration(sleepTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void initTable() {

        dataTable = (TableLayout) findViewById(R.id.dataTable);
        dataTable.setStretchAllColumns(true);
        dataTable.setShrinkAllColumns(true);
        TableRow headRow = new TableRow(this);
        headRow.setPadding(5, 5, 5, 5);
        headRow.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView stateTextView = new TextView(this);
        stateTextView.setText("State");

        stateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        stateTextView.setTextSize(17);
        stateTextView.setGravity(Gravity.LEFT);
        stateTextView.setPadding(10, 10, 10, 10);

        TextView intervalTextView = new TextView(this);
        intervalTextView.setText("Interval");
        intervalTextView.setTypeface(Typeface.SANS_SERIF);
        intervalTextView.setTextSize(17);
        intervalTextView.setGravity(Gravity.LEFT);
        intervalTextView.setPadding(10, 10, 10, 10);

        TextView durationTextView = new TextView(this);
        durationTextView.setText("Duration");

        durationTextView.setTypeface(Typeface.SANS_SERIF);
        durationTextView.setTextSize(17);
        durationTextView.setGravity(Gravity.RIGHT);
        durationTextView.setPadding(10, 10 ,10, 10);

        headRow.addView(stateTextView);
        headRow.addView(intervalTextView);
        headRow.addView(durationTextView);

        dataTable.addView(headRow, 0);

    }

    private void fillTable(ArrayList<Block> blockList) {

        for (int i = 0; i < blockList.size(); i++) {

            TableRow dataRow = new TableRow(this);
            TextView stateTextView = new TextView(this);
            stateTextView.setText(blockList.get(i).state);
            stateTextView.setTextSize(13);
            stateTextView.setTextColor(Color.rgb(33, 33, 33));
            stateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            stateTextView.setGravity(Gravity.LEFT);
            stateTextView.setPadding(10, 10, 10, 10);

            TextView intervalTextView = new TextView(this);
            intervalTextView.setText(blockList.get(i).startTime + " - " + blockList.get(i).endTime);
            intervalTextView.setTextColor(Color.rgb(33, 33, 33));
            intervalTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            intervalTextView.setTextSize(13);
            intervalTextView.setGravity(Gravity.LEFT);
            stateTextView.setPadding(10, 10, 10, 10);

            TextView durationTextView = new TextView(this);
            try {
                long interval = Long.parseLong(findInterval(blockList.get(i).endTime, blockList.get(i).startTime) + "");
                durationTextView.setText(findDuration(interval));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            durationTextView.setTextColor(Color.rgb(33, 33, 33));
            durationTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            durationTextView.setTextSize(13);
            durationTextView.setGravity(Gravity.RIGHT);
            stateTextView.setPadding(10, 10, 10, 10);

            dataRow.addView(stateTextView);
            dataRow.addView(intervalTextView);
            dataRow.addView(durationTextView);

            dataTable.addView(dataRow, i + 1);


        }


    }

    private long getSleepDuration(ArrayList<Block> blockList) throws ParseException {


        String sleepStartTime = "";
        String sleepStopTime = "";
        int sleepTime = 0;
        String moveStartTime = "";
        String moveStopTime = "";
        long moveTime = 0;
        long threshHold = 600;
        String curState = "";
        String curStart = "";
        String curEnd = "";

        for (int i = 0; i < blockList.size(); i++) {

            curState = blockList.get(i).state;
            curStart = blockList.get(i).startTime;
            curEnd = blockList.get(i).endTime;
            long curInterval = findInterval(curEnd, curStart);


            if (curInterval >= threshHold)
            {
                if (curState.contentEquals("STILL"))
                {

                    /*if (sleepStopTime.contentEquals(""))
                    {
                        sleepStartTime = curStart;   // first sleep block //
                        sleepStopTime = curEnd;
                    }
                    else
                        sleepStopTime = curEnd;
                    */

                    sleepTime += curInterval;

                }
                else
                {
                    /*if (moveStopTime == "")
                    {
                        moveStartTime = curStart;
                        moveStopTime = curEnd;
                    }
                    else
                        moveStopTime = curEnd;
                    sleepStopTime = "";
                      *///Destroying first Sleep block on getting a big move block //;
                    moveTime += curInterval;
                }
            }
            // If the current interval is less than threshold and state is moving. Just add it to sleepTime
            else
            {
                    sleepTime+= curInterval;

            }

        }
        return sleepTime;

    }

    // Find the duration in HH:mm
    private String findDuration(long seconds) throws ParseException {

        //convert to hh:mm//
        long hours = seconds /3600;
        long minutes = (seconds%3600)/60;
        return String.format("%02d:%02d", hours, minutes);
    }

    // Finds difference b/w 2 timeValues in seconds
    private long findInterval(String endTime, String startTime) throws ParseException {
        //Handle if any value not present. //
        return (convertDateToEpoch(endTime) - convertDateToEpoch(startTime));

    }

    private long convertDateToEpoch(String time) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date = formatter.parse(time);
        return date.getTime() / 1000;
    }


}
