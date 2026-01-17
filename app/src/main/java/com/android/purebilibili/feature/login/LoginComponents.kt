package com.android.purebilibili.feature.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.ui.LoadingAnimation
import com.android.purebilibili.core.ui.SuccessAnimation
import com.android.purebilibili.data.model.response.CaptchaData
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.*
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import kotlinx.coroutines.launch

// --- Core Design Components ---

@Composable
fun LoginBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    
    // Animate colors for a dynamic feel
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1E88E5), // Blue
        targetValue = Color(0xFF8E24AA), // Purple
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color1"
    )
    
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF00ACC1), // Cyan
        targetValue = Color(0xFF43A047), // Green
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color2"
    )

    val offset1 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Gradient Mesh
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.6f)) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A1A1A))
                )
            )
        }
        
        // Floating Orbs
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-50).dp)
                .size(400.dp)
                .alpha(0.3f)
                .blur(100.dp)
                .background(color1, CircleShape)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(350.dp)
                .alpha(0.2f)
                .blur(90.dp)
                .background(color2, CircleShape)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (50 + offset1).dp, y = 200.dp)
                .size(200.dp)
                .alpha(0.15f)
                .blur(80.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )

        // Noise Overlay
        // In a real app we might use a shader or a tileable image for noise
        // For now, we simulate depth with a solid overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
    ) {
        content()
    }
}

@Composable
fun BrandingHeader(isSmall: Boolean = false) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Logo
        Surface(
            shape = RoundedCornerShape(if (isSmall) 16.dp else 22.dp),
            shadowElevation = 0.dp, // No shadow for flat glass look
            color = Color.Transparent,
            modifier = Modifier.size(if (isSmall) 56.dp else 88.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(com.android.purebilibili.R.mipmap.ic_launcher_3d)
                    .crossfade(true)
                    .build(),
                contentDescription = "BiliPai",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(if (isSmall) 12.dp else 20.dp))

        Text(
            text = "BiliPai",
            fontSize = if (isSmall) 20.sp else 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (!isSmall) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Discover the anime world",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun LoginMethodTabs(
    selectedMethod: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit
) {
    GlassCard(
        shape = RoundedCornerShape(100.dp), // Capsule shape
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoginMethod.entries.forEach { method ->
                val isSelected = method == selectedMethod
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    label = "tab_bg"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                    label = "tab_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(animatedColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No ripple for clean look
                        ) { onMethodChange(method) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (method) {
                            LoginMethod.QR_CODE -> "扫码"
                            LoginMethod.PHONE_SMS -> "手机"
                            LoginMethod.WEB_LOGIN -> "网页"
                        },
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = animatedTextColor
                    )
                }
            }
        }
    }
}

// --- Specific Login Contents ---

