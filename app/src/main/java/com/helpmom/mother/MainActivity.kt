package com.helpmom.mother

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.helpmom.mother.core.ui.theme.HelpMomTheme
import com.helpmom.mother.navigation.HelpMomNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpMomTheme {
                HelpMomNavHost()
            }
        }
    }
}
