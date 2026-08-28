package com.gihansgamage.gpamaster.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Utility helper for managing Google AdMob SDK initialization, banner ads,
 * and interstitial ad loading safely across the application.
 */
object AdHelper {
    private const val TAG = "AdHelper"
    private var isInitialized = false

    /**
     * Initializes the Mobile Ads SDK.
     * Configures test devices automatically during development to prevent policy violations.
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialized successfully: ${initializationStatus.adapterStatusMap}")
            }

            // Configure Test Devices for development safety
            val testDeviceIds = listOf(
                AdRequest.DEVICE_ID_EMULATOR
            )
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds SDK", e)
        }
    }

    /**
     * Helper to load a banner ad into an AdView with error logging listeners.
     */
    fun loadBannerAd(adView: AdView?) {
        if (adView == null) return

        try {
            val adRequest = AdRequest.Builder().build()
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Banner ad loaded successfully: ${adView.adUnitId}")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Banner ad failed to load: ${error.message} (code: ${error.code})")
                }
            }
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad", e)
        }
    }

    /**
     * Helper to load an interstitial ad for high-value actions (e.g. PDF/Excel export).
     */
    fun loadInterstitialAd(
        context: Context,
        adUnitId: String,
        onAdLoaded: (InterstitialAd?) -> Unit
    ) {
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully: $adUnitId")
                        onAdLoaded(interstitialAd)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                        onAdLoaded(null)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading interstitial ad", e)
            onAdLoaded(null)
        }
    }
}
