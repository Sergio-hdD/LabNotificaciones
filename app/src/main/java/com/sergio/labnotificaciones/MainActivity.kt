package com.sergio.labnotificaciones

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //una forma de asignar directamente cuando lo voy a usar una vez
        findViewById<View>(R.id.btnIniciarServicio).setOnClickListener {
            val intent = Intent(this, ServicioMusica::class.java)
            intent.putExtra("pause", true) //Le decimos que la pausa está activa, esto es porque como hice play, la acción que sigue es pausa
            ContextCompat.startForegroundService(this, intent) //que arranque el ítem en este contexto (con estos pasos ya dimos play)
        }

        findViewById<View>(R.id.btnPararServicio).setOnClickListener {
            val intent = Intent(this, ServicioMusica::class.java)
            stopService(intent)
        }

    }
}