package com.base.app.core.ads.sdk

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.base.app.core.ads.safely

/**
 * Lớp phủ giữ liền mạch từ lúc show interstitial/rewarded tới khi ad đóng.
 *
 * Overlay loading của lib bị gỡ ngay ở `onAdShowedFullScreenContent` — AdMob bắn callback này khi Activity của ad vừa
 * start, cửa sổ ad chưa vẽ, nên màn app lộ ra một nhịp (interstitial: loading → màn app → ad; rewarded không có
 * min loading nên nháy đen → màn app → ad). Guard của lib (`screenGuardColor`) chỉ áp cho App Open.
 *
 * Guard này giống hệt overlay của lib (nền đen, spinner trắng 48dp giữa màn) và được thêm **trước** nên nằm dưới
 * overlay lib: lib gỡ overlay thì mắt vẫn thấy cùng một khung loading cho tới khi ad phủ lên.
 */
internal object AdTransitionGuard {

    private const val TAG = "app_ads_transition_guard"
    private const val SPINNER_DP = 48

    fun show(activity: Activity) {
        safely("show transition guard") {
            val root = activity.window?.decorView as? ViewGroup ?: return
            if (root.findViewWithTag<FrameLayout>(TAG) != null) return
            val size = (activity.resources.displayMetrics.density * SPINNER_DP).toInt()
            val guard = FrameLayout(activity).apply {
                tag = TAG
                setBackgroundColor(Color.BLACK)
                // Nuốt chạm: không bấm xuyên xuống màn app trong lúc chờ ad.
                isClickable = true
                isFocusable = true
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                addView(
                    ProgressBar(activity).apply {
                        isIndeterminate = true
                        indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
                        layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
                    },
                )
            }
            root.addView(guard)
        }
    }

    fun hide(activity: Activity) {
        safely("hide transition guard") {
            val root = activity.window?.decorView as? ViewGroup ?: return
            root.findViewWithTag<FrameLayout>(TAG)?.let(root::removeView)
        }
    }
}
