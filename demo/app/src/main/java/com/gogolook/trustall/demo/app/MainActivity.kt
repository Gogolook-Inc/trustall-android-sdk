package com.gogolook.trustall.demo.app

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.demo.core.designsystem.theme.AppTheme
import com.gogolook.trustall.demo.feature.auth.AuthScreen
import com.gogolook.trustall.demo.feature.block.BlockScreen
import com.gogolook.trustall.demo.feature.callerid.CallerIdScreen
import com.gogolook.trustall.demo.feature.calllog.CallLogScreen
import com.gogolook.trustall.demo.feature.msgfilter.MsgFilterScreen
import com.gogolook.trustall.demo.feature.smslog.SmsLogScreen
import com.gogolook.trustall.demo.feature.offlinedb.OfflineDbScreen
import com.gogolook.trustall.demo.feature.search.SearchScreen
import com.gogolook.trustall.demo.feature.urlscan.UrlScanScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition { Trustall.isInitialized.value.not() }

        setContent { AppTheme { MainApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var showClearDataDialog by remember { mutableStateOf(false) }

    val topBarTitle =
            when (currentRoute) {
                "home" -> "Trustall Demo"
                "auth" -> "Auth"
                "search" -> "Number Search"
                "offline" -> "Offline DB"
                "urlscan" -> "Url Scan"
                "msgfilter" -> "Msg Filter"
                "block" -> "Block List"
                "callerid" -> "Caller ID"
                "calllog" -> "Call Log"
                "smslog" -> "SMS Log"
                else -> "Trustall Demo"
            }

    if (showClearDataDialog) {
        AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text("Clear All Data") },
                text = {
                    Text(
                            "Are you sure you want to clear all application data? This action cannot be undone and the app will close."
                    )
                },
                confirmButton = {
                    Button(
                            onClick = {
                                showClearDataDialog = false
                                val activityManager =
                                        context.getSystemService(Context.ACTIVITY_SERVICE) as
                                                ActivityManager
                                activityManager.clearApplicationUserData()
                            }
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
                }
        )
    }

    Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                        title = { Text(topBarTitle) },
                        colors =
                                TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer,
                                        actionIconContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                        actions = {
                            IconButton(onClick = { showClearDataDialog = true }) {
                                Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Clear Data"
                                )
                            }
                        }
                )
            },
            bottomBar = {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .windowInsetsPadding(WindowInsets.navigationBars)
                                            .height(80.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        val screens = listOf(
                            Screen("home", "Home", Icons.Rounded.Home),
                            Screen("auth", "Auth", Icons.Rounded.AccountCircle),
                            Screen("search", "Search", Icons.Rounded.Search),
                            Screen("offline", "Offline DB", Icons.Rounded.List),
                            Screen("urlscan", "URL Scan", Icons.Rounded.Public),
                            Screen("msgfilter", "Msg Filter", Icons.Rounded.Email),
                            Screen("block", "Block", Icons.Rounded.Block),
                            Screen("callerid", "Caller ID", Icons.Rounded.Phone),
                            Screen("calllog", "Call", Icons.Rounded.List),
                            Screen("smslog", "SMS", Icons.Rounded.Sms)
                        )

                        screens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            val contentColor =
                                    if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                            val containerColor =
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent

                            Column(
                                    modifier =
                                            Modifier.padding(4.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(containerColor)
                                                    .clickable {
                                                        navController.navigate(screen.route) {
                                                            popUpTo(
                                                                    navController.graph
                                                                            .findStartDestination()
                                                                            .id
                                                            ) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                                    .defaultMinSize(minWidth = 56.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = contentColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = contentColor
                                )
                            }
                        }
                    }
                }
            }
    ) { innerPadding ->
        NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val isInitialized by Trustall.isInitialized.collectAsState()
                if (isInitialized) {
                    HomeScreen(
                            sdkVersion = Trustall.sdkVersion,
                            licenseId = Trustall.config.licenseId,
                            deviceId = Trustall.deviceId,
                            isDebug = Trustall.config.isDebug
                    )
                }
            }
            composable("auth") { AuthScreen() }
            composable("search") {
                SearchScreen(
                        onNavigateToCallerId = {
                            navController.navigate("callerid") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                )
            }
            composable("offline") { OfflineDbScreen() }
            composable("urlscan") { UrlScanScreen() }
            composable("msgfilter") { MsgFilterScreen() }
            composable("block") {
                BlockScreen(
                        onNavigateToCallerId = {
                            navController.navigate("callerid") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                )
            }
            composable("callerid") { CallerIdScreen() }
            composable("calllog") { CallLogScreen() }
            composable("smslog") { SmsLogScreen() }
        }
    }
}

data class Screen(val route: String, val title: String, val icon: ImageVector)
