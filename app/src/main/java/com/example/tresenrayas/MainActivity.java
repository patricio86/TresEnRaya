package com.example.tresenrayas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1000;
    private final int REQUEST_CODE_PERMISIONS = 1000;
    MediaPlayer mp, mp2;
    int posicion;
    private Toolbar toolbar;

    //botón que se ha pulsado (1 ó 2 jugadores)
    private int numJugadores;
    //array donde guardaremos las casillas del tablero
    private int[] casillas;
    //partida
    private Partida partida;
    private ContentValues valuesp;
    private int persona = 0;
    private String dificultad;
    private ImageButton activarsonido, desactivarsonido;
    private Double puntos;
    private String resultado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activarsonido = (ImageButton)findViewById(R.id.activar);

        //Guardamos cada una de las casillas en el array
        casillas=new int[9];
        casillas[0]=R.id.a1;
        casillas[1]=R.id.a2;
        casillas[2]=R.id.a3;
        casillas[3]=R.id.b1;
        casillas[4]=R.id.b2;
        casillas[5]=R.id.b3;
        casillas[6]=R.id.c1;
        casillas[7]=R.id.c2;
        casillas[8]=R.id.c3;

        ContentValues valuesp, valuespu;

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISIONS);
        }



        Log.i("ACTIVITY","metodo onCreate");



    }
    /***
     * Método asociado al evento onClick de los botones de 1 Jugador y 2 Jugadores
     * @param view
     */

    public void reproducirsonidojuego(View view){

        if(mp2 == null){
            activarsonido.setImageResource(R.drawable.soundon);
            mp2 = MediaPlayer.create(this, R.raw.juegodetronos);
            mp2.start();
            activarsonido.setImageResource(R.drawable.soundoff);
        }else if(mp2 != null){
            mp2.stop();
            mp2.release();
            mp2 = null;
            activarsonido.setImageResource(R.drawable.soundon);
        }
    }

    public void onDestroy(){
        super.onDestroy();

        if(mp2 != null){
            mp2.stop();
            mp2.release();
        }
    }

    public void botonpartidas(View view) {
        Intent partidas = new Intent(getApplicationContext(),ListarPartidas.class);
        startActivity(partidas);
    }

    public void botonranking(View view) {
        Intent ranking = new Intent(getApplicationContext(),ListarRanking.class);
        startActivity(ranking);
    }

    public void inicioJuego(View view) {



        ImageView imagen;



        //reseteamos el tablero
        //recorremos cada una de los elementos del array y a todos le asignamos la imagen de la casilla en blanco
        for (int casilla : casillas) {
            imagen = findViewById(casilla);
            imagen.setImageResource(R.drawable.casilla);
        }

        //determinamos qué botón se ha pulsado
        numJugadores = 1;

        if(view.getId()==R.id.btnDosJugadores){
            numJugadores = 2;
        }

        //comprobamos la dificultad elegida, 0:facil, 1:dificil, 2:extrema
        RadioGroup rgDificultad=findViewById(R.id.radioGroupDificultad);

        int idDif=rgDificultad.getCheckedRadioButtonId();

        int dificultad = 0;


        if(idDif==R.id.rbDificil){
            dificultad=1;


        }else if(idDif==R.id.rbExtremo){
            dificultad=2;


        }



        //comenzamos la partida
        partida=new Partida(dificultad);

        //deshabilitar los botones del tablero mientras dura la partida
        (findViewById(R.id.btnUnJugador)).setEnabled(false);
        (findViewById(R.id.btnDosJugadores)).setEnabled(false);
        (findViewById(R.id.radioGroupDificultad)).setAlpha(0); //lo hacemos transparente

    }

    /***
     * Método asociado al evento onClick de cada una de las casillas de juego
     * @param vista
     */
    public void toqueCasilla(View vista){


        //sólo ejecutaremos el contenido de este método si la partida está comenzada
        if(partida==null){
            return;
        }
        else {
            //comprobamos la casilla en la que se ha pulsado y la guardamos en la variable casillaPulsada
            int casillaPulsada = 0;

            for(int i=0;i<9;i++){
                if (casillas[i]==vista.getId()){ //asignamos a la posición del array el id de la casilla pulsada
                    casillaPulsada=i;
                    break;
                }
            }

            //comprobamos si la casilla está ocupada antes de marcarla
            if(partida.casillaLibre(casillaPulsada)==false){
                return; //nos salimos porque la casilla pulsada está ocupada
            }

            marcarCasilla(casillaPulsada);

            //cambiamos de jugador
            int resultadoJuego=partida.turnoJuego();

            if(resultadoJuego>0){ //o bien hay empate o bien alguien ha ganado
                evaluarFinal(resultadoJuego);
                return; //salimos del método porque alguien ha ganado ya
            }

            //después de marcar la casilla que hemos pulsado hacemos que juege la máquina
            //generamos una casilla al azar
            casillaPulsada=partida.ia();

            //hacemos que si la celda que ha elegido la máquina está ocupada no siga hasta que elija una que
            //esta libre
            while (partida.casillaLibre(casillaPulsada)!=true){
                casillaPulsada=partida.ia();
            }

            //la marcamos
            marcarCasilla(casillaPulsada);

            //volvemos a cambiar el turno
            resultadoJuego=partida.turnoJuego();

            if(mp != null){
                mp.stop();
                mp.release();
                mp = null;
            }
            if(mp == null){
                mp = MediaPlayer.create(this, R.raw.sonidocasilla);
                mp.start();
            }



            //evaluamos si el juego ha finalizado
            if(resultadoJuego>0){ //o bien hay empate o bien alguien ha ganado
                evaluarFinal(resultadoJuego);

            }
        }



        
    }

    /**
     * Método que comprueba si alguien ha ganado el juego o ha habido empate
     * @param resultadoJuego
     */
    private void evaluarFinal(int resultadoJuego) {

        AdminSQLiteOpenHelper helperBBDD = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = helperBBDD.getWritableDatabase();

        valuesp = new ContentValues();
        String mensaje;
        persona++;

        RadioGroup rgDificultad=findViewById(R.id.radioGroupDificultad);
        int idDif=rgDificultad.getCheckedRadioButtonId();

        if (resultadoJuego==1){ //ha ganado el jugador 1
            mensaje="Jugador 1 ha ganado";
            if(idDif==R.id.rbFacil) {
                puntos=1.0;
                dificultad = "facil";
            }else if(idDif==R.id.rbDificil){
                puntos=1.5;
                dificultad = "dificil";
            }else if(idDif==R.id.rbExtremo){
                puntos=3.0;
                dificultad = "extremo";
            }

            resultado = "usu" + persona;

        }else if (resultadoJuego==2){//ha ganado el jugador 2
            mensaje="Jugador 2 ha ganado";
            if(idDif==R.id.rbFacil) {
                puntos=0.5;
                dificultad = "facil";
            }else if(idDif==R.id.rbDificil){
                puntos=1.0;
                dificultad = "dificil";
            }else if(idDif==R.id.rbExtremo) {
                puntos=1.5;
                dificultad = "extremo";
            }
            resultado = "maquina";
        }else{
            mensaje="Empate";
            if(idDif==R.id.rbFacil) {
                puntos=0.5;
                dificultad = "facil";
            }else if(idDif==R.id.rbDificil){
                puntos=1.0;
                dificultad = "dificil";
            }else if(idDif==R.id.rbExtremo) {
                puntos=1.5;
                dificultad = "extremo";
            }
            resultado = "Empate";
        }

        valuesp.put("nombre","usu" + persona);
        valuesp.put("jugador2","maquina");
        valuesp.put("dificultad",dificultad);
        valuesp.put("resultado",resultado);

        db.insert("partidas",null,valuesp);
        db.close();



        Toast.makeText(this,mensaje,Toast.LENGTH_SHORT).show();

        //finalizamos el juego
        partida=null;
        //volvemos a habilitar los controles para que se pueda volver a jugar
        (findViewById(R.id.btnUnJugador)).setEnabled(true);
        (findViewById(R.id.btnDosJugadores)).setEnabled(true);
        (findViewById(R.id.radioGroupDificultad)).setAlpha(1); //lo hacemos transparente


        if(mp != null){
            mp.stop();
            mp.release();
            mp = null;
        }
        if(mp == null){
            mp = MediaPlayer.create(this, R.raw.ganador);
            mp.start();
        }


    }

    /**
     * Método que dibujará la casilla con un círculo o con un aspa
     * @param casilla
     */
    private void marcarCasilla(int casilla){
        ImageView imagen;
        imagen=findViewById(casillas[casilla]); //le asignamos el id de la imagen de la casilla correspondiente a la que hay que marcar

        if(partida.jugador==1){
            imagen.setImageResource(R.drawable.circulo); //si el jugador que está marcando es el 1 le asignamos el círculo a la casilla
        }else{
            imagen.setImageResource(R.drawable.aspa); //si el jugadro es el 2 dibujamos un aspa
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuitem,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.item1){
            Intent partidas = new Intent(getApplicationContext(),ListarPartidas.class);
            startActivity(partidas);
        }
        if(item.getItemId() == R.id.item2){
            Intent ranking = new Intent(getApplicationContext(),ListarRanking.class);
            startActivity(ranking);
        }
        if(item.getItemId() == R.id.item3){
            Intent ranking = new Intent(getApplicationContext(),ModificarUsuarios.class);
            startActivity(ranking);
        }
        return super.onOptionsItemSelected(item);
    }


}