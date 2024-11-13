package fr.isen.torres.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

class ScanActivity : ComponentActivity() {

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val scanPeriod: Long = 10000 // 10 seconds
    private val devices = mutableStateListOf<ScanResult>()
    private var isScanning by mutableStateOf(false)  // To track if the scan is in progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScanScreen()
        }
    }

    // Request permission for Bluetooth and location access (if needed)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                startScan()
            } else {
                Toast.makeText(this, "Permissions denied, Bluetooth scanning won't work.", Toast.LENGTH_SHORT).show()
            }
        }

    // Check if the required permissions are granted
    private fun isAllPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Request the necessary permissions
    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    // Start Bluetooth scanning
    @SuppressLint("MissingPermission")
    private fun startScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (bluetoothLeScanner != null) {
            bluetoothLeScanner?.startScan(scanCallback)
            Log.d("ScanActivity", "Scanning started...")
        } else {
            Toast.makeText(this, "Bluetooth LE is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    // Stop Bluetooth scanning
    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                this@ScanActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        )
        bluetoothLeScanner?.stopScan(scanCallback)
        Log.d("ScanActivity", "Scanning stopped.")
    }

    // Callback for Bluetooth LE scan results
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            // Add the device to the list if it's not already there, and only if scanning is active
            if (isScanning && devices.none { it.device.address == result.device.address }) {
                devices.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("ScanActivity", "Scan failed with error code: $errorCode")
        }
    }

    // Composable for scanning BLE devices
    @Composable
    fun ScanScreen() {
        val context = LocalContext.current

        // Check and request permissions on start
        if (isAllPermissionsGranted()) {
            if (isScanning) {
                startScan()  // Start scanning if scan is active
            }
        } else {
            requestPermissions()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header text
                Text("Scanning for BLE Devices", style = MaterialTheme.typography.headlineMedium)

                // Progress bar for scan in progress
                if (isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Button to start/stop scan
                Button(
                    onClick = {
                        isScanning = !isScanning
                        if (isScanning) {
                            startScan()  // Start scanning
                        } else {
                            stopScan()  // Stop scanning
                        }
                    }
                ) {
                    Text(text = if (isScanning) "Stop Scan" else "Start Scan")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to reset the list and stop scanning
                Button(onClick = {
                    devices.clear()  // Clear the scanned devices list
                    stopScan()  // Stop scanning when the list is reset
                }) {
                    Text("Reset Device List")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display the scanned devices
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    items(devices) { scanResult ->
                        DeviceItem(scanResult = scanResult)
                    }
                }
            }
        }
    }

    @Composable
    fun DeviceItem(scanResult: ScanResult) {
        val context = LocalContext.current

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Name: ${scanResult.device.name ?: "Unknown Device"}")
            Text(text = "Address: ${scanResult.device.address}")
            Text(text = "RSSI: ${scanResult.rssi}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                // Launch DeviceActivity to connect to the device
                val intent = Intent(context, DeviceActivity::class.java).apply {
                    if (ActivityCompat.checkSelfPermission(
                            this@ScanActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    )
                    putExtra("deviceName", scanResult.device.name ?: "Unknown Device")
                    putExtra("deviceAddress", scanResult.device.address)
                    putExtra("deviceRSSI", scanResult.rssi)
                    putExtra("device", scanResult.device)
                }
                context.startActivity(intent)
            }) {
                Text("Connect")
            }
        }
    }
}
