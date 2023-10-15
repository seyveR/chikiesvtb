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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.random.Random


class TalonBottomSheetDialogFragment(private val office: Office, private val serviceId: Int) : BottomSheetDialogFragment() {
    interface OnButtonClickListener {
        fun onCreateRouteClick()
    }

    private var listener: OnButtonClickListener? = null
    private val services = listOf<String>(
        "Открытие и ведение банковских счетов",
        "Выдача кредитных и дебетовых карт",
        "Выдача кредитов и займов",
        "Обмен валют и услуги кассы",
        "Сберегательные счета и депозиты",
        "Снятие наличных",
        "Снятие наличных от миллиона",
        "Страхование депозитов и др. виды",
        "Инвестиционные услуги и управление активами",
        "Переводы денежных средств и платежи"
    )
    private val documents = listOf<String>(
        "1. Паспорт\n" +
                "2. Договор банковского счета (по форме Банка) в 2 экз. \n" +
                "3. Заявление на открытие банковского счета (по форме Банка)\n" +
                "4. Карточка с образцами подписей и оттиска печати",
        "1. Паспорт\n" +
                "2. справка по форме 2-НДФЛ / справка по форме банка(документ подтверждающий доход), если вы не получаете з/п или пенсию на карту ВТБ",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\",",
        "1. Паспорт\\n\" +\n" +
                "                \"2. Договор банковского счета (по форме Банка) в 2 экз. \\n\" +\n" +
                "                \"3. Заявление на открытие банковского счета (по форме Банка)\\n\" +\n" +
                "                \"4. Карточка с образцами подписей и оттиска печати\","
    )


    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_three, container, false).apply {
            findViewById<TextView>(R.id.officeName4).text = office.SalePointName
            findViewById<TextView>(R.id.officeAddress4).text = office.Address
            Log.d("taplistener", serviceId.toString())
            findViewById<TextView>(R.id.selectedService).text = services[serviceId]
            findViewById<TextView>(R.id.documents).text = documents[serviceId]
            findViewById<TextView>(R.id.talon).text = findViewById<TextView>(R.id.talon).text.toString() + generateTicketNumber(serviceId)
            val firstButton = findViewById<AppCompatButton>(R.id.create_route2)
            firstButton.setOnClickListener {
                listener?.onCreateRouteClick()
            }
        }

    }
    fun generateTicketNumber(serviceId: Int): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val letter = letters[serviceId % letters.length]
        val randomNumber = Random.nextInt(1, 100)
        return "$letter-$randomNumber"
    }
}