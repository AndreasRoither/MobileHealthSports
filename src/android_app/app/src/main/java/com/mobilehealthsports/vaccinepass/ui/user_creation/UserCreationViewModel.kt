package com.mobilehealthsports.vaccinepass.ui.user_creation

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mobilehealthsports.vaccinepass.R
import com.mobilehealthsports.vaccinepass.business.models.User
import com.mobilehealthsports.vaccinepass.business.repository.UserRepository
import com.mobilehealthsports.vaccinepass.presentation.services.ServiceRequest
import com.mobilehealthsports.vaccinepass.presentation.services.messages.MessageRequest
import com.mobilehealthsports.vaccinepass.presentation.services.messages.ToastRequest
import com.mobilehealthsports.vaccinepass.presentation.services.navigation.MainRequest
import com.mobilehealthsports.vaccinepass.presentation.services.navigation.NavigationRequest
import com.mobilehealthsports.vaccinepass.presentation.viewmodels.BaseViewModel
import com.mobilehealthsports.vaccinepass.ui.start.StartActivity
import com.mobilehealthsports.vaccinepass.util.livedata.NonNullMutableLiveData
import com.mobilehealthsports.vaccinepass.util.PreferenceHelper
import com.mobilehealthsports.vaccinepass.util.PreferenceHelper.set
import com.mobilehealthsports.vaccinepass.util.ThemeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate


class UserCreationViewModel(
    private val sharedPreferences: SharedPreferences,
    private val userRepository: UserRepository
) : BaseViewModel() {
    val messageRequest = ServiceRequest<MessageRequest>()
    val navigationRequest = ServiceRequest<NavigationRequest>()
    var themeCallback: ((Int) -> Unit)? = null
    val permissionRequest = MutableLiveData<String>()

    val errorText = NonNullMutableLiveData("")
    val errorTextValidity = NonNullMutableLiveData("")

    private val regex = "[*/\\\'%;\"]+".toRegex()

    val firstName = NonNullMutableLiveData("")
    val lastName = NonNullMutableLiveData("")
    val bloodType = NonNullMutableLiveData("")
    val weight = NonNullMutableLiveData("")
    val height = NonNullMutableLiveData("")
    val photoTaken = NonNullMutableLiveData(false)

    val strokeWidthPurple = MutableLiveData(3)
    val strokeWidthGreen = MutableLiveData(0)
    val strokeWidthOrange = MutableLiveData(0)

    val currentPhotoPath: MutableLiveData<String?> = MutableLiveData(null)

    val finishBtnEnabled = Transformations.map(firstName) {
        it.isNotBlank()
    }

    private val _birthDate = NonNullMutableLiveData(LocalDate.now())
    val birthDate: LiveData<LocalDate> = _birthDate

    private val _color = NonNullMutableLiveData(ThemeColor.PURPLE)
    val color: LiveData<ThemeColor> = _color

    fun setThemeColor(color: ThemeColor) {
        sharedPreferences[PreferenceHelper.THEME_COLOR] = color.value
        _color.value = color

        strokeWidthPurple.value = 0
        strokeWidthGreen.value = 0
        strokeWidthOrange.value = 0

        when (color) {
            ThemeColor.PURPLE -> {
                themeCallback?.invoke(R.style.VaccinePass_purple)
                strokeWidthPurple.value = 3
            }
            ThemeColor.GREEN -> {
                themeCallback?.invoke(R.style.VaccinePass_green)
                strokeWidthGreen.value = 3
            }
            ThemeColor.ORANGE -> {
                themeCallback?.invoke(R.style.VaccinePass_orange)
                strokeWidthOrange.value = 3
            }
        }
    }

    fun errorTextString(str: String): String {
        return when {
            str.isBlank() -> errorText.value
            regex.containsMatchIn(str) -> errorTextValidity.value
            else -> ""
        }
    }

    fun errorTextStringWithoutBlank(str: String): String {
        return when {
            regex.containsMatchIn(str) -> errorTextValidity.value
            else -> ""
        }
    }

    fun setBirthDate(date: LocalDate?) {
        _birthDate.value = date
    }

    fun setPhoto() {
        permissionRequest.value = "camera"
    }

    fun finish() {

        if (bloodType.value.isEmpty()) bloodType.value = "-"
        if (weight.value.isEmpty()) weight.value = "0"
        if (height.value.isEmpty()) height.value = "0"

        viewModelScope.launch(Dispatchers.IO) {

            val tempFirst = sanitizeString(firstName.value)
            val tempLast = sanitizeString(lastName.value)
            val tempType = sanitizeString(bloodType.value)
            val tempWeight = weight.value.toFloatOrNull()
            val tempHeight = height.value.toFloatOrNull()

            val user = User(
                uid = 0,
                firstName = tempFirst,
                lastName = tempLast,
                bloodType = tempType,
                weight = tempWeight,
                height = tempHeight,
                birthDay = birthDate.value,
                themeColor = _color.value.value,
                photoPath = currentPhotoPath.value
            )

            val id = userRepository.insertUser(user)

            id?.let {
                val newUser = userRepository.getUser(id)

                newUser?.let {
                    sharedPreferences[StartActivity.LAST_USER_ID_PREF] = newUser.uid
                    navigationRequest.request(MainRequest)
                }

                if (newUser == null) {
                    messageRequest.request(ToastRequest("New user null"))
                }
            }

            if (id == null) {
                messageRequest.request(ToastRequest("Id null. Could not insert user"))
            }
        }
    }

    private fun sanitizeString(str: String): String {
        return str.replace("\\", "")
            .replace(";", "")
            .replace("%", "")
            .replace("\"", "")
            .replace("\'", "")
            .replace("*", "")
    }
}