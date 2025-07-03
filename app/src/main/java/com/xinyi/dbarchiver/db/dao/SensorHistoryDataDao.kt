package com.xinyi.dbarchiver.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xinyi.dbarchiver.db.entity.SensorHistoryDataEntity

/**
 * 传感器历史数据表Dao层
 *
 * @author 新一
 * @since 2025/6/30 13:35
 */
@Dao
interface SensorHistoryDataDao {

    /**
     * 插入单条记录，冲突时替换
     *
     * @param data 传感器历史数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: SensorHistoryDataEntity)

    /**
     * 插入多条记录，冲突时替换
     *
     * @param data 传感器历史数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<SensorHistoryDataEntity>)

    /**
     * 查询指定传感器和指定通道的数据有多少条
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     */
    @Query("SELECT COUNT(*) FROM sensor_history_data WHERE sensorName = :sensorName AND sensorChannel = :sensorChannel")
    suspend fun queryCount(sensorName: String, sensorChannel: Int): Int

    /**
     * 查询指定传感器在某时间段内的数据
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    @Query(
        """
        SELECT * FROM sensor_history_data
        WHERE sensorName = :sensorName
        AND sensorChannel = :sensorChannel
        AND createdAt BETWEEN :startTime AND :endTime
        ORDER BY createdAt ASC
        """
    )
    suspend fun queryInRange(
        sensorName: String,
        sensorChannel: Int,
        startTime: Long,
        endTime: Long,
    ): List<SensorHistoryDataEntity>

    /**
     * 分页查询指定传感器在某时间段内的数据
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道号
     * @param startTime 起始时间（包含）
     * @param endTime 结束时间（包含）
     * @param limit 每页条数
     * @param offset 偏移量（页码 * limit）
     */
    @Query(
        """
        SELECT * FROM sensor_history_data
        WHERE sensorName = :sensorName
        AND sensorChannel = :sensorChannel
        AND createdAt BETWEEN :startTime AND :endTime
        ORDER BY createdAt ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun queryInRangePaged(
        sensorName: String,
        sensorChannel: Int,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int,
    ): List<SensorHistoryDataEntity>

    /**
     * 查询某传感器通道的最新一条记录
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     */
    @Query(
        """
        SELECT * FROM sensor_history_data
        WHERE sensorName = :sensorName
          AND sensorChannel = :sensorChannel
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun queryLatest(sensorName: String, sensorChannel: Int): SensorHistoryDataEntity?

    /**
     * 分页查询
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     * @param limit 每页条数
     * @param offset 偏移
     */
    @Query(
        """
        SELECT * FROM sensor_history_data
        WHERE sensorName = :sensorName
          AND sensorChannel = :sensorChannel
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun queryPaged(
        sensorName: String,
        sensorChannel: Int,
        limit: Int,
        offset: Int,
    ): List<SensorHistoryDataEntity>

    /**
     * 删除单条记录
     *
     * @param data 传感器历史数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun delete(data: SensorHistoryDataEntity)

    /**
     * 删除多条记录
     *
     * @param data 传感器历史数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun deleteAll(data: List<SensorHistoryDataEntity>)

    /**
     * 删除某时间之前的所有数据
     */
    @Query("DELETE FROM sensor_history_data WHERE createdAt < :beforeTime")
    suspend fun deleteBefore(beforeTime: Long): Int
}