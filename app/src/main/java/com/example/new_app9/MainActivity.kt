package com.example.new_app9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.new_app9.ui.MainScreen
import com.example.new_app9.ui.theme.New_app9Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            New_app9Theme {
                MainScreen()
            }
        }
    }
}
