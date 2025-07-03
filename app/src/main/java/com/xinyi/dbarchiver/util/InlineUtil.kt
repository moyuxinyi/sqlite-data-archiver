package com.xinyi.dbarchiver.util

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * 一些内联函数
 *
 * @author 新一
 * @since 2025/6/30 17:18
 */

/**
 * 快捷 launch，自动记录耗时
 *
 * @param tag 日志 TAG，默认 TAG = "Timing"
 * @param label 日志前缀描述，例如 "查询1号通道"
 * @param block 执行的挂起代码块
 */
inline fun LifecycleCoroutineScope.launchWithTiming(
    tag: String = "Timing",
    label: String = "",
    crossinline block: suspend () -> Unit
) {
    this.launch {
        val duration = measureTimeMillis {
            block()
        }
        Log.d(tag, "$label 耗时: $duration 毫秒")
    }
}