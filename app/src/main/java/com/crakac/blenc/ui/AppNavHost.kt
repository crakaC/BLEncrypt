package com.crakac.blenc.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.crakac.blenc.device.detail.deviceDetailScreen
import com.crakac.blenc.device.detail.navigateToDeviceDetail
import com.crakac.blenc.device.list.deviceListRoute
import com.crakac.blenc.device.list.deviceListScreen

@Composable
fun AppNavHost(
    appState: AppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = deviceListRoute
) {
    val navController = appState.navController
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        deviceListScreen(onClickItem = navController::navigateToDeviceDetail)
        deviceDetailScreen()
    }
}