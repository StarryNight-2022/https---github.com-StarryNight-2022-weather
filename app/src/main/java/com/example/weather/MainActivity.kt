package com.example.weather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.ui.theme.WeatherTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        cityNames = listOf("未选择", "北京"),
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(cityNames: List<String>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var chosenCity by remember {
        mutableStateOf(cityNames[0])
    }
    val urlHead = "https://qqlykm.cn/api/weather/get?key=0n6bTg9oRHXkGwT1glMY8iBWuZ&city="
    var url by remember {
        mutableStateOf(urlHead + chosenCity)
    }
    var inputText by remember {
        mutableStateOf("")
    }
    var todayWeather by remember {
        mutableStateOf(JSONObject(
            """
            {
                "date": "未知",
                "weather": "未知",
                "high_temperature": "未知",
                "low_temperature": "未知",
                "wind_direction": "未知",
                "wind_level": "未知"
            }
            """.trimIndent()
        ))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(0.dp, 50.dp, 0.dp, 0.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.9F)
        ){
            Text(
                text = "天气预报",
                color = Color.Red,
                fontSize = 40.sp,
                modifier = modifier
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "城市选择，当前城市: $chosenCity",
                color = Color.Black,
                fontSize = 30.sp,
                modifier = modifier
            )
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth(1.0F)
            ){

                val fraction: Float = 0.8F
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                ){
                    Text(
                        text = "日期：",
                        color = Color.Black,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                    Text(
                        text = todayWeather.getString("date"),
                        color = Color.Blue,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                }
                // draw a line
                HorizontalDivider(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .padding(0.dp, 0.dp, 0.dp, 15.dp)
                )
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                )
                {
                    Text(
                        text = "天气：",
                        color = Color.Black,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                    Text(
                        text = todayWeather.getString("weather"),
                        color = Color.Blue,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                }
                HorizontalDivider(
                    color = Color.Black.copy(alpha = 0.5F),
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .padding(0.dp, 0.dp, 0.dp, 15.dp)
                )
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                )
                {
                    Text(
                        text = "温度：",
                        color = Color.Black,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                    Text(
                        text = todayWeather.getString("low_temperature") + " ~ " + todayWeather.getString("high_temperature"),
                        color = Color.Blue,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                }
                HorizontalDivider(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .padding(0.dp, 0.dp, 0.dp, 15.dp)
                )
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                )
                {
                    Text(
                        text = "风向：",
                        color = Color.Black,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                    Text(
                        text = todayWeather.getString("wind_direction"),
                        color = Color.Blue,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                }
                HorizontalDivider(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .padding(0.dp, 0.dp, 0.dp, 15.dp)
                )
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                )
                {
                    Text(
                        text = "风力：",
                        color = Color.Black,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                    Text(
                        text = todayWeather.getString("wind_level"),
                        color = Color.Blue,
                        fontSize = 25.sp,
                        modifier = modifier
                    )
                }

            }
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(text = "输入一个城市", fontSize = 25.sp) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 25.sp),
                modifier = modifier
                    .fillMaxWidth(1.0F)
                    .height(75.dp)
            )
            Button(
                onClick = {
                    chosenCity = inputText
                    Log.e("City", inputText)
                    GlobalScope.launch {
                        val result = fetchData(urlHead + chosenCity)
                        Log.e("API Data", result)
                        val apiData = JSONObject(result)
                        todayWeather = apiData.getJSONObject("data").getJSONArray("forecast_list").getJSONObject(1)
                        Log.e("Today Weather", todayWeather.toString())
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.5F)
                    .padding(5.dp)
            ) {
                Text(text = "查询", fontSize = 25.sp)
                Color.Blue
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(0.9F)
            )
            {
                Button(onClick = {
                    val intent = Intent(context, Page1Activity::class.java).apply {
                        putExtra("chosenCity", chosenCity)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to Page 1")
                    Color.Red
                }
                Button(onClick = {
                    val intent = Intent(context, Page2Activity::class.java).apply {
                        putExtra("chosenCity", chosenCity)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to Page 2")
                    Color.Red
                }
            }
        }
    }
}

// get data from API
suspend fun fetchData(apiUrl: String) : String{
    val client = HttpClient()
    return try {
        val response: HttpResponse = client.get(apiUrl)
        if (response.status.value == 200){
            String(response.body(), Charsets.UTF_8)
        }
        else{
            "Error code ${response.status.value}"
        }
    }
    catch (e : Exception){
        "Exception: ${e.message}"
    }
    finally {
        client.close()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherTheme {
        Greeting(listOf("未选择", "北京"))
    }
}
