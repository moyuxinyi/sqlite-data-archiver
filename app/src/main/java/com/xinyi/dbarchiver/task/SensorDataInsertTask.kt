package com.xinyi.dbarchiver.task

import android.util.Log
import com.xinyi.dbarchiver.db.dao.SensorHistoryDataDao
import com.xinyi.dbarchiver.db.entity.SensorHistoryDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 批量插入模拟数据的任务类（间隔 5 秒）
 *
 * @param dao 传感器历史数据表Dao层
 * @param startTime 开始时间
 * @param totalCount 总条数
 * @param batchSize 每次插入条数
 *
 * @author 新一
 * @since 2025/6/30 14:10
 */
class SensorDataInsertTask(
    private val dao: SensorHistoryDataDao,
    private val startTime: Long,
    private val totalCount: Int,
    private val batchSize: Int = 10000
) {

    companion object {
        private val TAG = SensorDataInsertTask::class.java.simpleName

        // 复用不可变 Map，避免频繁创建相同对象，减少GC压力
        private val mapPH = mapOf(SensorHistoryDataEntity.KEY_TEMPERATURE to 25.56)
        private val mapOXY = mapOf(
            SensorHistoryDataEntity.KEY_TEMPERATURE to 25.59,
            SensorHistoryDataEntity.KEY_PRESSURE to 100.14
        )
    }

    /**
     * 执行批量插入任务
     *
     * 日志每插入 10 万对数据打印一次进度。
     *
     * @throws Exception 插入过程中遇到异常时抛出
     */
    suspend fun run() = withContext(Dispatchers.IO) {
        var currentTime = startTime
        var insertedCount = 0
        val start = System.currentTimeMillis()

        try {
            while (insertedCount < totalCount) {
                val batchList = mutableListOf<SensorHistoryDataEntity>()
                for (i in 0 until batchSize.coerceAtMost(totalCount - insertedCount)) {
                    val dataPH = SensorHistoryDataEntity(
                        createdAt = currentTime,
                        sensorName = "pH",
                        sensorChannel = 1,
                        sensorType = 0x06,
                        sensorModel = "XC_PH_010Z",
                        sensorPrimaryValue = 6.18,
                        sensorOtherValueMap = mapPH
                    )
                    val dataOXY = SensorHistoryDataEntity(
                        createdAt = currentTime,
                        sensorName = "OXY",
                        sensorChannel = 2,
                        sensorType = 0x01,
                        sensorModel = "XC_OXY-DO_010A",
                        sensorPrimaryValue = 7.5,
                        sensorOtherValueMap = mapOXY
                    )
                    currentTime += 5000

                    batchList.add(dataPH)
                    batchList.add(dataOXY)
                    insertedCount++
                }
                dao.insertAll(batchList)
                // 每插入 10 万对数据打印一次进度
                if (insertedCount % 100_000 == 0 || insertedCount == totalCount) {
                    Log.d(TAG, "已插入数据条数: $insertedCount / $totalCount")
                }
            }
            Log.d(TAG, "任务完成，成功插入 $totalCount 条数据。")
        } catch (ex: Exception) {
            Log.e(TAG, "插入数据时发生错误: ", ex)
        }

        val end = System.currentTimeMillis()
        Log.d(TAG, "插入 $totalCount 条数据共耗时: ${end - start} ms")
    }
}