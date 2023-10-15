package network

import android.util.Log
import com.example.myapplication.model.Destination
import com.example.myapplication.model.Office
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OpenMeteoWeatherInteractor {
    val networkClient = NetworkClient()

    suspend fun requestWeather(latitute: Double, longitute:Double): String {
        Log.e("Interactor", "requestStarted")
        val url = "https://802f-95-54-230-204.ngrok-free.app/distance?lat1=$latitute&lon1=$longitute"

        val apiResponse = URL(url).readText()
        return apiResponse
//        return parseJSON(apiResponse)

    }

    suspend fun requestOffices(): List<Office> = withContext(Dispatchers.IO) {
        val maxRetries = 10
        for (i in 1..maxRetries) {
            try {
                Log.e("Interactor", "requestStarted")
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://802f-95-54-230-204.ngrok-free.app/data")
                    .build()
                val response = client.newCall(request).execute()
                val apiResponse = response.body?.string()
                if (apiResponse != null && (apiResponse.startsWith("{") || apiResponse.startsWith("["))) {
                    return@withContext parseJSONOffices(apiResponse)
                } else {
                    Log.e("Interactor", "Invalid response: $apiResponse")
                    if (i == maxRetries) throw Exception("Invalid response")
                }
            } catch (e: Exception) {
                Log.e("Interactor", "error", e)
                if (i == maxRetries) throw e
            }
        }
        return@withContext emptyList<Office>()
    }


    suspend fun requestDestination(url: String): Destination = withContext(Dispatchers.IO) {
        try {
            Log.e("Interactor", "requestStarted")
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            val apiResponse = response.body?.string()
            if (apiResponse != null && (apiResponse.startsWith("{") || apiResponse.startsWith("["))) {
                return@withContext parseJSONDestination(apiResponse)
            } else {
                Log.e("Interactor", "Invalid response: $apiResponse")
            }
        } catch (e: Exception) {
            Log.e("Interactor", "error", e)
        }
        return@withContext Destination(0.0,0.0,0.0,0.0,0.0)
    }

    private fun parseJSONOffices(jsonString: String): List<Office> {
        try {
            val gson = Gson()
            var JSON_Weather = JSONArray(jsonString)
            Log.e("Interactor", JSON_Weather.toString())
            val listType = object : TypeToken<List<Office>>() {}.type

            return gson.fromJson(JSON_Weather.toString(), listType)
        } catch (e: Exception) {
            Log.e("Interactor", "error", e)
            throw e
        }
    }
    private fun parseJSONDestination(jsonString: String): Destination {
        try {
            val gson = Gson()
            var JSON_Weather = JSONArray(jsonString)
            Log.e("Interactor", JSON_Weather.toString())

            val listType = object : TypeToken<List<Destination>>() {}.type
            val res: List<Destination> = gson.fromJson(JSON_Weather.toString(), listType)
            return res[0]
        } catch (e: Exception) {
            Log.e("Interactor", "error", e)
            throw e
        }
    }

//    private fun parseJSON(jsonString: String): OpenMeteoWeather {
//        try {
//            val gson = Gson()
//            var JSON_Weather = JSONObject(jsonString).getJSONObject("current_weather")
//            Log.e("Interactor", JSON_Weather.toString())
//            return gson.fromJson(JSON_Weather.toString(), OpenMeteoWeather::class.java)
//        } catch (e: Exception) {
//            Log.e("Interactor", "error", e)
//            return OpenMeteoWeather(0f,0f,0f,0,0,"")
//        }
//    }
}