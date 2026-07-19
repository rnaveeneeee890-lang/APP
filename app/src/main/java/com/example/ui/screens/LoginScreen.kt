package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppLanguage
import com.example.ui.components.LanguageHelper
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaxiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: TaxiViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var vehicleInput by remember { mutableStateOf("") }
    var plateInput by remember { mutableStateOf("") }
    var isPassengerTab by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    // Pulsing alpha for neon decoration
    val infiniteTransition = rememberInfiniteTransition(label = "login_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Helper translation method inside composable
    fun t(key: String): String {
        return LanguageHelper.translate(key, viewModel.currentLanguage)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(24.dp)
    ) {
        // Futuristic background pattern / glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricYellow.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header / Brand Logo Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricYellow, ElectricBlue)
                        )
                    )
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(19.dp))
                        .background(DeepObsidian),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Protocol Core Icon",
                        tint = ElectricYellow,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FUTURE TRANSIT",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = t("Futuristic Tamil Nadu Taxi Portal"),
                color = ElectricBlue.copy(alpha = pulseAlpha),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Language Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("language_selector_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("Select Preferred Language"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // English button
                        Button(
                            onClick = { viewModel.currentLanguage = AppLanguage.ENGLISH },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.currentLanguage == AppLanguage.ENGLISH) ElectricBlue else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lang_en_button")
                        ) {
                            Text(
                                text = "English",
                                color = if (viewModel.currentLanguage == AppLanguage.ENGLISH) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Tamil button
                        Button(
                            onClick = { viewModel.currentLanguage = AppLanguage.TAMIL },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.currentLanguage == AppLanguage.TAMIL) ElectricYellow else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lang_ta_button")
                        ) {
                            Text(
                                text = "தமிழ் (Tamil)",
                                color = if (viewModel.currentLanguage == AppLanguage.TAMIL) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Login Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_inputs_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TAB SELECTOR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(CarbonCardLight, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPassengerTab) ElectricYellow else Color.Transparent)
                                .clickable {
                                    isPassengerTab = true
                                    showError = false
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_passenger")
                        ) {
                            Text(
                                text = t("Passenger"),
                                color = if (isPassengerTab) Color.White else CoolGrey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isPassengerTab) ElectricBlue else Color.Transparent)
                                .clickable {
                                    isPassengerTab = false
                                    showError = false
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_pilot")
                        ) {
                            Text(
                                text = t("Pilot / Driver"),
                                color = if (!isPassengerTab) Color.Black else CoolGrey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        text = if (isPassengerTab) t("Passenger Login Portal") else t("Pilot Terminal Access"),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display Name Input
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            showError = false
                        },
                        label = { Text(if (isPassengerTab) t("Username") else t("Pilot Call Name")) },
                        placeholder = { Text(if (isPassengerTab) t("Enter Display Name") else t("Enter Pilot ID / Name")) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ElectricYellow
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricYellow,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = ElectricYellow,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            showError = false
                        },
                        label = { Text(t("Biometric Access Email")) },
                        placeholder = { Text(if (isPassengerTab) "passenger@futuretransit.in" else "pilot@futuretransit.in") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = ElectricBlue,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = if (isPassengerTab) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (isPassengerTab) focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    if (!isPassengerTab) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Vehicle Core Name Input
                        OutlinedTextField(
                            value = vehicleInput,
                            onValueChange = {
                                vehicleInput = it
                                showError = false
                            },
                            label = { Text(t("Vehicle Core Model")) },
                            placeholder = { Text("e.g. Solar Glide Pod VII") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = ElectricYellow
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricYellow,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = ElectricYellow,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_vehicle_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // License Node Plate Input
                        OutlinedTextField(
                            value = plateInput,
                            onValueChange = {
                                plateInput = it
                                showError = false
                            },
                            label = { Text(t("License Node Plate")) },
                            placeholder = { Text("e.g. TN-07-CYBER-99") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = ElectricBlue
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = ElectricBlue,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_plate_input")
                        )
                    }

                    if (showError) {
                        val errMsg = if (isPassengerTab) {
                            t("Please enter your name and email to proceed")
                        } else {
                            t("All pilot profile fields must be filled")
                        }
                        Text(
                            text = errMsg,
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Fast Track (Auto Fill) Option
                    Text(
                        text = "⚡ " + t("Fast Track (Auto-fill)"),
                        color = if (isPassengerTab) ElectricYellow else ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (isPassengerTab) {
                                    nameInput = "Alex Rivera"
                                    emailInput = "alex.rivera@futuremail.com"
                                } else {
                                    nameInput = "Capt. Vikranth"
                                    emailInput = "vikranth@pilot.futuretransit"
                                    vehicleInput = "Hyperion Solar Cruiser XL"
                                    plateInput = "TN-01-JET-007"
                                }
                                showError = false
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("login_autofill_button")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Access Protocol Button
                    Button(
                        onClick = {
                            if (isPassengerTab) {
                                if (nameInput.isNotBlank() && emailInput.isNotBlank()) {
                                    viewModel.profileNameInput = nameInput
                                    viewModel.profileEmailInput = emailInput
                                    viewModel.saveProfile()
                                    viewModel.isDriverMode = false
                                    onLoginSuccess()
                                } else {
                                    showError = true
                                }
                            } else {
                                if (nameInput.isNotBlank() && emailInput.isNotBlank() && vehicleInput.isNotBlank() && plateInput.isNotBlank()) {
                                    viewModel.driverNameInput = nameInput
                                    viewModel.driverEmailInput = emailInput
                                    viewModel.driverVehicleInput = vehicleInput
                                    viewModel.driverPlateInput = plateInput
                                    viewModel.isDriverMode = true
                                    onLoginSuccess()
                                } else {
                                    showError = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPassengerTab) ElectricYellow else ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("access_protocol_button")
                    ) {
                        Text(
                            text = if (isPassengerTab) t("Access Protocol (Login)") else t("Access Pilot Protocol"),
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
