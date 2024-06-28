package com.example.weather

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.weather.ui.theme.WeatherTheme
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import org.json.JSONObject

class Page2Activity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chosenCity = intent.getStringExtra("chosenCity")

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                sendStatusBarNotification(this, "条件满足，发送消息")
            } else {
                Log.e("Page2Activity", "通知权限被拒绝")
            }
        }

        setContent {
            WeatherTheme {
                val sharedPref = getSharedPreferences("SubscriptionPrefs", Context.MODE_PRIVATE)
                val subscriptionState = remember { mutableStateListOf<Boolean>() }
                for (i in 1..5) {
                    subscriptionState.add(sharedPref.getBoolean("button_$i", false))
                }
                Page2Screen(chosenCity, subscriptionState) { index, isSelected ->
                    with(sharedPref.edit()) {
                        putBoolean("button_${index + 1}", isSelected)
                        apply()
                    }
                }
            }
        }
    }
}

@Composable
fun Page2Screen(chosenCity: String? = null, subscriptionState: MutableList<Boolean>, onSelectionChanged: (Int, Boolean) -> Unit) {
    val buttonWidth = 150.dp
    val buttonHeight = 40.dp
    val buttonSpacing = 30.dp

    val buttonNames = listOf("户外预警", "商务出行", "种植活动", "摄影计划", "上学提醒")

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(buttonSpacing),
            modifier = Modifier.padding(top = buttonSpacing)
        ) {
            subscriptionState.forEachIndexed { index, isSelected ->
                Button(
                    onClick = {
                        val newSelection = !isSelected
                        subscriptionState[index] = newSelection
                        onSelectionChanged(index, newSelection)
                    },
                    modifier = Modifier
                        .width(buttonWidth)
                        .height(buttonHeight)
                ) {
                    Text("${buttonNames[index]} ${if (isSelected) "已选定" else "未选定"}")
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val job = scope.launch {
            try {
                while (isActive) {
                    val apiResponse = fetchDataFromApi("https://qqlykm.cn/api/weather/get?key=0n6bTg9oRHXkGwT1glMY8iBWuZ&city=$chosenCity")
                    Log.e("Page2Screen", "apiResponse: $apiResponse")
                    val apiData = JSONObject(apiResponse)

                    // 用户选定了户外活动(choose = 1)
                    if (subscriptionState[0]) {
                        // 检查天气数据，如果不适合户外活动，发送通知
                        if (checkCondition(apiData,1) == 0) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "不适合户外运动")
                            }
                        }
                        if (checkCondition(apiData,1) == 1) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "适合户外运动")
                            }
                        }
                    }

                    // 用户选定了商务出行(choose = 2)
                    if (subscriptionState[1]) {
                        // 检查天气数据，如果不适合商务出行，发送通知
                        if (checkCondition(apiData,2) == 0) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "不适合商务出行")
                            }
                        }
                        if (checkCondition(apiData,2) == 1) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "适合商务出行")
                            }
                        }
                    }

                    // 用户选定了种植活动(choose = 3)
                    if (subscriptionState[2]) {
                        // 检查天气数据，如果不适合种植活动，发送通知
                        if (checkCondition(apiData,3) == 0) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "不适合种植活动")
                            }
                        }
                        if (checkCondition(apiData,3) == 1) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "适合种植活动")
                            }
                        }
                    }

                    // 用户选定了摄影计划(choose = 4)
                    if (subscriptionState[3]) {
                        // 检查天气数据，如果不适合摄影计划，发送通知
                        if (checkCondition(apiData,4) == 0) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "不适合摄影计划")
                            }
                        }
                        if (checkCondition(apiData,4) == 1) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "适合摄影计划")
                            }
                        }
                    }

                    // 用户选定了上学提醒(choose = 5)
                    if (subscriptionState[4]) {
                        // 检查天气数据，如果不适合上学提醒，发送通知
                        if (checkCondition(apiData,5) == 0) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "不适合上学提醒")
                            }
                        }
                        if (checkCondition(apiData,5) == 1) {
                            withContext(Dispatchers.Main) {
                                sendStatusBarNotification(context, "适合上学提醒")
                            }
                        }
                    }

                    delay(60000)
                }
            } catch (e: Exception) {
                Log.e("Page2Screen", "Error in background task", e)
            }
        }
        onDispose {
            job.cancel()
        }
    }
}

