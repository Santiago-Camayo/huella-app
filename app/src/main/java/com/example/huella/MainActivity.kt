package com.example.huella

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.huella.data.Api
import com.example.huella.data.Sesion
import com.example.huella.ui.HuellaApp
import com.example.huella.ui.theme.HuellaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Api.sesion = Sesion(applicationContext)
        enableEdgeToEdge()
        setContent {
            HuellaTheme { HuellaApp() }
        }
    }
}