@Composable
fun QrCodeLoginContent(
    state: LoginState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // QR Code Card
        GlassCard(
            modifier = Modifier.size(240.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)) // White bg for QR readability
            ) {
                when (state) {
                    is LoginState.Loading -> LoadingAnimation(size = 48.dp)
                    is LoginState.QrCode -> Image(
                        bitmap = state.bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                    is LoginState.Scanned -> {
                        Image(
                            bitmap = state.bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.2f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                CupertinoIcons.Default.Iphone,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "已扫描\n请在其手机上确认",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is LoginState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                CupertinoIcons.Default.ExclamationmarkCircle,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("加载失败", color = Color.Black.copy(alpha = 0.6f), fontSize = 12.sp)
                            TextButton(onClick = onRefresh) {
                                Text("点击刷新", color = Color.Black)
                            }
                        }
                    }
                    is LoginState.Success -> {
                        SuccessAnimation(size = 64.dp)
                    }
                    else -> LoadingAnimation(size = 48.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instruction Text
        GlassCard(shape = RoundedCornerShape(12.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    CupertinoIcons.Default.Camera,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "使用 Bilibili 移动端扫码登录",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
             text = "推荐使用扫码登录，解锁 4K/HDR 画质",
             color = Color.White.copy(alpha = 0.4f),
             fontSize = 12.sp
        )
    }
}

@Composable
fun PhoneLoginContent(
    state: LoginState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var captchaManager by remember { mutableStateOf<CaptchaManager?>(null) }
    
    // Auto login on success
    LaunchedEffect(state) {
        if (state is LoginState.Success) onLoginSuccess()
    }
    
    // Dispose resources
    DisposableEffect(Unit) {
        onDispose { captchaManager?.destroy() }
    }
    
    // Captcha Logic
    val captchaReady = state is LoginState.CaptchaReady
    val captchaData = (state as? LoginState.CaptchaReady)?.captchaData
    
    LaunchedEffect(captchaReady, captchaData) {
        if (captchaReady && captchaData != null && activity != null) {
            captchaManager = CaptchaManager(activity)
            captchaManager?.startCaptcha(
                gt = captchaData.geetest?.gt ?: "",
                challenge = captchaData.geetest?.challenge ?: "",
                onSuccess = { validate, seccode, challenge ->
                    viewModel.saveCaptchaResult(validate, seccode, challenge)
                    viewModel.sendSmsCode(phoneNumber.toLongOrNull() ?: 0)
                },
                onFailed = { error ->
                    android.util.Log.e("PhoneLogin", "Captcha failed: $error")
                }
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
    ) {
        
        // Phone Input
        ModernTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) phoneNumber = it },
            placeholder = "手机号码",
            icon = CupertinoIcons.Default.Phone
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (state is LoginState.SmsSent || (state is LoginState.Error && smsCode.isNotEmpty())) {
             // SMS Code Input (only shown when SMS sent or typing code)
            ModernTextField(
                value = smsCode,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) smsCode = it },
                placeholder = "验证码",
                icon = CupertinoIcons.Default.Lock
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Button
            ModernButton(
                text = "登录",
                onClick = { 
                    smsCode.toIntOrNull()?.let { viewModel.loginBySms(it) } 
                },
                enabled = smsCode.length == 6,
                isLoading = state is LoginState.Loading
            )
        } else {
             Spacer(modifier = Modifier.height(8.dp))
             // Get Code Button
             ModernButton(
                text = "获取验证码",
                onClick = { 
                    viewModel.getCaptcha() 
                },
                enabled = phoneNumber.length == 11,
                isLoading = state is LoginState.Loading
            )
        }
        
        if (state is LoginState.Error) {
             Spacer(modifier = Modifier.height(16.dp))
             Text(
                 text = state.msg,
                 color = Color(0xFFFF5252),
                 fontSize = 13.sp
             )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
             text = "手机号登录最高支持 1080P 画质",
             color = Color.White.copy(alpha = 0.4f),
             fontSize = 12.sp
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebLoginContent(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    CupertinoIcons.Default.Network,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "网页安全登录",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "跳转至 Bilibili 官方网页完成登录",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                ModernButton(
                    text = "打开浏览器登录",
                    onClick = {
                         val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://passport.bilibili.com/h5-app/passport/login")
                        )
                        context.startActivity(intent)
                    }
                )
                
                 Spacer(modifier = Modifier.height(12.dp))
                 
                 TextButton(
                     onClick = { 
                         scope.launch {
                             isChecking = true
                             val cookieManager = CookieManager.getInstance()
                             val url = "https://passport.bilibili.com"
                             val cookies = cookieManager.getCookie(url)
                             if (cookies != null && cookies.contains("SESSDATA")) {
                                 com.android.purebilibili.core.store.TokenManager.saveCookies(context, 
                                     cookies.split(";").find { it.trim().startsWith("SESSDATA") }?.substringAfter("=") ?: "")
                                 onLoginSuccess()
                             }
                             isChecking = false
                         }
                     }
                 ) {
                     if (isChecking) {
                         CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                         Spacer(modifier = Modifier.width(8.dp))
                     }
                     Text("已完成登录？点击验证", color = MaterialTheme.colorScheme.primary)
                 }
            }
        }
    }
}

// --- Primitive Widgets ---

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            GlassCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        }
    )
}

@Composable
fun ModernButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.5f)
            )
        }
    }
}

@Composable
fun TopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                CupertinoIcons.Default.ChevronBackward,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
