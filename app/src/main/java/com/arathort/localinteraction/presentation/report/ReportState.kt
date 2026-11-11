package com.arathort.localinteraction.presentation.report

import androidx.annotation.StringRes
import com.arathort.localinteraction.domain.model.ComplaintTargetType

data class ReportState(
    val targetType: ComplaintTargetType = ComplaintTargetType.USER,
    val targetId: String = "",
    val targetTitle: String = "",
    val selectedCategory: String = "",
    val description: String = "",
    @param:StringRes val categoryErrorResId: Int? = null,
    @param:StringRes val descriptionErrorResId: Int? = null,
    val isSubmitting: Boolean = false
)
