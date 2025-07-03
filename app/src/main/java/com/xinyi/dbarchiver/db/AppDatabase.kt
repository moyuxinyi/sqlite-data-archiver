package com.xinyi.dbarchiver.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xinyi.dbarchiver.app.AppApplication
import com.xinyi.dbarchiver.db.converters.Converters
import com.xinyi.dbarchiver.db.dao.ArchiveRecordBookDao
import com.xinyi.dbarchiver.db.dao.SensorHistoryDataDao
import com.xinyi.dbarchiver.db.entity.ArchiveRecordBookEntity
import com.xinyi.dbarchiver.db.entity.NewSensorHistoryDataEntity
import com.xinyi.dbarchiver.db.entity.SensorHistoryDataEntity

/**
 * AppDatabase 类
 *
 * @see Database 是 Room 提供的注解，用于定义数据库类。
 * - `entities`：指定数据库中的实体类（表）。
 * - `version`：指定数据库的版本号，数据库模式改变时需要更新版本号。
 * - `exportSchema`：表示是否导出数据库的模式，`false` 则不导出。
 *
 * @author 新一
 * @since 2025/6/30 13:20
 */
@Database(
    entities = [
        SensorHistoryDataEntity::class,
        NewSensorHistoryDataEntity::class,
        ArchiveRecordBookEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取 SensorHistoricalDao 对象
     */
    abstract fun getSensorHistoricalDao(): SensorHistoryDataDao

    /**
     * 获取 ArchiveRecordBookDao 对象
     */
    abstract fun getArchiveRecordBookDao(): ArchiveRecordBookDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库单例
         */
        fun getInstance(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val context = AppApplication.instance
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .build().also {
                    INSTANCE = it
                }
            }
        }
    }
}