package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SavedCredentials
import com.example.model.COUNTRY_CODES
import com.example.model.CountryCodeOption
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeGoldDeep
import com.example.ui.theme.FestiveCrimson
import com.example.ui.theme.RoyalPlumCard
import com.example.ui.theme.RoyalPlumDark
import com.example.ui.theme.RoyalPlumSurface
import com.example.ui.theme.TextOnDarkPrimary
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextOnGold
import com.example.ui.theme.WinterFrostCyan

private val MaroonPillFill = Color(0xFF5D212D)
private val FrostedPanelFill = Color(0x8A3B1F26)

@Composable
fun AuthScreen(
    savedCredentials: SavedCredentials,
    pendingReferralCode: String? = null,
    authError: String?,
    isBusy: Boolean,
    onClearError: () -> Unit,
    onLogin: (countryCode: String, phone: String, password: String, savePassword: Boolean) -> Unit,
    onRegister: (
        countryCode: String,
        phone: String,
        password: String,
        confirmPassword: String,
        username: String,
        savePassword: Boolean,
        referralCode: String
    ) -> Unit,
    onResetPassword: (countryCode: String, phone: String, newPassword: String) -> Unit,
    onOpenAdmin: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(!pendingReferralCode.isNullOrBlank()) }
    var selectedCountry by remember {
        mutableStateOf(
            COUNTRY_CODES.firstOrNull { it.code == savedCredentials.countryCode } ?: COUNTRY_CODES.first()
        )
    }
    var phone by remember { mutableStateOf(savedCredentials.phoneNumber) }
    var password by remember { mutableStateOf(savedCredentials.password) }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf(pendingReferralCode.orEmpty()) }
    var savePassword by remember { mutableStateOf(savedCredentials.savePassword) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showCountryMenu by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(savedCredentials) {
        if (phone.isBlank() && savedCredentials.phoneNumber.isNotBlank()) {
            phone = savedCredentials.phoneNumber
        }
        if (password.isBlank() && savedCredentials.savePassword && savedCredentials.password.isNotBlank()) {
            password = savedCredentials.password
        }
        COUNTRY_CODES.firstOrNull { it.code == savedCredentials.countryCode }?.let {
            selectedCountry = it
        }
        savePassword = savedCredentials.savePassword
    }

    LaunchedEffect(pendingReferralCode) {
        if (!pendingReferralCode.isNullOrBlank()) {
            referralCode = pendingReferralCode
            isRegisterMode = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Warm Sunset Coastal Background matching reference screenshot
        Image(
            painter = painterResource(id = R.drawable.img_login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Warm sunset glaze
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0x33FF9E44),
                            Color(0x22EC4899),
                            Color(0x443B1053)
                        )
                    )
                )
        )

        // Winter snowflake sparkles across sky & bottom snow banks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val flakes = listOf(
                0.08f to 0.24f, 0.25f to 0.14f, 0.62f to 0.19f, 0.92f to 0.34f,
                0.15f to 0.78f, 0.32f to 0.84f, 0.56f to 0.80f, 0.84f to 0.86f
            )
            flakes.forEachIndexed { idx, (fx, fy) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = (3 + (idx % 3) * 2).toFloat(),
                    center = Offset(size.width * fx, size.height * fy)
                )
            }
            // Subtle snow mound accents along bottom edge
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = size.height * 0.08f,
                center = Offset(size.width * 0.24f, size.height * 1.03f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = size.height * 0.07f,
                center = Offset(size.width * 0.44f, size.height * 1.04f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.72f),
                radius = size.height * 0.08f,
                center = Offset(size.width * 0.94f, size.height * 1.03f)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            val isWide = maxWidth >= 540.dp

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Luck780 Logo + Admin Pill + Stylish Cartoon/Anime Host Illustration
                    Box(
                        modifier = Modifier
                            .weight(0.43f)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_login_character),
                            contentDescription = "Luck780 Host Character",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(ArcadeGold.copy(alpha = 0.75f), Color.Transparent)
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                        )

                        // Floating Brand Logo + Admin button at Top-Left
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_luck780_logo),
                                contentDescription = "Luck780 Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(48.dp)
                            )
                            Surface(
                                onClick = { showAdminPinDialog = true },
                                shape = RoundedCornerShape(50),
                                color = Color(0xAA2A164A),
                                border = BorderStroke(1.dp, WinterFrostCyan.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("auth_admin_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "Admin Panel",
                                        tint = WinterFrostCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Admin",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = WinterFrostCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Right Side: Frosted Maroon Carnival Login Card matching reference screenshot
                    Box(
                        modifier = Modifier
                            .weight(0.57f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CarnivalFrostedLoginCard(
                            isRegisterMode = isRegisterMode,
                            onToggleMode = {
                                isRegisterMode = !isRegisterMode
                                onClearError()
                            },
                            selectedCountry = selectedCountry,
                            showCountryMenu = showCountryMenu,
                            onToggleCountryMenu = { showCountryMenu = it },
                            onSelectCountry = {
                                selectedCountry = it
                                showCountryMenu = false
                            },
                            phone = phone,
                            onPhoneChange = { phone = it; onClearError() },
                            password = password,
                            onPasswordChange = { password = it; onClearError() },
                            confirmPassword = confirmPassword,
                            onConfirmPasswordChange = { confirmPassword = it; onClearError() },
                            username = username,
                            onUsernameChange = { username = it; onClearError() },
                            referralCode = referralCode,
                            onReferralCodeChange = { referralCode = it; onClearError() },
                            hasPendingInviteLink = !pendingReferralCode.isNullOrBlank(),
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                            savePassword = savePassword,
                            onSavePasswordChange = { savePassword = it },
                            authError = authError,
                            isBusy = isBusy,
                            onForgotPassword = { showForgotDialog = true },
                            onPlayDemo = {
                                isRegisterMode = false
                                selectedCountry = COUNTRY_CODES.first()
                                phone = "3001234567"
                                password = "luck780"
                                savePassword = true
                                onClearError()
                                onLogin(COUNTRY_CODES.first().code, "3001234567", "luck780", true)
                            },
                            onSubmit = {
                                if (isRegisterMode) {
                                    onRegister(
                                        selectedCountry.code,
                                        phone,
                                        password,
                                        confirmPassword,
                                        username,
                                        savePassword,
                                        referralCode
                                    )
                                } else {
                                    onLogin(selectedCountry.code, phone, password, savePassword)
                                }
                            }
                        )
                    }
                }
            } else {
                // Portrait fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_luck780_logo),
                                contentDescription = "Luck780 Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(46.dp)
                            )
                            Text("Luck780", style = MaterialTheme.typography.headlineMedium, color = ArcadeGold)
                        }
                        Surface(
                            onClick = { showAdminPinDialog = true },
                            shape = RoundedCornerShape(50),
                            color = Color(0xAA2A164A),
                            border = BorderStroke(1.dp, WinterFrostCyan),
                            modifier = Modifier.testTag("auth_admin_button")
                        ) {
                            Text(
                                text = "Admin",
                                style = MaterialTheme.typography.labelLarge,
                                color = WinterFrostCyan,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    CarnivalFrostedLoginCard(
                        isRegisterMode = isRegisterMode,
                        onToggleMode = {
                            isRegisterMode = !isRegisterMode
                            onClearError()
                        },
                        selectedCountry = selectedCountry,
                        showCountryMenu = showCountryMenu,
                        onToggleCountryMenu = { showCountryMenu = it },
                        onSelectCountry = {
                            selectedCountry = it
                            showCountryMenu = false
                        },
                        phone = phone,
                        onPhoneChange = { phone = it; onClearError() },
                        password = password,
                        onPasswordChange = { password = it; onClearError() },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it; onClearError() },
                        username = username,
                        onUsernameChange = { username = it; onClearError() },
                        referralCode = referralCode,
                        onReferralCodeChange = { referralCode = it; onClearError() },
                        hasPendingInviteLink = !pendingReferralCode.isNullOrBlank(),
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                        savePassword = savePassword,
                        onSavePasswordChange = { savePassword = it },
                        authError = authError,
                        isBusy = isBusy,
                        onForgotPassword = { showForgotDialog = true },
                        onPlayDemo = {
                            isRegisterMode = false
                            selectedCountry = COUNTRY_CODES.first()
                            phone = "3001234567"
                            password = "luck780"
                            savePassword = true
                            onClearError()
                            onLogin(COUNTRY_CODES.first().code, "3001234567", "luck780", true)
                        },
                        onSubmit = {
                            if (isRegisterMode) {
                                onRegister(
                                    selectedCountry.code,
                                    phone,
                                    password,
                                    confirmPassword,
                                    username,
                                    savePassword,
                                    referralCode
                                )
                            } else {
                                onLogin(selectedCountry.code, phone, password, savePassword)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showForgotDialog) {
        ForgotPasswordDialog(
            initialCountry = selectedCountry,
            initialPhone = phone,
            onDismiss = { showForgotDialog = false },
            onConfirmReset = { cc, ph, newPwd ->
                onResetPassword(cc, ph, newPwd)
                showForgotDialog = false
            }
        )
    }

    if (showAdminPinDialog) {
        AdminPinGateDialog(
            onDismiss = { showAdminPinDialog = false },
            onSuccess = {
                showAdminPinDialog = false
                onOpenAdmin()
            }
        )
    }
}

@Composable
private fun CarnivalFrostedLoginCard(
    isRegisterMode: Boolean,
    onToggleMode: () -> Unit,
    selectedCountry: CountryCodeOption,
    showCountryMenu: Boolean,
    onToggleCountryMenu: (Boolean) -> Unit,
    onSelectCountry: (CountryCodeOption) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    referralCode: String,
    onReferralCodeChange: (String) -> Unit,
    hasPendingInviteLink: Boolean,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    savePassword: Boolean,
    onSavePasswordChange: (Boolean) -> Unit,
    authError: String?,
    isBusy: Boolean,
    onForgotPassword: () -> Unit,
    onPlayDemo: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FrostedPanelFill,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Garland Accent + Underlined "Register" / "Login" link at top-right like screenshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegisterMode) "Create Luck780 Account" else "Luck780 Player Sign-In",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRegisterMode) "Back to Login" else "Register",
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Color(0xFFF3E8EE),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleMode)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("auth_mode_toggle")
                )
            }

            // Phone No Row: "Phone No" label + Maroon Country Pill + Maroon Phone Input Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Phone No",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFDF2F8),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(82.dp)
                )

                Box {
                    Surface(
                        onClick = { onToggleCountryMenu(true) },
                        shape = RoundedCornerShape(50),
                        color = MaroonPillFill,
                        modifier = Modifier
                            .height(42.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("country_code_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedCountry.code,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select country code",
                                tint = ArcadeGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCountryMenu,
                        onDismissRequest = { onToggleCountryMenu(false) },
                        modifier = Modifier.background(RoyalPlumCard)
                    ) {
                        COUNTRY_CODES.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${option.flagEmoji} ${option.code}  ${option.country}",
                                        color = TextOnDarkPrimary
                                    )
                                },
                                onClick = { onSelectCountry(option) }
                            )
                        }
                    }
                }

                MaroonPillInput(
                    value = phone,
                    onValueChange = { input -> onPhoneChange(input.filter { it.isDigit() }.take(13)) },
                    placeholder = "3001234567",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("phone_number_input")
                )
            }

            // Optional Display Name row when registering
            AnimatedVisibility(visible = isRegisterMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Nickname",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFDF2F8),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(82.dp)
                    )
                    MaroonPillInput(
                        value = username,
                        onValueChange = onUsernameChange,
                        placeholder = "LuckyPlayer780 (Optional)",
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("register_username_input")
                    )
                }
            }

            // Password Row: "Password" label + Maroon Masked Pill Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFDF2F8),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(82.dp)
                )

                MaroonPillInput(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = "********",
                    keyboardType = KeyboardType.Password,
                    isPassword = !passwordVisible,
                    trailingContent = {
                        IconButton(
                            onClick = onTogglePasswordVisible,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFFF3E8EE).copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("password_input")
                )
            }

            // Confirm Password + Optional Reference Code/Link rows when registering
            AnimatedVisibility(visible = isRegisterMode) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFDF2F8),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(82.dp)
                        )
                        MaroonPillInput(
                            value = confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            placeholder = "Re-enter password",
                            keyboardType = KeyboardType.Password,
                            isPassword = !passwordVisible,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_password_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ref Code",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArcadeGold,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(82.dp)
                        )
                        MaroonPillInput(
                            value = referralCode,
                            onValueChange = onReferralCodeChange,
                            placeholder = "Invite Code or Link (+100 Coins)",
                            keyboardType = KeyboardType.Text,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("register_referral_input")
                        )
                    }

                    if (hasPendingInviteLink || referralCode.isNotBlank()) {
                        Surface(
                            color = Color(0x5510B981),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF34D399)),
                            modifier = Modifier.testTag("referral_bonus_banner")
                        ) {
                            Text(
                                text = "Reference Invite Active: Register to get +100 Extra Coins (988 Total)!",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFD1FAE5),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Save password checkbox (green checkmark square) + underlined Forgot password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSavePasswordChange(!savePassword) }
                        .minimumInteractiveComponentSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .testTag("save_password_checkbox")
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4B282E))
                            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (savePassword) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Saved",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Save password",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFDF2F8),
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Forgot password",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Color(0xFFFDF2F8),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onForgotPassword() }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .testTag("forgot_password_link")
                )
            }

            if (!authError.isNullOrBlank()) {
                Surface(
                    color = FestiveCrimson.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, FestiveCrimson)
                ) {
                    Text(
                        text = authError,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD6DE),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom Action Buttons: Glossy Blue "Play" + Glossy Gold "Login" (matching screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glossy Blue "Play" Button (One-Tap Demo VIP Instant Play)
                Surface(
                    onClick = onPlayDemo,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, Color(0xFF7DD3FC)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("demo_fill_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF0EA5E9),
                                        Color(0xFF0284C7)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Play",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F294A)
                        )
                    }
                }

                // Glossy Gold "Login" / "Register" Button
                Surface(
                    onClick = onSubmit,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, Color(0xFFFEF08A)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("login_submit_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFDE047),
                                        Color(0xFFF59E0B),
                                        Color(0xFFD97706)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF6B2D00),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) "Register" else "Login",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaroonPillInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaroonPillFill,
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(ArcadeGold),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
private fun AdminPinGateDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalPlumSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = ArcadeGold)
                Text("Admin Panel Access", color = ArcadeGold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Enter Admin Passcode (Demo PIN: 780780) to manage users, withdrawals, and game controls.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDarkSecondary
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; pinError = null },
                    label = { Text("Admin PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )
                if (pinError != null) {
                    Text(pinError!!, color = FestiveCrimson, style = MaterialTheme.typography.labelLarge)
                }
                TextButton(
                    onClick = { pin = "780780"; pinError = null },
                    modifier = Modifier.testTag("demo_admin_pin_fill")
                ) {
                    Text("Fill Demo Admin PIN (780780)", color = WinterFrostCyan)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.trim() == "780780" || pin.trim() == "admin123") {
                        onSuccess()
                    } else {
                        pinError = "Invalid Admin PIN. Use 780780."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold),
                modifier = Modifier.testTag("admin_pin_submit")
            ) {
                Text("Enter Admin Panel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextOnDarkSecondary)
            }
        }
    )
}

@Composable
private fun ForgotPasswordDialog(
    initialCountry: CountryCodeOption,
    initialPhone: String,
    onDismiss: () -> Unit,
    onConfirmReset: (String, String, String) -> Unit
) {
    var phone by remember { mutableStateOf(initialPhone) }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalPlumSurface,
        title = {
            Text("Reset Password", color = ArcadeGold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Enter your registered ${initialCountry.code} phone number and set a new password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnDarkSecondary
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Phone (${initialCountry.code})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmReset(initialCountry.code, phone, newPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = ArcadeGold, contentColor = TextOnGold)
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextOnDarkSecondary)
            }
        }
    )
}
