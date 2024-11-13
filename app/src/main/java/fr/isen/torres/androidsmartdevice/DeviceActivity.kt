package fr.isen.torres.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

class DeviceActivity : ComponentActivity() {

    private var bluetoothGatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceName = intent.getStringExtra("deviceName") ?: "Unknown Device"
        val deviceAddress = intent.getStringExtra("deviceAddress") ?: "Unknown Address"
        val deviceRSSI = intent.getStringExtra("deviceRSSI") ?: "Unknown RSSI"
        val device = intent.getParcelableExtra<BluetoothDevice>("device")

        setContent {
            DeviceScreen(deviceName, deviceAddress, deviceRSSI)
        }

        // Connect to the Bluetooth device
        device?.let {
            connectToDevice(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        // Connect to the Bluetooth device using GATT
        bluetoothGatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt?,
                status: Int,
                newState: Int
            ) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i("DeviceActivity", "Connected to GATT server.")
                        runOnUiThread {
                            Toast.makeText(this@DeviceActivity, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                        }
                        // Start service discovery
                        bluetoothGatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i("DeviceActivity", "Disconnected from GATT server.")
                        runOnUiThread {
                            Toast.makeText(this@DeviceActivity, "Disconnected from ${device.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("DeviceActivity", "Services discovered.")
                    // Example: Get the first available service and characteristic
                    val service = gatt?.getService(YOUR_SERVICE_UUID)
                    service?.let {
                        val characteristic = it.getCharacteristic(YOUR_CHARACTERISTIC_UUID)
                        // Read the characteristic (if applicable)
                        gatt.readCharacteristic(characteristic)
                    }
                } else {
                    Log.w("DeviceActivity", "onServicesDiscovered received: $status")
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                    // Handle the characteristic data here
                    Log.i("DeviceActivity", "Characteristic read: ${characteristic.value}")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this@DeviceActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        )
        bluetoothGatt?.close()
    }

    @Composable
    fun DeviceScreen(deviceName: String, deviceAddress: String, deviceRSSI: String) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Device Name: $deviceName")
                Text(text = "Device Address: $deviceAddress")
                Text(text = "RSSI: $deviceRSSI")

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { /* Implement interaction with the device */ }) {
                    Text("Interact with Device")
                }
            }
        }
    }

    companion object {
        // Replace these UUIDs with your actual service and characteristic UUIDs
        private val YOUR_SERVICE_UUID = java.util.UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        private val YOUR_CHARACTERISTIC_UUID = java.util.UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    }
}