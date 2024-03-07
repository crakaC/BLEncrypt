package com.crakac.blenc.ble.server

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ
import android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.crakac.blenc.util.millis
import com.crakac.blenc.util.toHex
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class GattEchoServer @Inject @RequiresPermission(BLUETOOTH_CONNECT) constructor(
    @ApplicationContext private val context: Context
) : BluetoothGattServerCallback() {

    companion object {
        val EchoServiceUUID: UUID = UUID.fromString("eb5ac374-b364-4b90-bf05-0000000000")
        private val CharacteristicUUID = UUID.fromString("eb5ac374-b364-4b90-bf05-0000000001")
        private val DescriptorUUID = UUID.fromString("eb5ac374-b364-4b90-bf05-0000000002")
    }

    private val manager = context.getSystemService<BluetoothManager>()!!
    private val gattServer: BluetoothGattServer
    private val primaryService: BluetoothGattService
    private val advertiser: BluetoothLeAdvertiser

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.d("settingsInEffect:  $settingsInEffect")
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.w("errorCode: $errorCode")
        }
    }

    init {
        gattServer = manager.openGattServer(context, this)
        primaryService = BluetoothGattService(EchoServiceUUID, SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CharacteristicUUID,
            PROPERTY_READ or PROPERTY_WRITE or PROPERTY_NOTIFY,
            PERMISSION_READ or PERMISSION_WRITE
        )
        characteristic.addDescriptor(
            BluetoothGattDescriptor(
                DescriptorUUID,
                PERMISSION_READ or PERMISSION_WRITE
            )
        )
        primaryService.addCharacteristic(characteristic)
        gattServer.addService(primaryService)
        advertiser = manager.adapter.bluetoothLeAdvertiser
    }

    @RequiresPermission(allOf = [BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE])
    fun startAdvertise(duration: Duration = 60.seconds) {
        val data = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(EchoServiceUUID))
            .build()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTimeout(duration.millis)
            .setConnectable(true)
            .build()

        val response = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        advertiser.startAdvertising(settings, data, response, advertiseCallback)
    }

    @RequiresPermission(allOf = [BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT])
    fun close() {
        advertiser.stopAdvertising(advertiseCallback)
        gattServer.close()
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        Timber.d("status: $status, newState: $newState, device: $device")
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Timber.i("Connected!")
        } else {
            Timber.d("Disconnected!")
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        Timber.d("status: $status, service: $service")
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        Timber.d("requestId: $requestId, offset: $offset, characteristic: $characteristic, device: $device")
        gattServer.sendResponse(
            device,
            requestId,
            GATT_SUCCESS,
            offset,
            "Hello!".toByteArray()
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        Timber.d("requestId: $requestId, characteristic: $characteristic, preparedWrite: $preparedWrite, responseNeeded: $responseNeeded, offset: $offset, value: ${value?.toHex()}")
        gattServer.sendResponse(device, requestId, GATT_SUCCESS, offset, "Wrote!".toByteArray())
        if (PROPERTY_NOTIFY and (characteristic?.properties ?: 0) != 0) {
            Timber.d("notify")
        }
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor?
    ) {
        Timber.d("requestId: $requestId, offset: $offset, descriptor: $descriptor, device: $device")
        if (descriptor != null && descriptor.uuid == DescriptorUUID) {
//            gattServer.sendResponse(device, requestId, GATT_SUCCESS, offset, notifyDescriptor)
        }
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        Timber.d(
            "device: $device, requestId: $requestId, descriptor: $descriptor, preparedWrite: $preparedWrite, responseNeeded: $responseNeeded" +
                    "offset: $offset, value: ${value?.toHex()}"
        )
    }

    override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
        Timber.d("device: $device, requestId: $requestId, execute: $execute")
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        Timber.d("device: $device, status: $status")
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        Timber.d("device: $device, mtu: $mtu")
    }

    override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        Timber.d("device: $device, txPhy: $txPhy, rxPhy: $rxPhy, status: $status")
    }

    override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        Timber.d("device: $device, txPhy: $txPhy, rxPhy: $rxPhy, status: $status")
    }
}