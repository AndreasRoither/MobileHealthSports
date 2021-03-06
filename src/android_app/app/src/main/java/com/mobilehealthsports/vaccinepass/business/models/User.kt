package com.mobilehealthsports.vaccinepass.business.models

import android.content.Context
import androidx.core.content.ContextCompat
import java.io.Serializable
import java.time.LocalDate

data class User(
        val uid: Long,
        val firstName: String?,
        val lastName: String?,
        val bloodType: String?,
        val birthDay: LocalDate?,
        val weight: Float?,
        val height: Float?,
        val themeColor: Int,
        val photoPath: String?
) : Serializable {
    fun resolveColor(context: Context): Int {
        return ContextCompat.getColor(context, themeColor)
    }
}
