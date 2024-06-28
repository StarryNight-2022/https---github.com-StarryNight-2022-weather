package com.example.weather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.weather.ui.theme.WeatherTheme
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

class Page1Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chosenCity = intent.getStringExtra("chosenCity")

        setContent {
            WeatherTheme {
                Page1Screen(chosenCity)
            }
        }
    }
}

fun transformUrlWithLocalDate(url: String): List<String> {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH")

    // 生成三个日期：当前时间的前一个小时、12小时前、23小时前
    val timestamps = listOf(
        currentDateTime.minusHours(1).format(formatter),
        currentDateTime.minusHours(12).format(formatter),
        currentDateTime.minusHours(23).format(formatter)
    )

    // 替换 URL 中的日期时间部分
    return timestamps.map { timestamp ->
        url.replaceFirst("""/\d{4}/\d{2}/\d{2}/\d{2}/""".toRegex(), "/$timestamp/")
    }
}

// get data from API
fun fetchData1(apiUrl: String): CompletableFuture<String> {
    val future = CompletableFuture<String>()
    val client = HttpClient()

    Thread {
        try {
            val result = runBlocking {
                val response: HttpResponse = client.get(apiUrl)
                if (response.status.value == 200) {
                    String(response.readBytes(), Charsets.UTF_8)
                } else {
                    "Error code ${response.status.value}"
                }
            }
            future.complete(result)
        } catch (e: Exception) {
            future.complete("Exception: ${e.message}")
        } finally {
            client.close()
        }
    }.start()

    return future
}
fun main() {
    val url = "https://imagery.qweather.net/imagery/tmp/2024/06/27/17/cn.jpg"
    val transformedUrls = transformUrlWithLocalDate(url)
    transformedUrls.forEach { println(it) }
}


@Composable
fun Page1Screen(chosenCity: String? = null) {
    val context = LocalContext.current
    val url = "https://imagery.qweather.net/imagery/tmp/2024/06/27/17/cn.jpg"
    val transformedUrls = transformUrlWithLocalDate(url)

    val apiUrl = "https://qqlykm.cn/api/weather/get?key=0n6bTg9oRHXkGwT1glMY8iBWuZ&city=$chosenCity"
    val resultFuture: CompletableFuture<String> = fetchData1(apiUrl)
    var result = ""

    try {
        result = resultFuture.get() // 这将阻塞直到结果可用
        println("Result: $result")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    Log.e("Page1Activity","API Data $result")

    // 获取到了JSON格式的数据
    val apiData = JSONObject(result)
    val forecastWeatherList = apiData.getJSONObject("data").getJSONArray("forecast_list")

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Weather Image",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.offset(x = 10.dp, y = 10.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    item {
                        WeatherImage(
                            imageUrl = transformedUrls.getOrNull(0) ?: ""
                        )
                    }
                    item {
                        WeatherImage(
                            imageUrl = transformedUrls.getOrNull(1) ?: ""
                        )
                    }
                    item {
                        WeatherImage(
                            imageUrl = transformedUrls.getOrNull(2) ?: ""
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp)
                        .background(Color.White) // 假设这里使用了MaterialTheme的背景色
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    ) {
                        // Day1
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(1).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(1).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(1).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(1).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(1).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(1).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(1).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }

                        // Day2
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(2).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(2).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(2).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(2).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(2).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(2).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(2).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }

                        // Day3
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(3).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(3).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(3).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(3).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(3).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(3).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(3).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }

                        // Day4
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(4).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(4).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(4).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(4).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(4).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(4).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(4).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }

                        // Day5
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(5).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(5).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(5).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(5).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(5).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(5).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(5).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                        // Day6
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(6).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(6).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(6).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(6).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(6).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(6).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(6).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                        // Day7
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                                    .padding(16.dp)
                            ) {
                                // 日期
                                Text(
                                    text = forecastWeatherList.getJSONObject(7).getString("date"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // 图标和天气情况
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    WeatherIcon(forecastWeatherList.getJSONObject(7).getString("weather"))

                                    Text(
                                        text = forecastWeatherList.getJSONObject(7).getString("weather"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // 日最高气温和日最低气温
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "High: " + forecastWeatherList.getJSONObject(7).getString("high_temperature"),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )

                                    Text(
                                        text = "Low: " + forecastWeatherList.getJSONObject(7).getString("low_temperature"),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                // 风向+风速
                                Text(
                                    text = "Wind: " + forecastWeatherList.getJSONObject(7).getString("wind_direction") + " " + forecastWeatherList.getJSONObject(7).getString("wind_level"),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("返回")
                }

                Button(
                    onClick = {
                        val intent = Intent(context, Page2Activity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("下一页")
                }
            }
        }
    }
}

@Composable
fun WeatherIcon(weatherString: String) {
    val iconRes = when {
        "晴" in weatherString -> R.drawable.sunny
        "雨" in weatherString -> R.drawable.rain
        "云" in weatherString -> R.drawable.cloudy
        "雷" in weatherString -> R.drawable.thunder
        else -> R.drawable.default_icon // 你可以设置一个默认图标
    }

    Image(painter = painterResource(id = iconRes), contentDescription = null)
}

@Composable
fun WeatherImage(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(
            model = imageUrl,
            placeholder = painterResource(R.drawable.ic_launcher_foreground)
        ),
        contentDescription = "Weather Image",
        modifier = Modifier
            .padding(8.dp)
            .size(350.dp)  // 调整图片大小以占据大部分屏幕空间
            .aspectRatio(1f),  // 保持宽高比为1:1
        contentScale = ContentScale.Crop
    )
}

@Preview(showBackground = true)
@Composable
fun Page1ScreenPreview() {
    WeatherTheme {
        Page1Screen()
    }
}
