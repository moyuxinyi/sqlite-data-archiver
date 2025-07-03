package com.xinyi.dbarchiver.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 传感器历史数据表结构的实体类
 *
 * @author 杨耿雷
 * @since 2025/7/1 11:05
 */
@Entity(
    tableName = "new_sensor_history_data",
    indices = [
        Index(value = ["sensorName", "sensorChannel", "createdAt", "sensorType", "sensorModel"])
    ]
)
data class NewSensorHistoryDataEntity(
    /** 自增主键 */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 标记是否归档 */
    val isArchived: Boolean = false,
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
    val sensorOtherValueMap: String = "{}"
)