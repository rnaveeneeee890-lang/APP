package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SavedAddress
import com.example.ui.components.SimulatedMap
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingStep
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.TaxiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRideScreen(
    viewModel: TaxiViewModel,
    savedAddresses: List<SavedAddress>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var showChatSheet by remember { mutableStateOf(false) }
    var chatTextInput by remember { mutableStateOf("") }
    
    val chatMessages by viewModel.chatMessages.collectAsState()

    if (viewModel.showDriverProfileModal) {
        viewModel.activeDriver?.let { driver ->
            DriverProfileDialog(
                driver = driver,
                onDismiss = { viewModel.showDriverProfileModal = false }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- 1. Interactive simulated map container ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .testTag("interactive_simulated_map")
        ) {
            SimulatedMap(
                modifier = Modifier.fillMaxSize(),
                pickupPoint = viewModel.selectedPickupCoords,
                dropoffPoint = viewModel.selectedDropoffCoords,
                driverPoint = if (viewModel.bookingStep == BookingStep.ACTIVE_RIDE) viewModel.currentDriverCoords else null,
                driverRotation = viewModel.driverRotationDegrees,
                isBookingActive = viewModel.bookingStep != BookingStep.SELECT_LOCATIONS,
                onMapTap = { point ->
                    if (viewModel.bookingStep == BookingStep.SELECT_LOCATIONS) {
                        if (viewModel.selectedPickupCoords == null) {
                            viewModel.selectPickup(point.copy(name = "Selected Pickup Spot"))
                        } else if (viewModel.selectedDropoffCoords == null) {
                            viewModel.selectDropoff(point.copy(name = "Selected Dropoff Spot"))
                        }
                    }
                }
            )

            // Dynamic float indicator overlay (ETA or status)
            if (viewModel.bookingStep == BookingStep.ACTIVE_RIDE) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ElectricYellow, ElectricBlue))),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ElectricYellow,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = viewModel.driverStatusText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. Dynamic state control panels ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (viewModel.bookingStep) {
                BookingStep.SELECT_LOCATIONS -> {
                    LocationSelectionPanel(
                        viewModel = viewModel,
                        savedAddresses = savedAddresses,
                        onQuickBook = { label, addr ->
                            viewModel.quickBookPreset(label, addr)
                        }
                    )
                }

                BookingStep.CHOOSE_TIER -> {
                    ChooseTierPanel(
                        viewModel = viewModel,
                        onCancel = { viewModel.startBookingSearch() },
                        onConfirm = { viewModel.initiateRideBooking() }
                    )
                }

                BookingStep.MATCHING -> {
                    MatchingAnimationPanel(
                        statusText = viewModel.driverStatusText,
                        onCancel = { viewModel.cancelActiveRide() }
                    )
                }

                BookingStep.ACTIVE_RIDE -> {
                    ActiveRidePanel(
                        viewModel = viewModel,
                        onOpenChat = { showChatSheet = true },
                        onCancel = { viewModel.cancelActiveRide() }
                    )
                }

                BookingStep.ARRIVED -> {
                    ArrivedPanel(
                        viewModel = viewModel,
                        onRate = { viewModel.navigateToStep(BookingStep.RATE_DRIVER) }
                    )
                }

                BookingStep.RATE_DRIVER -> {
                    RatingPanel(
                        viewModel = viewModel,
                        onSubmit = { viewModel.submitDriverRating() }
                    )
                }
            }
        }
    }

    // --- 3. Simulated Live Chat Sheet ---
    if (showChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChatSheet = false },
            containerColor = DeepObsidian,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .padding(16.dp)
            ) {
                // Chat Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pilot Chat: ${viewModel.activeDriver?.name ?: "Pilot"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricYellow,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showChatSheet = false },
                        modifier = Modifier.testTag("close_chat_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close chat")
                    }
                }

                Divider(color = CarbonCardLight, modifier = Modifier.padding(vertical = 8.dp))

                // Messages Timeline
                LazyColumn(
                    reverseLayout = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(chatMessages) { message ->
                        ChatBubble(message = message)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Quick replies template list
                Text(
                    text = "Quick templates",
                    color = CoolGrey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    val quickReplies = listOf("I'm waiting here!", "Got it, thanks!", "Traffic is light.")
                    quickReplies.forEach { reply ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { viewModel.sendUserChatMessage(reply) }
                        ) {
                            Text(
                                text = reply,
                                color = ElectricBlue,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Chat Input Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = chatTextInput,
                        onValueChange = { chatTextInput = it },
                        placeholder = { Text("Write message...", color = CoolGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricYellow,
                            unfocusedBorderColor = CarbonCardLight,
                            focusedContainerColor = CarbonCard,
                            unfocusedContainerColor = CarbonCard
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (chatTextInput.isNotBlank()) {
                                    viewModel.sendUserChatMessage(chatTextInput)
                                    chatTextInput = ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (chatTextInput.isNotBlank()) {
                                viewModel.sendUserChatMessage(chatTextInput)
                                chatTextInput = ""
                            }
                        },
                        containerColor = ElectricYellow,
                        contentColor = DeepObsidian,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("send_chat_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send message")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
                .widthIn(max = 260.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) ElectricYellow else CarbonCardLight
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                )
            ) {
                Text(
                    text = message.text,
                    color = if (isUser) DeepObsidian else Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LocationSelectionPanel(
    viewModel: TaxiViewModel,
    savedAddresses: List<SavedAddress>,
    onQuickBook: (String, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    // Dynamic translation helper
    fun t(text: String): String {
        return viewModel.t(text)
    }

    val pickupQueryMatches = remember(viewModel.pickupText) {
        viewModel.getFilteredPickups(viewModel.pickupText)
    }
    val dropoffQueryMatches = remember(viewModel.dropoffText) {
        viewModel.getFilteredDropoffs(viewModel.dropoffText)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Heading Banner
        Text(
            text = t("Where are you traveling today?"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = t("Tap map pins directly or enter address details"),
            fontSize = 12.sp,
            color = CoolGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Address Fields
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Pickup Field
                OutlinedTextField(
                    value = viewModel.pickupText,
                    onValueChange = {
                        viewModel.pickupText = it
                        viewModel.selectedPickupCoords = null
                    },
                    label = { Text(t("Where from?"), color = CoolGrey) },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Pickup", tint = ElectricYellow) },
                    trailingIcon = {
                        if (viewModel.pickupText.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.pickupText = ""
                                viewModel.selectedPickupCoords = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = CoolGrey)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricYellow,
                        unfocusedBorderColor = CarbonCardLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pickup_address_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dropoff Field
                OutlinedTextField(
                    value = viewModel.dropoffText,
                    onValueChange = {
                        viewModel.dropoffText = it
                        viewModel.selectedDropoffCoords = null
                    },
                    label = { Text(t("Where to?"), color = CoolGrey) },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Drop off", tint = ElectricBlue) },
                    trailingIcon = {
                        if (viewModel.dropoffText.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.dropoffText = ""
                                viewModel.selectedDropoffCoords = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = CoolGrey)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CarbonCardLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dropoff_address_field")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pickup Suggestions Matches (districts)
        if (viewModel.selectedPickupCoords == null && viewModel.pickupText.isNotEmpty()) {
            Text(
                text = t("Select Pickup Node"),
                color = ElectricYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pickupQueryMatches.take(3).forEach { match ->
                    val displayName = t(match.name)
                    AssistChip(
                        onClick = { viewModel.selectPickup(match) },
                        label = { Text(displayName, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = Color.White,
                            containerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("pickup_match_${match.name}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Dropoff Suggestions Matches (districts)
        if (viewModel.selectedDropoffCoords == null && viewModel.dropoffText.isNotEmpty()) {
            Text(
                text = t("Select Dropoff Node"),
                color = ElectricBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dropoffQueryMatches.take(3).forEach { match ->
                    val displayName = t(match.name)
                    AssistChip(
                        onClick = { viewModel.selectDropoff(match) },
                        label = { Text(displayName, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = Color.White,
                            containerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("dropoff_match_${match.name}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Auto-complete or Quick Preset suggestions
        if (viewModel.selectedPickupCoords != null && viewModel.selectedDropoffCoords != null) {
            // Both selected, show primary proceed button
            Button(
                onClick = { viewModel.navigateToStep(BookingStep.CHOOSE_TIER) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = DeepObsidian),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("proceed_to_booking_button")
            ) {
                Text(text = t("Confirm Ride"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Next")
            }
        } else {
            // Show saved shortcut quick-presets if empty
            Text(
                text = t("Teleport Shortcuts (Saved Address Nodes)"),
                color = CoolGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            if (savedAddresses.isEmpty()) {
                // Show dynamic standard system shortcuts if database is empty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val samplePresets = listOf(
                        Triple("Home", "Chennai", Icons.Default.Home),
                        Triple("Work", "Coimbatore", Icons.Default.Work)
                    )
                    samplePresets.forEach { preset ->
                        AssistChip(
                            onClick = { onQuickBook(preset.first, preset.second) },
                            label = { Text(t(preset.first)) },
                            leadingIcon = { Icon(preset.third, contentDescription = null, tint = ElectricYellow) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = Color.White, containerColor = CarbonCard)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedAddresses.take(3).forEach { address ->
                        val icon = when (address.label.uppercase()) {
                            "HOME" -> Icons.Default.Home
                            "WORK" -> Icons.Default.Work
                            else -> Icons.Default.Place
                        }
                        AssistChip(
                            onClick = { onQuickBook(address.label, address.address) },
                            label = { Text(t(address.label)) },
                            leadingIcon = { Icon(icon, contentDescription = null, tint = ElectricYellow) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = Color.White, containerColor = CarbonCard)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChooseTierPanel(
    viewModel: TaxiViewModel,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fare details header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Select Future Cab",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Distance: ${viewModel.routeDistanceKm} km • Duration: ${viewModel.routeDurationMin} min",
                    fontSize = 11.sp,
                    color = CoolGrey
                )
            }
            Text(
                text = "$${String.format("%.2f", maxOf(0.0, viewModel.calculatedFare - viewModel.activePromoDiscount))}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = ElectricYellow
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Ride Tiers
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(viewModel.rideTiers) { tier ->
                val isSelected = viewModel.selectedTier.id == tier.id
                val icon = when (tier.iconName) {
                    "eco" -> Icons.Default.DirectionsCar
                    "electric_car" -> Icons.Default.ElectricCar
                    "motorcycle" -> Icons.Default.Motorcycle
                    else -> Icons.Default.LocalTaxi
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CarbonCardLight else CarbonCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ElectricYellow, ElectricBlue))) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clickable { viewModel.selectTier(tier) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) ElectricYellow.copy(alpha = 0.2f) else CarbonCardLight,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) ElectricYellow else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tier.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ElectricYellow else Color.White
                            )
                            Text(
                                text = tier.description,
                                fontSize = 10.sp,
                                color = CoolGrey
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val estFare = tier.baseFare + (viewModel.routeDistanceKm * tier.perKmFare)
                            Text(
                                text = "$${String.format("%.2f", estFare)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "ETA ${tier.etaMin}m",
                                fontSize = 10.sp,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
        }

        // Promo code bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = viewModel.promoCodeInput,
                onValueChange = { viewModel.promoCodeInput = it },
                placeholder = { Text("Enter Promo Code", color = CoolGrey, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ElectricYellow,
                    unfocusedBorderColor = CarbonCardLight,
                    focusedContainerColor = CarbonCard,
                    unfocusedContainerColor = CarbonCard
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = { viewModel.applyPromoCode() },
                colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(text = "Apply", fontSize = 11.sp)
            }
        }
        if (viewModel.promoAppliedMsg.isNotEmpty()) {
            Text(
                text = viewModel.promoAppliedMsg,
                color = if (viewModel.activePromoDiscount > 0) SuccessGreen else AlertRed,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Text(text = "Cancel", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = DeepObsidian),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(46.dp)
                    .testTag("book_ride_confirm_button")
            ) {
                Text(text = "Confirm Booking", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MatchingAnimationPanel(
    statusText: String,
    onCancel: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = ElectricYellow,
                strokeWidth = 4.dp,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Securing Your Autonomous Pilot",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = CoolGrey,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Cancel Request", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveRidePanel(
    viewModel: TaxiViewModel,
    onOpenChat: () -> Unit,
    onCancel: () -> Unit
) {
    val driver = viewModel.activeDriver ?: return
    var showCancelConfirmation by remember { mutableStateOf(false) }

    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = {
                Text(
                    text = "Cancel Trip?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel this active ride? A cancellation fee may apply.",
                    color = CoolGrey,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmation = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_cancel_button")
                ) {
                    Text("Yes, Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCancelConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dismiss_cancel_button")
                ) {
                    Text("Keep Ride", color = Color.White)
                }
            },
            containerColor = DeepObsidian,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, CarbonCardLight, RoundedCornerShape(16.dp))
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Route details card
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Pilot Card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .background(ElectricYellow.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.ElectricCar, contentDescription = null, tint = ElectricYellow)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = driver.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = ElectricYellow, modifier = Modifier.size(12.dp))
                            Text(text = "${driver.rating} • Pilot Rating", color = CoolGrey, fontSize = 10.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = driver.plateNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricYellow
                        )
                        Text(
                            text = "EST ${viewModel.driverEtaMinutes}m",
                            fontSize = 10.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(color = CarbonCardLight, modifier = Modifier.padding(vertical = 10.dp))

                // Vehicle details description
                Text(
                    text = "VEHICLE",
                    color = CoolGrey,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = driver.vehicleName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onOpenChat,
                colors = ButtonDefaults.buttonColors(containerColor = CarbonCard, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("chat_pilot_button")
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat with pilot")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Chat Pilot", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showCancelConfirmation = true },
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("cancel_ride_button")
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel Ride")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cancel Ride", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ArrivedPanel(
    viewModel: TaxiViewModel,
    onRate: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .background(SuccessGreen.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to Your Destination!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your pilot ${viewModel.activeDriver?.name ?: "AutoPilot"} has completed the transit successfully.",
            fontSize = 11.sp,
            color = CoolGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRate,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = DeepObsidian),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("proceed_to_rating_button")
        ) {
            Text(text = "Rate Pilot & Finish", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RatingPanel(
    viewModel: TaxiViewModel,
    onSubmit: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Rate Your Travel Experience",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Your evaluation helps maintain premium transit standard",
            fontSize = 11.sp,
            color = CoolGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Pilot rating row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            for (i in 1..5) {
                val isSelected = viewModel.ratingScore >= i
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$i Stars",
                    tint = if (isSelected) ElectricYellow else CoolGrey,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { viewModel.ratingScore = i }
                        .testTag("star_rating_$i")
                )
            }
        }

        // Written feedback textfield
        OutlinedTextField(
            value = viewModel.ratingComment,
            onValueChange = { viewModel.ratingComment = it },
            placeholder = { Text("What made the transit pleasant? Leave feedback...", color = CoolGrey, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = ElectricYellow,
                unfocusedBorderColor = CarbonCardLight,
                focusedContainerColor = CarbonCard,
                unfocusedContainerColor = CarbonCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp)
                .testTag("rating_feedback_input")
        )

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = DeepObsidian),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("submit_rating_button")
        ) {
            Text(text = "Submit Review", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DriverProfileDialog(
    driver: com.example.ui.viewmodel.DriverInfo,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ElectricYellow.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("driver_profile_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "PILOT ASSIGNED",
                    color = ElectricYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Driver Photo
                val drawableId = when (driver.avatarUrl) {
                    "marcus" -> com.example.R.drawable.img_driver_marcus_1784453240777
                    "elena" -> com.example.R.drawable.img_driver_elena_1784453255769
                    "david" -> com.example.R.drawable.img_driver_david_1784453267917
                    "sarah" -> com.example.R.drawable.img_driver_sarah_1784453283917
                    else -> android.R.drawable.ic_menu_gallery
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(CarbonCardLight)
                        .border(2.dp, ElectricYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = "Driver Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .testTag("driver_profile_image"),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name
                Text(
                    text = driver.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("driver_profile_name")
                )

                // Rating & Flight badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating Star",
                        tint = ElectricYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${driver.rating} • ${driver.phone}",
                        fontSize = 12.sp,
                        color = CoolGrey,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("driver_profile_rating")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = CarbonCardLight)
                Spacer(modifier = Modifier.height(12.dp))

                // Vehicle Details Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonCardLight, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VEHICLE CORE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = driver.vehicleName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .testTag("driver_profile_vehicle")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LICENSE NODE PLATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoolGrey,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = driver.plateNumber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricYellow,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .testTag("driver_profile_plate")
                            )
                        }

                        // Call indicator / phone icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(ElectricBlue.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Pilot",
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dismiss action button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricYellow,
                        contentColor = DeepObsidian
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("driver_profile_acknowledge_button")
                ) {
                    Text(
                        text = "Track Flight",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

