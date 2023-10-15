package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.myapplication.BottomSheetDialogFragments.OfficeInfoBottomSheetDialogFragment
import com.example.myapplication.BottomSheetDialogFragments.SelectTalonBottomSheetDialogFragment
import com.example.myapplication.BottomSheetDialogFragments.TalonBottomSheetDialogFragment
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.Destination
import com.example.myapplication.model.Office
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.*
import network.OpenMeteoWeatherInteractor
import com.google.android.gms.location.LocationCallback as LocationCallback1

class MainActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener,
    OfficeInfoBottomSheetDialogFragment.OnButtonClickListener,
    SelectTalonBottomSheetDialogFragment.OnButtonClickListener,
    TalonBottomSheetDialogFragment.OnButtonClickListener{
    private lateinit var mapView: MapView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var drivingRouter:DrivingRouter? = null
    private var drivingSession:DrivingSession? = null
    private var mapObjects: MapObjectCollection? = null
    private var currentPlacementPlacemark: PlacemarkMapObject? = null
    private var currentRoute: PolylineMapObject? = null
    private lateinit var selectedOffice: Office
    lateinit var bottomSheetDialogFragment: OfficeInfoBottomSheetDialogFragment
    lateinit var bottomSheetDialogFragmentSelectTalon: SelectTalonBottomSheetDialogFragment
    var bottomSheetDialogFragmentTalon: TalonBottomSheetDialogFragment? = null

    private val placemarkTapListener = object : MapObjectTapListener {
        override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
            selectedOffice = mapObject.userData as Office
            showBottomSheet(selectedOffice)
            return true
        }
    }
    fun showBottomSheet(office: Office) {
        bottomSheetDialogFragment = OfficeInfoBottomSheetDialogFragment(office)
        bottomSheetDialogFragment.setOnButtonClickListener(this)
        bottomSheetDialogFragment.show(supportFragmentManager, "BottomSheetDialogFragment")
    }

    override fun onCreateRouteClick() {
        createRoute(currentPlacementPlacemark!!.geometry, Point(selectedOffice.Lat,selectedOffice.Lon))
        bottomSheetDialogFragment.dismiss()
        if (bottomSheetDialogFragmentTalon != null){
            bottomSheetDialogFragmentTalon!!.dismiss()
        }
    }

    override fun onStartCreateTalonClick() {
        bottomSheetDialogFragment.dismiss()
        bottomSheetDialogFragmentSelectTalon = SelectTalonBottomSheetDialogFragment(selectedOffice)
        bottomSheetDialogFragmentSelectTalon.setOnButtonClickListener(this)
        bottomSheetDialogFragmentSelectTalon.show(supportFragmentManager, "SecondBottomSheetDialogFragment")
    }

    override fun onGetTalonClick() {
        bottomSheetDialogFragmentSelectTalon.dismiss()
        bottomSheetDialogFragmentTalon = TalonBottomSheetDialogFragment(selectedOffice, bottomSheetDialogFragmentSelectTalon.getSelectedRadioButtonIndex())
        bottomSheetDialogFragmentTalon!!.setOnButtonClickListener(this)
        bottomSheetDialogFragmentTalon!!.show(supportFragmentManager, "ThirdBottomSheetDialogFragment")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("a914760d-b0cf-4955-8ddb-27cabeb17854")
        MapKitFactory.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)



        setContentView(binding.root)
        mapView = findViewById(R.id.mapview)
        mapObjects = mapView.map.mapObjects.addCollection()
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()

        val mapKit: MapKit = MapKitFactory.getInstance()
        val trafficLayer = mapKit.createTrafficLayer(mapView.mapWindow)
        trafficLayer.isTrafficVisible = true

        noticeCurrentPlacement()
        placeOfficesOnMap()


        binding.fab.setOnClickListener { view ->
            if (currentPlacementPlacemark != null){
                val intent = Intent(this, SelectSearchActivity::class.java)
                intent.putExtra("latitude", currentPlacementPlacemark!!.geometry.latitude)
                intent.putExtra("longitude", currentPlacementPlacemark!!.geometry.longitude)
                startActivityForResult(intent, 1)
            }
        }
    }

    private fun placeOfficesOnMap(){
        try {
            var x:List<Office>
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    GlobalScope.async {
                        x = OpenMeteoWeatherInteractor().requestOffices()
                        val imageProvider = ImageProvider.fromResource(applicationContext, R.drawable.logosmall)
                        x.forEach(){ office ->
                            runOnUiThread{
                                val point = Point(office.Lat, office.Lon)
                                val placemark = mapView.map.mapObjects.addPlacemark(point).apply {
                                    setIcon(imageProvider)
                                    addTapListener(placemarkTapListener)
                                    userData = office
                                }

                            }
                        }
                    }.await()
                } catch (e: Exception) {
                    // Обработка исключения
                    Log.e("Interactor", "error", e)
                    Toast.makeText(applicationContext,"Проблемы с сервером, попробуйте перезайти позже", Toast.LENGTH_LONG).show()
                }
            }
        }
        catch (e: Exception) {
            Toast.makeText(this,"Проблемы с сервером, попробуйте перезайти позже", Toast.LENGTH_LONG).show()
        }
    }

    fun noticeCurrentPlacement(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationCallback = object : LocationCallback1() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    val point = Point(location.latitude, location.longitude)
                    if (currentPlacementPlacemark == null){
                        mapView.map.move(CameraPosition(point, 13f, 0f, 30f))
                    }
                    currentPlacementPlacemark?.let {
                        mapView.map.mapObjects.remove(it)
                    }
                    currentPlacementPlacemark = mapView.map.mapObjects.addPlacemark(point)
                    currentPlacementPlacemark?.setIcon(ImageProvider.fromResource(applicationContext, R.drawable.placemark_icon))
                }
            }
        }
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

    }

    private fun createRoute(start:Point, end:Point) {
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        val requestPoints:ArrayList<RequestPoint> = ArrayList()
        requestPoints.add(RequestPoint(start, RequestPointType.WAYPOINT,null))
        requestPoints.add(RequestPoint(end, RequestPointType.WAYPOINT,null))
        drivingSession = drivingRouter!!.requestRoutes(requestPoints,drivingOptions,vehicleOptions,this)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val url = data?.getStringExtra("url")
            Log.d("DESTINATION", url!!)
            var startPoint = Point(data?.getDoubleExtra("lat",0.0)!!,
                data?.getDoubleExtra("lon", 0.0)!!)
            mapView.map.move(CameraPosition(startPoint, 13f, 0f, 30f))
            GlobalScope.async {
                val destination: Destination =
                    OpenMeteoWeatherInteractor().requestDestination(url!!)           // Вызовите метод с полученным URL
                runOnUiThread {
                    createRoute(startPoint, Point(destination.Lat, destination.Lon))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
        currentRoute?.let {
            mapObjects?.remove(it)
        }
        val route = routes[0]
        currentRoute = mapObjects?.addPolyline(route.geometry)
    }

    override fun onDrivingRoutesError(p0: Error) {
        var errorMessage = "Неизвестная ошибка!"
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT)
    }


}