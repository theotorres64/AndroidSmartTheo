package fr.isen.torres.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat


class DeviceActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var ledCharacteristic: BluetoothGattCharacteristic? = null
    private var button1Characteristic: BluetoothGattCharacteristic? = null
    private var button3Characteristic: BluetoothGattCharacteristic? = null

    // Variable to track the button click count
    private var clickCount by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the data passed from ScanActivity
        val deviceName = intent.getStringExtra("deviceName")
        val deviceAddress = intent.getStringExtra("deviceAddress")

        // Initialize the Bluetooth device
        bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)

        setContent {
            DeviceScreen(deviceName = deviceName, deviceAddress = deviceAddress, clickCount = clickCount)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        vibratePhone()
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected GATT server. Discovering services...")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from GATT server.")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt.services
                    ledCharacteristic = services?.get(2)?.characteristics?.get(0)
                    button1Characteristic = services?.get(2)?.characteristics?.get(1)
                    button3Characteristic = services?.get(3)?.characteristics?.get(0)

                    // Subscribe to notifications for button presses (button1 and button3)
                    if (button1Characteristic != null) {
                        gatt.setCharacteristicNotification(button1Characteristic, true)
                    }
                    if (button3Characteristic != null) {
                        gatt.setCharacteristicNotification(button3Characteristic, true)
                    }

                    Log.d("BLE", "Services discovered: ${services.map { it.uuid }}")
                } else {
                    Log.e("BLE", "Service discovery failed with status $status")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                super.onCharacteristicChanged(gatt, characteristic)
                // Check if the notification is for the button1 or button3 characteristic
                if (characteristic == button1Characteristic || characteristic == button3Characteristic) {
                    incrementClickCount()  // Increment the button press count
                    Log.d("BLE", "Button pressed, incrementing count.")
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Handle missing permission
                return
            }
            bluetoothGatt?.writeCharacteristic(ledCharacteristic)
            Log.d("BLE", "LED state set to: ${state.name}")
        } else {
            Log.e("BLE", "LED characteristic not found.")
        }
    }

    private fun incrementClickCount() {
        clickCount += 1
    }

    @Composable
    fun DeviceScreen(deviceName: String?, deviceAddress: String?, clickCount: Int) {
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

            // Button to connect to device
            Button(
                onClick = { connectToDevice() },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Connect to Device")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons to control LED 1, 2, 3
            LedControlButton(1, LEDStateEnum.LED_1, LEDStateEnum.NONE)
            LedControlButton(2, LEDStateEnum.LED_2, LEDStateEnum.NONE)
            LedControlButton(3, LEDStateEnum.LED_3, LEDStateEnum.NONE)

            Spacer(modifier = Modifier.height(16.dp))

            // Button to disconnect from device
            Button(
                onClick = { disconnectFromDevice() },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Disconnect from Device")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the click count
            Text("Button pressed $clickCount times", fontSize = 18.sp)
        }
    }
    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(1000)
        }
    }
    @Composable
    fun LedControlButton(ledNumber: Int, ledOnState: LEDStateEnum, ledOffState: LEDStateEnum) {
        val ledOn = painterResource(id = R.drawable.led_on)  // Replace with your LED ON .png file
        val ledOff = painterResource(id = R.drawable.led_off)  // Replace with your LED OFF .png file
        var ledState by remember { mutableStateOf(false) }

        // Fixed size for the button and image
        val buttonSize = 100.dp
        val imageSize = 50.dp

        Button(
            onClick = {
                ledState = !ledState
                // Toggle between the states of the LED
                val state = if (ledState) ledOnState else ledOffState
                writeToLEDCharacteristic(state)  // Send the command to the device
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(buttonSize)  // Ensure uniform button size
        ) {
            Image(
                painter = if (ledState) ledOn else ledOff,
                contentDescription = "LED $ledNumber",
                modifier = Modifier.size(imageSize)  // Ensure uniform image size
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Control LED $ledNumber")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }
}
