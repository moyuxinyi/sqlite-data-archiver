package com.xinyi.dbarchiver.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 传感器历史数据表结构的实体类
 *
 * @author 新一
 * @since 2025/6/30 13:30
 */
@Entity(tableName = "sensor_history_data")
data class SensorHistoryDataEntity(
    /** 自增主键 */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 传感器名称 */
    val sensorName: String,
    /** 传感器通道号 */
    val sensorChannel: Int,
    /** 传感器类型 */
    val sensorType: Int,
    /** 传感器型号 */
    val sensorModel: String,
    /** 传感器主测量值 */
    val sensorPrimaryValue: Double = 0.0,
    /** 传感器其他测量值(键值对) */
    val sensorOtherValueMap: Map<String, Double> = emptyMap(),
) {

    companion object {
        /** 其他测量值(键) */
        /** 温度 */
        const val KEY_TEMPERATURE = "temperature"

        /** 压力 */
        const val KEY_PRESSURE = "pressure"
    }
}