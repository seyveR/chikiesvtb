package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import java.io.IOException
import java.util.*

class FastSearchAtm: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fast_search_activity)


        val searchTV:TextView = findViewById(R.id.searchTextView)
        searchTV.setText("Поиск отделения")

        val backButton: Button = findViewById(R.id.back)
        backButton.setOnClickListener {
            onBackPressed()
        }
        var startPoint: Point = Point(intent.getDoubleExtra("latitude", 0.0),
            intent.getDoubleExtra("longitude", 0.0))

        val addressEditText: EditText = findViewById(R.id.address)
        addressEditText.setText(getAddressFromLatLng(startPoint.latitude, startPoint.longitude))
        addressEditText.setOnClickListener {
            val mapView: MapView = findViewById(R.id.mapview2)
            mapView.map.move(CameraPosition(startPoint, 13f, 0f, 30f))
            mapView.visibility = View.VISIBLE

            val inputListener = object : InputListener {
                override fun onMapTap(map: Map, point: Point) {
                    startPoint = point
                    addressEditText.setText(getAddressFromLatLng(point.latitude, point.longitude))
                    mapView.visibility = View.INVISIBLE
                }

                override fun onMapLongTap(map: Map, point: Point) {
                    startPoint = point
                    addressEditText.setText(getAddressFromLatLng(point.latitude, point.longitude))
                    mapView.visibility = View.INVISIBLE
                }
            }
            mapView.map.addInputListener(inputListener)
        }

        val openFilters: TextView = findViewById(R.id.openFilters)
        openFilters.visibility = View.INVISIBLE

        val createRouteButton: Button = findViewById(R.id.createRoute)
        createRouteButton.setOnClickListener {
            createRoute(startPoint)
        }

    }
    fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val locale = Locale("ru", "RU")
        val geocoder = Geocoder(this, locale)
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)!!
            val address = addresses[0].getAddressLine(0)
            return address
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    fun createRoute(point: Point){
        var url:String = "https://802f-95-54-230-204.ngrok-free.app/distance_model?lat=${point.latitude}&lon=${point.longitude}&office=false"
        val car: RadioButton = findViewById(R.id.radioButtonCar)
        url += "&venicle=${car.isChecked}"

        var filtersUrl: String ="&filters="

        val invalid: CheckBox = findViewById(R.id.work_with_bank_account)
        if (invalid.isChecked){
            filtersUrl += "invalid==1,"
        }

        val blind: CheckBox = findViewById(R.id.blind)
        if (blind.isChecked){
            filtersUrl += "blind==1,"
        }

        if (filtersUrl != "&filters="){
            url += filtersUrl.dropLast(1)
        }

        Log.e("url", url)
        val intent = Intent()
        intent.putExtra("url", url)
        intent.putExtra("lat", point.latitude)
        intent.putExtra("lon", point.longitude)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }
}