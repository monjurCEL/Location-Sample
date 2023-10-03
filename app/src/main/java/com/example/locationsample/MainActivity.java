package com.example.locationsample;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.locationsample.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private Location forcedLocation;
    MyDbHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        forcedLocation=new Location("GPS");
        forcedLocation.setLatitude(0.0);
        forcedLocation.setLongitude(0.0);
        dbHelper = new MyDbHelper(this);
        db = dbHelper.getWritableDatabase();
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                });


        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            RxGPSLocationTrack();

        } else {

            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }


        binding.cardView.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(this, "Collecting location data.Please wait..", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.outlet1LastLocation.setText("Last Location: " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
            saveAlertDialog("Outlet1");
        });
        binding.cardView2.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(this, "Collecting location data.Please wait..", Toast.LENGTH_SHORT).show();
                return;

            }
            binding.outlet2LastLocation.setText("Last Location: " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
            saveAlertDialog("Outlet2");
        });
        binding.cardView3.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(this, "Collecting location data.Please wait..", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.outlet3LastLocation.setText("Last Location: " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
            saveAlertDialog("Outlet3");
        });
    }

    private void RxGPSLocationTrack() {
        try {

            mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(true)
                    .setMinUpdateIntervalMillis(1000)
                    .setMaxUpdateDelayMillis(5000)
                    .build();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }


            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        Log.e("TAG", "onLocationResult: " + location.getLatitude()+" "+location.getLongitude());
                        currentLocation = location;
                        getCurrentLocation();
                    }
                }
            };

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = mFusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null);

            locationTask.addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                     forcedLocation = task.getResult();
                    double latitude = forcedLocation.getLatitude();
                    double longitude = forcedLocation.getLongitude();
                    Log.e("TAG", "getCurrentLocation: "+latitude +"     "+longitude );

                }
            });

    }

    private void saveAlertDialog(String outletName) {
        if (currentLocation == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to save this location").setTitle("Save Location!");

        builder.setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    saveLocation(outletName, currentLocation);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.cancel();
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Save Location!");
        alert.show();
    }

    private void saveLocation(String outletName, Location currentLocation) {
        String baseLatitudeColumnName = "base_latitude";
        String baseLongitudeColumnName = "base_longitude";
        double baseLatitude = 0.0;
        double baseLongitude = 0.0;

        DecimalFormat decimalFormat = new DecimalFormat("#.##");




        try {
            String selection = "outlet_name = ? AND base_latitude IS NOT NULL";
            String[] selectionArgs = {outletName};
            Cursor cursor = db.query("my_table", null, selection, selectionArgs, null, null, null);

            ContentValues values = new ContentValues();
            values.put("outlet_name", outletName);
            if (cursor.getCount() > 0) {
                int baseLatitudeColumnIndex = cursor.getColumnIndex(baseLatitudeColumnName);
                int baseLongitudeColumnIndex = cursor.getColumnIndex(baseLongitudeColumnName);
                if (cursor.moveToFirst()) {
                    baseLatitude = cursor.getDouble(baseLatitudeColumnIndex);
                    baseLongitude = cursor.getDouble(baseLongitudeColumnIndex);
                    values.put("base_latitude", baseLatitude);
                    values.put("base_longitude", baseLongitude);
                }

            } else {
                values.put("base_latitude", currentLocation.getLatitude());
                values.put("base_longitude", currentLocation.getLongitude());
            }

            values.put("forced_latitude", forcedLocation.getLatitude());
            values.put("forced_longitude", forcedLocation.getLongitude());
            Double distancefromBaseToForcedLocation = DistanceOfDestinationV2(baseLatitude, baseLongitude,forcedLocation.getLatitude(), forcedLocation.getLongitude());
            values.put("distance_base_forced",  decimalFormat.format(distancefromBaseToForcedLocation));

            values.put("timestamp", getTime());
            values.put("current_latitude", currentLocation.getLatitude());
            values.put("current_longitude", currentLocation.getLongitude());
            Double distance = DistanceOfDestinationV2(baseLatitude, baseLongitude, currentLocation.getLatitude(), currentLocation.getLongitude());
            values.put("distance",   decimalFormat.format(distance));
            if (distance >= 99999999) {
                values.put("distance", 0.0);
            }
            long newRowId = db.insert("my_table", null, values);
        }catch (Exception exception){
            Log.e("TAG", "saveLocation: "+exception );
        }
    }

    private String getTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }


    public static double DistanceOfDestinationV2(double base_latitude, double base_longitude, double _curLatitude, double _curLongitude) {
        double distance;

        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(base_latitude - _curLatitude);
        double lngDiff = Math.toRadians(base_longitude - _curLongitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) + Math.cos(Math.toRadians(_curLatitude)) * Math.cos(Math.toRadians(base_latitude)) * Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        distance = earthRadius * c;

        int meterConversion = 1609;

        return Float.parseFloat(String.valueOf(distance * meterConversion));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0)
            RxGPSLocationTrack();
    }
}