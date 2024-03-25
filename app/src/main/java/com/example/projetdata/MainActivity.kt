package com.example.projetdata

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var dataTextView: TextView
    private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothSocket: BluetoothSocket? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.connectButton)
        dataTextView = findViewById(R.id.dataTextView)

        connectButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT
                    )
                } else {
                    connectToBluetoothDevice()
                }
            } else {
                // For versions below Android 12, BLUETOOTH_CONNECT permission is not required.
                connectToBluetoothDevice()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    connectToBluetoothDevice()
                } else {
                    // Handle the case where the user denies the permission.
                }
                return
            }
        }
    }

    private fun connectToBluetoothDevice() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            if (device.name == "YourDeviceName") {
                try {
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
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)
                    bluetoothSocket?.connect()
                    receiveData()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return
            }
        }
    }

    private fun receiveData() {
        Thread {
            val inputStream = bluetoothSocket?.inputStream
            val buffer = ByteArray(1024)  // Buffer store for the stream
            var numBytes: Int  // Bytes returned from read()

            while (true) {
                // Read from the InputStream.
                try {
                    numBytes = inputStream!!.read(buffer)
                    val readMessage = String(buffer, 0, numBytes)
                    // Send the obtained bytes to the UI activity.
                    runOnUiThread {
                        dataTextView.text = readMessage
                    }
                } catch (e: IOException) {
                    Log.d("MainActivity", "Input stream was disconnected", e)
                    break
                }
            }
        }.start()
    }
}

