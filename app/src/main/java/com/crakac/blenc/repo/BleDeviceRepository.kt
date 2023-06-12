package com.crakac.blenc.repo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val BluetoothPermissions = arrayOf(
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_ADVERTISE
)

@SuppressLint("MissingPermission")
class BleDeviceRepository(
    @ApplicationContext private val context: Context,
) {
    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName("BleDevice"))
    private val devices = mutableMapOf<String, BluetoothDevice>()
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices = _scannedDevices.asStateFlow()

    private val bluetoothManager: BluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private val scannerCallback = object : ScanCallback() {
        private val mutex = Mutex()
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Timber.d("callbackType: $callbackType, result: $result")
            coroutineScope.launch {
                mutex.withLock {
                    result?.device?.let {
                        devices[it.address] = it
                        _scannedDevices.emit(devices.values.toList())
                    }
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Timber.d("results: $results")
            if (results == null) {
                return
            }
            coroutineScope.launch {
                mutex.withLock {
                    results.map { it.device }.forEach {
                        devices[it.address] = it
                    }
                    _scannedDevices.emit(devices.values.toList())
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.w("ScanFailed. errorCode = $errorCode")
        }
    }

    suspend fun startScan(duration: Duration = 10.seconds) {
        if (!permissionGranted()) return
        coroutineScope.launch {
            scanner.startScan(scannerCallback)
            delay(duration)
            stopScan()
        }
    }

    fun stopScan() {
        if (!permissionGranted()) return
        scanner.stopScan(scannerCallback)
    }

    private fun permissionGranted(): Boolean {
        return BluetoothPermissions.all { permission ->
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun clear() {
        stopScan()
        devices.clear()
        coroutineScope.launch {
            _scannedDevices.emit(emptyList())
        }
    }
}
