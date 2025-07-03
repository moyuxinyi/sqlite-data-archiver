package com.xinyi.dbarchiver.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xinyi.dbarchiver.db.entity.ArchiveRecordBookEntity

/**
 * 传感器历史数据表归档记录册的Dao层接口
 *
 * @author 新一
 * @since 2025/7/1 15:29
 */
@Dao
interface ArchiveRecordBookDao {

    /**
     * 查询所有记录册的数据
     */
    @Query("SELECT monthKey, tableName FROM archive_record_book")
    suspend fun queryAll(): List<ArchiveRecordBookEntity>

    /**
     * 插入记录册的数据
     *
     * @param data 数据
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: ArchiveRecordBookEntity)

    /**
     * 根据月份删除记录册的数据
     *
     * @param monthKey 月份
     */
    @Query("DELETE FROM archive_record_book WHERE monthKey = :monthKey")
    suspend fun deleteByMonth(monthKey: String)
}