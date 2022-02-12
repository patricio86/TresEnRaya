package com.example.tresenrayas;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class ServicioMusica extends Service {

    MediaPlayer mp, mp2;

    public ServicioMusica() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mp = MediaPlayer.create(this, R.raw.juegodetronos);
        mp.setLooping(true);
        mp.start();

        return START_STICKY;
    }

    public void onDestroy(){
        super.onDestroy();

        if(mp != null){
            mp.stop();
            mp.release();
        }
    }
}