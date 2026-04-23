package dev.comon.wildex.component

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

private const val CLICK_DEBOUNCE_MS = 500L

/**
 * 연속 클릭 방지 래퍼. [CLICK_DEBOUNCE_MS] 이내의 중복 클릭은 무시한다.
 * [rememberUpdatedState] 없이 [remember] 블록 안에서 State를 캡처하므로
 * [onClick] 참조가 바뀌어도 항상 최신 람다를 호출한다.
 */
@Composable
fun rememberDebounceClick(onClick: () -> Unit): () -> Unit {
    val lastClickTime = remember { mutableLongStateOf(0L) }
    val onClickRef = rememberUpdatedState(onClick)
    return remember {
        {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime.longValue >= CLICK_DEBOUNCE_MS) {
                lastClickTime.longValue = now
                onClickRef.value()
            }
        }
    }
}
