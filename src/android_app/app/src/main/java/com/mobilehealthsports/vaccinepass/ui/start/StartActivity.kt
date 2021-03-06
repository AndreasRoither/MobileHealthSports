package com.mobilehealthsports.vaccinepass.ui.start

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.mobilehealthsports.vaccinepass.BuildConfig
import com.mobilehealthsports.vaccinepass.R
import com.mobilehealthsports.vaccinepass.TestActivity
import com.mobilehealthsports.vaccinepass.business.models.Reminder
import com.mobilehealthsports.vaccinepass.ui.introduction.IntroductionActivity
import com.mobilehealthsports.vaccinepass.ui.main.MainActivity
import com.mobilehealthsports.vaccinepass.ui.pin.PinActivity
import com.mobilehealthsports.vaccinepass.ui.pin.PinViewModel
import com.mobilehealthsports.vaccinepass.ui.user_creation.UserCreationActivity
import com.mobilehealthsports.vaccinepass.util.BaseActivity
import com.mobilehealthsports.vaccinepass.util.PreferenceHelper.get
import com.mobilehealthsports.vaccinepass.util.PreferenceHelper.set
import com.mobilehealthsports.vaccinepass.util.notification.AlarmScheduler
import com.mobilehealthsports.vaccinepass.util.notification.NotificationHelper
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


class StartActivity : BaseActivity() {
    private val sharedPreferences: SharedPreferences by inject()
    private var lastUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val buildConfigExtra = intent.getBooleanExtra(EXTRA_1, true)

        if (BuildConfig.DEBUG && buildConfigExtra) {
            startActivity(TestActivity.intent(this))
            finish()
        } else {
            lastUserId = sharedPreferences[LAST_USER_ID_PREF, -1L]!!

            if (lastUserId != -1L) {
                // pin check
                startActivityForResult(
                    PinActivity.intent(
                        this,
                        PinViewModel.PinState.CHECK,
                        PIN_LENGTH
                    ), REQUESTS.PIN.code
                )
            } else {
                // introduction
                startActivityForResult(
                    IntroductionActivity.intent(
                        this
                    ), REQUESTS.INTRO.code
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUESTS.PIN.code -> {
                // if result code is not ok restart
                if (resultCode != RESULT_OK) {
                    startActivityForResult(
                        PinActivity.intent(
                            this,
                            PinViewModel.PinState.INITIAL,
                            PIN_LENGTH
                        ), REQUESTS.PIN.code
                    )
                }

                val result: Boolean = data?.getBooleanExtra("result", false) == true

                if (lastUserId == -1L && result) {
                    // start user creation
                    startActivityForResult(UserCreationActivity.intent(this), REQUESTS.USER.code)
                } else if (lastUserId != -1L && result) {
                    startActivity(MainActivity.intent(this))
                    finish()
                } else {
                    // just restart pin on-boarding again
                    startActivityForResult(
                        PinActivity.intent(
                            this,
                            PinViewModel.PinState.INITIAL,
                            PIN_LENGTH
                        ), REQUESTS.PIN.code
                    )
                }
            }
            REQUESTS.USER.code -> {
                if (resultCode != RESULT_OK) {
                    startActivityForResult(UserCreationActivity.intent(this), REQUESTS.USER.code)
                }

                val result: Long = data?.getLongExtra("result", -1L) ?: -1L

                if (result != -1L) {
                    sharedPreferences[LAST_USER_ID_PREF] = result
                    startActivity(MainActivity.intent(this))
                    finish()
                }

                if (lastUserId != -1L) {
                    // start user creation
                    startActivityForResult(UserCreationActivity.intent(this), REQUESTS.USER.code)
                } else {
                    // just restart pin on-boarding again
                    startActivityForResult(
                        PinActivity.intent(
                            this,
                            PinViewModel.PinState.INITIAL,
                            PIN_LENGTH
                        ), REQUESTS.PIN.code
                    )
                }
            }
            REQUESTS.INTRO.code -> {
                // start pin on-boarding
                startActivityForResult(
                    PinActivity.intent(
                        this,
                        PinViewModel.PinState.INITIAL,
                        PIN_LENGTH
                    ), REQUESTS.PIN.code
                )
            }
        }
    }

    enum class REQUESTS(val code: Int) {
        PIN(1),
        USER(2),
        INTRO(3)
    }

    companion object {
        private const val EXTRA_1 = "ignoreBuild"
        const val PIN_LENGTH = 4
        const val LAST_USER_ID_PREF = "lastUserId"

        // create intent to navigate to this class
        fun intent(context: Context, buildFlag: Boolean): Intent {
            return Intent(context, StartActivity::class.java).apply {
                putExtra(EXTRA_1, buildFlag)
            }
        }
    }
}

