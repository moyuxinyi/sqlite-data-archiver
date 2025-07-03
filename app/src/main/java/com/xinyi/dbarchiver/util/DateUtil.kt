package com.xinyi.dbarchiver.util

import java.util.Calendar

/**
 * 日期工具类
 *
 * @author 新一
 * @since 2025/6/30 16:36
 */
object DateUtil {

    /**
     * 获取 N 年前“今天”的日期的 00:00:00.000 时间戳
     *
     * @param yearsAgo 向前推的年份数
     * @return 时间戳（毫秒）
     */
    fun getTimestampYearsAgo(yearsAgo: Int): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.YEAR, -yearsAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * 获取指定年月日的 00:00:00 的时间戳（单位：毫秒）
     *
     * @param year 年
     * @param month 月（1-12）
     * @param day 日
     * @return 时间戳（毫秒）
     */
    fun getStartOfDayTimestamp(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * 获取指定年月日的 23:59:59.999 的时间戳（单位：毫秒）
     *
     * @param year 年
     * @param month 月（1-12）
     * @param day 日
     * @return 时间戳（毫秒）
     */
    fun getEndOfDayTimestamp(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}