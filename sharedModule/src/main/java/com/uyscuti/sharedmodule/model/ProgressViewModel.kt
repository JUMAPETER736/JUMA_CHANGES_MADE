package com.uyscuti.sharedmodule.model

import androidx.lifecycle.ViewModel

class ProgressViewModel : ViewModel() {
    var totalProgress: Int = 0
    var overallProgress: Double = 0.0
    // Add other progress-related variables if needed
}
