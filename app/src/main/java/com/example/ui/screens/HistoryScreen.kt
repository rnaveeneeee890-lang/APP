package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    trips: List<Trip>,
    onDeleteTrip: (Int) -> Unit,
    viewModel: com.example.ui.viewmodel.TaxiViewModel,
    modifier: Modifier = Modifier
) {
    // Translation helper
    fun t(text: String): String {
        return viewModel.t(text)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Timeline Header
        Text(
            text = t("Transit Chronicles"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = t("Reactive timeline of your historical space travels"),
            fontSize = 11.sp,
            color = CoolGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (trips.isEmpty()) {
            // Empty State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(CarbonCard, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = ElectricYellow,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = t("No Historical Log Found"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t("Your completed transit records will automatically sync here."),
                        fontSize = 11.sp,
                        color = CoolGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Stats Panel
            StatsCard(trips = trips, viewModel = viewModel)

            Spacer(modifier = Modifier.height(12.dp))

            // History Timeline List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(trips) { trip ->
                    TripTimelineItem(trip = trip, viewModel = viewModel, onDelete = { onDeleteTrip(trip.id) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    trips: List<Trip>,
    viewModel: com.example.ui.viewmodel.TaxiViewModel
) {
    val totalRides = trips.size
    val completedRides = trips.filter { it.status == "COMPLETED" }
    val totalSpend = completedRides.sumOf { it.fare }
    val totalDist = completedRides.sumOf { it.distance }

    // Translation helper
    fun t(text: String): String {
        return viewModel.t(text)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CarbonCardLight, CarbonCard))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = t("Rides"), color = CoolGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "$totalRides", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = t("Expended"), color = CoolGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "$${String.format("%.2f", totalSpend)}", color = ElectricYellow, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = t("Distance"), color = CoolGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "${String.format("%.1f", totalDist)} km", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun TripTimelineItem(
    trip: Trip,
    viewModel: com.example.ui.viewmodel.TaxiViewModel,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(trip.timestamp))
    val isCancelled = trip.status == "CANCELLED"

    // Translation helper
    fun t(text: String): String {
        return viewModel.t(text)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_item_card_${trip.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isCancelled) AlertRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isCancelled) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isCancelled) AlertRed else SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t(trip.tierName),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = CoolGrey
                    )
                }

                // Fare / Cost
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isCancelled) "$0.00" else "$${String.format("%.2f", trip.fare)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCancelled) CoolGrey else ElectricYellow
                    )
                    Text(
                        text = if (isCancelled) t("Cancelled") else t("Completed"),
                        color = if (isCancelled) AlertRed else SuccessGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete log button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_trip_log_${trip.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete log",
                        tint = CoolGrey.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = CarbonCardLight, modifier = Modifier.padding(vertical = 8.dp))

            // Pickup & Destination address flow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ElectricYellow, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t(trip.pickupAddress),
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ElectricBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t(trip.dropoffAddress),
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Show feedback rating details if rated and not cancelled
            if (!isCancelled) {
                Divider(color = CarbonCardLight.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = t("Pilot") + ": ${trip.driverName}",
                        fontSize = 10.sp,
                        color = CoolGrey,
                        modifier = Modifier.weight(1f)
                    )

                    // Rated stars
                    Row {
                        val rating = if (trip.userRating > 0) trip.userRating else 5
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) ElectricYellow else CoolGrey,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
                if (trip.userFeedback.isNotBlank()) {
                    Text(
                        text = "\"${trip.userFeedback}\"",
                        fontSize = 10.sp,
                        color = CoolGrey,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
