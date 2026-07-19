package com.example.ui.screens

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    profile: UserProfile?,
    trips: List<Trip>,
    viewModel: com.example.ui.viewmodel.TaxiViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val balance = profile?.walletBalance ?: 50.00
    val name = profile?.name ?: "Pilot Traveler"

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
            text = t("Digital Wallet"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = t("Manage your credits and apply futuristic fuel vouchers"),
            fontSize = 11.sp,
            color = CoolGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Glowing Futuristic Wallet Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A), // Deep Blue
                            Color(0xFF0F172A), // Slate Black
                            Color(0xFF581C87)  // Deep Purple Accent
                        )
                    )
                )
                .testTag("wallet_glow_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = ElectricYellow,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FUTURE TRANSIT CORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                }

                // Balance Info
                Column {
                    Text(
                        text = t("ACTIVE CREDIT BALANCE"),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoolGrey
                    )
                    Text(
                        text = "$${String.format("%.2f", balance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricYellow
                    )
                }

                // Footer user name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = t("VALID PROTOCOL CHIP"),
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Top-up Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = t("Quick Balance Ingress"),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(10.0, 20.0, 50.0)
                    presets.forEach { amt ->
                        Button(
                            onClick = {
                                viewModel.topUpAmountText = amt.toString()
                                viewModel.addWalletFunds()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("top_up_btn_$amt")
                        ) {
                            Text(text = "+$${amt.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom top-up text field
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.topUpAmountText,
                        onValueChange = { viewModel.topUpAmountText = it },
                        label = { Text(t("Custom Credit Amount ($)"), color = CoolGrey, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.addWalletFunds()
                                focusManager.clearFocus()
                            }
                        ),
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
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("custom_topup_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.addWalletFunds()
                            focusManager.clearFocus()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = DeepObsidian),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("custom_topup_confirm_btn")
                    ) {
                        Text(text = t("Add Cash"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Promo voucher top-up voucher code
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = t("Apply Top-up Voucher"),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.promoCodeWalletText,
                        onValueChange = { viewModel.promoCodeWalletText = it },
                        placeholder = { Text(t("Voucher (Try 'FUTURE50')"), color = CoolGrey, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricYellow,
                            unfocusedBorderColor = CarbonCardLight,
                            focusedContainerColor = CarbonCardLight,
                            unfocusedContainerColor = CarbonCardLight
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("wallet_voucher_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.applyPromoWallet()
                            focusManager.clearFocus()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("wallet_voucher_apply_btn")
                    ) {
                        Text(text = t("Verify"), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Wallet Transactions log header
        Text(
            text = t("Wallet Deductions History"),
            color = CoolGrey,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val completedTrips = trips.filter { it.status == "COMPLETED" }
        if (completedTrips.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(text = t("No charges recorded yet."), color = CoolGrey, fontSize = 11.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(completedTrips) { trip ->
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val formatted = sdf.format(Date(trip.timestamp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricCar,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = t("Ride with") + " ${trip.driverName}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = formatted, color = CoolGrey, fontSize = 9.sp)
                            }
                            Text(
                                text = "-$${String.format("%.2f", trip.fare)}",
                                color = AlertRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                              )
                        }
                    }
                }
            }
        }
    }
}
