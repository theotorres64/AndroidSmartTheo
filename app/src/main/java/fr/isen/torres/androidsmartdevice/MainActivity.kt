package fr.isen.torres.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.torres.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContentComponent(innerPadding, onButtonClick = {
                        val intent = Intent(this, ScanActivity::class.java)
                        startActivity(intent)
                    })
                }
            }
        }
    }
}


@Composable
fun MainContentComponent(innerPadding: PaddingValues, onButtonClick: () -> Unit) {
    Column {
        Text(
            text = "Bienvenue sur AndroidSmartDevice",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(innerPadding)
        )
        Text(text = "Pour démarrer le scan des devices BLE, cliquer sur le bouton en dessous")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Centre la colonne au milieu de l'écran
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally // Centre le contenu horizontalement dans la colonne
            ) {
                // Image centrée
                Image(
                    painter = painterResource(R.drawable.logo_bluetooth),
                    contentDescription = "Centered Image"
                )

                Spacer(modifier = Modifier.height(130.dp)) // Espace entre l'image et le bouton

                // Bouton en dessous de l'image
                Button(onClick = onButtonClick) {
                    Text(text = "Valider")
                }
            }
        }
    }
}