suspend fun fetchDataFromApi(apiUrl: String): String {
    val client = HttpClient()
    return try {
        val response: HttpResponse = client.get(apiUrl)
        if (response.status.value == 200) {
            response.bodyAsText()
        } else {
            "Error: ${response.status.value}"
        }
    } catch (e: Exception) {
        "Exception: ${e.message}"
    } finally {
        client.close()
    }
}

fun checkCondition(apiData: JSONObject, choose: Int): Int {
    //从JSON格式的apiData中提取主要的天气数据，根据天气数据判断是否适合户外活动、商务出行、种植活动、摄影计划、上学提醒
    //取出实时温度，天气状况，空气污染指数，日平均风力，日最高气温，日最低气温
    val realtimeTemp = apiData.getJSONObject("data").getString("current_temperature").toInt()
    val weather = apiData.getJSONObject("data").getString("current_weather")
    val aqi = apiData.getJSONObject("data").getString("aqi").toInt()
    val wind = apiData.getJSONObject("data").getString("wind_level").toInt()
    val maxTemp = apiData.getJSONObject("data").getString("today_high_temperature").toInt()
    val minTemp = apiData.getJSONObject("data").getString("today_low_temperature").toInt()

    // 0表示不适合户外活动，1表示适合户外活动
    if (choose == 1) {
        if (realtimeTemp < 0 || realtimeTemp > 30) {
            return 0
        }
        if (weather == "雨" || weather == "雪") {
            return 0
        }
        if (aqi > 100) {
            return 0
        }
        if (wind > 3) {
            return 0
        }
        if (maxTemp > 30 || minTemp < 0) {
            return 0
        }
    }

    // 0表示不适合商务出行，1表示适合商务出行
    if (choose == 2) {
        if (realtimeTemp < 0 || realtimeTemp > 30) {
            return 0
        }
        if (weather == "雨" || weather == "雪") {
            return 0
        }
        if (aqi > 100) {
            return 0
        }
        if (wind > 3) {
            return 0
        }
        if (maxTemp > 30 || minTemp < 0) {
            return 0
        }
    }

    // 0表示不适合种植出行，1表示适合种植出行
    if (choose == 3) {
        if (realtimeTemp < 0 || realtimeTemp > 30) {
            return 0
        }
        if (weather == "雨" || weather == "雪") {
            return 0
        }
        if (aqi > 100) {
            return 0
        }
        if (wind > 3) {
            return 0
        }
        if (maxTemp > 30 || minTemp < 0) {
            return 0
        }
    }

    // 0表示不适合摄影计划，1表示适合摄影计划
    if (choose == 4) {
        if (realtimeTemp < 0 || realtimeTemp > 30) {
            return 0
        }
        if (weather == "雨" || weather == "雪") {
            return 0
        }
        if (aqi > 100) {
            return 0
        }
        if (wind > 3) {
            return 0
        }
        if (maxTemp > 30 || minTemp < 0) {
            return 0
        }
    }

    // 0表示不适合上学提醒，1表示适合上学提醒
    if (choose == 5) {
        if (realtimeTemp < 0 || realtimeTemp > 30) {
            return 0
        }
        if (weather == "雨" || weather == "雪") {
            return 0
        }
        if (aqi > 100) {
            return 0
        }
        if (wind > 3) {
            return 0
        }
        if (maxTemp > 30 || minTemp < 0) {
            return 0
        }
    }

    return 1
}

fun sendStatusBarNotification(context: Context, message: String) {
    val channelId = "weather_channel_id"
    val channelName = "Weather Notifications"
    val importance = NotificationManager.IMPORTANCE_DEFAULT

    val channel = NotificationChannel(channelId, channelName, importance).apply {
        description = "Channel for weather notifications"
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_xiaoheizi)
        .setContentTitle("Weather Alert")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        (context as? Activity)?.let {
            ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        return
    }

    with(NotificationManagerCompat.from(context)) {
        notify(1, builder.build())
    }
}

@Preview(showBackground = true)
@Composable
fun Page2ScreenPreview() {
    WeatherTheme {
        val subscriptionState = remember { mutableStateListOf(false, false, false, false, false) }
        Page2Screen(null,subscriptionState) { _, _ -> }
    }
}
