package com.advancehdt.flyers.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.advancehdt.flyers.R;
import com.advancehdt.flyers.clases.Campaign;
import com.advancehdt.flyers.clases.GPS;
import com.advancehdt.flyers.config.AppPreferences;
import com.advancehdt.flyers.config.Constants;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends Activity implements GoogleMap.OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMapLongClickListener,LocationListener {

    private GoogleMap map;
    private MapView mapView;
    private String TAG = MainActivity.class.getName();
    private GPS gps = null;
    private AlertDialog message;
    private String shift_id,sign_rockers_id;
    private TextView txtcampania,txtdescripcion;
    public static final String KEY_POLYGON_POINTS_COUNT = "key_polygon_points_count";
    public static final String KEY_LATITUDE = "key_lat";
    public static final String KEY_LONGITUDE = "key_long";

    private static final int INTERVALO = 2000;
    private long tiempoPrimerClick;

    private List<LatLng> mPolygonPoints;
    private ArrayList<Campaign> mListCampaign= new ArrayList<Campaign>();

    private static final int NOTIFICATION_INSIDE_POLYGON = 777;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private GoogleApiClient googleApiClient;
    private AppPreferences app;
    private BottomNavigationView opciones;
    private LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static double lag = 0;
    private static double lng = 0;
    private MarkerOptions myPosition;
    private Marker marker;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
       // mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/RobotoLight.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        app = new AppPreferences(getApplicationContext());


        txtcampania = (TextView) findViewById(R.id.txtcampania);
        txtdescripcion = (TextView) findViewById(R.id.txtdescripcion);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        opciones = (BottomNavigationView) findViewById(R.id.opciones);




        opciones.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_take:

                                Intent intent =  new Intent(MainActivity.this,Main2Activity.class);
                                startActivity(intent);

                            case R.id.nav_out:


                        }
                        return false;
                    }
                });


        mapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map=googleMap;
                //changePosition();
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.setOnMarkerClickListener(MainActivity.this);

            }
        });



        //this.mapaTask("eliseo.mcdermott");

        mPolygonPoints = new ArrayList<>();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        gps = new GPS(MainActivity.this);
        if (!gps.canGetLocation()) {
            settingsrequest();
        }else
        {
            this.mapaTask(app.getUser());
        }




    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.e("lag",latLng.toString());
        map.addMarker(new MarkerOptions().position(latLng).draggable(true));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

    }




    private  void mapaTask(final String username)
    {
        try {


            if(Constants.getNetworkClass(getApplicationContext()).equals("-"))
            {
                alert(getString(R.string.internet));
                return;
            }

            Constants.deleteCache(getApplicationContext());
            final ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progressdialog);
            progressDialog.setCancelable(false);


            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);


            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_SERVER + "rest_campaigns/available", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_RESPONSE", response);
                    JSONObject res = null;



                    progressDialog.dismiss();
                    try {
                        res = new JSONObject(response);



                        if (res.getString("errorMessage").equals(""))
                        {
                            JSONObject campaign = null;
                            campaign = new JSONObject(res.getString("campaign"));
                            shift_id = campaign.getString("shift_id");
                            sign_rockers_id = campaign.getString("sign_rockers_id");

                            txtcampania.setText(campaign.getString("name"));
                            txtdescripcion.setText(campaign.getString("description"));

                            lag = campaign.getDouble("latitude");
                            lng = campaign.getDouble("longitude");

                            if(campaign.getString("sign_type_id").equals("3"))
                            {
                                JSONArray campaign_coordinates = null;
                                campaign_coordinates =  new JSONArray(campaign.getString("campaign_coordinates"));


                                for (int x=0; x<campaign_coordinates.length();x++)
                                {
                                    Campaign temp= new Campaign();
                                    ArrayList<LatLng> mPolygonPoints = new ArrayList<LatLng>();
                                    PolylineOptions mCoordenadas = new PolylineOptions();

                                    temp.setIs_polygon(campaign_coordinates.getJSONObject(x).getBoolean("is_polygon"));
                                    temp.setValid_area(campaign_coordinates.getJSONObject(x).getBoolean("valid_area"));
                                    temp.setId(campaign_coordinates.getJSONObject(x).getInt("id"));

                                    JSONArray coordinates = new JSONArray(campaign_coordinates.getJSONObject(x).getString("coordinates"));

                                    for (int i = 0; i < coordinates.length(); i++) {
                                        JSONObject jsonobject = coordinates.getJSONObject(i);

                                        mCoordenadas.add(new LatLng(jsonobject.getDouble("latitude"), jsonobject.getDouble("longitude")));
                                        mPolygonPoints.add(new LatLng(jsonobject.getDouble("latitude"), jsonobject.getDouble("longitude")));


                                    }
                                    
                                    
                                    temp.setmCoordenadas(mCoordenadas);
                                    temp.setmPolygonPoints(mPolygonPoints);
                                    mListCampaign.add(temp);
                                    

                                }












                                lag = gps.getLatitude();
                                lng = gps.getLongitude();

                                drawMaps(lag,lng);

                                createLocationRequest();

                                //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, createLocationRequest(), this);

                                startLocationUpdates();
                                //startService(intent);


                            }else
                            {


                                retorno(res.getString("shift"));

                            }


                        }else {


                            retorno(res.getString("errorMessage"));

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_RESPONSE", error.toString());
                    progressDialog.dismiss();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> headers = new HashMap();
                    headers.put("Authorization-Number", "a1b2c3");

                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed(){
        if (tiempoPrimerClick + INTERVALO > System.currentTimeMillis()){
            super.onBackPressed();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            return;
        }else {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        }
        tiempoPrimerClick = System.currentTimeMillis();
    }

    public void settingsrequest()
    {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        this.mapaTask("eliseo.mcdermott");
                        break;
                    case Activity.RESULT_CANCELED:
                        settingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void logut()
    {
        message = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog).create();
        final View alertView = getLayoutInflater().inflate(R.layout.alert2, null);
        Button okButton = (Button) alertView.findViewById(R.id.btnOk);
        Button cancelButton = (Button) alertView.findViewById(R.id.btnCancel);

        TextView text = (TextView) alertView.findViewById(R.id.message);
        message.setView(alertView);
        text.setText(getString(R.string.msg_out));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.setUser("");
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.dismiss();

            }
        });


        message.show();
    }

    public void alert(String msg)
    {
        message = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog).create();
        final View alertView = getLayoutInflater().inflate(R.layout.alert, null);
        Button dialogButton = (Button) alertView.findViewById(R.id.btnOk);
        TextView text = (TextView) alertView.findViewById(R.id.message);
        message.setView(alertView);
        text.setText(msg);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.dismiss();

            }
        });
        message.show();
    }

    public void retorno(String msg)
    {
        message = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog).create();
        final View alertView = getLayoutInflater().inflate(R.layout.alert, null);
        Button dialogButton = (Button) alertView.findViewById(R.id.btnOk);
        TextView text = (TextView) alertView.findViewById(R.id.message);
        message.setView(alertView);
        text.setText(msg);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });
        message.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient.isConnected()) {

            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        for (int i=0;i<mListCampaign.size();i++) {


            if (mListCampaign.get(i).isValid_area()) {

                if (location == null ||  mListCampaign.get(i).getmPolygonPoints()== null ||  mListCampaign.get(i).getmPolygonPoints().size() < 3 || lag == 0 || lng == 0 || location.getLatitude() == lag || location.getLongitude() == lng) {
                    return;
                }

                lag = location.getLatitude();
                lng = location.getLongitude();

                LatLng newPosition = new LatLng(lag, lng);
                marker.setPosition(newPosition);


                String msg = "";
                LatLng triggeredPoint = new LatLng(location.getLatitude(), location.getLongitude());
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                if (PolyUtil.containsLocation(triggeredPoint, mPolygonPoints, true)) {
                    msg += "Inside the polygon geofence.";
                    Log.d(TAG, msg);
                } else {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                    Date date = new Date();
                    alertTask(shift_id, sign_rockers_id, dateFormat.format(date), String.valueOf(lag), String.valueOf(lng));

                    msg += "Outside of polygon geofence.";
                    Log.d(TAG, "Outside of polygon geofence.");
                }
                inboxStyle.setBigContentTitle(msg);
                inboxStyle.addLine("Accuracy: " + location.getAccuracy());
                inboxStyle.addLine("Latitude: " + location.getLatitude());
                inboxStyle.addLine("Longitude: " + location.getLongitude());
                showNotification(msg, NOTIFICATION_INSIDE_POLYGON, inboxStyle);
            }
        }

    }

    protected void startLocationUpdates() {
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
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    private void showNotification(String message, int notifId, NotificationCompat.InboxStyle style) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setStyle(style)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(notifId, builder.build());
    }

    private void drawMaps(double lat,double lng)
    {


        LatLng posicion = new LatLng(lat, lng);
        map.clear();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        myPosition = new MarkerOptions()
                .position(posicion);

        marker = map.addMarker(myPosition);


        map.moveCamera(CameraUpdateFactory.newLatLng(posicion));
        // Move the camera instantly to Sydney with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 17));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);


        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(posicion)      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.setOnMapLongClickListener(MainActivity.this);


        for (int i=0;i< mListCampaign.size();i++)
        {
            Polyline polyline = map.addPolyline(mListCampaign.get(i).getmCoordenadas()
                    .add()
                    .width(4)
                    .color(Color.RED));
        }




    }

    private  void alertTask(final String shift_id,final String sign_rockers_id,final String date_alert,final String latitude,final String longitude)
    {
        try {


            if(Constants.getNetworkClass(getApplicationContext()).equals("-"))
            {
                alert(getString(R.string.internet));
                return;
            }

            Constants.deleteCache(getApplicationContext());
            /*final ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progressdialog);
            progressDialog.setCancelable(false);*/


            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("shift_id", shift_id);
            jsonBody.put("signrocker_id", sign_rockers_id);
            jsonBody.put("date_alert", date_alert);
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);


            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_SERVER + "rest_campaigns/addAlert", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_RESPONSE", response);
                    JSONObject res = null;



                    //progressDialog.dismiss();
                    try {
                        res = new JSONObject(response);



                        if (res.getString("errorMessage").equals(""))
                        {



                        }else {


                            alert(res.getString("errorMessage"));

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_RESPONSE", error.toString());
                   // progressDialog.dismiss();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> headers = new HashMap();
                    headers.put("Authorization-Number", "a1b2c3");

                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }







}
