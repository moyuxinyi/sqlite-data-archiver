package com.xinyi.dbarchiver.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 自定义转换器
 *
 * @author 新一
 * @since 2025/6/30 13:53
 */
class Converters {

    companion object {
        // Gson单例，避免多实例
        private val gson = Gson()

        /**
         * 将 Map 转换为 JSON 字符串
         *
         * @param map Map 对象
         * @return JSON 字符串
         */
        fun toJson(map: Map<String, Double>?): String {
            return if (map.isNullOrEmpty()) {
                "{}"
            } else {
                gson.toJson(map)
            }
        }

        /**
         * 将 JSON 字符串转换为 Map 对象
         *
         * @param json JSON 字符串
         * @return Map 对象
         */
        fun toMap(json: String?): Map<String, Double> {
            return try {
                if (json.isNullOrEmpty()) {
                    emptyMap()
                } else {
                    gson.fromJson(json, object : TypeToken<Map<String, Double>>() {}.type)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyMap()
            }
        }
    }

    /**
     * 将 Map 转换为 JSON 字符串
     *
     * @param map Map 对象
     * @return JSON 字符串
     */
    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String {
        return if (map.isNullOrEmpty()) {
            "{}"
        } else {
            gson.toJson(map)
        }
    }

    /**
     * 将 JSON 字符串转换为 Map 对象
     *
     * @param json JSON 字符串
     * @return Map 对象
     */
    @TypeConverter
    fun toMap(json: String?): Map<String, Double> {
        return try {
            if (json.isNullOrEmpty()) {
                emptyMap()
            } else {
                gson.fromJson(json, object : TypeToken<Map<String, Double>>() {}.type)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyMap()
        }
    }
}