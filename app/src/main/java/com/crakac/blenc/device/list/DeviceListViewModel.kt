package com.crakac.blenc.device.list

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crakac.blenc.repo.BleDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val bleDeviceRepo: BleDeviceRepository
) : ViewModel() {
    val devices: StateFlow<List<BluetoothDevice>> = bleDeviceRepo.scannedDevices

    fun startScan() {
        viewModelScope.launch {
            bleDeviceRepo.startScan()
        }
    }

    fun stopScan() {
        bleDeviceRepo.stopScan()
    }

    fun clearDevices() {
        bleDeviceRepo.clear()
    }
}