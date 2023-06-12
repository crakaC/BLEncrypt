package com.crakac.blenc.device.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val DEVICE_ID_KEY = "device_id"
private const val deviceDetailRoute = "device_detail"

fun NavController.navigateToDeviceDetail(id: String, navOptions: NavOptions? = null) {
    navigate("$deviceDetailRoute/$id", navOptions)
}

fun NavGraphBuilder.deviceDetailScreen() {
    composable(route = "$deviceDetailRoute/{$DEVICE_ID_KEY}", arguments = listOf(
        navArgument(DEVICE_ID_KEY) { type = NavType.StringType }
    )) {
        DeviceDetailScreen()
    }
}

@Composable
fun DeviceDetailScreen() {
    val viewModel: DeviceDetailViewModel = hiltViewModel()
    val address by viewModel.deviceId.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("デバイス詳細")
        address?.let {
            Text(it)
        }
    }
}