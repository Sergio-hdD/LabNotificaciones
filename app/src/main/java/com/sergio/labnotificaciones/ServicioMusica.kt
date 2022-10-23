package com.sergio.labnotificaciones

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ServicioMusica : Service(){

    private var mediaPlayer : MediaPlayer? = null //Puede ser nulo
    private var CHANEL_ID : String = "Musica"

    //Configuro el mediaPlayer
    private fun configurarMediaPlayer(){
        mediaPlayer = MediaPlayer.create(this, R.raw.musica)
        mediaPlayer?.setOnCompletionListener {
            stop()
        }
    }

    private fun stop() {
        stopMusic() //Termino/paro el mediaPlayer
        stopNotificacion() //Termino las notificación, si hace falta
        stopSelf() //Paro el servicio
    }

    override fun onDestroy() {//además de lo que hace por defecto, debe parar la música y la notificación
        stopMusic()
        stopNotificacion()
        super.onDestroy()
    }
    private fun stopMusic() {
        mediaPlayer?.stop() //Que pare
        mediaPlayer?.release() //Que saque lo que tenga adentro
        mediaPlayer = null //La volvemos a inicializar en nulo como la teniamos al comienzo
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)//Esta notación es para poder verificar la versión de código que tiene android
    private fun crearCanalNotificaciones(){
        val importancia = NotificationManager.IMPORTANCE_LOW
        val canal = NotificationChannel(CHANEL_ID, "Servicio de Musica", importancia)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(canal)
    }

    private fun crearNotificacion(): Notification{
        //Pausa
        val intentPausa = Intent(this, ServicioMusica::class.java) //desde acá hasta acá mismo
        intentPausa.putExtra("pause", true)
        val piPausa = PendingIntent.getService(this, 1, intentPausa, PendingIntent.FLAG_UPDATE_CURRENT) //Que esté pendiente y actualiza el estado actual del servicio

        val pausaAccion = NotificationCompat.Action.Builder(
            R.drawable.ic_play, "Play/Pausa", piPausa
        ).build() //.build() para que se construya

        //Stop
        val intentStop = Intent(this, ServicioMusica::class.java) //desde acá hasta acá mismo
        intentStop.putExtra("stop", true)
        val piStop = PendingIntent.getService(this, 2, intentStop, PendingIntent.FLAG_UPDATE_CURRENT) //Que esté pendiente y actualiza el estado actual del servicio

        val stopAccion = NotificationCompat.Action.Builder(
            R.drawable.ic_stop, "Stop", piStop
        ).build() //.build() para que se construya

        return NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle("Reproduciendo Musica")
            .addAction(pausaAccion)
            .addAction(stopAccion)
            .setOngoing(true) //Para que se siga visualizando
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_play)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noficacion = crearNotificacion()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Si la versión de SDK es mayor o igual a la versión de código que tiene android
            crearCanalNotificaciones() //Creo el canal, si es necesario
            startForeground(1, noficacion) //Lanzo la notificación
        } else {
            NotificationManagerCompat.from(this).notify(1, noficacion) //Directamente lanzo la notificación
        }

        if (intent?.getBooleanExtra("stop", false) == true) { //Si el intent que recibimos por parámetros tiene un boolean que se llama stop en true
            stop() //Paramos el servicio (el "stop" del getBooleanExtra no lo cargamos nosotros, ya que lo carga el "stopService(intent)" que pusimos en el MainActivity)
        } else { ////Si el intent que recibimos por parámetros NO tiene un boolean que se llama stop en true
            //revisar si existe el mediaPlayer
            if (mediaPlayer == null){
                configurarMediaPlayer()
            }

            //Ya configurado
            if (intent?.getBooleanExtra("pause", false) == true){
                if (mediaPlayer?.isPlaying == true) { //si está ejecutando
                    mediaPlayer?.pause() //Lo pauso
                } else {
                    mediaPlayer?.start() //Que arranque
                }
            } else {
                mediaPlayer?.start() //Que arranque
            }
        }

        return START_STICKY
    }

    //Solo es necesario parar la notificación cuando la ntificación es de las anteriores,
    // si no usamos CHANEL (y no es necesario crearlo), quiere decir que estamos usando
    // el manager anterior y, por ende, en ese caso se debe terminar la notificación
    private fun stopNotificacion(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //Si no usamos CHANEL (la versión de SDK es menor a la versión de código que tiene android)
            NotificationManagerCompat.from(this).cancel(1)
        }
    }

}