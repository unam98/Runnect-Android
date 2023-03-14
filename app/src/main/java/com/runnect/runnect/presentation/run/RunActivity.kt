package com.runnect.runnect.presentation.run

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.runnect.runnect.R
import com.runnect.runnect.data.model.*
import com.runnect.runnect.databinding.ActivityRunBinding
import com.runnect.runnect.presentation.endrun.EndRunActivity
import kotlinx.android.synthetic.main.custom_dialog_finish_run.view.*
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.concurrent.timer

class RunActivity :
    com.runnect.runnect.binding.BindingActivity<ActivityRunBinding>(R.layout.activity_run),
    OnMapReadyCallback {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private lateinit var fusedLocation: FusedLocationProviderClient//현재 위치 반환 객체 변수
    private var currentLocation: LatLng = LatLng(37.52901832956373, 126.9136196847032) //국회의사당 좌표

    val path = PathOverlay()
    lateinit var departureLatLng: LatLng
    var coords = mutableListOf<LatLng>()
    lateinit var countToRunData: CountToRunData


    //타이머
    var time = 0

    var timerTask: Timer? = null

    var timerSecond: Int = 0
    var timerMinute: Int = 0
    var timerHour: Int = 0


    val viewModel: RunViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.model = viewModel
        binding.lifecycleOwner = this

        init()
        startTimer()
        getCurrentLocation()
        seeRecord()
        backButton()


    }

    override fun onBackPressed() {
        stopTimer()
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun backButton() {
        binding.imgBtnBack.setOnClickListener {
            stopTimer()
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun initView() {

        //MapFragment 추가
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.mapView) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.mapView, it).commit()
            }
        mapFragment.getMapAsync(this) //지도 객체 얻어오기
        locationSource = FusedLocationSource(
            this,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun init() {
        fusedLocation = LocationServices.getFusedLocationProviderClient(this) //
        initView() //지도 뷰 표시
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0


        map.locationSource = locationSource
        map.locationTrackingMode = LocationTrackingMode.Follow //위치추적 모드 Follow

        //네이버 맵 sdk에 위치 정보 제공
        locationSource = FusedLocationSource(this@RunActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        naverMap.addOnLocationChangeListener { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
            map.locationOverlay.run { //현재 위치 마커
                isVisible = true //현재 위치 마커 가시성(default = false)
                position = LatLng(currentLocation.latitude, currentLocation.longitude)
            }
        }
        setCourse()

        val uiSettings = naverMap.uiSettings
        uiSettings.isZoomControlEnabled = false

        //현위치 커스텀 이미지
        val locationOverlay = naverMap.locationOverlay
        locationOverlay.icon = OverlayImage.fromResource(R.drawable.ic_location_overlay)

    }

    //카메라 위치 변경 함수
    private fun cameraUpdate(location: LatLng) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))
            .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)

    }

    private fun getCurrentLocation() {
        binding.btnCurrentLocation.setOnClickListener {
            cameraUpdate(currentLocation)
        }
    }


    private fun setCourse() {
        setViewModelValue()
        createDepartureMarker()
        createRouteMarker()
    }

    private fun setViewModelValue() {
        countToRunData = intent.getParcelableExtra("CountToRunData")!!
        viewModel.courseId.value = countToRunData.courseId
        viewModel.publicCourseId.value = countToRunData.publicCourseId
        viewModel.departure.value = countToRunData.departure
        viewModel.startLatLng.value = countToRunData.startLatLng
        viewModel.touchList.value = countToRunData.touchList
        viewModel.captureUri.value = countToRunData.image
        viewModel.dataFrom.value = countToRunData.dataFrom

        val distanceCut =
            BigDecimal(countToRunData.distance.toDouble()).setScale(1, RoundingMode.FLOOR)
                .toDouble()
        viewModel.distanceSum.value = distanceCut
    }

    private fun createDepartureMarker() {
        val departureMarker = Marker()
        departureLatLng = countToRunData.startLatLng

        departureMarker.position =
            LatLng(departureLatLng.latitude, departureLatLng.longitude) // 출발지점
        departureMarker.anchor = PointF(0.5f, 0.7f)
        departureMarker.icon = OverlayImage.fromResource(R.drawable.marker_departure)
        departureMarker.map = naverMap
        cameraUpdate(
            LatLng(departureLatLng.latitude, departureLatLng.longitude)
        )
        coords.add(LatLng(departureLatLng.latitude, departureLatLng.longitude))
    }

    private fun createRouteMarker() {
        for (i in 1..countToRunData.touchList.size) {
            val routeMarker = Marker()
            routeMarker.position = LatLng(countToRunData.touchList[i - 1].latitude,
                countToRunData.touchList[i - 1].longitude)
            routeMarker.anchor = PointF(0.5f, 0.5f)
            routeMarker.icon = OverlayImage.fromResource(R.drawable.marker_route)
            routeMarker.map = naverMap

            // coords에 터치로 받아온 좌표값을 추가
            coords.add(LatLng(countToRunData.touchList[i - 1].latitude,
                countToRunData.touchList[i - 1].longitude))
            // 경로선 그리기
            path.coords = coords
            // 경로선 색상
            path.color = Color.parseColor("#593EEC")
            // 경로선 테두리 색상
            path.outlineColor = Color.parseColor("#593EEC")
            path.map = naverMap
        }
    }

    private fun seeRecord() {
        binding.btnRunFinish.setOnClickListener {
            bottomSheet()
            stopTimer()
        }
    }

    @SuppressLint("MissingInflatedId")
    fun bottomSheet() {
        // bottomSheetDialog 객체 생성
        val bottomSheetDialog = BottomSheetDialog(
            this@RunActivity, R.style.BottomSheetDialogTheme
        )

        // layout_bottom_sheet를 뷰 객체로 생성
        val bottomSheetView = LayoutInflater.from(applicationContext).inflate(
            R.layout.custom_dialog_finish_run,
            findViewById(R.id.bottomSheet) as LinearLayout?
        )
        // bottomSheetDialog의 dismiss 버튼 선택시 dialog disappear
        bottomSheetView.findViewById<View>(R.id.btn_see_record).setOnClickListener {
            val intent = Intent(this@RunActivity, EndRunActivity::class.java).apply {

                putExtra("RunToEndRunData",
                    RunToEndRunData(
                        courseId = viewModel.courseId.value!!,
                        publicCourseId = viewModel.publicCourseId.value,
                        viewModel.distanceSum.value,
                        viewModel.captureUri.value,
                        viewModel.departure.value,
                        timerHour,
                        timerMinute,
                        timerSecond,
                        viewModel.dataFrom.value!!))

                addFlags(FLAG_ACTIVITY_NO_ANIMATION) //페이지 전환 시 애니메이션 제거

            }
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }
        // bottomSheetDialog 뷰 생성
        bottomSheetDialog.setContentView(bottomSheetView)
        // bottomSheetDialog 호출
        bottomSheetDialog.show()
    }

    private fun startTimer() {
        timerTask = timer(period = 1000) {

            time++ //1초에 한 번씩 timer 값이 1씩 증가, 초기값은 0

            val hour = time / 3600
            val minute = time / 60 //60이 되는 순간 몫이 1이 돼서 1로 표기
            val second = time % 60 //60이 되는순간 나머지가 0이라 0으로 표기

            runOnUiThread { //더 좋은 방법이 있나.
                if (hour < 10) {
                    binding.tvTimeHour.text = "0$hour"
                } else {
                    binding.tvTimeHour.text = "$hour"
                }

                if (minute < 10) {
                    binding.tvTimeMinute.text = "0$minute"
                } else {
                    binding.tvTimeMinute.text = "$minute"
                }

                if (second < 10) {
                    binding.tvTimeSecond.text = "0$second"
                } else {
                    binding.tvTimeSecond.text = "$second"
                }
            }

            timerHour = hour
            timerMinute = minute
            timerSecond = second

            Timber.tag(ContentValues.TAG).d("timerHour 값 : $timerHour")
            Timber.tag(ContentValues.TAG).d("timerMinute 값 : $timerMinute")
            Timber.tag(ContentValues.TAG).d("timerSecond 값 : $timerSecond")


        }
    }

    private fun stopTimer() {
        timerTask?.cancel()
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
