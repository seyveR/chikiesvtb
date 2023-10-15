package com.example.myapplication.model

data class Office(
    val SalePointName: String,
    val Address: String,
    val openHours: List<OpenHours>,
    val openHoursIndividual: List<OpenHoursIndividual>,
    val Lat: Double,
    val Lon: Double,
    val servLe: Int?,
    val work_with_bank_account: Int?,
    val working_with_debit_credit_cards: Int?,
    val credits: Int?,
    val currency_cash: Int?,
    val saving_account_deposits: Int?,
    val money: Int?,
    val bigmoney: Int?,
    val transfers_and_payments: Int?,
    val transfers_and_payments_1: Int?,
    val insurance: Int?,
    val investment_managment: Int?,
    val openHour: List<List<String>>,
    )

data class OpenHours(
    val busy: List<String> ,
    val days: String?,
    val hours: String?,
)

data class OpenHoursIndividual(
    val days: String?,
    val hours: String?,
)