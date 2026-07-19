package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainRideScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.DriverConsoleScreen
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricYellow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TaxiViewModel

// Enum for bottom/side navigation tabs
enum class NavTab {
    EXPLORE,
    HISTORY,
    WALLET,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TaxiAppMainContent()
            }
        }
    }
}

@Composable
fun TaxiAppMainContent() {
    val viewModel: TaxiViewModel = viewModel()
    
    // Collect reactive Room DB states
    val trips by viewModel.trips.collectAsState()
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var activeTab by remember { mutableStateOf(NavTab.EXPLORE) }

    // Detect screen width for responsive design (Window Size Classes simulation)
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    // Dynamic translation helper
    fun t(text: String): String {
        return viewModel.t(text)
    }

    Surface(
        color = DeepObsidian,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!viewModel.isLoggedIn) {
            com.example.ui.screens.LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { viewModel.isLoggedIn = true }
            )
        } else {
            if (isWideScreen) {
                // Adaptive Side Navigation Rail for Large Screen Devices
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = DeepObsidian,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("side_navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "未来", // FUTURE in Kanji/Hanzi, matching our luxury cyber cab vibe
                            fontSize = 20.sp,
                            color = ElectricYellow,
                            modifier = Modifier.padding(12.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        NavigationRailItem(
                            selected = activeTab == NavTab.EXPLORE,
                            onClick = { activeTab = NavTab.EXPLORE },
                            icon = { Icon(Icons.Default.LocalTaxi, contentDescription = "Booking") },
                            label = { Text(t("Book Cab"), fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DeepObsidian,
                                selectedTextColor = ElectricYellow,
                                indicatorColor = ElectricYellow,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("rail_tab_explore")
                        )

                        NavigationRailItem(
                            selected = activeTab == NavTab.HISTORY,
                            onClick = { activeTab = NavTab.HISTORY },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text(t("History"), fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DeepObsidian,
                                selectedTextColor = ElectricYellow,
                                indicatorColor = ElectricYellow,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("rail_tab_history")
                        )

                        NavigationRailItem(
                            selected = activeTab == NavTab.WALLET,
                            onClick = { activeTab = NavTab.WALLET },
                            icon = { Icon(Icons.Default.Wallet, contentDescription = "Wallet") },
                            label = { Text(t("Wallet"), fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DeepObsidian,
                                selectedTextColor = ElectricYellow,
                                indicatorColor = ElectricYellow,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("rail_tab_wallet")
                        )

                        NavigationRailItem(
                            selected = activeTab == NavTab.SETTINGS,
                            onClick = { activeTab = NavTab.SETTINGS },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text(t("Profile"), fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DeepObsidian,
                                selectedTextColor = ElectricYellow,
                                indicatorColor = ElectricYellow,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                unselectedTextColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.testTag("rail_tab_settings")
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFF1E293B))
                    )

                    // Main screen view content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        when (activeTab) {
                            NavTab.EXPLORE -> {
                                if (viewModel.isDriverMode) {
                                    DriverConsoleScreen(viewModel = viewModel)
                                } else {
                                    MainRideScreen(viewModel = viewModel, savedAddresses = savedAddresses)
                                }
                            }
                            NavTab.HISTORY -> HistoryScreen(trips = trips, onDeleteTrip = { id -> viewModel.deleteTrip(id) }, viewModel = viewModel)
                            NavTab.WALLET -> WalletScreen(profile = userProfile, trips = trips, viewModel = viewModel)
                            NavTab.SETTINGS -> SettingsScreen(profile = userProfile, savedAddresses = savedAddresses, viewModel = viewModel)
                        }
                    }
                }
            } else {
                // Adaptive Bottom Navigation for Compact Screen Devices (Mobile Phones)
                Scaffold(
                    containerColor = DeepObsidian,
                    bottomBar = {
                        NavigationBar(
                            containerColor = DeepObsidian,
                            contentColor = Color.White,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .testTag("bottom_navigation_bar")
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = activeTab == NavTab.EXPLORE,
                                onClick = { activeTab = NavTab.EXPLORE },
                                icon = { Icon(Icons.Default.LocalTaxi, contentDescription = "Booking") },
                                label = { Text(t("Book"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = ElectricYellow,
                                    indicatorColor = ElectricYellow,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_explore")
                            )

                            NavigationBarItem(
                                selected = activeTab == NavTab.HISTORY,
                                onClick = { activeTab = NavTab.HISTORY },
                                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                label = { Text(t("History"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = ElectricYellow,
                                    indicatorColor = ElectricYellow,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_history")
                            )

                            NavigationBarItem(
                                selected = activeTab == NavTab.WALLET,
                                onClick = { activeTab = NavTab.WALLET },
                                icon = { Icon(Icons.Default.Wallet, contentDescription = "Wallet") },
                                label = { Text(t("Wallet"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = ElectricYellow,
                                    indicatorColor = ElectricYellow,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_wallet")
                            )

                            NavigationBarItem(
                                selected = activeTab == NavTab.SETTINGS,
                                onClick = { activeTab = NavTab.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text(t("Profile"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DeepObsidian,
                                    selectedTextColor = ElectricYellow,
                                    indicatorColor = ElectricYellow,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_settings")
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        when (activeTab) {
                            NavTab.EXPLORE -> {
                                if (viewModel.isDriverMode) {
                                    DriverConsoleScreen(viewModel = viewModel)
                                } else {
                                    MainRideScreen(viewModel = viewModel, savedAddresses = savedAddresses)
                                }
                            }
                            NavTab.HISTORY -> HistoryScreen(trips = trips, onDeleteTrip = { id -> viewModel.deleteTrip(id) }, viewModel = viewModel)
                            NavTab.WALLET -> WalletScreen(profile = userProfile, trips = trips, viewModel = viewModel)
                            NavTab.SETTINGS -> SettingsScreen(profile = userProfile, savedAddresses = savedAddresses, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
