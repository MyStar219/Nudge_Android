package com.nudge.nudge;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    public MyFirebaseInstanceIDService() {

    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        RegistrationToken(refreshedToken);
    }

    public void  RegistrationToken(String token) {
//        SendTokenTask sendTokenTask = new SendTokenTask(token);
//        sendTokenTask.execute();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public class SendTokenTask extends AsyncTask<Void, Void, String> {
        String m_regId;

        SendTokenTask(String regId) {
            m_regId = regId;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String groupid = prefs.getString("groupid", "0");
                String oldregid = prefs.getString("token", "");
                HttpGet get = new HttpGet("http://wwh-alert.com/twiliosms/push_fcm.php?regid="+ m_regId + "&oldregid=" + oldregid + "&groupid=" + groupid);
                HttpClient client = new DefaultHttpClient();
                client.execute(get);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onCancelled() {

        }
    }
}
