package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaxiViewModel

@Composable
fun DriverConsoleScreen(
    viewModel: TaxiViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Pulse animation for online indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PILOT TERMINAL HEADER ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ElectricYellow.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("driver_console_header_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pilot Icon Box with initials
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricYellow, ElectricBlue)
                            )
                        )
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    Text(
                        text = viewModel.driverNameInput.take(2).uppercase(),
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = viewModel.driverNameInput.ifBlank { "Pilot Core" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("driver_console_name")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(ElectricYellow.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lvl 4",
                                fontSize = 9.sp,
                                color = ElectricYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${viewModel.driverVehicleInput} • ${viewModel.driverPlateInput}",
                        fontSize = 12.sp,
                        color = CoolGrey,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = ElectricYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${viewModel.driverRatingInput} Rating",
                            fontSize = 11.sp,
                            color = CoolGrey,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- ACTIVE DUTY TOGGLE CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (viewModel.isDriverOnline) ElectricBlue.copy(alpha = 0.4f) else Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("duty_toggle_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing Dot for Online Status
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (viewModel.isDriverOnline) {
                                    ElectricBlue.copy(alpha = pulseScale)
                                } else {
                                    CoolGrey
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (viewModel.isDriverOnline) "CONNECTED TO GRID" else "DISCONNECTED FROM GRID",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (viewModel.isDriverOnline) ElectricBlue else CoolGrey,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (viewModel.isDriverOnline) "Actively listening for teleport dispatches" else "Go online to receive transit rides",
                            fontSize = 11.sp,
                            color = CoolGrey
                        )
                    }
                }

                Switch(
                    checked = viewModel.isDriverOnline,
                    onCheckedChange = { viewModel.isDriverOnline = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = ElectricBlue,
                        uncheckedThumbColor = CoolGrey,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("duty_status_switch")
                )
            }
        }

        // --- STATS ROW (Earnings & Trips) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Earnings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("driver_earnings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL REVENUE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoolGrey,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", viewModel.driverEarnings)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "+$45.00 today",
                        fontSize = 10.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Trips Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("driver_trips_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FLIGHTS DONE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoolGrey,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${viewModel.driverTripsCompleted}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricYellow,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "100% Accept Rate",
                        fontSize = 10.sp,
                        color = CoolGrey,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // --- QUANTUM DISPATCH GRID PANEL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("dispatch_panel")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DISPATCH ENGINE PROTOCOL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricYellow,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (viewModel.isDrivingTransit) {
                    // Flight simulation active progress UI
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalTaxi,
                            contentDescription = "Flight Active",
                            tint = ElectricBlue,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 12.dp)
                        )

                        Text(
                            text = "FLIGHT IN PROGRESS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = viewModel.driverStatusMessage,
                            fontSize = 12.sp,
                            color = CoolGrey,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        LinearProgressIndicator(
                            progress = { viewModel.drivingProgress },
                            color = ElectricBlue,
                            trackColor = CarbonCardLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                } else if (viewModel.activeRequestPassenger != null) {
                    // Incoming Dispatch Alert
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CarbonCardLight, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "TRANSIT ALERT",
                                    fontSize = 10.sp,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                    text = "$${String.format("%.2f", viewModel.activeRequestFare)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricYellow
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Passenger Info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CoolGrey,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Passenger: ${viewModel.activeRequestPassenger}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nodes
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ElectricBlue)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Pickup: ${viewModel.activeRequestPickup}",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ElectricYellow)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Dropoff: ${viewModel.activeRequestDropoff}",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Distance Metric: ${String.format("%.1f", viewModel.activeRequestDistance)} km",
                            fontSize = 11.sp,
                            color = CoolGrey
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.rejectDriverRide() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, AlertRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("driver_reject_button")
                            ) {
                                Text("Reject", color = AlertRed, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.acceptDriverRide() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("driver_accept_button")
                            ) {
                                Text("Accept Flight", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                } else {
                    // Locked, awaiting dispatches
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (viewModel.isDriverOnline) {
                            // Listening animation (glowing rotating/scanning loader)
                            CircularProgressIndicator(
                                color = ElectricBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "LOCKED ON TAMIL NADU TRANSIT NETWORK...",
                                fontSize = 11.sp,
                                color = CoolGrey,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Awaiting telecommunication bookings from Salem, Madurai, Trichy or Chennai Central...",
                                fontSize = 10.sp,
                                color = CoolGrey.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PowerOff,
                                contentDescription = "Offline",
                                tint = CoolGrey,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "DUTY GRID OFFLINE",
                                fontSize = 12.sp,
                                color = CoolGrey,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Switch duty status to Online to scan the neural network for flights.",
                                fontSize = 10.sp,
                                color = CoolGrey.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- DISCONNECT BUTTON ---
        Button(
            onClick = {
                viewModel.isDriverMode = false
                viewModel.isLoggedIn = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight),
            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("driver_disconnect_button")
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Disconnect",
                tint = AlertRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Disconnect from Pilot Terminal",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
