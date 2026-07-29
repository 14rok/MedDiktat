package com.meddiktat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.meddiktat.ui.navigation.MedDiktatNavHost
import com.meddiktat.ui.theme.MedDiktatTheme
import dagger.hilt.android.AndroidEntryPoint

/** Einzige Activity (Single-Activity-Architektur). Hostet den Compose-NavHost. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MedDiktatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MedDiktatNavHost()
                }
            }
        }
    }
}
