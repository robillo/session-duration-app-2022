package com.firstapp.robinpc.tongue_twisters_deluxe.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firstapp.robinpc.tongue_twisters_deluxe.BuildConfig
import com.firstapp.robinpc.tongue_twisters_deluxe.R
import com.firstapp.robinpc.tongue_twisters_deluxe.ui.base.common_adapter.AppEnums
import dagger.Reusable
import jp.co.cyberagent.android.gpuimage.filter.*
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.gpu.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@Reusable
class BitmapUtil
@Inject constructor() {

    interface BitmapCallback {
        fun onSuccess(bitmap: Bitmap?) {}
        fun onSuccess(bitmap: Bitmap?, position: Int) {}
        fun onError(){}
    }

    fun resizeBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
        try {
            var image = source
            return if (maxHeight > 0 && maxWidth > 0) {
                val width = image.width
                val height = image.height
                val ratioBitmap = width.toFloat() / height.toFloat()
                val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
                var finalWidth = maxWidth
                var finalHeight = maxHeight
                if (ratioMax > ratioBitmap) {
                    finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
                } else {
                    finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
                }
                image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
                image
            } else {
                image
            }
        } catch (e: Exception) {}
        return null
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

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback.onError()
                }
            })
    }

    fun getCircularBitmapFromUrl(
        context: Context,
        url: String,
        callback: BitmapUtil.BitmapCallback,
        transformation: Transformation<Bitmap?>? = null
    ) {
        val reqOptIn =
            RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.notification_image_placeholder)
                .error(R.drawable.notification_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)

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

    fun getProfileBitmapFromUrl(
        context: Context,
        url: String,
        callback: BitmapUtil.BitmapCallback,
        transformation: Transformation<Bitmap?>? = null
    ) {

        val reqOptIn =
            RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.notification_image_placeholder)
                .error(R.drawable.notification_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.IMMEDIATE)

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
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback.onError()
                }
            })


    }

    fun getBitmapFromUri(
        context: Context,
        uri: Uri,
        callback: BitmapUtil.BitmapCallback,
        transformation: Transformation<Bitmap?>? = null
    ) {
        val reqOptIn = RequestOptions().centerCrop().placeholder(R.drawable.notification_image_placeholder)
            .error(R.drawable.notification_image_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.IMMEDIATE)

        transformation?.let {
            reqOptIn.transform(it)
        }

        Glide.with(context)
            .asBitmap()
            .load(uri)
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

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback.onError()
                }
            })


    }

    fun getTransform(filter: AppEnums.ImageFilter?): Transformation<Bitmap?>? {
        try {
            when (filter) {
                AppEnums.ImageFilter.SEPIA -> return SepiaFilterTransformation()
                AppEnums.ImageFilter.CONTRAST -> return ContrastFilterTransformation(3f)
                AppEnums.ImageFilter.BRIGHTNESS -> return BrightnessFilterTransformation(0.25f)
                AppEnums.ImageFilter.DARK_BLENDER -> return BrightnessFilterTransformation(-0.25f)
                AppEnums.ImageFilter.BOXBLUR -> return BlurTransformation()
                AppEnums.ImageFilter.DARK_BLENDER -> return GPUFilterTransformation(
                    GPUImageDarkenBlendFilter()
                )
                AppEnums.ImageFilter.GRAY_SCALE -> return GrayscaleTransformation()
                AppEnums.ImageFilter.HUE -> return GPUFilterTransformation(GPUImageHueFilter())
                AppEnums.ImageFilter.OPACITY -> return GPUFilterTransformation(
                    GPUImageOpacityFilter()
                )
                AppEnums.ImageFilter.OVERLAY -> return KuwaharaFilterTransformation()
                AppEnums.ImageFilter.POSTERIZE -> return GPUFilterTransformation(
                    GPUImagePosterizeFilter()
                )
                AppEnums.ImageFilter.SKETCH -> return GPUFilterTransformation(
                    GPUImageSketchFilter()
                )
                AppEnums.ImageFilter.NORMAL -> {}
                else -> {}
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getUriFromBitmap(activity: Context?, bitmap: Bitmap?): Uri? {
        var imageUri: Uri? = null
        try {
            activity?.let { context ->
                val fileName = "image"
                try {
                    bitmap?.let {
                        val cachePath = File(context.cacheDir, "images")
                        cachePath.mkdirs() // don't forget to make the directory
                        val stream =
                            FileOutputStream("$cachePath/$fileName.png") // overwrites this image every time
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.close()

                        val imagePath = File(context.cacheDir, "images")
                        val newFile = File(imagePath, "$fileName.png")
                        imageUri =
                            FileProvider.getUriForFile(
                                context,
                                "com.kutumb.android.fileprovider",
                                newFile
                            )
                    }
                } catch (e: IOException) {}

            }

        } catch (e: Exception) {}

        return imageUri
    }
}