package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.TaxiDatabase
import com.example.data.model.SavedAddress
import com.example.data.model.Trip
import com.example.data.model.UserProfile
import com.example.data.repository.TaxiRepository
import com.example.ui.components.AppLanguage
import com.example.ui.components.LanguageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

// Enum for booking screens/steps
enum class BookingStep {
    SELECT_LOCATIONS,
    CHOOSE_TIER,
    MATCHING,
    ACTIVE_RIDE,
    ARRIVED,
    RATE_DRIVER
}

// Ride Tier Definition
data class RideTier(
    val id: String,
    val name: String,
    val description: String,
    val baseFare: Double,
    val perKmFare: Double,
    val etaMin: Int,
    val iconName: String
)

// Driver Information
data class DriverInfo(
    val name: String,
    val rating: Float,
    val vehicleName: String,
    val plateNumber: String,
    val phone: String,
    val avatarUrl: String
)

// Simple Chat Message
data class ChatMessage(
    val sender: String, // "user", "driver"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Simulated Location Point
data class MapPoint(val x: Float, val y: Float, val name: String = "")

class TaxiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaxiRepository

    // Reactive database flows
    val trips: StateFlow<List<Trip>>
    val savedAddresses: StateFlow<List<SavedAddress>>
    val userProfile: StateFlow<UserProfile?>

    init {
        val database = TaxiDatabase.getDatabase(application)
        repository = TaxiRepository(database.taxiDao())
        
        trips = repository.allTrips.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        savedAddresses = repository.savedAddresses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateIfNeeded()
        }
        startDriverRequestSimulation()
    }

    // --- UI/UX Interactive State ---
    var bookingStep by mutableStateOf(BookingStep.SELECT_LOCATIONS)
        private set

    var pickupText by mutableStateOf("")
    var dropoffText by mutableStateOf("")

    var selectedPickupCoords by mutableStateOf<MapPoint?>(null)
    var selectedDropoffCoords by mutableStateOf<MapPoint?>(null)

    // --- Dynamic Language and Login Authentication State ---
    var currentLanguage by mutableStateOf(AppLanguage.ENGLISH)
    var isLoggedIn by mutableStateOf(false)

    // Dynamic translation helper
    fun t(text: String): String {
        return LanguageHelper.translate(text, currentLanguage)
    }

    // Current lists of preset suggestions including all 38 districts of Tamil Nadu
    val mapDestinations = listOf(
        MapPoint(22f, 32f, "Home"),
        MapPoint(78f, 68f, "Work"),
        MapPoint(18f, 72f, "Gym"),
        MapPoint(50f, 15f, "Chennai"),
        MapPoint(15f, 65f, "Coimbatore"),
        MapPoint(35f, 75f, "Madurai"),
        MapPoint(42f, 55f, "Tiruchirappalli"),
        MapPoint(32f, 42f, "Salem"),
        MapPoint(25f, 90f, "Tirunelveli"),
        MapPoint(48f, 25f, "Vellore"),
        MapPoint(52f, 58f, "Thanjavur"),
        MapPoint(22f, 52f, "Erode"),
        MapPoint(35f, 92f, "Thoothukudi"),
        MapPoint(20f, 60f, "Tiruppur"),
        MapPoint(52f, 22f, "Kanchipuram"),
        MapPoint(54f, 12f, "Tiruvallur"),
        MapPoint(60f, 38f, "Cuddalore"),
        MapPoint(30f, 68f, "Dindigul"),
        MapPoint(20f, 98f, "Kanniyakumari"),
        MapPoint(28f, 28f, "Krishnagiri"),
        MapPoint(64f, 55f, "Nagapattinam"),
        MapPoint(34f, 48f, "Namakkal"),
        MapPoint(46f, 48f, "Perambalur"),
        MapPoint(48f, 65f, "Pudukkottai"),
        MapPoint(50f, 85f, "Ramanathapuram"),
        MapPoint(44f, 75f, "Sivagangai"),
        MapPoint(20f, 85f, "Tenkasi"),
        MapPoint(22f, 72f, "Theni"),
        MapPoint(12f, 48f, "Nilgiris"),
        MapPoint(58f, 55f, "Thiruvarur"),
        MapPoint(38f, 28f, "Tirupathur"),
        MapPoint(44f, 32f, "Tiruvannamalai"),
        MapPoint(50f, 48f, "Ariyalur"),
        MapPoint(30f, 34f, "Dharmapuri"),
        MapPoint(46f, 38f, "Kallakurichi"),
        MapPoint(32f, 56f, "Karur"),
        MapPoint(62f, 48f, "Mayiladuthurai"),
        MapPoint(48f, 22f, "Ranipet"),
        MapPoint(56f, 22f, "Chengalpattu"),
        MapPoint(52f, 34f, "Villupuram"),
        MapPoint(30f, 82f, "Virudhunagar")
    )

    // Filtered suggestions
    fun getFilteredPickups(query: String): List<MapPoint> {
        if (query.isEmpty()) return mapDestinations.take(6)
        return mapDestinations.filter { 
            it.name.contains(query, ignoreCase = true) || 
            t(it.name).contains(query, ignoreCase = true)
        }
    }

    fun getFilteredDropoffs(query: String): List<MapPoint> {
        if (query.isEmpty()) return mapDestinations.take(6)
        return mapDestinations.filter { 
            it.name.contains(query, ignoreCase = true) || 
            t(it.name).contains(query, ignoreCase = true)
        }
    }

    // Ride Tiers
    val rideTiers = listOf(
        RideTier("eco", "EcoFuture", "Carbon-neutral budget hatchback", 2.50, 1.20, 4, "eco"),
        RideTier("lux", "Tesla Lux", "Premium autonomous electric cruiser", 6.00, 2.50, 2, "electric_car"),
        RideTier("moto", "MotoSprint", "Speedy electric delivery & passenger cycle", 1.50, 0.80, 1, "motorcycle"),
        RideTier("xl", "Family XL", "Spacious 7-seater smart SUV", 4.50, 1.80, 5, "airport_shuttle")
    )

    var selectedTier by mutableStateOf(rideTiers[0])
    var paymentMethod by mutableStateOf("Wallet") // Wallet, Card, Cash
    var promoCodeInput by mutableStateOf("")
    var activePromoDiscount by mutableStateOf(0.0) // Promo discount in dollars
    var promoAppliedMsg by mutableStateOf("")

    // Simulated Route Info
    var routeDistanceKm by mutableStateOf(0.0)
    var routeDurationMin by mutableStateOf(0)
    var calculatedFare by mutableStateOf(0.0)

    // Active Simulation parameters
    var activeDriver by mutableStateOf<DriverInfo?>(null)
    var showDriverProfileModal by mutableStateOf(false)
    var currentDriverCoords by mutableStateOf<MapPoint>(MapPoint(0f, 0f))
    var driverRotationDegrees by mutableStateOf(0f)
    var rideProgress by mutableStateOf(0f) // 0.0 to 1.0
    var driverEtaMinutes by mutableStateOf(0)
    var driverStatusText by mutableStateOf("Matching your pilot...")

    // Real-time Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Rating / Review form
    var ratingScore by mutableStateOf(5)
    var ratingComment by mutableStateOf("")

    // Wallet transaction panel
    var topUpAmountText by mutableStateOf("")
    var promoCodeWalletText by mutableStateOf("")

    // Profile Settings Form
    var profileNameInput by mutableStateOf("")
    var profileEmailInput by mutableStateOf("")
    var showProfileSuccessToast by mutableStateOf(false)

    // --- Driver Portal / Pilot Core State ---
    var isDriverMode by mutableStateOf(false)
    var driverNameInput by mutableStateOf("")
    var driverEmailInput by mutableStateOf("")
    var driverVehicleInput by mutableStateOf("")
    var driverPlateInput by mutableStateOf("")
    var driverRatingInput by mutableStateOf(4.95f)
    var isDriverOnline by mutableStateOf(true)
    var driverEarnings by mutableStateOf(245.00)
    var driverTripsCompleted by mutableStateOf(14)
    
    // Active simulated request for Driver mode
    var activeRequestPassenger by mutableStateOf<String?>(null)
    var activeRequestPickup by mutableStateOf("")
    var activeRequestDropoff by mutableStateOf("")
    var activeRequestFare by mutableStateOf(0.0)
    var activeRequestDistance by mutableStateOf(0.0)
    
    var isDrivingTransit by mutableStateOf(false)
    var drivingProgress by mutableStateOf(0f)
    var driverStatusMessage by mutableStateOf("Ready for dispatch")
    
    private var driverSimulationJob: Job? = null

    // --- Driver Portal Actions ---
    fun acceptDriverRide() {
        val passenger = activeRequestPassenger ?: return
        isDrivingTransit = true
        drivingProgress = 0f
        driverStatusMessage = "Navigating to $activeRequestPickup..."
        
        driverSimulationJob?.cancel()
        driverSimulationJob = viewModelScope.launch {
            // Stage 1: Heading to pickup
            for (i in 1..5) {
                delay(800)
                drivingProgress = i * 0.1f
            }
            
            // Stage 2: Driving to dropoff
            driverStatusMessage = "Transporting passenger to $activeRequestDropoff..."
            for (i in 6..10) {
                delay(800)
                drivingProgress = i * 0.1f
            }
            
            // Success
            driverEarnings += activeRequestFare
            driverTripsCompleted += 1
            isDrivingTransit = false
            activeRequestPassenger = null
            driverStatusMessage = "Ride Completed! Earned \$${String.format("%.2f", activeRequestFare)}"
        }
    }
    
    fun rejectDriverRide() {
        activeRequestPassenger = null
    }

    fun startDriverRequestSimulation() {
        // Start a job that occasionally spawns fake ride requests if driver is online
        viewModelScope.launch {
            while (true) {
                delay(10000) // every 10 seconds check if we should spawn
                if (isDriverMode && isDriverOnline && activeRequestPassenger == null && !isDrivingTransit) {
                    val passengers = listOf("Ravi Kumar", "Priya Sharma", "Suresh Kumar", "Abhishek Das")
                    val destinations = listOf("Chennai Central", "Madurai Temple Node", "Coimbatore IT Park", "Trichy Airport Node")
                    val pickups = listOf("Guindy Tech Park", "Salem Junction", "Vellore Fort Node", "Erode Bus Stand")
                    
                    activeRequestPassenger = passengers.random()
                    activeRequestPickup = pickups.random()
                    activeRequestDropoff = destinations.random()
                    activeRequestDistance = (5..80).random().toDouble()
                    activeRequestFare = 15.0 + activeRequestDistance * 1.5
                }
            }
        }
    }

    private var simulationJob: Job? = null

    // Driver list for random assignment
    private val driverPool = listOf(
        DriverInfo("Marcus Vance", 4.95f, "Tesla Model S (Carbon Silver)", "7XYZ99", "+1 (555) 982-1923", "marcus"),
        DriverInfo("Elena Rostov", 4.88f, "Nissan Leaf (Eco Mint)", "3ABC12", "+1 (555) 304-2041", "elena"),
        DriverInfo("David Kene", 4.91f, "Chevy Bolt (Galaxy Blue)", "5EDF77", "+1 (555) 781-4402", "david"),
        DriverInfo("Sarah Jenkins", 4.97f, "Tesla Model Y (Starlight White)", "9KLP50", "+1 (555) 219-5881", "sarah")
    )

    // Chat Auto-Responses pool
    private val driverResponses = listOf(
        "Hello! I am heading your way now. Be there in a couple of minutes.",
        "Got it! I am at the traffic signal, should be with you shortly.",
        "Perfect! I will pull up near the main entrance.",
        "Understood, traffic is a bit heavy but I am cruising.",
        "Hello! Please ensure you're in a safe pickup spot. See you soon!"
    )

    // Navigation trigger methods
    fun navigateToStep(step: BookingStep) {
        bookingStep = step
    }

    fun selectPickup(point: MapPoint) {
        selectedPickupCoords = point
        pickupText = point.name
        calculateRouteEstimates()
    }

    fun selectDropoff(point: MapPoint) {
        selectedDropoffCoords = point
        dropoffText = point.name
        calculateRouteEstimates()
    }

    fun quickBookPreset(label: String, address: String) {
        // Map preset address to a coordinate
        val matchedPoint = mapDestinations.find { it.name.contains(label, ignoreCase = true) || it.name.contains(address, ignoreCase = true) } 
            ?: MapPoint(50f, 50f, "$label ($address)")
        
        selectedDropoffCoords = matchedPoint
        dropoffText = matchedPoint.name
        
        // Default pickup to current location (Home if not set, or central hub)
        if (selectedPickupCoords == null) {
            val homePoint = mapDestinations.find { it.name.contains("Home", ignoreCase = true) } ?: mapDestinations[0]
            selectedPickupCoords = homePoint
            pickupText = homePoint.name
        }
        
        calculateRouteEstimates()
        navigateToStep(BookingStep.CHOOSE_TIER)
    }

    fun selectTier(tier: RideTier) {
        selectedTier = tier
        calculateRouteEstimates()
    }

    fun applyPromoCode() {
        val code = promoCodeInput.trim().uppercase()
        if (code == "TAXI50") {
            activePromoDiscount = calculatedFare * 0.50
            promoAppliedMsg = "Promo TAXI50 applied! 50% discount."
        } else if (code == "RIDEFREE") {
            activePromoDiscount = calculatedFare
            promoAppliedMsg = "Promo RIDEFREE applied! 100% discount."
        } else if (code == "FUTURE5") {
            activePromoDiscount = 5.0
            if (activePromoDiscount > calculatedFare) activePromoDiscount = calculatedFare
            promoAppliedMsg = "Promo FUTURE5 applied! $5.00 off."
        } else {
            activePromoDiscount = 0.0
            promoAppliedMsg = "Invalid promotional code."
        }
    }

    private fun calculateRouteEstimates() {
        val p = selectedPickupCoords ?: MapPoint(20f, 30f)
        val d = selectedDropoffCoords ?: MapPoint(80f, 70f)
        
        // Euclidean distance representation for coordinates
        val dx = d.x - p.x
        val dy = d.y - p.y
        val distanceUnits = sqrt(dx * dx + dy * dy)
        
        // Scale to simulated kilometers: e.g. 1 unit = 0.2 km
        routeDistanceKm = Math.round(distanceUnits * 0.2 * 100.0) / 100.0
        if (routeDistanceKm < 0.5) routeDistanceKm = 0.8
        
        // Duration: ~2 minutes per km + traffic random factor
        routeDurationMin = (routeDistanceKm * 2.2 + 2).toInt()
        
        // Fare calculation
        val rawFare = selectedTier.baseFare + (routeDistanceKm * selectedTier.perKmFare)
        calculatedFare = Math.round(rawFare * 100.0) / 100.0
        
        // Re-apply promo if exists
        applyPromoCode()
    }

    fun startBookingSearch() {
        // Clear previous state if restarting
        bookingStep = BookingStep.SELECT_LOCATIONS
        activePromoDiscount = 0.0
        promoCodeInput = ""
        promoAppliedMsg = ""
        _chatMessages.value = emptyList()
    }

    fun initiateRideBooking() {
        navigateToStep(BookingStep.MATCHING)
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            // Step 1: Matching animation
            driverStatusText = "Broadcasting your request to pilots..."
            delay(1500)
            driverStatusText = "Connecting with nearby autonomous and human captains..."
            delay(1500)
            
            // Assign random driver
            val driver = driverPool.random()
            activeDriver = driver
            showDriverProfileModal = true
            currentDriverCoords = MapPoint(
                x = (selectedPickupCoords?.x ?: 50f) + (-15..15).random(),
                y = (selectedPickupCoords?.y ?: 50f) + (-15..15).random()
            )
            driverStatusText = "Pilot found! ${driver.name} is on the way."
            
            delay(2000)
            navigateToStep(BookingStep.ACTIVE_RIDE)
            
            // Step 2: Driver heading to pickup
            val pickup = selectedPickupCoords ?: MapPoint(20f, 30f)
            val dropoff = selectedDropoffCoords ?: MapPoint(80f, 70f)
            
            animateDriverMovement(currentDriverCoords, pickup, "Your pilot is arriving shortly", 6)
            
            // Arrived at pickup
            driverStatusText = "Pilot has arrived at your pickup spot!"
            _chatMessages.value = _chatMessages.value + ChatMessage("driver", "I have arrived at your pickup point. See you soon!")
            delay(3000)
            
            // Step 3: Ride in Progress
            animateDriverMovement(pickup, dropoff, "Heading to destination", routeDurationMin)
            
            // Arrived at destination
            driverStatusText = "Arrived at destination!"
            navigateToStep(BookingStep.ARRIVED)
            
            // Record trip in database
            deductFareAndRecordTrip()
        }
    }

    private suspend fun animateDriverMovement(from: MapPoint, to: MapPoint, statusPrefix: String, totalSeconds: Int) {
        val startX = from.x
        val startY = from.y
        val endX = to.x
        val endY = to.y

        val midX = startX + (endX - startX) * 0.5f

        // Define the 3 segment legs of the grid-based map route
        val leg1Start = MapPoint(startX, startY)
        val leg1End = MapPoint(midX, startY)

        val leg2Start = MapPoint(midX, startY)
        val leg2End = MapPoint(midX, endY)

        val leg3Start = MapPoint(midX, endY)
        val leg3End = MapPoint(endX, endY)

        // Calculate lengths of each segment
        val d1 = abs(midX - startX)
        val d2 = abs(endY - startY)
        val d3 = abs(endX - midX)
        val totalDist = d1 + d2 + d3

        val stepsPerSec = 10
        val totalSteps = totalSeconds * stepsPerSec

        if (totalDist == 0f) {
            currentDriverCoords = to
            return
        }

        // Distribute the steps proportionally to segment lengths
        val steps1 = (d1 / totalDist * totalSteps).toInt()
        val steps2 = (d2 / totalDist * totalSteps).toInt()
        val steps3 = maxOf(0, totalSteps - steps1 - steps2)

        var currentStep = 0

        suspend fun runLeg(legStart: MapPoint, legEnd: MapPoint, legSteps: Int) {
            if (legSteps <= 0) return
            val dx = (legEnd.x - legStart.x) / legSteps
            val dy = (legEnd.y - legStart.y) / legSteps

            // Set the exact rotation degrees for this segment
            val angle = (atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat() + 90f
            driverRotationDegrees = angle

            for (i in 1..legSteps) {
                currentStep++
                val progressFactor = currentStep.toFloat() / totalSteps
                rideProgress = progressFactor

                val curX = legStart.x + dx * i
                val curY = legStart.y + dy * i
                currentDriverCoords = MapPoint(curX, curY)

                val remainingSec = maxOf(0, totalSeconds - (currentStep / stepsPerSec))
                driverEtaMinutes = (remainingSec / 2) + 1

                driverStatusText = "$statusPrefix • ETA $driverEtaMinutes min"
                delay(100L)
            }
        }

        runLeg(leg1Start, leg1End, steps1)
        runLeg(leg2Start, leg2End, steps2)
        runLeg(leg3Start, leg3End, steps3)
    }

    fun sendUserChatMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage("user", text)
        _chatMessages.value = _chatMessages.value + userMsg
        
        // Trigger simulated driver reply after 1.5 seconds
        viewModelScope.launch {
            delay(1500)
            val replyText = driverResponses.random()
            _chatMessages.value = _chatMessages.value + ChatMessage("driver", replyText)
        }
    }

    private suspend fun deductFareAndRecordTrip() {
        val finalFare = maxOf(0.0, calculatedFare - activePromoDiscount)
        val profile = repository.getUserProfileDirect() ?: UserProfile()
        
        // Check if paying with wallet, deduct cash
        if (paymentMethod == "Wallet") {
            val newBalance = maxOf(0.0, profile.walletBalance - finalFare)
            val updatedProfile = profile.copy(walletBalance = newBalance)
            repository.updateUserProfile(updatedProfile)
        }
        
        // Insert new completed trip
        val trip = Trip(
            pickupAddress = selectedPickupCoords?.name ?: "Current Location",
            dropoffAddress = selectedDropoffCoords?.name ?: "Unknown Destination",
            fare = finalFare,
            distance = routeDistanceKm,
            duration = routeDurationMin,
            tierName = selectedTier.name,
            driverName = activeDriver?.name ?: "AutoPilot",
            driverVehicle = activeDriver?.vehicleName ?: "Autonomous Pod",
            driverRating = activeDriver?.rating ?: 4.9f,
            status = "COMPLETED"
        )
        
        withContext(Dispatchers.IO) {
            repository.insertTrip(trip)
        }
    }

    fun cancelActiveRide() {
        simulationJob?.cancel()
        simulationJob = null
        
        viewModelScope.launch {
            // Record trip as cancelled if in active ride
            if (bookingStep == BookingStep.ACTIVE_RIDE || bookingStep == BookingStep.MATCHING) {
                val trip = Trip(
                    pickupAddress = selectedPickupCoords?.name ?: "Current Location",
                    dropoffAddress = selectedDropoffCoords?.name ?: "Unknown Destination",
                    fare = 0.0,
                    distance = 0.0,
                    duration = 0,
                    tierName = selectedTier.name,
                    driverName = activeDriver?.name ?: "None",
                    driverVehicle = activeDriver?.vehicleName ?: "None",
                    driverRating = 0.0f,
                    status = "CANCELLED"
                )
                withContext(Dispatchers.IO) {
                    repository.insertTrip(trip)
                }
            }
            bookingStep = BookingStep.SELECT_LOCATIONS
            activeDriver = null
            showDriverProfileModal = false
        }
    }

    fun submitDriverRating() {
        viewModelScope.launch {
            // Update last trip in the database with the feedback
            val lastTrip = trips.value.firstOrNull()
            if (lastTrip != null && lastTrip.userRating == 0) {
                val updatedTrip = lastTrip.copy(
                    userRating = ratingScore,
                    userFeedback = ratingComment
                )
                withContext(Dispatchers.IO) {
                    repository.updateTrip(updatedTrip)
                }
            }
            
            // Reset state
            ratingScore = 5
            ratingComment = ""
            bookingStep = BookingStep.SELECT_LOCATIONS
            activeDriver = null
            showDriverProfileModal = false
        }
    }

    // --- Wallet Transactions ---
    fun addWalletFunds() {
        val amount = topUpAmountText.toDoubleOrNull() ?: return
        if (amount <= 0.0) return
        
        viewModelScope.launch {
            val current = repository.getUserProfileDirect() ?: UserProfile()
            val updated = current.copy(walletBalance = current.walletBalance + amount)
            withContext(Dispatchers.IO) {
                repository.updateUserProfile(updated)
            }
            topUpAmountText = ""
        }
    }

    fun applyPromoWallet() {
        val code = promoCodeWalletText.trim().uppercase()
        viewModelScope.launch {
            if (code == "FUTURE50") {
                val current = repository.getUserProfileDirect() ?: UserProfile()
                val updated = current.copy(walletBalance = current.walletBalance + 50.0)
                withContext(Dispatchers.IO) {
                    repository.updateUserProfile(updated)
                }
                promoCodeWalletText = "Promo SUCCESS! +$50.00 Added"
            } else {
                promoCodeWalletText = "Invalid Code"
            }
        }
    }

    // --- Profile Management ---
    fun loadProfileToForm() {
        userProfile.value?.let {
            profileNameInput = it.name
            profileEmailInput = it.email
        }
    }

    fun saveProfile() {
        if (profileNameInput.isBlank() || profileEmailInput.isBlank()) return
        
        viewModelScope.launch {
            val current = repository.getUserProfileDirect() ?: UserProfile()
            val updated = current.copy(name = profileNameInput, email = profileEmailInput)
            withContext(Dispatchers.IO) {
                repository.updateUserProfile(updated)
            }
            showProfileSuccessToast = true
            delay(2000)
            showProfileSuccessToast = false
        }
    }

    // --- Saved Addresses CRUD ---
    fun saveNewAddress(label: String, address: String) {
        if (label.isBlank() || address.isBlank()) return
        viewModelScope.launch {
            val randomOffsetLat = (kotlin.random.Random.nextDouble() - 0.5) * 0.1
            val randomOffsetLng = (kotlin.random.Random.nextDouble() - 0.5) * 0.1
            val savedAddress = SavedAddress(
                label = label,
                address = address,
                lat = 37.7749 + randomOffsetLat,
                lng = -122.4194 + randomOffsetLng
            )
            withContext(Dispatchers.IO) {
                repository.insertSavedAddress(savedAddress)
            }
        }
    }

    fun deleteSavedAddress(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteSavedAddress(id)
            }
        }
    }

    fun deleteTrip(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteTrip(id)
            }
        }
    }
}
