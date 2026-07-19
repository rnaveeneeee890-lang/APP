package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedAddress
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    profile: UserProfile?,
    savedAddresses: List<SavedAddress>,
    viewModel: com.example.ui.viewmodel.TaxiViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    // Initialize form fields once profile loads
    LaunchedEffect(profile) {
        viewModel.loadProfileToForm()
    }

    // New Saved Address State
    var newAddressLabel by remember { mutableStateOf("") }
    var newAddressValue by remember { mutableStateOf("") }

    // Translation helper
    fun t(text: String): String {
        return viewModel.t(text)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Title
        Text(
            text = t("Shortcuts & Profile"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = t("Modify credentials and configure immediate teleport coordinate nodes"),
            fontSize = 11.sp,
            color = CoolGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Language toggle at the top of the settings page
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("settings_language_selector")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Language",
                    tint = ElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = t("Choose Language"),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.currentLanguage = com.example.ui.components.AppLanguage.ENGLISH },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.currentLanguage == com.example.ui.components.AppLanguage.ENGLISH) ElectricBlue else Color(0xFF1E293B)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "English",
                            color = if (viewModel.currentLanguage == com.example.ui.components.AppLanguage.ENGLISH) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { viewModel.currentLanguage = com.example.ui.components.AppLanguage.TAMIL },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.currentLanguage == com.example.ui.components.AppLanguage.TAMIL) ElectricYellow else Color(0xFF1E293B)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "தமிழ்",
                            color = if (viewModel.currentLanguage == com.example.ui.components.AppLanguage.TAMIL) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // --- SECTION 1: USER PROFILE ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_section")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = t("Passenger ID Record"),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            // Rating Badge
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonCardLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ElectricYellow,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${profile?.passengerRating ?: 4.92} " + t("Score"),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Edit Name Field
                        OutlinedTextField(
                            value = viewModel.profileNameInput,
                            onValueChange = { viewModel.profileNameInput = it },
                            label = { Text(t("Display Name"), color = CoolGrey, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ElectricYellow,
                                unfocusedBorderColor = CarbonCardLight,
                                focusedContainerColor = CarbonCardLight,
                                unfocusedContainerColor = CarbonCardLight
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("profile_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Edit Email Field
                        OutlinedTextField(
                            value = viewModel.profileEmailInput,
                            onValueChange = { viewModel.profileEmailInput = it },
                            label = { Text(t("Comm Address (Email)"), color = CoolGrey, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ElectricYellow,
                                unfocusedBorderColor = CarbonCardLight,
                                focusedContainerColor = CarbonCardLight,
                                unfocusedContainerColor = CarbonCardLight
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("profile_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save Button
                        Button(
                            onClick = {
                                viewModel.saveProfile()
                                focusManager.clearFocus()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = DeepObsidian),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("save_profile_button")
                        ) {
                            Text(text = t("Save Profile Credentials"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Success Indicator Toast
                        if (viewModel.showProfileSuccessToast) {
                            Text(
                                text = t("Profile records updated successfully!"),
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                             )
                        }
                    }
                }
            }

            // --- SECTION 2: SAVED ADDR HEADER ---
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = t("Teleport Shortcuts (Saved Address Nodes)"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- SECTION 3: SAVED ADDR LIST ---
            if (savedAddresses.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = t("No custom shortcut locations configured. Use the form below to register shortcuts."),
                            color = CoolGrey,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(savedAddresses) { address ->
                    val icon = when (address.label.uppercase()) {
                        "HOME" -> Icons.Default.Home
                        "WORK" -> Icons.Default.Work
                        else -> Icons.Default.Place
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ElectricYellow.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(icon, contentDescription = null, tint = ElectricYellow, modifier = Modifier.size(16.dp))
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = t(address.label),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = t(address.address),
                                    color = CoolGrey,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSavedAddress(address.id) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("delete_saved_address_${address.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete address", tint = AlertRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // --- SECTION 4: ADD SAVED ADDR FORM ---
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = t("Register New Coordinate Node"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // Label field (Home, Work, Partner etc)
                        OutlinedTextField(
                            value = newAddressLabel,
                            onValueChange = { newAddressLabel = it },
                            label = { Text(t("Shortcut Label (e.g., Home, Work, Gym)"), color = CoolGrey, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ElectricYellow,
                                unfocusedBorderColor = CarbonCardLight,
                                focusedContainerColor = CarbonCardLight,
                                unfocusedContainerColor = CarbonCardLight
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("new_address_label_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Value Address Field
                        OutlinedTextField(
                            value = newAddressValue,
                            onValueChange = { newAddressValue = it },
                            label = { Text(t("Exact Node Address"), color = CoolGrey, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ElectricYellow,
                                unfocusedBorderColor = CarbonCardLight,
                                focusedContainerColor = CarbonCardLight,
                                unfocusedContainerColor = CarbonCardLight
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("new_address_value_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (newAddressLabel.isNotBlank() && newAddressValue.isNotBlank()) {
                                    viewModel.saveNewAddress(newAddressLabel, newAddressValue)
                                    newAddressLabel = ""
                                    newAddressValue = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("add_saved_address_button")
                        ) {
                            Text(text = t("Register Node Node"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
