package com.firstapp.robinpc.tongue_twisters_deluxe.utils.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firstapp.robinpc.tongue_twisters_deluxe.BuildConfig
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.data.database.TwisterDatabase
import com.firstapp.robinpc.tongue_twisters_deluxe.data.model.Twister
import com.firstapp.robinpc.tongue_twisters_deluxe.data.repository.TwisterRepository
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.splash.SplashActivity
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.BitmapUtil
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.Constants.Companion.TWISTER_COUNT
import com.firstapp.robinpc.tongue_twisters_deluxe.utils.background.AlarmSchedulerUtil
import dagger.android.AndroidInjection
import java.util.*
import javax.inject.Inject


class RecurringNotificationService : LifecycleService() {

    @Inject
    lateinit var database: TwisterDatabase

    private lateinit var twisterRepo: TwisterRepository

    companion object {
        var WAIT_DURATION_MILLIS =
            if(BuildConfig.DEBUG) 1 * 60 * 1000
            else 6 * 60 * 60 * 1000
        private const val INTRO_STRING_SMALL = "Hey buddy. Here is a new twister for you - "
        private const val INTRO_STRING_BIG = "Hey buddy. Here is a new twister for you named "
        private const val OUTRO_STRING = "Open the app to read out loud."
        private const val ACTION_NAME_STRING = "OPEN IN APP"
        private const val LINE_BREAKER_STRING = "."
        private const val LINE_SPACER_STRING = " "
        private const val MIN_BOUND = 0
        private const val MAX_BOUND = TWISTER_COUNT
        private const val CHANNEL_ID = "tongue_twisters_default"
        private const val CHANNEL_NAME = "Tongue Twisters Deluxe"
        private const val CHANNEL_DESCRIPTION = "Pronunciation Improvement"
        const val NOTIFICATION_ID = 1234
        fun newIntent(context: Context): Intent {
            return Intent(context, RecurringNotificationService::class.java)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        setup()
        super.onCreate()
    }

    private fun setup() {
        setComponent()
        createNotificationChannel()
        configureNotification()
    }

    private fun configureNotification() {
        twisterRepo = TwisterRepository(database.twisterDao())

        getTwisterForIndex(generateRandomIndex()).observe(this) {
            it?.let {
                buildNotification(it)
            }
            rescheduleAlarmForRecurringTrigger()
            stopSelf()
        }
    }

    private fun generateRandomIndex(): Int {
        return Random().nextInt(MAX_BOUND - MIN_BOUND + 1) + MIN_BOUND
    }

    private fun setComponent() {
        AndroidInjection.inject(this)
    }

    private fun buildNotification(twister: Twister) {
        getBitmapFromUrl(
            this, twister.iconUrl,
            object: BitmapUtil.BitmapCallback {
                override fun onSuccess(bitmap: Bitmap?) {
                    super.onSuccess(bitmap)

                    val contentView = RemoteViews(packageName, R.layout.custom_notification)
                    val contentViewHeadsUp = RemoteViews(packageName, R.layout.custom_notification_heads_up)
                    val contentViewLarge = RemoteViews(packageName, R.layout.custom_notification_expanded)

                    contentView.setImageViewBitmap(R.id.iconIv, bitmap)
                    contentViewHeadsUp.setImageViewBitmap(R.id.iconIv, bitmap)
                    contentViewLarge.setImageViewBitmap(R.id.iconIv, bitmap)

                    contentView.setTextViewText(R.id.twisterNameTv, twister.name)
                    contentViewHeadsUp.setTextViewText(R.id.twisterNameTv, twister.name)
                    contentViewLarge.setTextViewText(R.id.twisterNameTv, twister.name)

                    contentView.setTextViewText(
                        R.id.notificationDescriptionTv,
                        String.format(
                            Locale.ENGLISH,
                            getString(R.string.notification_description_compact),
                            twister.name
                        )
                    )
                    contentViewHeadsUp.setTextViewText(
                        R.id.notificationDescriptionTv,
                        String.format(
                            Locale.ENGLISH,
                            getString(R.string.notification_description_heads_up),
                            twister.name
                        )
                    )
                    contentViewLarge.setTextViewText(
                        R.id.notificationDescriptionTv,
                        String.format(
                            Locale.ENGLISH,
                            getString(R.string.notification_description_expanded),
                            twister.name
                        )
                    )

                    val builder = NotificationCompat.Builder(this@RecurringNotificationService, CHANNEL_ID)
                        .setSmallIcon(R.drawable.tornado)
                        .setContentTitle(twister.name)
                        .setColor(ContextCompat.getColor(this@RecurringNotificationService, R.color.day_twister_reading_activity_twister_color))
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setContentIntent(getTwisterIntent(twister.id))
                        .setContentText(INTRO_STRING_SMALL.plus(twister.twister))
                        .setCustomContentView(contentView)
                        .setCustomHeadsUpContentView(contentView)
                        .setCustomBigContentView(contentViewLarge)
                        .setCustomHeadsUpContentView(contentViewHeadsUp)
                        .setAutoCancel(true)
                        .setStyle(
                            NotificationCompat
                                .BigTextStyle()
                                .bigText(
                                    INTRO_STRING_BIG
                                        .plus(twister.name)
                                        .plus(LINE_BREAKER_STRING)
                                        .plus(LINE_SPACER_STRING)
                                        .plus(OUTRO_STRING)
                                )
                        )
                        .addAction(R.drawable.ic_next_filled, ACTION_NAME_STRING, getTwisterIntent(twister.id))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    sendNotification(builder)
                }

                override fun onError() {
                    super.onError()

                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_hand_hello)

                    val contentView = RemoteViews(packageName, R.layout.custom_notification)
                    val contentViewHeadsUp = RemoteViews(packageName, R.layout.custom_notification_heads_up)
                    val contentViewLarge = RemoteViews(packageName, R.layout.custom_notification_expanded)

                    contentView.setImageViewBitmap(R.id.iconIv, bitmap)
                    contentViewHeadsUp.setImageViewBitmap(R.id.iconIv, bitmap)
                    contentViewLarge.setImageViewBitmap(R.id.iconIv, bitmap)

                    contentView.setTextViewText(R.id.twisterNameTv, twister.name)
                    contentViewHeadsUp.setTextViewText(R.id.twisterNameTv, twister.name)
                    contentViewLarge.setTextViewText(R.id.twisterNameTv, twister.name)

                    contentView.setTextViewText(
                        R.id.notificationDescriptionTv,
                        String.format(
                            Locale.ENGLISH,
                            getString(R.string.notification_description_compact),
                            twister.name
                        )
                    )
                    contentViewHeadsUp.setTextViewText(
                        R.id.notificationDescriptionTv,
                        String.format(
                            Locale.ENGLISH,
                            getString(R.string.notification_description_heads_up),
                            twister.name
                        )
                    )
                    contentViewLarge.setTextViewText(
                        R.id.notificationDescriptionTv,
                        String.format(
                            Locale.ENGLISH,
                            getString(R.string.notification_description_expanded),
                            twister.name
                        )
                    )

                    val builder = NotificationCompat.Builder(this@RecurringNotificationService, CHANNEL_ID)
                        .setSmallIcon(R.drawable.tornado)
                        .setContentTitle(twister.name)
                        .setColor(ContextCompat.getColor(this@RecurringNotificationService, R.color.day_twister_reading_activity_twister_color))
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setContentIntent(getTwisterIntent(twister.id))
                        .setContentText(INTRO_STRING_SMALL.plus(twister.twister))
                        .setCustomContentView(contentView)
                        .setCustomHeadsUpContentView(contentView)
                        .setCustomBigContentView(contentViewLarge)
                        .setCustomHeadsUpContentView(contentViewHeadsUp)
                        .setAutoCancel(true)
                        .setStyle(
                            NotificationCompat
                                .BigTextStyle()
                                .bigText(
                                    INTRO_STRING_BIG
                                        .plus(twister.name)
                                        .plus(LINE_BREAKER_STRING)
                                        .plus(LINE_SPACER_STRING)
                                        .plus(OUTRO_STRING)
                                )
                        )
                        .addAction(R.drawable.ic_next_filled, ACTION_NAME_STRING, getTwisterIntent(twister.id))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    sendNotification(builder)
                }
            }
        )
    }

