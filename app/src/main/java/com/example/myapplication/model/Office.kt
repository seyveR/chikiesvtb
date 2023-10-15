package com.example.myapplication.model

data class Office(
    val salePointName: String,
    val address: String,
    val openHours: List<OpenHours>,
    val openHoursIndividual: List<OpenHours>,
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
    val investment_managment: Int?
)

data class OpenHours(
    val days: String?,
    val hours: String?,
    val busy: List<Int?> // Assuming the type is Int, replace it with the actual type
)