package com.example.ui.components

enum class AppLanguage {
    ENGLISH,
    TAMIL
}

object LanguageHelper {
    private val tamilTranslations = mapOf(
        // General Tabs & Actions
        "Book" to "முன்பதிவு",
        "Book Cab" to "டாக்ஸி முன்பதிவு",
        "History" to "பயண வரலாறு",
        "Wallet" to "பணப்பை",
        "Profile" to "சுயவிவரம்",
        "Booking" to "முன்பதிவு",
        "Settings" to "அமைப்புகள்",
        "Cancel Ride" to "பயணத்தை ரத்துசெய்",
        "Confirm Ride" to "பயணத்தை உறுதிசெய்",
        "Select Ride Tier" to "பயண வகையைத் தேர்வுசெய்",
        "Looking for Cabs..." to "டாக்ஸிகளைத் தேடுகிறது...",
        "Arriving..." to "டாக்ஸி வந்து கொண்டிருக்கிறது...",
        "Active Trip" to "செயலில் உள்ள பயணம்",
        "Arrived at destination!" to "இலக்கை அடைந்துவிட்டது!",
        "Submit Feedback" to "கருத்தைச் சமர்ப்பிக்கவும்",
        "How was your quantum commute?" to "உங்கள் பயணம் எவ்வாறு இருந்தது?",
        "Rate your driver" to "ஓட்டுநரை மதிப்பிடவும்",
        "Any additional pilot feedback?" to "கூடுதல் கருத்து ஏதும் உள்ளதா?",
        "Skip and Reset" to "தவிர் மற்றும் மீட்டமை",
        "Where from?" to "எங்கிருந்து?",
        "Where to?" to "எங்கு செல்ல வேண்டும்?",
        "Tap map pins directly or enter address details" to "வரைபடத்தில் உள்ள இடங்களைத் தட்டவும் அல்லது முகவரியை உள்ளிடவும்",
        "Select Pickup Node" to "ஏறும் இடத்தைத் தேர்ந்தெடுக்கவும்",
        "Select Dropoff Node" to "இறங்கும் இடத்தைத் தேர்ந்தெடுக்கவும்",
        "Selected Pickup Spot" to "தேர்ந்தெடுக்கப்பட்ட ஏறும் இடம்",
        "Selected Dropoff Spot" to "தேர்ந்தெடுக்கப்பட்ட இறங்கும் இடம்",
        
        // Districts & Locations
        "Chennai" to "சென்னை",
        "Coimbatore" to "கோயம்புத்தூர்",
        "Madurai" to "மதுரை",
        "Tiruchirappalli" to "திருச்சிராப்பள்ளி",
        "Salem" to "சேலம்",
        "Tirunelveli" to "திருநெல்வேலி",
        "Vellore" to "வேலூர்",
        "Thanjavur" to "தஞ்சாவூர்",
        "Erode" to "ஈரோடு",
        "Thoothukudi" to "தூத்துக்குடி",
        "Tiruppur" to "திருப்பூர்",
        "Kanchipuram" to "காஞ்சிபுரம்",
        "Tiruvallur" to "திருவள்ளூர்",
        "Cuddalore" to "கடலூர்",
        "Dindigul" to "திண்டுக்கல்",
        "Kanniyakumari" to "கன்னியாகுமரி",
        "Krishnagiri" to "கிருஷ்ணகிரி",
        "Nagapattinam" to "நாகப்பட்டினம்",
        "Namakkal" to "நாமக்கல்",
        "Perambalur" to "பெரம்பலூர்",
        "Pudukkottai" to "புதுக்கோட்டை",
        "Ramanathapuram" to "இராமநாதபுரம்",
        "Sivagangai" to "சிவகங்கை",
        "Tenkasi" to "தென்காசி",
        "Theni" to "தேனி",
        "Nilgiris" to "நீலகிரி",
        "Thiruvarur" to "திருவாரூர்",
        "Tirupathur" to "திருப்பத்தூர்",
        "Tiruvannamalai" to "திருவண்ணாமலை",
        "Ariyalur" to "அரியலூர்",
        "Dharmapuri" to "தர்மபுரி",
        "Kallakurichi" to "கள்ளக்குறிச்சி",
        "Karur" to "கரூர்",
        "Mayiladuthurai" to "மயிலாடுதுறை",
        "Ranipet" to "இராணிப்பேட்டை",
        "Chengalpattu" to "செங்கல்பட்டு",
        "Villupuram" to "விழுப்புரம்",
        "Virudhunagar" to "விருதுநகர்",

        // Login Screen
        "Login to Future Transit" to "டாக்ஸி சேவையில் உள்நுழைக",
        "Futuristic Tamil Nadu Taxi Portal" to "தமிழ்நாடு எதிர்கால டாக்ஸி இணையதளம்",
        "Select Preferred Language" to "விருப்பமான மொழியைத் தேர்ந்தெடுக்கவும்",
        "Username / Pilot Name" to "பயனர் பெயர் / பயணிகள் பெயர்",
        "Biometric Access Email" to "மின்னஞ்சல் முகவரி",
        "Enter Display Name" to "காட்சி பெயரை உள்ளிடவும்",
        "Access Protocol (Login)" to "உள்நுழை (Login)",
        "Please enter your name and email to proceed" to "தொடர உங்கள் பெயர் மற்றும் மின்னஞ்சலை உள்ளிடவும்",
        "Fast Track (Auto-fill)" to "தானியங்கி நிரப்புதல்",

        // Wallet Screen
        "Digital Wallet" to "டிஜிட்டல் பணப்பை",
        "Manage your credits and apply futuristic fuel vouchers" to "உங்கள் வரவுகளை நிர்வகிக்கவும் மற்றும் எரிபொருள் வவுச்சர்களைப் பயன்படுத்தவும்",
        "FUTURE TRANSIT CORE" to "எதிர்கால போக்குவரத்து மையம்",
        "ACTIVE CREDIT BALANCE" to "செயலில் உள்ள இருப்புத்தொகை",
        "VALID PROTOCOL CHIP" to "முறையான பாதுகாப்பு சில்லு",
        "Quick Balance Ingress" to "விரைவான பணம் ஏற்றுதல்",
        "Custom Credit Amount ($)" to "விருப்பமான தொகை ($)",
        "Add Cash" to "பணம் சேர்க்கவும்",
        "Apply Top-up Voucher" to "வவுச்சரை பயன்படுத்தவும்",
        "Verify" to "சரிபார்க்கவும்",
        "Wallet Deductions History" to "பணப்பை கழிக்கப்பட்ட வரலாறு",
        "No charges recorded yet." to "கட்டணங்கள் ஏதும் இன்னும் பதிவு செய்யப்படவில்லை.",
        "Add Funds" to "பணம் சேர்க்கவும்",
        "Enter Promo Voucher" to "வவுச்சர் குறியீட்டை உள்ளிடவும்",

        // Settings / Profile Screen
        "Shortcuts & Profile" to "குறுக்குவழிகள் மற்றும் சுயவிவரம்",
        "Modify credentials and configure immediate teleport coordinate nodes" to "சுயவிவரம் மற்றும் முகவரி குறுக்குவழிகளை மாற்றியமைக்கவும்",
        "Passenger ID Record" to "பயணிகள் அடையாள பதிவு",
        "Display Name" to "காட்சி பெயர்",
        "Comm Address (Email)" to "தொடர்பு மின்னஞ்சல்",
        "Save Profile Credentials" to "சுயவிவர சான்றுகளை சேமிக்கவும்",
        "Profile records updated successfully!" to "சுயவிவரப் பதிவுகள் வெற்றிகரமாகப் புதுப்பிக்கப்பட்டன!",
        "Teleport Shortcuts (Saved Address Nodes)" to "தொலைத்தொடர்பு குறுக்குவழிகள் (சேமிக்கப்பட்ட முகவரிகள்)",
        "No custom shortcut locations configured. Use the form below to register shortcuts." to "தனிப்பயன் முகவரிகள் ஏதும் இல்லை. குறுக்குவழிகளைப் பதிவு செய்ய கீழே உள்ள படிவத்தைப் பயன்படுத்தவும்.",
        "Register New Coordinate Node" to "புதிய முகவரியை பதிவு செய்யவும்",
        "Shortcut Label (e.g., Home, Work, Gym)" to "குறுக்குவழிப் பெயர் (எ.கா., வீடு, வேலை)",
        "Exact Node Address" to "சரியான முகவரி",
        "Register Node" to "முகவரியைச் சேமி",

        // History Screen
        "Futuristic Ride History" to "எதிர்கால பயண வரலாறு",
        "Review of your past spatial teleport logs and automated pilot ratings" to "உங்கள் கடந்த கால பயண பதிவுகள் மற்றும் டாக்ஸி மதிப்பீடுகள்",
        "No trips recorded yet." to "பயணங்கள் ஏதும் இன்னும் பதிவு செய்யப்படவில்லை.",
        "Delete Log" to "பதிவை நீக்கு",
        "Fare" to "கட்டணம்",
        "Distance" to "தூரம்",
        "Duration" to "நேரம்",
        "Driver" to "ஓட்டுநர்",
        "Vehicle" to "வாகனம்",
        "Rating" to "மதிப்பீடு",
        "Feedback" to "கருத்து",

        // Live Location / Live Map
        "Live Location Tracking" to "நேரடி இருப்பிட கண்காணிப்பு",
        "Live Teleport Tracker" to "நேரடி இருப்பிட கண்காணிப்பு",
        "Tap on map to select pickup and drop off points" to "இடத்தைத் தேர்ந்தெடுக்க வரைபடத்தில் தட்டவும்",
        "CYBER BROADWAY" to "சைபர் பிராட்வே",
        "TECH AVENUE" to "டெக் அவென்யூ",
        "NEO FOREST" to "நியோ காடு",
        "AETHER RIVER" to "ஏதர் ஆறு"
    )

    fun translate(text: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return text
        
        // Find if the text contains a district name, or matches fully
        val result = tamilTranslations[text]
        if (result != null) return result

        // Check if it's a partial match or compound text, e.g. "Chennai (சென்னை)" or contains district name
        for ((en, ta) in tamilTranslations) {
            if (text.equals(en, ignoreCase = true)) {
                return ta
            }
            if (text.contains(en, ignoreCase = true)) {
                return text.replace(en, ta, ignoreCase = true)
            }
        }
        return text
    }
}
