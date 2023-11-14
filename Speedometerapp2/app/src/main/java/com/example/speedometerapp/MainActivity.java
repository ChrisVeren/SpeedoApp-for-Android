package com.example.speedometerapp;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, CompoundButton.OnCheckedChangeListener {


    LocationManager locationManager;
    TextView textView;
    TextView textView2;
    TextView textView11;
    TextView textView10;
    private Switch mphswitch;
    Boolean mph = false;
    EditText editTextNumber;
    Button button5;
    int speedkmh;
    int speedmph;
    boolean speedchecker = true;
    MyTts myTts;
    SQLiteDatabase db;
    Double Lat;
    Double Long;


    // For speedlimit
    private Handler speedCheckHandler = new Handler(Looper.getMainLooper());
    private boolean isSpeedChecking = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("mydb.db",MODE_PRIVATE,null);
        db.execSQL("Create table if not exists Records(" +
                "latitude DOUBLE," +
                "longitude DOUBLE,"+
                "speed INTEGER,"+
                "time TEXT)");


        textView = findViewById(R.id.textView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        textView2 = findViewById(R.id.textView2);


        mphswitch = findViewById(R.id.switch1);
        mphswitch.setOnCheckedChangeListener(this);


        editTextNumber = findViewById(R.id.editTextNumber);


        textView10 = findViewById(R.id.textView10);
        textView11 = findViewById(R.id.textView11);


        button5 = findViewById(R.id.button5);

        // For speedlimit
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button5.getText().toString().equals("set")){
                    button5.setText("remove");
                    startSpeedChecking();
                }else {
                    button5.setText("set");
                    stopSpeedChecking();
                }
            }
        });

        myTts = new MyTts(this);


    }



    private void startSpeedChecking() { // For speedlimit
        isSpeedChecking = true;
        speedCheckHandler.post(speedCheckRunnable);
    }
    private void stopSpeedChecking() { // For speedlimit
        isSpeedChecking = false;
        speedCheckHandler.removeCallbacks(speedCheckRunnable);
    }

    private Runnable speedCheckRunnable = new Runnable() { // For speedlimit
        @Override
        public void run() {

            String unexpectedText = "";
            if (!editTextNumber.getText().toString().equals(unexpectedText)) {

                String limit = editTextNumber.getText().toString();
                int limiterWarning = Integer.parseInt(limit);

                if(mph){
                    if(speedmph < limiterWarning){
                        speedchecker = true;
                    }
                }else{
                    if(speedkmh < limiterWarning){
                        speedchecker = true;
                    }
                }

                // Check if the current speed exceeds the limit
                if (speedkmh > limiterWarning || speedmph > limiterWarning) {

                    if (speedchecker) {
                        // Show an AlertDialog
                        showSpeedLimitAlertDialog();
                        speedchecker = false;
                    }
                }

            }

            // Repeat the check after a delay (every 0.5 second)
            if (isSpeedChecking) {
                speedCheckHandler.postDelayed(this, 500);
            }
        }
    };


    private void showMessage(String Title,String message){ //Records message
        new AlertDialog.Builder(this)
                .setTitle(Title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                })
                .show();
    }


    private void showSpeedLimitAlertDialog() { // Warning for speedlimit
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //getting the time
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        long timestamp = currentDate.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        String formattedDate = sdf.format(date);


        if(mph){
            db.execSQL("INSERT INTO Records VALUES(" + Lat + "," + Long + "," + speedmph + ",'" + formattedDate + "')");;
        }else{
            db.execSQL("INSERT INTO Records VALUES(" + Lat + "," + Long + "," + speedkmh + ",'" + formattedDate + "')");
        }

        myTts.speak("SPEED ALERT!! You have passed the speed limit, please reduce your speed, or increase your speed limit.");
        builder.setTitle("Speed Limit Exceeded")
                .setMessage("You have passed the speed limit, please reduce your speed or increase your speed limit.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Thanks for noticing", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }




    public void StartRecording(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }



    double totalDistance = 0.0;
    Location lastLocation;
    @Override
    public void onLocationChanged(@NonNull Location location) {


        Lat = location.getLatitude();
        Long = location.getLongitude();


        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance;


            if(mph == false){
                double totalDistanceInKm = totalDistance / 1000.0;
                textView10.setText(String.valueOf(totalDistanceInKm));
                lastLocation = location;

            }else {
                double totalDistanceInMiles = totalDistance / 1609.344;
                textView10.setText(String.valueOf(totalDistanceInMiles));
                lastLocation = location;
            }

        }else {
            lastLocation = location;
        }


        float speedInMetersPerSecond = location.getSpeed();

        if(mph == false){
            speedkmh = (int) (speedInMetersPerSecond * 3.6f);
            textView.setText(String.valueOf(speedkmh));
            textView2.setText("Km/h");
            textView11.setText("Km");
        }else {
            speedmph = (int) (speedInMetersPerSecond * 2.23694f);
            textView.setText(String.valueOf(speedmph));
            textView2.setText("Mph");
            textView11.setText("Miles");
        }
    }

    public void StopRecording(View view) {
        textView.setText("0");
        speedkmh = 0;
        speedmph = 0;
        totalDistance = 0.0;
        isSpeedChecking = false;
        locationManager.removeUpdates(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        mph = false;
        if(isChecked){
            mph = true;
        }

        if(mph == false){
            textView2.setText("Km/h");
            textView11.setText("Km");
        }else{
            textView2.setText("Mph");
            textView11.setText("Miles");
        }
    }



    public void ShowLimiter(View view) {

        if (button5.getVisibility() == View.INVISIBLE){

            button5.setVisibility(View.VISIBLE);
            editTextNumber.setVisibility(View.VISIBLE);

            button5.setClickable(true);
            editTextNumber.setClickable(true);
        }else {
            button5.setVisibility(View.INVISIBLE);
            editTextNumber.setVisibility(View.INVISIBLE);

            button5.setClickable(false);
            editTextNumber.setClickable(false);
        }

    }

    public void SetRemoveSpeed(View view) {

    }

    public void ShowRecords(View view) {
        Cursor cursor = db.rawQuery("Select * from Records",null);
        StringBuilder data = new StringBuilder();
        while(cursor.moveToNext()){
            data.append("Lat: "+ cursor.getString(0)+"\n");
            data.append("Long: "+cursor.getString(1)+"\n");
            data.append("Speed: "+cursor.getString(2)+"\n");
            data.append("Time: "+cursor.getString(3)+"\n");
            data.append("-------------------------- \n");
        }
        showMessage("Records of exceeding the limit",data.toString());
    }
}

