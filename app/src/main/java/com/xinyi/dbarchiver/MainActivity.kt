package com.xinyi.dbarchiver

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.xinyi.dbarchiver.databinding.ActivityMainBinding
import com.xinyi.dbarchiver.db.archive.SensorHistoryDataArchive
import com.xinyi.dbarchiver.db.manager.SensorHistoryDataManager
import com.xinyi.dbarchiver.task.SensorDataInsertAndArchiveTask
import com.xinyi.dbarchiver.util.DateUtil
import com.xinyi.dbarchiver.util.launchWithTiming
import kotlinx.coroutines.launch

/**
 * 主页
 * 
 * @author 新一
 * @since 2025/6/30 14:47
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initParams(savedInstanceState)
        initListeners()
    }

    /**
     * 初始化视图
     */
    private fun initViews() { }

    /**
     * 初始化参数
     */
    private fun initParams(savedInstanceState: Bundle?) {
        // 插入模拟数据, 每种5000万，任务模拟任务类中每次会分别插入一条pH和OXY，所以实际数据量为1个亿
        /*val task = SensorDataInsertTask(
            dao = AppApplication.dataBase.getSensorHistoricalDao(),
            // 7年前的今天
            startTime = DateUtil.getTimestampYearsAgo(7),
            totalCount = 5000_0000,
        )
        lifecycleScope.launch {
            task.run()
        }*/

        // 历史数据的dao层接口
       // val dao = AppApplication.dataBase.getSensorHistoricalDao()

/*
        lifecycleScope.launchWithTiming(tag = TAG, label = "查询1号通道 pH") {
            val count = dao.queryCount("pH", 1)
            Log.d(TAG, "1号通道 pH 的数据有: $count 条")
        }

        lifecycleScope.launchWithTiming(tag = TAG, label = "查询2号通道 OXY") {
            val count = dao.queryCount("OXY", 2)
            Log.d(TAG, "2号通道 OXY 的数据有: $count 条")
        }
*/
        // 插入模拟数据, 每种5000万，每条数据间隔5秒，任务模拟任务类中每次会分别插入一条pH和OXY，所以实际数据量为1个亿
        /*val task = SensorDataInsertAndArchiveTask(
            dao = AppDatabase.getInstance().getNewSensorHistoricalDao(),
            // 7年前的今天
            startTime = DateUtil.getTimestampYearsAgo(7),
            totalCount = 50_000_000  // 每种 5000 万
        )
        lifecycleScope.launch {
            // 初始化缓存归档表名称
            SensorHistoryDataArchive.initArchiveRecordBookCache()
            task.run()
        }*/
        lifecycleScope.launch {
            // 初始化缓存归档表名称
            SensorHistoryDataArchive.initArchiveRecordBookCache()
        }
    }

    /**
     * 初始化监听器
     */
    private fun initListeners() {
        binding.btTestDb.setOnClickListener {
            lifecycleScope.launchWithTiming(tag = TAG, label = "查询2号通道 OXY") {
                SensorHistoryDataManager.instance.queryInRangePaged(
                    "OXY",
                    2,
                    DateUtil.getStartOfDayTimestamp(2018, 7, 1),
                    DateUtil.getEndOfDayTimestamp(2018, 7, 1),
                    50,
                    1
                ).let {
                    Log.d(TAG, "2号通道 OXY 在2018年7月1日第一页的数据有: ${it.size} 条")
                }
            }
        }
    }
}