package com.example.smokemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smokemap.navigation.SmokeMapNavGraph
import com.example.smokemap.ui.theme.SmokeMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmokeMapTheme {
                SmokeMapNavGraph()
            }
        }
    }
}
