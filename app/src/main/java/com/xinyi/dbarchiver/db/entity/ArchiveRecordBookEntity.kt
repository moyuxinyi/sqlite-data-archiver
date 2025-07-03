package com.xinyi.dbarchiver.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 传感器历史数据表归档记录册的数据结构实体类
 *
 * @author 杨耿雷
 * @since 2025/7/1 15:28
 */
@Entity(
    tableName = "archive_record_book",
    indices = [Index("tableName")]
)
data class ArchiveRecordBookEntity(
    @PrimaryKey val tableName: String,
    /** 归档时间 "yyyy_MM" */
    val monthKey: String,
)