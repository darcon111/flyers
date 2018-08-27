package com.advancehdt.flyers.config;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {



    public static final int CALL_ACTIVITY_CLOSE = 1000;
    //CALL ACTIVITY CLOSE REASONS
    public static final int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    public static final String WIFI_DISABLED = "wifi_disabled";

    public static final String URL_GOOGLE= "https://maps.googleapis.com/maps/api/geocode/json";
    public static final String URL_PUSH= "https://fcm.googleapis.com/fcm/send";
    public static final String FIREBASE_SERVER_KEY= "AAAAbr-sji4:APA91bGSXXIxBrPLXG998BnPGbPnXW1cAYo0iZmdP9mmilO1V8I3rapNr7oWN-9gfxxiPMIEiL_NLxknL8kv785WokJsF-c7lDIx2-1jtIPZtL2iPCdW6LlptgyL99-lY946Mc4-KXSz";
    public static final String GOOGLE_API_KEY="AIzaSyAWc_pX61n4Y1aeRYBk14uPtBlhmtuQMK8";
    public static final String URL_SERVER= "http://138.36.239.11:8085/api/";



    private static final String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  /*get type conetions*/
    public static String getNetworkClass(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info==null || !info.isConnected())
            return "-"; //not connected
        if(info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "0";
            }
        }
        return "0";
    }


    public static boolean validateEmail(String email) {
        // Compiles the given regular expression into a pattern.
        Pattern pattern = Pattern.compile(PATTERN_EMAIL);
        // Match the given input against this pattern
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();

    }



    public static class PushTask extends AsyncTask<String,Void,Void> {


        protected void onPreExecute() {
            //display progress dialog.

        }

        protected Void doInBackground(String... params) {
            String DeviceIdKey="";
            String title=params[0];
            String message=params[1];
            String device=params[2];
            String type=params[3];
            String msg=params[4];
            String firebaseId=params[5];
            //1 android
            //2 ios

            URL url = null;
            try {
                url = new URL(URL_PUSH);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + FIREBASE_SERVER_KEY);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject data = new JSONObject();

                data.put("to", device.trim());
                /*if(type.equals("1")) {
                    data.put("to", "/topics/news");
                }else
                {
                    data.put("to", "/topics/admin");
                }*/
                data.put("collapse_key","demo");
                data.put("delay_while_idle ","true");
                data.put("priority","high");

                JSONObject info = new JSONObject();
                info.put("title", title);
                info.put("body", message);
                info.put("msg",msg);
                //info.put("device",device);
                info.put("firebaseId",firebaseId);
                info.put("click_action", ".activity.NotificationActivity");
                //info.put("icon","ic_notification");
                if(type.equals("2")) {
                    data.put("notification", info);
                }else {
                    data.put("data", info);
                }

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data.toString());
                wr.flush();
                wr.close();

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }


        protected void onPostExecute(int result) {
            // dismiss progress dialog and update ui
        }
    }

    /* bitmap to base64 string */
    public static String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    /* base 64 to bitmap */
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
     /* get degraded color*/
    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }
     /* compare date range*/
    public static boolean compareDateRange(String date1,String date2,String datecurrent)
    {
        SimpleDateFormat sdt=new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d1=sdt.parse(date1);
            Date d2=sdt.parse(date2);
            Date d3=sdt.parse(datecurrent);

            if(d3.getTime()>=d1.getTime() && d3.getTime()<=d2.getTime())
            {
                return true;
            }

            /*if( (d1.equals(d3) || d1.after(d3)) || (d3.equals(d2) || d2.after(d3)))
            {
                return true;
            }*/
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean compareDate(String date1,String date2)
    {
        SimpleDateFormat sdt=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date d1=sdt.parse(date1);
            Date d2=sdt.parse(date2);
            if(d2.getTime()>=d1.getTime())
            {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private static void setSystemLocaleLegacy(Configuration config, Locale locale){
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static void setSystemLocale(Configuration config, Locale locale){
        config.setLocale(locale);
    }

    /* set language */
    public static void setLanguage(String lang, Context context) {
        Locale locale;
        Configuration config = new Configuration();
        locale = new Locale( lang );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(config, locale);
        }else{
            setSystemLocaleLegacy(config, locale);
        }
        context.getResources().updateConfiguration(config, null);



    }
    /* secondary menu with icon*/
    public static CharSequence menuIconWithText(Drawable r, String title) {
        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("  " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_MARK_POINT);
        return sb;
    }
    /* static class for recyclerview touch*/
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent event) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent event) {
                    View child = recyclerView.findChildViewUnder(event.getX(), event.getY());
                    if (child != null && clickListener != null) {
                        //clickListener.onClick(child, recyclerView.getChildPosition(child));
                        clickListener.onClick(child, recyclerView.getChildAdapterPosition(child));

                    }
                }


            });
        }



        @Override
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(motionEvent)) {
                //clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }


    }

    public static interface ClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);
    }

    public static void teclado_oculta(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public static float distancia(double latA, double lngA,double latB,double lngB)
    {
        Location locationA = new Location("punto A");

        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);

        Location locationB = new Location("punto B");

        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);

        BigDecimal bd = new BigDecimal(Float.toString(locationA.distanceTo(locationB)));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

        float temp = bd.floatValue();
        if(temp<1000)
        {
            return 500;
        }else if(temp>=1000 && temp <=10000)
        {
            return 10;
        }else if(temp>=10001 && temp <=25000)
        {
            return 25;
        }else
        {
            return 26;
        }

        //return  bd.floatValue();
    }


    public static String formatoFecha (String fecha)
    {
        String[] temp=fecha.split("-");

        return temp[2]+"-"+temp[1]+"-"+temp[0].substring(2);
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }




}
