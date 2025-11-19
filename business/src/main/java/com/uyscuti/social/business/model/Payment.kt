package com.uyscuti.social.business.model

import android.health.connect.datatypes.ExerciseRoute.Location

data class Payment(
    var ageRange: String,
    var gender: Int,
    var location: String,
    var dailyBudget: Int,
    var duration:Int,
    var paymentType: Int,
    var mobileNumber: Int
)
