package servicetutorial.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by QuanPV
 */

public class GoogleService extends Service implements LocationListener {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 10000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;
    private double latitude_des, longitude_des;

    public GoogleService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Nullable
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
//        data=(String) intent.getExtras().get("data");
        latitude_des = intent.getDoubleExtra("ed_latitude", 0);
        longitude_des = intent.getDoubleExtra("ed_longitude", 0);
        Log.i("onBind", ""+latitude_des+"-"+longitude_des);
        return 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
//        fn_getlocation();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {

                        Log.e("latitude", location.getLatitude() + "");
                        Log.e("longitude", location.getLongitude() + "");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }

            }


            if (isGPSEnable) {
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        Log.e("latitude", location.getLatitude() + "");
                        Log.e("longitude", location.getLongitude() + "");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }
            }


        }

    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void fn_update(Location location) {

        intent.putExtra("latutide", location.getLatitude() + "");
        intent.putExtra("longitude", location.getLongitude() + "");

        double distance = SphericalUtil.computeDistanceBetween(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(latitude_des, longitude_des));
        if (distance < 1000) {
            showNotification();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
            stopService(new Intent(GoogleService.this, GoogleService.class));
        }
        Log.i("distance", "" + distance);
        sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent localPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle("Notification");
        builder.setContentText("You have come here");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        builder.setVibrate(pattern);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        builder.setContentIntent(localPendingIntent);
        builder.setAutoCancel(true);
        builder.setContentInfo("You have come here!");
//        builder.build();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setContentTitle("My notification")
//                .setContentText("Hello World!")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
    }

}
