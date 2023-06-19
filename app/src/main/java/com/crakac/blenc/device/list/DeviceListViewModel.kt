package com.crakac.blenc.device.list

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crakac.blenc.ble.server.GattEchoServer
import com.crakac.blenc.repo.BleDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    context: Application,
    private val bleDeviceRepo: BleDeviceRepository,
    private val bleServer: GattEchoServer
) : AndroidViewModel(context) {
    val devices: StateFlow<List<BluetoothDevice>> = bleDeviceRepo.scannedDevices

    fun startScan() {
        viewModelScope.launch {
            if (!checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                return@launch
            }
            bleDeviceRepo.startScan()
        }
    }

    fun stopScan() {
        if (!checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bleDeviceRepo.stopScan()
    }

    fun clearDevices() {
        if (!checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bleDeviceRepo.clear()
    }

    fun startAdvertising() {
        if (!checkSelfPermission(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        ) {
            return
        }
        bleServer.startAdvertise()
    }

    override fun onCleared() {
        if (!checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bleDeviceRepo.stopScan()
        bleServer.close()
    }
}

fun AndroidViewModel.checkSelfPermission(vararg permissions: String): Boolean {
    return permissions.all { permission ->
        ActivityCompat.checkSelfPermission(
            getApplication(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}