    fun getBitmapFromUrl(
        context: Context,
        url: String,
        callback: BitmapUtil.BitmapCallback,
        transformation: Transformation<Bitmap?>? = null
    ) {

        val reqOptIn =
            if(BuildConfig.DEBUG) {
                RequestOptions().centerCrop().placeholder(R.drawable.notification_image_placeholder)
                    .error(R.drawable.notification_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .priority(Priority.IMMEDIATE)
            }
            else {
                RequestOptions().centerCrop().placeholder(R.drawable.notification_image_placeholder)
                    .error(R.drawable.notification_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .priority(Priority.IMMEDIATE)
            }

        transformation?.let {
            reqOptIn.transform(it)
        }

        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(reqOptIn)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadStarted(placeholder: Drawable?) {}
                override fun onStop() {}
                override fun onStart() {}
                override fun onDestroy() {}
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    try { callback.onSuccess(bitmap) } catch (ignored: Exception) {}
                }
                override fun onLoadFailed(errorDrawable: Drawable?) { callback.onError() }
            })
    }

    private fun getTwisterIntent(twisterIndex: Int): PendingIntent {
        val splashIntent = SplashActivity.newIntent(this, twisterIndex)
        val taskStackBuilder = TaskStackBuilder.create(this)
        taskStackBuilder.addNextIntent(splashIntent)
        val requestCode = 0
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        return taskStackBuilder.getPendingIntent(requestCode, flag)
    }

    private fun sendNotification(builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun rescheduleAlarmForRecurringTrigger() {
        AlarmSchedulerUtil.setAlarm(this, System.currentTimeMillis() + WAIT_DURATION_MILLIS)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationChannel()?.let {
                getNotificationManager(this).createNotificationChannel(it)
            }
        }
    }

    private fun getNotificationChannel(): NotificationChannel? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESCRIPTION
                val soundUri =
                    //try {
                    Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.access_allowed)
                //}
                //catch (ignored: Exception) { null }
                soundUri?.let {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(it, audioAttributes)
                }
            }
        }
        return null
    }

    private fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun getTwisterForIndex(index: Int): LiveData<Twister> {
        return twisterRepo.getTwisterForIndex(index)
    }
}
