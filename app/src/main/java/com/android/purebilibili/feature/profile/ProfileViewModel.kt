package com.android.purebilibili.feature.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.store.TokenManager
import com.android.purebilibili.feature.home.UserState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: UserState) : ProfileUiState()
    // LoggedOut 代表“当前是游客/未登录状态”，UI 应该显示“去登录”
    object LoggedOut : ProfileUiState()
    // 🔧 [新增] 网络错误状态 — 保持登录但显示离线提示
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                // 1. 检查本地是否有 Token，如果没有直接设为 LoggedOut
                if (TokenManager.sessDataCache.isNullOrEmpty()) {
                    _uiState.value = ProfileUiState.LoggedOut
                    return@launch
                }

                _uiState.value = ProfileUiState.Loading

                // 2. 并行请求：基本信息 + 统计信息
                val navDeferred = async { NetworkModule.api.getNavInfo() }
                val statDeferred = async { NetworkModule.api.getNavStat() }

                val navResp = navDeferred.await()
                val statResp = statDeferred.await()

                val data = navResp.data
                val statData = statResp.data

                // 3. 判断是否登录有效
                if (data != null && data.isLogin) {
                    _uiState.value = ProfileUiState.Success(
                        UserState(
                            isLogin = true,
                            face = data.face,
                            name = data.uname,
                            mid = data.mid,
                            level = data.level_info.current_level,
                            coin = data.money,
                            bcoin = data.wallet.bcoin_balance,
                            isVip = data.vip.status == 1,
                            vipLabel = data.vip.label.text,
                            // 绑定统计数据
                            following = statData?.following ?: 0,
                            follower = statData?.follower ?: 0,
                            dynamic = statData?.dynamic_count ?: 0
                        )
                    )
                } else {
                    // Cookie 过期或无效
                    TokenManager.clear(getApplication())
                    _uiState.value = ProfileUiState.LoggedOut
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 🔧 [修复] 网络错误时不清除 Token，保持登录状态
                // 区分「无网络」和「真正的服务器错误」
                val hasToken = !TokenManager.sessDataCache.isNullOrEmpty()
                if (hasToken && isNetworkError(e)) {
                    // 有 Token 但网络不可用 → 显示离线提示，不退出登录
                    _uiState.value = ProfileUiState.Error("网络不可用，请检查网络连接")
                } else if (hasToken) {
                    // 有 Token 但其他错误 → 也显示错误，不清除登录
                    _uiState.value = ProfileUiState.Error("加载失败，点击重试")
                } else {
                    // 无 Token → 显示未登录
                    _uiState.value = ProfileUiState.LoggedOut
                }
            }
        }
    }
    
    /**
     * 判断是否为网络相关错误
     */
    private fun isNetworkError(e: Exception): Boolean {
        return e is UnknownHostException ||
               e is SocketTimeoutException ||
               e is java.net.ConnectException ||
               e.cause is UnknownHostException ||
               e.cause is SocketTimeoutException
    }

    fun logout() {
        viewModelScope.launch {
            TokenManager.clear(getApplication())
            _uiState.value = ProfileUiState.LoggedOut
            //  记录登出事件
            com.android.purebilibili.core.util.AnalyticsHelper.logLogout()
        }
    }
}