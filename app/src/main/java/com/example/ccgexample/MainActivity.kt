package com.example.ccgexample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var adLoader: AdLoader
    private lateinit var adFrame: FrameLayout
    private lateinit var btnRefresh: Button
    private lateinit var statusText: TextView
    private lateinit var enableCCGOption: CheckBox
    private lateinit var btnRecordCCG: Button

    private val logMessages: MutableList<String> = mutableListOf()
    private val sdf = SimpleDateFormat("hh:mm:ss")

    private var mNativeAd: NativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adFrame = findViewById(R.id.ad_frame)
        btnRefresh = findViewById(R.id.btn_refresh_ad)
        statusText = findViewById(R.id.status_text)
        enableCCGOption = findViewById(R.id.option_enable_ccg)
        btnRecordCCG = findViewById(R.id.btn_record_ccg)

        initAdLoader()
        enableCCGOption.setOnCheckedChangeListener { _, checked ->
            btnRecordCCG.visibility = if (checked) View.VISIBLE else View.GONE
        }
        btnRecordCCG.setOnClickListener { manualAdClick() }
        btnRefresh.setOnClickListener { refreshAd() }
        refreshAd()
    }

    private fun manualAdClick() = mNativeAd?.recordCustomClickGesture()

    private fun refreshAd() {
        btnRefresh.isEnabled = false
        loadAd()
    }

    private fun initAdLoader() {
        adLoader = AdLoader.Builder(this, AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                if (isDestroyed) {
                    nativeAd.destroy()
                    return@forNativeAd
                }
                mNativeAd = nativeAd
                if (enableCCGOption.isChecked) {
                    nativeAd.enableCustomClickGesture()
                }
                renderAd(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    log("onAdClicked")
                }

                override fun onAdClosed() {
                    log("onAdClosed")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    btnRefresh.isEnabled = true
                    log("onAdFailedToLoad: $error")
                }

                override fun onAdImpression() {
                    log("onAdImpression")
                }

                override fun onAdLoaded() {
                    log("onAdLoaded")
                }

                override fun onAdOpened() {
                    log("onAdOpened")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
    }

    private fun loadAd() = adLoader.loadAd(AdRequest.Builder().build())

    private fun renderAd(nativeAd: NativeAd) {
        val adView = layoutInflater.inflate(R.layout.ad_layout, null) as NativeAdView
        populateNativeAdView(adView, nativeAd)
        if (enableCCGOption.isChecked) {
            adView.mediaView?.setOnClickListener { manualAdClick() }
            adView.callToActionView?.setOnClickListener { manualAdClick() }

            val btnWithinAdView = adView.findViewById<Button>(R.id.btn_within_adview)
            btnWithinAdView.visibility = View.VISIBLE
            btnWithinAdView.setOnClickListener { manualAdClick() }
        }

        adFrame.removeAllViews()
        if (enableCCGOption.isChecked) {
            val frameLayout = object : FrameLayout(this) {
                override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                    onTouchEvent(ev)
                    return false
                }

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouchEvent(ev: MotionEvent): Boolean {
                    // Relay the touch event to your custom click gesture detector.
                    handleTouchEvent(adView, ev)
                    return false
                }
            }
            frameLayout.addView(adView)
            adFrame.addView(frameLayout)
        } else {
            adFrame.addView(adView)
        }
    }

    private fun populateNativeAdView(adView: NativeAdView, nativeAd: NativeAd) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every NativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc?.hasVideoContent() == true) {
            log("Video status: Ad contains a ${nativeAd.mediaContent?.aspectRatio}:1 video asset.")
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    btnRefresh.isEnabled = true
                    log("Video status: Video playback has ended.")
                    super.onVideoEnd()
                }
            }
        } else {
            log("Video status: Ad does not contain a video asset.")
            btnRefresh.isEnabled = true
        }
    }

    private fun handleTouchEvent(touchView: View, event: MotionEvent) {
        val displayMetrics = resources.displayMetrics
        val cardWidth = touchView.width
        val cardStart = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)

        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (touchView.x < MIN_SWIPE_DISTANCE) {
                    log("swiped left")
                    manualAdClick()
                }
                touchView.animate()
                    .x(cardStart)
                    .setDuration(150)
                    .start()
            }
            MotionEvent.ACTION_MOVE -> {
                // get the new co-ordinate of X-axis
                val newX = event.rawX

                // carry out swipe only if newX < cardStart, that is,
                // the card is swiped to the left side, not to the right
                if (newX - cardWidth < cardStart) {
                    touchView.animate()
                        .x(
                            min(cardStart, newX - (cardWidth / 2))
                        )
                        .setDuration(0)
                        .start()
                }
            }
        }
    }

    private fun log(msg: String) {
        val dateStr = sdf.format(Date(System.currentTimeMillis()))
        logMessages.add("[$dateStr] $msg")
        statusText.text = logMessages.reversed().joinToString(separator = "\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        mNativeAd?.destroy()
    }

    companion object {
        private const val AD_UNIT_ID = "/6499/example/native"
        private const val MIN_SWIPE_DISTANCE = -450
    }
}