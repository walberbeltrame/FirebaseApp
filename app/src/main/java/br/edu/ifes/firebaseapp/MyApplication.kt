package br.edu.ifes.firebaseapp

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)
    }
}