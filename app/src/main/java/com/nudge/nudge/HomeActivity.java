package com.nudge.nudge;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.DataFormatException;

public class HomeActivity extends Activity {

    private ListView homeListView;
    private TextView lastNudge, senderNickName, receiverNickName, sendingCount, receivingCount;
    private Button sendButton;
    private String userID, friendID, userNickName, friendNickName, friendDeviceToken, currentDateTimeString, lastReceivedDateTimeString;

    static HomeActivity instance;

//    ArrayList<HomeModel> dataModels;
//    private static HomeAdapter adapter;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeListView = findViewById(R.id.nudge_listview);
        lastNudge = findViewById(R.id.lastNudge);
        senderNickName = findViewById(R.id.senderNickName);
        sendingCount = findViewById(R.id.sendingCount);
        receiverNickName = findViewById(R.id.receiverNickName);
        receivingCount = findViewById(R.id.receivingCount);
        sendButton = findViewById(R.id.sendButton);

        userID = getIntent().getStringExtra("EXTRA_USERID");
        friendID = getIntent().getStringExtra("EXTRA_FRIENDID");
        userNickName = getIntent().getStringExtra("EXTRA_USERNICKNAME");
        friendNickName = getIntent().getStringExtra("EXTRA_FRIENDNICKNAME");
        friendDeviceToken = getIntent().getStringExtra("EXTRA_FRIENDDEVICETOKEN");

        instance = this;

        getSendingCount(userID);
        getNudgeDatas(friendID);
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        addNudgeDatas(currentDateTimeString);

        senderNickName.setText(userNickName);
        receiverNickName.setText(friendNickName);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!friendDeviceToken.isEmpty()) {
                    sendPushNotification(friendDeviceToken);
                } else {
                    showAlertDialog("Error.", "Your friend has been disconnected now. Please try again later");
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // Get the nudge datas from DB

    public void  getNudgeDatas(String friendID) {
        GetNudgeDatasTask getNudgeDatasTask = new GetNudgeDatasTask(friendID);
        getNudgeDatasTask.execute();
    }

    public class GetNudgeDatasTask extends AsyncTask<Void, Void, String> {
        String friendID;

        GetNudgeDatasTask(String userID) {
            friendID = userID;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=getNudges"+ "&fromUserID=" + friendID + "&toUserID=" + userID);
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
            processGetNudgeDatas(result);

        }

        @Override
        protected void onCancelled() {

        }
    }

    public void processGetNudgeDatas (String responseText) {

        if (responseText.matches("no")) {
            receivingCount.setText("0");
        } else {
            try {
                JSONObject jsonObject = new JSONObject(responseText);
                JSONArray data = jsonObject.getJSONArray("results");
                receivingCount.setText(data.length());
                GlobalApplication.dataModels= new ArrayList<>();
                long MaxTime = 0;
                int maxPostion = 0;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject c = data.getJSONObject(i);
                    GlobalApplication.dataModels.add(new HomeModel(friendNickName + "Nudge", c.getString("timestamp")));

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
                    Date convertedDate = new Date();
                    try {
                        convertedDate = dateFormat.parse(c.getString("timestamp"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (MaxTime < convertedDate.getTime()) {
                        MaxTime = convertedDate.getTime();
                        maxPostion = i;
                    }
                }
                JSONObject c = data.getJSONObject(maxPostion);
                lastReceivedDateTimeString = "Last Nudge: " + c.getString("timestamp") + " (Received)";
                lastNudge.setText(lastReceivedDateTimeString);

                GlobalApplication.adapter = new HomeAdapter(GlobalApplication.dataModels, getApplicationContext());

                homeListView.setAdapter(GlobalApplication.adapter);
                homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        HomeModel homeModel = GlobalApplication.dataModels.get(position);

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    // Add the nudge datas to DB

    public void  addNudgeDatas(String currentDateTimeString) {
        AddNudgeDatasTask addNudgeDatasTask = new AddNudgeDatasTask(currentDateTimeString);
        addNudgeDatasTask.execute();
    }

    public class AddNudgeDatasTask extends AsyncTask<Void, Void, String> {
        String currentDateTimeString;

        AddNudgeDatasTask(String currentDateTime) {
            currentDateTimeString = currentDateTime;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=addNudges"+ "&timestamp=" + currentDateTimeString + "&fromUserID=" + userID + "&toUserID=" + friendID);
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
            processAddNudgeDats(result);

        }

        @Override
        protected void onCancelled() {

        }
    }

    public void processAddNudgeDats (String responseText) {
        if (responseText.matches("no")) {
            showAlertDialog("Error.", "Please try again later.");
        } else {
            showAlertDialog("Success.", "The nudge has been sent successfully.");
            try {
                JSONObject jsonObject = new JSONObject(responseText);
                JSONArray data = jsonObject.getJSONArray("results");
                sendingCount.setText(data.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Get the sendingCount from DB

    public void  getSendingCount(String userID) {
        GetSendingCountTask getSendingCountTask = new GetSendingCountTask(userID);
        getSendingCountTask.execute();
    }

    public class GetSendingCountTask extends AsyncTask<Void, Void, String> {
        String userID;

        GetSendingCountTask(String user) {
            userID = user;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=getSendingCount"+ "&fromUserID=" + userID + "&toUserID=" + friendID);
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
            processGetSendingCount(result);

        }

        @Override
        protected void onCancelled() {

        }
    }

    public void processGetSendingCount (String responseText) {

        if (responseText.matches("no")) {
            sendingCount.setText("0");
        } else {
            try {
                JSONObject jsonObject = new JSONObject(responseText);
                JSONArray data = jsonObject.getJSONArray("results");
                sendingCount.setText(data.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    // Delete the nudge datas from DB

    public void  deleteNudgeDatas(String timeStamp) {
        DeleteNudgeDatasTask deleteNudgeDatasTask = new DeleteNudgeDatasTask(timeStamp);
        deleteNudgeDatasTask.execute();
    }

    public class DeleteNudgeDatasTask extends AsyncTask<Void, Void, String> {
        String timeStamp;

        DeleteNudgeDatasTask(String time) {
            timeStamp = time;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=deleteNudges"+ "&timestamp=" + timeStamp);
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

        }

        @Override
        protected void onCancelled() {

        }
    }

    // send push notification

    public void  sendPushNotification(String friendDeviceToken) {
        SendNotificationTask sendNotificationTask = new SendNotificationTask(friendDeviceToken);
        sendNotificationTask.execute();
    }

    public class SendNotificationTask extends AsyncTask<Void, Void, String> {
        String friendDeviceToken;

        SendNotificationTask(String deviceToken) {
            friendDeviceToken = deviceToken;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpGet get = new HttpGet("http://kidcarejourney.com/request.php?action=sendPushNotification"+ "&friendDeviceToken=" + friendDeviceToken + "&nickName=" + userNickName);
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
            registerNotification(result);

        }

        @Override
        protected void onCancelled() {

        }
    }

    public void registerNotification(String responseText) {

        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

//        addNudgeDatas(currentDateTimeString);
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
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }



}
