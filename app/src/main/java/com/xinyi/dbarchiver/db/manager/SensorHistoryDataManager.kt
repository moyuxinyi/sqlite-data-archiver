package com.xinyi.dbarchiver.db.manager

import com.xinyi.dbarchiver.db.archive.SensorHistoryDataArchive
import com.xinyi.dbarchiver.db.entity.NewSensorHistoryDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * 传感器历史数据统一管理类
 *
 * 为了减轻主表压力，提升性能的问题，而实现了数据归档存储后，该类就负责处理传感器历史数据的归档和查询等高频操作。
 *
 * @author 新一
 * @since 2025/7/1 11:46
 */
class SensorHistoryDataManager {

    companion object {

        /** 单例设计 */
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            SensorHistoryDataManager()
        }
    }

    /**
     * 查询某通道的数据总条数。
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     */
    suspend fun getCount(sensorName: String, sensorChannel: Int): Int =
        withContext(Dispatchers.IO) {
            val countArchivesDeferred = async {
                SensorHistoryDataArchive.countAllArchives(sensorName, sensorChannel)
            }
            val countArchives = countArchivesDeferred.await()
            return@withContext countArchives
        }

    /**
     * 查询某时间段内的所有数据（跨主表和归档表）
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    suspend fun queryInRange(
        sensorName: String,
        sensorChannel: Int,
        startTime: Long,
        endTime: Long,
    ): List<NewSensorHistoryDataEntity> = withContext(Dispatchers.IO) {
        return@withContext SensorHistoryDataArchive.queryInRange(
            sensorName,
            sensorChannel,
            startTime,
            endTime
        )
    }

    /**
     * 分页查询时间段内的数据
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param pageSize 每页条数
     */
    suspend fun queryInRangePaged(
        sensorName: String,
        sensorChannel: Int,
        startTime: Long,
        endTime: Long,
        pageSize: Int,
        page: Int,
    ): List<NewSensorHistoryDataEntity> {
        return SensorHistoryDataArchive.queryInRangePaged(
            sensorName, sensorChannel, startTime, endTime, pageSize, page
        )
    }
}