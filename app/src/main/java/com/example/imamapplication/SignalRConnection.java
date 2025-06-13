package com.example.imamapplication;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import microsoft.aspnet.signalr.client.MessageReceivedHandler;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;


public class SignalRConnection {
    public HubConnection mHubConnection;
    public HubProxy mHubProxy;
    public Handler mHandler;
    ReponseModel reponseModel = new ReponseModel();

    public void StartConnection(Context context){

        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        String serverUrl = "YOUR URL NAME";
        mHubConnection = new HubConnection("http://192.168.16.161:99/");
        String SERVER_HUB_CHAT_NAME = "chatHub";
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
                        Log.e("SignalR ", msg.toString());
                        Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                , Object.class);

        mHubConnection.received(new MessageReceivedHandler() {
            @Override
            public void onMessageReceived(final JsonElement json) {
                Log.e("onMessageReceived ", json.toString());
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(json);
                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                    reponseModel.QueueNo = jsonobject.getString("QueueNo");
                    reponseModel.Room = jsonobject.getString("Room");
                    reponseModel.ConsultantName = jsonobject.getString("ConsultantName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
