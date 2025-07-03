package com.xinyi.dbarchiver.db.archive

import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement
import com.xinyi.dbarchiver.app.AppApplication
import com.xinyi.dbarchiver.db.AppDatabase
import com.xinyi.dbarchiver.db.entity.ArchiveRecordBookEntity
import com.xinyi.dbarchiver.db.entity.NewSensorHistoryDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 传感器历史数据归档表操作类
 *
 * 每月归档数据将存储至名如 sensor_history_data_archive_yyyy_MM 的表中。
 *
 * @author 新一
 * @since 2025/7/1 11:59
 */
object SensorHistoryDataArchive {

    private val TAG = SensorHistoryDataArchive::class.java.simpleName

    /** Room 数据库访问实例 */
    private val mDatabase = AppDatabase.getInstance()

    /** 月份格式化器，用于生成归档表名 */
    private val mMonthFormatter = SimpleDateFormat("yyyy_MM", Locale.CHINA)

    /** 归档表记录册 */
    private val mArchiveRecordBookDao = mDatabase.getArchiveRecordBookDao()

    /**
     * 缓存归档表名称
     *
     * key: 月份字符串
     * value: 归档表名称
     */
    private val mArchiveTableNameCache = mutableMapOf<String, String>()

    /**
     * 限制同时访问数据库的协程数量，防止 SQLite 并发压力过大
     */
    private val mDbSemaphore = Semaphore(4)

    /**
     * 初始化缓存归档表名称
     */
    suspend fun initArchiveRecordBookCache() {
        mArchiveTableNameCache.clear()
        mArchiveRecordBookDao.queryAll().forEach { meta ->
            mArchiveTableNameCache[meta.tableName] = meta.monthKey
        }
    }

    /**
     * 获取两个时间戳之间的月份列表
     *
     * @param start 开始时间戳
     * @param end 结束时间戳
     * @return 月份列表
     */
    private fun monthKeysBetween(start: Long, end: Long): List<String> {
        val cal = Calendar.getInstance()
        val tableNames = mutableListOf<String>()
        cal.timeInMillis = start
        // 逐月遍历
        while (cal.timeInMillis <= end) {
            tableNames += "sensor_history_data_archive_${mMonthFormatter.format(cal.time)}"
            cal.add(Calendar.MONTH, 1)
        }
        return tableNames.distinct()
    }

    /**
     * 获取归档表名称
     *
     * @param monthKey 月份字符串
     * @return 归档表名称
     */
    fun getExistingArchiveTable(monthKey: String): String? {
        return mArchiveTableNameCache[monthKey]
    }

    /**
     * 将给定的传感器历史数据按月归档到归档表中
     *
     * 注意：
     * > 实际业务中，归档逻辑是从表里拿指定日期的数据，然后迁移到归档表里，
     * > 但是这里是从生成数据的时候就已经归档到归档表中了，不需要从主表里拿数据迁移到归档表里。
     *
     * @param records 需要归档的记录列表
     * @return 实际归档的数据条目总数
     */
    suspend fun archiveData(records: List<NewSensorHistoryDataEntity>): Int = withContext(Dispatchers.IO) {
        if (records.isEmpty()) {
            return@withContext 0
        }

        var totalArchived = 0
        // 按月份分组
        val grouped = records.groupBy {
            mMonthFormatter.format(Date(it.createdAt))
        }

        val sqlDb: SupportSQLiteDatabase = AppApplication.dataBase.openHelper.writableDatabase
        for ((monthKey, recs) in grouped) {
            // 确保表存在 & 已注册
            val tableName = mArchiveTableNameCache.getOrPut(monthKey) {
                val name = "sensor_history_data_archive_$monthKey"
                createArchiveTableIfNotExists(sqlDb, name)
                mArchiveRecordBookDao.insert(ArchiveRecordBookEntity(monthKey, name))
                name
            }

            // 构造插入语句
            val stmt = sqlDb.compileStatement(buildInsertSQL(tableName))
            sqlDb.beginTransaction()
            try {
                recs.forEach { record ->
                    bindSensorRecord(stmt, record)
                    stmt.executeInsert()
                }
                sqlDb.setTransactionSuccessful()
                totalArchived += recs.size
            } catch (ex: Exception) {
                Log.e(TAG, "批量归档失败: $tableName", ex)
            } finally {
                sqlDb.endTransaction()
            }
        }
        totalArchived
    }

