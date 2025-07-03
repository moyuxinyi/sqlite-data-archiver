package com.xinyi.dbarchiver.task

import android.util.Log
import com.xinyi.dbarchiver.db.archive.SensorHistoryDataArchive
import com.xinyi.dbarchiver.db.converters.Converters
import com.xinyi.dbarchiver.db.entity.NewSensorHistoryDataEntity
import com.xinyi.dbarchiver.db.entity.SensorHistoryDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 批量插入模拟数据并按月归档的任务类（间隔 5 秒）
 *
 * 插入 pH 和 OXY 两支传感器数据共计 totalCount*2 条，
 * 每个月写完后自动调用归档，移动到对应年月的 archive 表。
 *
 * @param dao 主表 DAO
 * @param startTime 起始时间（毫秒）
 * @param totalCount 每种传感器要插入的记录对数
 * @param batchSize 每次批量插入的对数
 *
 * @author 杨耿雷
 * @since 2025/7/1 16:06
 */
class SensorDataInsertAndArchiveTask(
    private val startTime: Long,
    private val totalCount: Int,
    private val batchSize: Int = 20_000,
) {
    companion object {
        private val TAG = SensorDataInsertAndArchiveTask::class.java.simpleName

        // 复用不可变 Map，避免频繁创建相同对象，减少GC压力
        private val mapPH = mapOf(SensorHistoryDataEntity.KEY_TEMPERATURE to 25.56)
        private val mapOXY = mapOf(
            SensorHistoryDataEntity.KEY_TEMPERATURE to 25.59,
            SensorHistoryDataEntity.KEY_PRESSURE to 100.14
        )

        /** 月份格式化，生成 yyyy_MM */
        private val monthFmt = SimpleDateFormat("yyyy_MM", Locale.CHINA)
    }

    /**
     * 执行批量插入并归档任务
     */
    suspend fun run() = withContext(Dispatchers.IO) {
        var currentTime = startTime
        var processedPairs = 0
        val systemMonthKey = monthFmt.format(Date(System.currentTimeMillis()))
        Log.d(TAG, "仅保留系统当月 ($systemMonthKey) 数据，其他月份自动归档")

        try {
            while (processedPairs < totalCount) {
                // 计算当前批次数据所属的模拟月份
                val batchMonthKey = monthFmt.format(Date(currentTime))
                // 本批次处理数量
                val thisBatch = minOf(batchSize, totalCount - processedPairs)
                // 构建实体列表
                val list = ArrayList<NewSensorHistoryDataEntity>(thisBatch * 2)
                repeat(thisBatch) {
                    list += NewSensorHistoryDataEntity(
                        createdAt = currentTime,
                        sensorName = "pH",
                        sensorChannel = 1,
                        sensorType = 0x06,
                        sensorModel = "XC_PH_010Z",
                        sensorPrimaryValue = 6.18,
                        sensorOtherValueMap = Converters.toJson(mapPH)
                    )
                    list += NewSensorHistoryDataEntity(
                        createdAt = currentTime,
                        sensorName = "OXY",
                        sensorChannel = 2,
                        sensorType = 0x01,
                        sensorModel = "XC_OXY_010Z",
                        sensorPrimaryValue = 6.18,
                        sensorOtherValueMap = Converters.toJson(mapOXY)
                    )
                    currentTime += 5_000L
                }

                // 同步归档该月所有数据
                val count = SensorHistoryDataArchive.archiveData(list)
                Log.d(TAG, "归档月份 $batchMonthKey，共 $count 条")
                processedPairs += thisBatch

                if (processedPairs % 100_000 == 0 || processedPairs == totalCount) {
                    Log.d(TAG, "进度：已处理对数 $processedPairs / $totalCount")
                }
            }
            Log.d(TAG, "任务完成，共处理 ${totalCount * 2} 条数据。")
        } catch (ex: Exception) {
            Log.e(TAG, "执行过程中发生错误", ex)
        }
    }
}