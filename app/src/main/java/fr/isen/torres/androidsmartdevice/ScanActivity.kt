package fr.isen.torres.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.torres.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidSmartDeviceTheme {
                ScanBleScreen(bluetoothAdapter, bluetoothLeScanner, handler, onBackPressed = { finish() })
            }
        }
    }
}
@SuppressLint("MissingPermission")
@Composable
fun ScanBleScreen(
    bluetoothAdapter: BluetoothAdapter?,
    bluetoothLeScanner: BluetoothLeScanner?,
    handler: Handler,
    onBackPressed: () -> Unit,
) {
    var isScanning by remember { mutableStateOf(false) }
    var detectedDevices = remember { mutableStateListOf<ScanResult>() }
    var showBluetoothDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.BLUETOOTH] == true &&
                permissions[Manifest.permission.BLUETOOTH_ADMIN] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted && bluetoothAdapter?.isEnabled == true) {
            startBleScan(bluetoothLeScanner, detectedDevices)
        } else {
            showBluetoothDialog = true
        }
    }

    fun onScanButtonClick() {
        if (bluetoothAdapter?.isEnabled == true) {
            isScanning = !isScanning
            if (isScanning) {
                startBleScan(bluetoothLeScanner, detectedDevices)
            } else {
                stopBleScan(bluetoothLeScanner)
            }
        } else {
            showBluetoothDialog = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedSize by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isScanning) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    if (showBluetoothDialog) {
        BluetoothAlertDialog(
            onDismiss = { showBluetoothDialog = false },
            onEnableBluetooth = {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableBtIntent)
                showBluetoothDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Bouton retour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onBackPressed() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Scan BLE", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .padding(16.dp)
                .graphicsLayer(scaleX = animatedSize, scaleY = animatedSize)
        ) {
            Button(
                onClick = { onScanButtonClick() },
                modifier = Modifier.size(150.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (isScanning) R.drawable.stop else R.drawable.start
                    ),
                    contentDescription = if (isScanning) "Arrêter le scan" else "Démarrer le scan",
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Text(
            text = if (isScanning) "Scan en cours..." else "Appuyez pour scanner",
            fontSize = 16.sp,
            color = if (isScanning) Color.Blue else Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn {
            items(detectedDevices) { result ->
                val signalStrength = result.rssi
                val deviceName = result.device.name ?: "Appareil inconnu"
                val deviceAddress = result.device.address
                val signalColor = when {
                    signalStrength > -50 -> Color.Green
                    signalStrength > -70 -> Color.Yellow
                    else -> Color.Red
                }

                DeviceItem(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    signalStrength = signalStrength,
                    signalColor = signalColor,
                    onClick = {
                        val intent = Intent(context, DeviceActivity::class.java)
                        intent.putExtra("deviceName", deviceName)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}


@Composable
fun DeviceItem(
    deviceName: String,
    deviceAddress: String,
    signalStrength: Int,
    signalColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Vérifiez ici que l'intent est bien configuré pour démarrer DeviceActivity
                val intent = Intent(context, DeviceActivity::class.java).apply {
                    putExtra("deviceName", deviceName)  // Pass the device name
                    putExtra("deviceAddress", deviceAddress)  // Pass the device address
                    putExtra("deviceRSSI", signalStrength)  // Pass the device RSSI
                }
                context.startActivity(intent)  // This should start DeviceActivity
            }
            .background(signalColor.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Text(
            text = "Nom : $deviceName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Adresse : $deviceAddress", fontSize = 14.sp, color = Color.Gray)
        Text(text = "Signal : $signalStrength dBm", fontSize = 14.sp, color = Color.Gray)
    }
}


@SuppressLint("MissingPermission")
fun startBleScan(bluetoothLeScanner: BluetoothLeScanner?, detectedDevices: MutableList<ScanResult>) {
    bluetoothLeScanner?.startScan(object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name != null && detectedDevices.none { it.device.address == result.device.address }) {
                detectedDevices.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Scan failed with error code $errorCode")
        }
    })
}

@SuppressLint("MissingPermission")
fun stopBleScan(bluetoothLeScanner: BluetoothLeScanner?) {
    bluetoothLeScanner?.stopScan(object : ScanCallback() {})
}

@Composable
fun BluetoothAlertDialog(
    onDismiss: () -> Unit,
    onEnableBluetooth: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.White)
        ) {
            Text(text = "Le Bluetooth est désactivé. Souhaitez-vous l'activer ?", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onDismiss) {
                    Text(text = "Annuler")
                }
                Button(onClick = onEnableBluetooth) {
                    Text(text = "Activer Bluetooth")
                }
            }
        }
    }
}

