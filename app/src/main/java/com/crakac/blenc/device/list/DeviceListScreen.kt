package com.crakac.blenc.device.list

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val deviceListRoute = "list"
fun NavGraphBuilder.deviceListScreen(onClickItem: (String) -> Unit) {
    composable(deviceListRoute) {
        DeviceListScreen(onClickItem = onClickItem)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceListScreen(onClickItem: (String) -> Unit) {
    val viewModel: DeviceListViewModel = hiltViewModel()
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                viewModel.startAdvertising()
            }) {
                Text("Advertise!")
            }
            Button(onClick = {
                viewModel.startScan()
            }) {
                Text("Scan!")
            }
            Button(onClick = {
                viewModel.stopScan()
            }) {
                Text("Stop!")
            }
            FilledIconButton(onClick = { viewModel.clearDevices() }) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "clear")
            }
        }
        LazyColumn {
            items(devices) { device ->
                var expand by rememberSaveable { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expand = !expand }
                        .border(1.dp, Color.Gray)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .width(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("name: ${device.name ?: "<null>"}")
                        Text("address: ${device.address}")
                        if (expand) {
                            Column {
                                Text("class: ${device.bluetoothClass}")
                                Text("alias: ${device.alias.toString()}")
                            }
                        }
                    }
                    OutlinedButton(onClick = { onClickItem(device.address) }) {
                        Text("Detail")
                    }
                }
            }
        }
    }
}