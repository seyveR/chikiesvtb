package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
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



class FastSearchActivity: AppCompatActivity() {
    private lateinit var bottomSheetDialog : BottomSheetDialog
    private lateinit var bottomSheetView : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fast_search_activity)

        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetView = layoutInflater.inflate(R.layout.filters, null)

        bottomSheetDialog.setContentView(bottomSheetView)

        val backButton:Button = findViewById(R.id.back)
        backButton.setOnClickListener {
            onBackPressed()
        }
        var startPoint:Point = Point(intent.getDoubleExtra("latitude", 0.0),
            intent.getDoubleExtra("longitude", 0.0))

        val addressEditText: EditText = findViewById(R.id.address)
        Log.d("spoint", startPoint.longitude.toString())
        Log.d("spoint", startPoint.latitude.toString())

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
        openFilters.setOnClickListener{
            bottomSheetDialog.show()
        }

        val buttonInBottomSheet: TextView = bottomSheetView.findViewById(R.id.backButton)
        buttonInBottomSheet.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val createRouteButton:Button = findViewById(R.id.createRoute)
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

    fun createRoute(point:Point){
        var url:String = "https://802f-95-54-230-204.ngrok-free.app/distance_model?lat=${point.latitude}&lon=${point.longitude}&office=true"
        val car:RadioButton = findViewById(R.id.radioButtonCar)
        url += "&venicle=${car.isChecked}"

        var filtersUrl: String ="&filters="

        val invalid:CheckBox = findViewById(R.id.work_with_bank_account)
        if (invalid.isChecked){
            filtersUrl += "invalid==1,"
        }

        val blind:CheckBox = findViewById(R.id.blind)
        if (blind.isChecked){
            filtersUrl += "blind==1,"
        }

        val servLe:CheckBox = bottomSheetView.findViewById(R.id.servLe)
        if (servLe.isChecked){
            filtersUrl += "servLe==1,"
        }

        val work_with_bank_account:CheckBox = bottomSheetView.findViewById(R.id.work_with_bank_account)
        if (work_with_bank_account.isChecked){
            filtersUrl += "work_with_bank_account==1,"
        }

        val working_with_debit_credit_cards:CheckBox = bottomSheetView.findViewById(R.id.working_with_debit_credit_cards)
        if (working_with_debit_credit_cards.isChecked){
            filtersUrl += "working_with_debit_credit_cards==1,"
        }

        val credits:CheckBox = bottomSheetView.findViewById(R.id.credits)
        if (credits.isChecked){
            filtersUrl += "credits==1,"
        }

        val currency_cash:CheckBox = bottomSheetView.findViewById(R.id.currency_cash)
        if (currency_cash.isChecked){
            filtersUrl += "currency_cash==1,"
        }

        val saving_account_deposits:CheckBox = bottomSheetView.findViewById(R.id.saving_account_deposits)
        if (saving_account_deposits.isChecked){
            filtersUrl += "saving_account_deposits==1,"
        }

        val money:CheckBox = bottomSheetView.findViewById(R.id.money)
        if (money.isChecked){
            filtersUrl += "money==1,"
        }

        val bigmoney:CheckBox = bottomSheetView.findViewById(R.id.bigmoney)
        if (bigmoney.isChecked){
            filtersUrl += "bigmoney==1,"
        }

        val insurance:CheckBox = bottomSheetView.findViewById(R.id.insurance)
        if (insurance.isChecked){
            filtersUrl += "insurance==1,"
        }

        val investment_managment:CheckBox = bottomSheetView.findViewById(R.id.investment_managment)
        if (investment_managment.isChecked){
            filtersUrl += "investment_managment==1,"
        }

        val transfers_and_payments:CheckBox = bottomSheetView.findViewById(R.id.transfers_and_payments)
        if (transfers_and_payments.isChecked){
            filtersUrl += "transfers_and_payments==1,"
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