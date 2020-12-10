package com.desarrollo.notificacionespushloja.actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.desarrollo.notificacionespushloja.R;
import com.desarrollo.notificacionespushloja.app.Config;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    TextView txtRegId, txtMensaje;
    BroadcastReceiver broadcastReceiver;
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(MainActivity.this,
                        new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                String nuevoToken = instanceIdResult.getToken();
                                Log.e("NEWTOKEN", nuevoToken);
                                almacenarPreferencia(nuevoToken);
                            }
                        });

        txtRegId = (TextView)findViewById(R.id.reg_id);
        txtMensaje = (TextView)findViewById(R.id.txt_mensaje);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    mostrarFirebaseId();
                }else if(intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    String mensaje = intent.getStringExtra("mensaje");
                    Toast.makeText(getApplicationContext(),"Notificación Push: "
                            + mensaje,Toast.LENGTH_LONG).show();
                    txtMensaje.setText(mensaje);
                }
            }
        };
        mostrarFirebaseId();
    }

    private void almacenarPreferencia(String token){
        SharedPreferences sharedPreferences
                = getApplicationContext().getSharedPreferences(Config.SHARED_PREF,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("REGID", token);
        editor.commit();
    }

    private void mostrarFirebaseId(){
        SharedPreferences sharedPreferences
                = getApplicationContext().getSharedPreferences(Config.SHARED_PREF,0);
        String regId = sharedPreferences.getString("REGID",null);
        Log.e(TAG, "Firebase Id: " + regId);
        if(!TextUtils.isEmpty(regId)){
            txtRegId.setText("Firebase ID: " + regId);
        }else{
            txtRegId.setText("No existe una respuesta de Firebase aún");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver
                , new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver
                , new IntentFilter(Config.PUSH_NOTIFICATION));

        clearNotification();
    }

    public void clearNotification(){
        NotificationManager notificationManager
                = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

}

