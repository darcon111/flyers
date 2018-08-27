package com.advancehdt.flyers.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.advancehdt.flyers.R;
import com.advancehdt.flyers.config.AppPreferences;
import com.advancehdt.flyers.config.Constants;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUser,txtPass;
    private Button btnLogin;
    private String TAG = LoginActivity.class.getName();
    private AlertDialog message;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private View alert;
    private AppPreferences app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        app = new AppPreferences(getApplicationContext());
        if (!app.getUser().equals(""))
        {
            openMain();
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/RobotoLight.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        txtUser=(EditText)findViewById(R.id.txtUser);
        txtPass=(EditText)findViewById(R.id.txtPass);
        btnLogin=(Button)findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(txtUser.getText().toString().trim().equals(""))
                {
                    txtUser.setError(getString(R.string.error_user));

                    return;
                }
                if(txtPass.getText().toString().trim().equals(""))
                {
                    txtPass.setError(getString(R.string.error_pass));

                    return;
                }


                loginTask(txtUser.getText().toString(),txtPass.getText().toString());

            }
        });

        LayoutInflater inflater = getLayoutInflater();
        alert = inflater.inflate(R.layout.alert, null);


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private  void loginTask(final String username,final String password)
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
            jsonBody.put("password", password);
            jsonBody.put("imei", "12345578");
            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_SERVER + "rest_users/login", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_RESPONSE", response);
                    JSONObject res = null;
                    progressDialog.dismiss();
                    try {
                        res = new JSONObject(response);

                        if (res.getString("isAuthed").equals("true"))
                        {


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                    ActivityCompat.requestPermissions(LoginActivity.this,
                                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            PERMISSION_REQUEST_CODE);


                                else {
                                    app.setUser(txtUser.getText().toString().trim());
                                    openMain();
                                }
                            }else
                            {
                                app.setUser(txtUser.getText().toString().trim());
                                openMain();
                            }




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
                    //headers.put("Content-Type", "application/json");

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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openMain();
                    app.setUser(txtUser.getText().toString().trim());
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void openMain()
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void alert(String msg)
    {
        message = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialog).create();
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





}
