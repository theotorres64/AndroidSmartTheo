package fr.isen.torres.androidsmartdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.torres.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                ScanScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen() {
    var isScanning by remember { mutableStateOf(false) }
    val devices = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan BLE") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isScanning) "Scanning for devices..." else "Scan stopped",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            // Bouton pour démarrer ou arrêter le scan
            Button(
                onClick = {
                    isScanning = !isScanning
                    if (isScanning) {
                        // Simulation d'appareils trouvés
                        devices.clear()
                        devices.addAll(listOf("Device 1", "Device 2", "Device 3"))
                    } else {
                        devices.clear()
                    }
                },
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(0.5f)
            ) {
                Text(text = if (isScanning) "Arrêter le scan" else "Démarrer le scan")
            }

            // Liste des appareils détectés
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(devices) { device ->
                    Text(
                        text = device,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
