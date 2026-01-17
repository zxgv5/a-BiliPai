package com.android.purebilibili.feature.login

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// Enums
enum class LoginMethod {
    QR_CODE,
    PHONE_SMS,
    WEB_LOGIN
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedMethod by remember { mutableStateOf(LoginMethod.QR_CODE) }

    // Handle navigation when login is successful
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
        }
    }
    
    // System Bar Handling
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val insetsController = if (window != null) WindowInsetsControllerCompat(window, view) else null
        
        val originalStatusBarColor = window?.statusBarColor ?: 0
        val originalNavBarColor = window?.navigationBarColor ?: 0
        val originalLightStatusBars = insetsController?.isAppearanceLightStatusBars ?: true
        
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            insetsController?.isAppearanceLightStatusBars = false // Dark text for our dark bg
            insetsController?.isAppearanceLightNavigationBars = false
        }
        
        onDispose {
            if (window != null) {
                window.statusBarColor = originalStatusBarColor
                window.navigationBarColor = originalNavBarColor
                insetsController?.isAppearanceLightStatusBars = originalLightStatusBars
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadQrCode()
    }
    
    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Shared Animated Background
        LoginBackground()
        
        // 2. Responsive Layout Switcher
        ResponsiveLoginLayout(
            selectedMethod = selectedMethod,
            onMethodChange = { selectedMethod = it },
            state = state,
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onClose = onClose
        )
    }
}

@Composable
fun ResponsiveLoginLayout(
    selectedMethod: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit,
    state: LoginState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onClose: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight
        // Simple logic for demonstration: Tablet if width > 600dp, else Phone
        val isTablet = width >= 600.dp
        val isLandscape = width > height

        if (isTablet) {
            LoginLayoutTablet(
                selectedMethod, onMethodChange, state, viewModel, onLoginSuccess, onClose
            )
        } else if (isLandscape && height < 500.dp) {
            LoginLayoutMobileLandscape(
                selectedMethod, onMethodChange, state, viewModel, onLoginSuccess, onClose
            )
        } else {
            LoginLayoutMobilePortrait(
                selectedMethod, onMethodChange, state, viewModel, onLoginSuccess, onClose
            )
        }
    }
}

// --- Layout Variants ---

@Composable
fun LoginLayoutMobilePortrait(
    selectedMethod: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit,
    state: LoginState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        TopBar(onClose = onClose)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Branding
        BrandingHeader(isSmall = false)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Tabs
        LoginMethodTabs(selectedMethod, onMethodChange)
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Content with transition
        LoginContentArea(
            selectedMethod = selectedMethod,
            state = state,
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onRefreshQr = { viewModel.loadQrCode() },
            modifier = Modifier.weight(1f, fill = false)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footer
        Text(
            text = "登录即代表同意用户协议和隐私政策",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun LoginLayoutMobileLandscape(
    selectedMethod: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit,
    state: LoginState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        // Left Side: Branding & Back
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            TopBar(onClose = onClose)
            Spacer(modifier = Modifier.weight(1f))
            Box(Modifier.padding(start = 24.dp)) {
                BrandingHeader(isSmall = false)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Right Side: Login Card
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginMethodTabs(selectedMethod, onMethodChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    LoginContentArea(
                        selectedMethod = selectedMethod,
                        state = state,
                        viewModel = viewModel,
                        onLoginSuccess = onLoginSuccess,
                        onRefreshQr = { viewModel.loadQrCode() }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginLayoutTablet(
    selectedMethod: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit,
    state: LoginState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Close Button (Fixed at top-right of screen)
        Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            TopBar(onClose = onClose) // Reuse TopBar but maybe position it absolutely if needed
        }

        // Central Card
        GlassCard(
            modifier = Modifier
                .width(420.dp)
                .heightIn(min = 600.dp, max = 800.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                BrandingHeader(isSmall = true)
                Spacer(modifier = Modifier.height(32.dp))
                
                LoginMethodTabs(selectedMethod, onMethodChange)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LoginContentArea(
                    selectedMethod = selectedMethod,
                    state = state,
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess,
                    onRefreshQr = { viewModel.loadQrCode() }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "登录即代表同意用户协议和隐私政策",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// --- Content Switcher with Animation ---

@Composable
fun LoginContentArea(
    selectedMethod: LoginMethod,
    state: LoginState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onRefreshQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = selectedMethod,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) + slideInVertically { height -> height / 20 } togetherWith
            fadeOut(animationSpec = tween(300)) + slideOutVertically { height -> -height / 20 }
        },
        label = "login_content",
        modifier = modifier
    ) { method ->
        when (method) {
            LoginMethod.QR_CODE -> QrCodeLoginContent(state, onRefreshQr)
            LoginMethod.PHONE_SMS -> PhoneLoginContent(state, viewModel, onLoginSuccess)
            LoginMethod.WEB_LOGIN -> WebLoginContent(onLoginSuccess)
        }
    }
}