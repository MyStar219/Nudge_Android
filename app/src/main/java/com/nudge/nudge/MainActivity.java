package com.nudge.nudge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends Activity {

    final Context context = this;

    private LinearLayout layout;
    private Button validateBtn;
    private EditText validateEdit;
    private String validateToken, friendID, userNickName, friendNickName, friendDeviceToken;

    public static final int DIALOG_LOADING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layout);
        validateBtn = findViewById(R.id.button);
        validateEdit = findViewById(R.id.editText);

        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateToken = validateEdit.getText().toString();
                if (validateToken.matches("")) {
                    layout.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Please insert the Access token.", Toast.LENGTH_LONG).show();
                } else {
                    layout.setVisibility(View.INVISIBLE);
                    sendRegistrationToServer(validateToken);
                    showDialog(DIALOG_LOADING);

                    // delay the time
//                    Thread thread =  new Thread(null, doSomeTask);
//                    thread.start();

                }
            }
        });
    }
    public void  sendRegistrationToServer(String validateToken) {
        SendTokenTask sendTokenTask = new SendTokenTask(validateToken);
        sendTokenTask.execute();
    }

    public class SendTokenTask extends AsyncTask<Void, Void, String> {
        String userID;

        SendTokenTask(String user) {
            userID = user;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String deviceToken = prefs.getString("token", "");
                Log.d("deviceToken", deviceToken);
                Log.d("UserID", userID);
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=validate"+ "&userID=" + userID + "&deviceToken=" + deviceToken);
                HttpClient client = new DefaultHttpClient();
                client.execute(get);
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                String responseText = EntityUtils.toString(entity);
                Log.d("responseText", responseText);
                return responseText;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            checkVAlidateToken(result);

        }

        @Override
        protected void onCancelled() {

        }
    }
//
//    private Runnable doSomeTask = new Runnable() {
//        public void run() {
//            try {
//                //Code of your task
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {}
//            //Done! now continue on the UI thread
//            runOnUiThread(taskDone);
//        }
//    };
//
//    //Since we can't update our UI from a thread this Runnable takes care of that!
//    private Runnable taskDone = new Runnable() {
//        public void run() {
//            dismissDialog(DIALOG_LOADING);
//        }
//    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOADING:
                final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //here we set layout of progress dialog
                dialog.setContentView(R.layout.custom_progress_dialog);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                    }
                });
                return dialog;

            default:
                return null;
        }
    };

    public  void checkVAlidateToken(String responseText) {

        if (responseText.matches("wrong")) {
            dismissDialog(DIALOG_LOADING);
            showAlertDialog("Wrong Access Token.", "Please insert the Access token again.");
//            Toast.makeText(this, "Wrong Access Token. Please insert the Access token again.", Toast.LENGTH_LONG).show();
        } else if (responseText.matches("no")) {
            dismissDialog(DIALOG_LOADING);
            showAlertDialog("Server Error.", "Please insert the Access token again.");
//            Toast.makeText(this, "Server Error. Please try again later.", Toast.LENGTH_LONG).show();
        } else {
            // Please parsing the json file
            jsonParsing(responseText);
        }
    }

    public void sendHomeActivity(String responseText) {
        if (responseText.matches("no")) {
            dismissDialog(DIALOG_LOADING);
            showAlertDialog("Connect Fail.", "Please try again later");
        } else {
            try {
                JSONObject jsonObject = new JSONObject(responseText);
                JSONArray data = jsonObject.getJSONArray("results");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject c = data.getJSONObject(i);
                    friendNickName = c.getString("nickName");
                    friendDeviceToken = c.getString("deviceToken");
                }

                dismissDialog(DIALOG_LOADING);
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("EXTRA_USERID", validateToken);
                intent.putExtra("EXTRA_FRIENDID", friendID);
                intent.putExtra("EXTRA_USERNICKNAME", userNickName);
                intent.putExtra("EXTRA_FRIENDNICKNAME", friendNickName);
                intent.putExtra("EXTRA_FRIENDDEVICETOKEN", friendDeviceToken);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void jsonParsing (String responseText) {
        try {
            JSONObject jsonObject = new JSONObject(responseText);
            JSONArray data = jsonObject.getJSONArray("results");
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);
                friendID = c.getString("friendID");
                userNickName = c.getString("nickName");
            }
            sendFrinedIDToServer(friendID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog (String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    // Hide keyboard when user click anywhere of screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    public void  sendFrinedIDToServer(String friendID) {
        SendFriendTokenTask sendFriendTokenTask = new SendFriendTokenTask(friendID);
        sendFriendTokenTask.execute();
    }

    public class SendFriendTokenTask extends AsyncTask<Void, Void, String> {
        String friendID;

        SendFriendTokenTask(String friend) {
            friendID = friend;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                Log.d("FriendID", friendID);
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=getFriendToken"+ "&friendID=" + friendID);
                HttpClient client = new DefaultHttpClient();
                client.execute(get);
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                String responseText = EntityUtils.toString(entity);
                Log.d("responseText", responseText);
                return responseText;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            sendHomeActivity(result);

        }

        @Override
        protected void onCancelled() {

        }
    }
}
