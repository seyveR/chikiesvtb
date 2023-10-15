package com.example.myapplication

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

class SelectSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choise_activity)

        val officeButton: Button = findViewById(R.id.office)
        Log.d("spoint", intent.getDoubleExtra("latitude", 0.0).toString())
        Log.d("spoint", intent.getDoubleExtra("longitude",0.0).toString())

        officeButton.setOnClickListener {
            val new_intent = Intent(this, FastSearchActivity::class.java)
            new_intent.putExtra("latitude", intent.getDoubleExtra("latitude", 0.0))
            new_intent.putExtra("longitude", intent.getDoubleExtra("longitude", 0.0))
            startActivityForResult(new_intent, 1)
        }

        val atmButton: Button = findViewById(R.id.atm)
        atmButton.setOnClickListener {
            val new_intent = Intent(this, FastSearchAtm::class.java)
            new_intent.putExtra("latitude", intent.getDoubleExtra("latitude", 0.0))
            new_intent.putExtra("longitude", intent.getDoubleExtra("longitude", 0.0))
            startActivityForResult(new_intent, 1)
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}