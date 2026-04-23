package dev.comon.wildex.navigation

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.comon.wildex.component.WildexConfirmDialog
import dev.comon.wildex.network.SupabaseClient
import dev.comon.wildex.screen.GuideScreen
import dev.comon.wildex.screen.MainMenuScreen
import dev.comon.wildex.screen.SplashScreen
import dev.comon.wildex.screen.TitleScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// ── 루트 네비게이션 라우트 ────────────────────────────────────────────
@Serializable
data object WildexSplashRoute

@Serializable
data object WildexTitleRoute

@Serializable
data object WildexGuideRoute

/** MainMenuScreen 셸 전체를 가리키는 루트 레벨 라우트. */
@Serializable
data object WildexMainShellRoute

@Composable
fun WildexRoot(isDarkTheme: Boolean) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        NavHost(
            navController = navController,
            startDestination = WildexSplashRoute,
        ) {
            composable<WildexSplashRoute>(
                exitTransition = { fadeOut(tween(300, easing = FastOutSlowInEasing)) },
            ) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(WildexTitleRoute) {
                            popUpTo(WildexSplashRoute) { inclusive = true }
                        }
                    },
                )
            }
            composable<WildexTitleRoute>(
                enterTransition = {
                    if (initialState.destination.hasRoute(WildexSplashRoute::class)) {
                        fadeIn(tween(300, easing = FastOutSlowInEasing))
                    } else {
                        scaleIn(
                            initialScale = 0f,
                            animationSpec = tween(500, easing = FastOutSlowInEasing),
                        ) + fadeIn(tween(500, easing = FastOutSlowInEasing))
                    }
                },
                exitTransition = {
                    scaleOut(
                        targetScale = 0f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                    ) + fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
            ) {
                val sessionStatus by SupabaseClient.client.auth.sessionStatus.collectAsState()
                val isLoggedIn = sessionStatus is SessionStatus.Authenticated

                var isLoggingIn by remember { mutableStateOf(false) }
                val googleLoginAction = SupabaseClient.client.composeAuth.rememberSignInWithGoogle(
                    onResult = { result ->
                        if (result is NativeSignInResult.Success) {
                            isLoggingIn = true
                            navController.navigate(WildexMainShellRoute) {
                                popUpTo(WildexTitleRoute) { inclusive = true }
                            }
                        }
                    },
                )
                val userNickname = if (isLoggedIn) {
                    SupabaseClient.client.auth.currentUserOrNull()
                        ?.userMetadata
                        ?.get("full_name")
                        ?.toString()
                        ?.trim('"')
                        ?: "TRAINER"
                } else "TRAINER"

                val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                    Build.MODEL.contains("Emulator", ignoreCase = true) ||
                    Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
                    Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                    (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    Build.PRODUCT.contains("sdk_gphone", ignoreCase = true) ||
                    Build.PRODUCT.contains("emulator", ignoreCase = true) ||
                    Build.PRODUCT.contains("simulator", ignoreCase = true) ||
                    Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                    Build.HARDWARE.contains("ranchu", ignoreCase = true)

                TitleScreen(
                    userNickname = userNickname,
                    isDarkTheme = isDarkTheme,
                    isLoading = isLoggingIn,
                    isLoggedIn = isLoggedIn,
                    onLoginClick = {
                        if (isLoggedIn || isEmulator) {
                            navController.navigate(WildexMainShellRoute) {
                                popUpTo(WildexTitleRoute) { inclusive = true }
                            }
                        } else {
                            googleLoginAction.startFlow()
                        }
                    },
                    onGuideClick = { navController.navigate(WildexGuideRoute) },
                )
            }
            composable<WildexGuideRoute> {
                GuideScreen(
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable<WildexMainShellRoute>(
                enterTransition = { fadeIn(tween(350, easing = FastOutSlowInEasing)) },
                exitTransition = { fadeOut(tween(350, easing = FastOutSlowInEasing)) },
                popEnterTransition = { fadeIn(tween(350, easing = FastOutSlowInEasing)) },
                popExitTransition = { fadeOut(tween(350, easing = FastOutSlowInEasing)) },
            ) {
                val sessionStatus by SupabaseClient.client.auth.sessionStatus.collectAsState()
                var showSessionExpiredDialog by remember { mutableStateOf(false) }
                // 명시적 로그아웃 여부를 로컬 플래그로 관리
                // (SDK는 signOut()과 토큰 강제 해제 모두 NotAuthenticated(isSignOut=true)를 방출하므로
                //  isSignOut만으로는 두 경우를 구별할 수 없음)
                var intentionalLogout by remember { mutableStateOf(false) }

                // 에뮬레이터 여부: Play Store AVD 포함 (Build.HARDWARE == "ranchu"/"goldfish")
                val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                    Build.MODEL.contains("Emulator", ignoreCase = true) ||
                    Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
                    Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                    (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    Build.PRODUCT.contains("sdk_gphone", ignoreCase = true) ||
                    Build.PRODUCT.contains("emulator", ignoreCase = true) ||
                    Build.PRODUCT.contains("simulator", ignoreCase = true) ||
                    Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                    Build.HARDWARE.contains("ranchu", ignoreCase = true)

                // 타이틀 화면으로 복귀하는 단일 경로 — back stack을 완전히 비우고 TitleRoute를 추가
                val navigateToTitle: () -> Unit = {
                    navController.navigate(WildexTitleRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }

                // 포그라운드 복귀 시마다 세션 유효성을 서버에 즉시 확인 (에뮬레이터 제외)
                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    if (isEmulator) return@LaunchedEffect
                    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        Log.d("test1234", "resumed")
                        try {
                            SupabaseClient.client.auth.refreshCurrentSession()
                        } catch (_: RestException) {
                            // 서버가 세션을 거부 (유저 삭제·토큰 무효 등 401/403)
                            // SDK가 sessionStatus를 업데이트하지 않고 예외만 던지는 경우 직접 처리
                            showSessionExpiredDialog = true
                        } catch (_: Exception) {
                            // 네트워크 오류 등 일시적 문제 → 무시
                        }
                    }
                }

                // 세션 상태 감시 — 에뮬레이터에서는 인증 없이 진입하므로 건너뜀
                LaunchedEffect(sessionStatus) {
                    if (isEmulator) return@LaunchedEffect
                    when (sessionStatus) {
                        is SessionStatus.NotAuthenticated -> {
                            if (!intentionalLogout) {
                                // 유저 삭제·토큰 만료 등 외부 세션 해제 → 안내 다이얼로그 표시
                                showSessionExpiredDialog = true
                            }
                            // intentionalLogout 경우 navigation은 onLogout / onConfirm 에서 직접 처리
                        }
                        is SessionStatus.RefreshFailure -> {
                            // 500 에러·네트워크 오류로 갱신 실패 → 안내 다이얼로그 표시
                            showSessionExpiredDialog = true
                        }
                        else -> Unit
                    }
                }

                if (showSessionExpiredDialog) {
                    WildexConfirmDialog(
                        titleText = "세션 만료",
                        messageText = "인증 토큰이 만료되었습니다.\n다시 로그인해주세요.",
                        confirmText = "확인",
                        dismissText = null,
                        onDismiss = {},
                        onConfirm = {
                            showSessionExpiredDialog = false
                            scope.launch {
                                intentionalLogout = true
                                runCatching { SupabaseClient.client.auth.signOut() }
                                navigateToTitle()
                            }
                        },
                    )
                }

                val isLoggedIn = sessionStatus is SessionStatus.Authenticated
                val userNickname = if (isLoggedIn) {
                    SupabaseClient.client.auth.currentUserOrNull()
                        ?.userMetadata
                        ?.get("full_name")
                        ?.toString()
                        ?.trim('"')
                        ?: "TRAINER"
                } else "TRAINER"

                MainMenuScreen(
                    animatedVisibilityScope = this,
                    isLoggedIn = isLoggedIn,
                    userNickname = userNickname,
                    onLoginClick = { navigateToTitle() },
                    onLogout = {
                        scope.launch {
                            intentionalLogout = true
                            SupabaseClient.client.auth.signOut()
                            navigateToTitle()
                        }
                    },
                )
            }
        }
    }
}
