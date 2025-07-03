package com.xinyi.dbarchiver.app

import android.app.Application
import com.xinyi.dbarchiver.db.AppDatabase

/**
 * 应用Application
 *
 * @author 新一
 * @since 2025/6/30 14:41
 */
class AppApplication : Application() {

    companion object {
        
        /**
         * 应用单例
         */
        lateinit var instance: AppApplication
        
        /**
         * 数据库单例
         */
        lateinit var dataBase: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        dataBase = AppDatabase.getInstance()
    }
}