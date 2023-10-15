package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.model.Office
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OfficeInfoBottomSheetDialogFragment(private val office: Office) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_first, container, false).apply {
            findViewById<TextView>(R.id.officeName).text = office.salePointName
            findViewById<TextView>(R.id.officeAddress).text = office.address
            // Add more fields here...
        }
    }
}