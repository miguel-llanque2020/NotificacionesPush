package com.desarrollo.notificacionespushloja.servicio;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.desarrollo.notificacionespushloja.actividades.MainActivity;
import com.desarrollo.notificacionespushloja.app.Config;
import com.desarrollo.notificacionespushloja.util.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onNewToken(String s){
        super.onNewToken(s);
        Log.e("NEW TOKEN",s);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.e(TAG, " DESDE:" + remoteMessage.getFrom());
        if(remoteMessage == null)return;
        if(remoteMessage.getNotification() != null){
            Log.e(TAG, "Cuerpo de la Notificación: "
                    + remoteMessage.getNotification().getBody());
            manejarNotificacion(remoteMessage.getNotification().getBody());
        }

        if(remoteMessage.getData().size() > 0){
            Log.e(TAG, " Darga de Data: "
                    + remoteMessage.getData().toString());
            try{
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                interpretarMensaje(jsonObject);
            }catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Excepción : " + e.getMessage());
            }
        }
    }

    private void manejarNotificacion(String mensaje){
        if(!NotificationUtils.isAppIsInBackground(getApplicationContext())){
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("mensaje",mensaje);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationAlarm();
        }
    }

    private void interpretarMensaje(JSONObject jsonObject){
        Log.e(TAG,"push JSON: " + jsonObject.toString());

        try{
            JSONObject datos = jsonObject.getJSONObject("data");
            String titulo = datos.getString("title");
            String mensaje = datos.getString("message");
            boolean isBackground = datos.getBoolean("is_background");
            String urlImagen = datos.getString("image");
            String timeStamp = datos.getString("timestamp");
            JSONObject payload = datos.getJSONObject("payload");

            Log.e(TAG,"titulo: " + titulo);
            Log.e(TAG,"mensaje: " + mensaje);
            Log.e(TAG,"background: " + isBackground);
            Log.e(TAG,"imagen: " + urlImagen);
            Log.e(TAG,"timeStamp: " + timeStamp);
            Log.e(TAG,"payload: " + payload.toString());

            if(!NotificationUtils.isAppIsInBackground(getApplicationContext())){
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("mensaje", mensaje);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationAlarm();
            }else{
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("mensaje",mensaje);
                if(TextUtils.isEmpty(urlImagen)){
                    notificationUtils.showNotificationMessage(titulo,mensaje,timeStamp,resultIntent);
                }else{
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    notificationUtils
                            .showNotificationMessage(titulo,mensaje,timeStamp,resultIntent,urlImagen);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

