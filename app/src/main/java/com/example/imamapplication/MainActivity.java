package com.example.imamapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class MainActivity extends AppCompatActivity {

    private TextView tvConsultant;
    private TextView tvRoomNo;
    private TextView tvToken;
    private TextView tvMessage;
    private TextView tvStatus;
    private String IpAddress = null;
    private AppProperties prop = null;
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler;
    private View mLoading;
    private boolean boolConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prop = new AppProperties();
        tvConsultant = (TextView)findViewById(R.id.Consultant);
        tvRoomNo = (TextView)findViewById(R.id.RoomNo);
        tvToken = (TextView)findViewById(R.id.Token);
        tvMessage = (TextView)findViewById(R.id.Message);
        tvStatus = (TextView)findViewById(R.id.StatusBar);
        ((TextView)tvMessage).setText("");
        ((TextView)tvStatus).setText("");
        setUIRef();
        hideLoading();
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    do{
                        boolConnected = isConnectedToServer();
                        if(boolConnected == false){
                            boolConnected = false;
                            ((TextView)tvStatus).setText("!CNCTD-Trying To Connect....");
                        }
                        else{

                            boolConnected = true;
                            try {
                                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                                    NetworkInterface intf = en.nextElement();
                                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                        InetAddress inetAddress = enumIpAddr.nextElement();
                                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                            IpAddress = inetAddress.getHostAddress();
                                        }
                                    }
                                }
                            } catch (SocketException ex) {
                                Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            ((TextView)tvStatus).setText("CNCTD-PA-"+ IpAddress.replace(".","") + " " );
                            PlaySound();
                            LoadData(IpAddress);
                            StartConnection(getApplicationContext(),IpAddress);
                        }
                    }while (boolConnected == false);
                    if(boolConnected == false){
                        ((TextView)tvStatus).setText("!CNCTD-Unable To Connect....");
                    }
                }
          }, 7000);
    }

    private void PlaySound()
    {
       MediaPlayer mediaPlayer = new MediaPlayer();
        try
        {
            mediaPlayer = MediaPlayer.create(this, R.raw.notify);
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

//        try {
//            mediaPlayer.setDataSource(getApplicationContext(), defaultRingtoneUri);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
//            mediaPlayer.prepare();
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//                @Override
//                public void onCompletion(MediaPlayer mp)
//                {
//                    mp.release();
//                }
//            });
//            mediaPlayer.start();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void setUIRef()
    {
        //Create a Instance of the Loading Layout
        mLoading = (View) findViewById(R.id.my_loading_layout);
    }

    private void hideLoading()
    {
        /*Call this function when you want progress dialog to disappear*/
        if (mLoading != null)
        {
            mLoading.setVisibility(View.GONE);
        }
    }

    // Ping DNS
    public boolean isConnectedToServer() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 192.168.16.111");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    private void LoadData(String IpAddress){
        if (boolConnected)
        {
            try {
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                String URL = prop.getServerURL() + prop.getServerQMAPI() + IpAddress;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonarray = new JSONArray(response);
                            JSONObject jsonobject = jsonarray.getJSONObject(0);
                            String Room = jsonobject.getString("ConsultantRoom");
                            String strCCPA = jsonobject.getString("ConsultantSystemIP").replace(".","");
                            String strStatusBarText = tvStatus.getText().toString();
                            tvRoomNo.setText(Room);
                            tvStatus.setText(strStatusBarText + " " +"CCPA-"+strCCPA);
                            tvConsultant.setText("");
                        }catch (JSONException err){
                            Log.d("Error", err.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                JSONObject obj = new JSONObject(res);
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                            } catch (JSONException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                });
                requestQueue.add(stringRequest);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Network is not available", Toast.LENGTH_LONG).show();
        }
    }

    public void StartConnection(Context context,String IpAddress)
    {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        mHubConnection = new HubConnection(prop.getServerURL());
        String SERVER_HUB_CHAT_NAME = "QueueHub";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT_NAME);
        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);
        try {
            signalRFuture.get();
            Log.e("SimpleSignalR", mHubConnection.getState().toString());
        } catch (InterruptedException | ExecutionException e) {
            Log.e("SimpleSignalR", e.toString());
            return;
        }

        Log.e("Connection",mHubConnection.getConnectionData());
        String CLIENT_METHOD_BROADAST_MESSAGE = "message";
        mHubProxy.on(CLIENT_METHOD_BROADAST_MESSAGE,
                new SubscriptionHandler1<Object>() {
                    @Override
                    public void run(final Object msg) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                , Object.class);
        mHubConnection.received(new MessageReceivedHandler() {
            @Override
            public void onMessageReceived(final JsonElement json) {
                Log.e("onMessageReceived ", json.toString());
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        JsonObject jsonObject = json.getAsJsonObject();
                        try {
                            JSONArray jsonArray = new JSONArray(jsonObject.get("A").toString());
                            Log.e("Message ", jsonArray.toString());
                            try
                            {
                                if(jsonArray.getString(3).equals(IpAddress)) // (jsonArray.getString(3).equals(IpAddress))
                                {
                                    mLoading = (View) findViewById(R.id.my_loading_layout);
                                    mLoading.setVisibility(View.VISIBLE);

                                    //Display Loading
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(1000);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                       mLoading.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                            catch(InterruptedException ex){
                                                ex.printStackTrace();
                                            }
                                        }
                                    }).start();
                                    PlaySound();
                                    Toast.makeText(getApplicationContext(),"Matched : "+IpAddress+", "+jsonArray.getString(3), Toast.LENGTH_LONG).show();
                                    tvConsultant = (TextView)findViewById(R.id.Consultant);
                                    tvRoomNo = (TextView)findViewById(R.id.RoomNo);
                                    tvToken = (TextView)findViewById(R.id.Token);
                                    tvMessage = (TextView)findViewById(R.id.Message);

                                    String StatusCode = jsonArray.getString(6);
                                    if(StatusCode.equals("9"))
                                    {
                                        ((TextView)tvToken).setText(jsonArray.getString(0));
                                        ((TextView)tvRoomNo).setText(jsonArray.getString(2));
                                        ((TextView)tvConsultant).setText(jsonArray.getString(4));
                                        ((TextView)tvMessage).setText(jsonArray.getString(7));
                                    }else if(StatusCode.equals("7"))
                                    {
                                        ((TextView)tvToken).setText("---");
                                        ((TextView)tvRoomNo).setText(jsonArray.getString(2));
                                        ((TextView)tvConsultant).setText(jsonArray.getString(4));
                                        ((TextView)tvMessage).setText(jsonArray.getString(7));
                                    }else if(StatusCode.equals("11"))
                                    {
                                        ((TextView)tvConsultant).setText(jsonArray.getString(4));
                                    }else if(StatusCode.equals("10"))
                                    {
                                        ((TextView)tvToken).setText("---");
                                        ((TextView)tvRoomNo).setText(jsonArray.getString(2));
                                        ((TextView)tvConsultant).setText("Vaccant");
                                        ((TextView)tvMessage).setText("");
                                    }else
                                     {
                                         ((TextView)tvToken).setText(jsonArray.getString(0));
                                         ((TextView)tvRoomNo).setText(jsonArray.getString(2));
                                         ((TextView)tvConsultant).setText(jsonArray.getString(4));
                                         ((TextView)tvMessage).setText("");
                                     }
                               }
                               else{
                                    Toast.makeText(getApplicationContext(),"Un Matched : "+IpAddress+", "+jsonArray.getString(3), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();
        super.onDestroy();
    }

    public class ReponseModel{
        private String QueueNo;
        private String OPNO;
        private String Room;
        private String ConsultantName;
        private String DisplayIP;
        private String PatientName;
        private String EndSessionflag;
    }

}
