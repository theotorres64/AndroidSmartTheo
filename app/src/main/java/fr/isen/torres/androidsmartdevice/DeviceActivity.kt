package fr.isen.torres.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

class DeviceActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var ledCharacteristic: BluetoothGattCharacteristic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupérer les données envoyées depuis ScanActivity
        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")

        // Initialiser l'appareil Bluetooth
        bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)

        setContent {
            DeviceScreen(deviceName = deviceName, deviceAddress = deviceAddress)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to GATT server. Discovering services...")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from GATT server.")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt.services
                    ledCharacteristic = services?.get(2)?.characteristics?.get(0)
                    Log.d("BLE", "Services discovered: ${services.map { it.uuid }}")
                } else {
                    Log.e("BLE", "Service discovery failed with status $status")
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Characteristic written successfully: ${characteristic.uuid}")
                } else {
                    Log.e("BLE", "Failed to write characteristic: ${characteristic.uuid}")
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun disconnectFromDevice() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("BLE", "Disconnected from device.")
        Toast.makeText(this, "Disconnected from device", Toast.LENGTH_SHORT).show()
    }

    private fun writeToLEDCharacteristic(state: LEDStateEnum) {
        if (ledCharacteristic != null) {
            ledCharacteristic?.value = state.hex
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothGatt?.writeCharacteristic(ledCharacteristic)
            Log.d("BLE", "LED state set to: ${state.name}")
        } else {
            Log.e("BLE", "LED characteristic not found.")
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun DeviceScreen(deviceName: String?, deviceAddress: String?) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Device Info", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Name: ${deviceName ?: "Unknown"}")
            Text("Address: ${deviceAddress ?: "Unknown"}")

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { connectToDevice() },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Connect to Device")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.LED_1) },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn On LED 1")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { writeToLEDCharacteristic(LEDStateEnum.NONE) },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Turn Off LED")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { disconnectFromDevice() },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Disconnect from Device")
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }
}