    /**
     * 动态创建归档表和组合索引，加速基于 sensorName, sensorChannel, createdAt 的查询
     *
     * @param db 数据库
     * @param tableName 归档表名
     */
    private fun createArchiveTableIfNotExists(db: SupportSQLiteDatabase, tableName: String) {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS $tableName (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                isArchived INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                sensorName TEXT NOT NULL,
                sensorChannel INTEGER NOT NULL,
                sensorType INTEGER NOT NULL,
                sensorModel TEXT NOT NULL,
                sensorPrimaryValue REAL NOT NULL,
                sensorOtherValueMap BLOB NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableSQL)

        // 建索引加速查询：组合索引
        val createIndexSQL = """
            CREATE INDEX IF NOT EXISTS idx_${tableName}_sensor_createdAt
            ON $tableName(sensorName, sensorChannel, createdAt)
        """.trimIndent()
        db.execSQL(createIndexSQL)
    }

    /**
     * 构建插入归档表的 SQL 语句。
     *
     * @param tableName 归档表名
     */
    private fun buildInsertSQL(tableName: String): String {
        return """
            INSERT INTO $tableName (
                isArchived, createdAt, sensorName, sensorChannel,
                sensorType, sensorModel, sensorPrimaryValue, sensorOtherValueMap
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
    }

    /**
     * 将实体绑定到插入语句。
     *
     * @param stmt 插入语句
     * @param record 实体
     */
    private fun bindSensorRecord(stmt: SupportSQLiteStatement, record: NewSensorHistoryDataEntity) {
        stmt.clearBindings()
        stmt.bindLong(1, 1) // isArchived = true
        stmt.bindLong(2, record.createdAt)
        stmt.bindString(3, record.sensorName)
        stmt.bindLong(4, record.sensorChannel.toLong())
        stmt.bindLong(5, record.sensorType.toLong())
        stmt.bindString(6, record.sensorModel)
        stmt.bindDouble(7, record.sensorPrimaryValue)
        stmt.bindString(8, record.sensorOtherValueMap)
    }

    /**
     * 获取所有归档表名。
     *
     * @return 归档表名列表
     */
    fun getAllArchiveTableNames(): List<String> = mArchiveTableNameCache.values.toList()

    /**
     * 高性能统计所有归档表中满足条件的记录总数（支持并发 + 分批查询）
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 通道号
     */
    suspend fun countAllArchives(sensorName: String, sensorChannel: Int): Int = withContext(Dispatchers.IO) {
        val db = mDatabase.openHelper.readableDatabase
        val tables = getAllArchiveTableNames()
        if (tables.isEmpty()) {
            return@withContext 0
        }

        // 并发控制
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        // 每个批处理的表数量
        val batchSize = (availableProcessors * 1.5).toInt().coerceAtLeast(1)
        val batches = tables.chunked(batchSize)

        mDbSemaphore.withPermit {
            val deferredResults = batches.map { batch ->
                async {
                    var batchSum = 0
                    for (table in batch) {
                        try {
                            val sql = """
                                SELECT COUNT(*) FROM $table 
                                WHERE sensorName = ? AND sensorChannel = ?
                            """.trimIndent()
                            val cursor = db.query(sql, arrayOf(sensorName, sensorChannel.toString()))
                            cursor.use {
                                if (it.moveToFirst()) {
                                    batchSum += it.getInt(0)
                                }
                            }
                        } catch (ex: Exception) {
                            // 可选日志打印
                            Log.e(TAG, "查询表 $table 计数失败", ex)
                        }
                    }
                    batchSum
                }
            }

            // 等待所有批处理完成，合计总数
            deferredResults.sumOf { it.await() }
        }
    }

    /**
     * 查询所有归档表指定时间段的数据并合并返回
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道号
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    suspend fun queryInRange(
        sensorName: String,
        sensorChannel: Int,
        startTime: Long,
        endTime: Long
    ): List<NewSensorHistoryDataEntity> = withContext(Dispatchers.IO) {
        val db = mDatabase.openHelper.readableDatabase
        val tables = monthKeysBetween(startTime, endTime)
        if (tables.isEmpty()) return@withContext emptyList()

        mDbSemaphore.withPermit {
            val deferredResults = tables.map { table ->
                async {
                    try {
                        val sql = """
                        SELECT * FROM $table
                        WHERE sensorName = ? AND sensorChannel = ? AND createdAt BETWEEN ? AND ?
                    """.trimIndent()
                        val args = arrayOf(sensorName, sensorChannel.toString(), startTime.toString(), endTime.toString())
                        val cursor = db.query(sql, args)
                        val list = mutableListOf<NewSensorHistoryDataEntity>()
                        cursor.use {
                            while (it.moveToNext()) {
                                list.add(cursorToEntity(it))
                            }
                        }
                        list
                    } catch (ex: Exception) {
                        Log.e(TAG, "查询表 $table 失败", ex)
                        emptyList()
                    }
                }
            }

            // 等待所有并发完成，合并结果，按 createdAt 排序
            deferredResults.flatMap { it.await() }.sortedBy { it.createdAt }
        }
    }

    /**
     * 多表 UNION ALL 查询指定时间段分页数据，按 createdAt 升序排序
     *
     * @param sensorName 传感器名称
     * @param sensorChannel 传感器通道号
     * @param startTime 起始时间戳
     * @param endTime 结束时间戳
     * @param pageSize 每页条数
     * @param page 当前页码(1起)
     * @return 当前页数据列表
     */
    suspend fun queryInRangePaged(
        sensorName: String,
        sensorChannel: Int,
        startTime: Long,
        endTime: Long,
        pageSize: Int,
        page: Int
    ): List<NewSensorHistoryDataEntity> = withContext(Dispatchers.IO) {
        val db = mDatabase.openHelper.readableDatabase
        val tables = monthKeysBetween(startTime, endTime)
        if (tables.isEmpty()) {
            return@withContext emptyList()
        }

        // 计算目标页的偏移和结束索引
        val offset = (page - 1) * pageSize
        val endIndex = offset + pageSize

        mDbSemaphore.withPermit {
            // 并发查询各表在时间范围内最多取 pageSize 条（或更多保证覆盖当前页）
            val deferredResults = tables.map { table ->
                async {
                    val sql = """
                SELECT * FROM $table
                WHERE sensorName = ? AND sensorChannel = ? AND createdAt BETWEEN ? AND ?
                ORDER BY createdAt ASC
                LIMIT ?
            """.trimIndent()

                    val args = arrayOf(
                        sensorName,
                        sensorChannel.toString(),
                        startTime.toString(),
                        endTime.toString(),
                        pageSize.toString()  // 每表最多取 pageSize 条
                    )
                    val list = mutableListOf<NewSensorHistoryDataEntity>()
                    db.query(sql, args).use { cursor ->
                        while (cursor.moveToNext()) {
                            list.add(cursorToEntity(cursor))
                        }
                    }
                    list
                }
            }

            // 收集所有结果，内存合并排序
            val allResults = deferredResults.awaitAll().flatten()
                .sortedBy { it.createdAt }

            // 截取目标页
            if (offset >= allResults.size) {
                return@withContext emptyList()
            }
            val subList = allResults.subList(offset.coerceAtLeast(0), endIndex.coerceAtMost(allResults.size))
            subList
        }
    }

    /**
     * 将 Cursor 转成实体
     */
    private fun cursorToEntity(cursor: Cursor): NewSensorHistoryDataEntity {
        return NewSensorHistoryDataEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            isArchived = cursor.getInt(cursor.getColumnIndexOrThrow("isArchived")) != 0,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            sensorName = cursor.getString(cursor.getColumnIndexOrThrow("sensorName")),
            sensorChannel = cursor.getInt(cursor.getColumnIndexOrThrow("sensorChannel")),
            sensorType = cursor.getInt(cursor.getColumnIndexOrThrow("sensorType")),
            sensorModel = cursor.getString(cursor.getColumnIndexOrThrow("sensorModel")),
            sensorPrimaryValue = cursor.getDouble(cursor.getColumnIndexOrThrow("sensorPrimaryValue")),
            sensorOtherValueMap = cursor.getString(cursor.getColumnIndexOrThrow("sensorOtherValueMap"))
        )
    }
}