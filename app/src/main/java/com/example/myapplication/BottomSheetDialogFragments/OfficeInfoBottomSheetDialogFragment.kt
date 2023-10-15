package com.example.myapplication.BottomSheetDialogFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.myapplication.R
import com.example.myapplication.model.Office
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class OfficeInfoBottomSheetDialogFragment(private val office: Office) : BottomSheetDialogFragment() {
    interface OnButtonClickListener {
        fun onCreateRouteClick()
        fun onStartCreateTalonClick()
    }

    private var listener: OnButtonClickListener? = null

    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_first, container, false).apply {
            findViewById<TextView>(R.id.officeName2).text = office.SalePointName
            findViewById<TextView>(R.id.officeAddress2).text = office.Address

            val chart = findViewById<BarChart>(R.id.chart)

            val entries = ArrayList<BarEntry>()
            var x = 6f

            office.openHour[0].forEach{
                entries.add(BarEntry(x, it?.toFloat()!!))
                x+=1
            }

                class WholeNumberValueFormatter : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            val dataSet = BarDataSet(entries, "Загруженность").apply {
                valueFormatter = WholeNumberValueFormatter()
            }
            val data = BarData(dataSet)
            chart.data = data


            val firstButton = findViewById<AppCompatButton>(R.id.create_route1)
            firstButton.setOnClickListener {
                listener?.onCreateRouteClick()
            }

            val secondButton = findViewById<AppCompatButton>(R.id.get_talon)
            secondButton.setOnClickListener {
                listener?.onStartCreateTalonClick()
            }
        }

    }
}