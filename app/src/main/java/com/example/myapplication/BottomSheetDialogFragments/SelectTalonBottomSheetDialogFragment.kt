package com.example.myapplication.BottomSheetDialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.myapplication.R
import com.example.myapplication.model.Office
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectTalonBottomSheetDialogFragment(private val office: Office) : BottomSheetDialogFragment() {
    interface OnButtonClickListener {
        fun onGetTalonClick()
    }

    private var listener: OnButtonClickListener? = null

    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        this.listener = listener
    }
    lateinit var radioGroup: RadioGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_second, container, false).apply {
            findViewById<TextView>(R.id.officeName3).text = office.SalePointName
            findViewById<TextView>(R.id.officeAddress3).text = office.Address
            radioGroup = findViewById<RadioGroup>(R.id.radioGroupTalon)
            val firstButton = findViewById<AppCompatButton>(R.id.get_talon2)
            firstButton.setOnClickListener {
                listener?.onGetTalonClick()
            }
        }

    }
    fun getSelectedRadioButtonIndex(): Int {
        val selectedId = radioGroup.checkedRadioButtonId

        val selectedRadioButton = view?.findViewById<RadioButton>(selectedId)
        return radioGroup.indexOfChild(selectedRadioButton)
    }
}