package com.instagram.hdprofile.downloader.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.DefaultLifecycleObserver
import com.instagram.hdprofile.downloader.R
import com.instagram.hdprofile.downloader.activitys.MainActivity
import com.instagram.hdprofile.downloader.api.ApiHelper
import com.instagram.hdprofile.downloader.databinding.FragmentWebBinding
import com.instagram.hdprofile.downloader.utility.Helper
import com.instagram.hdprofile.downloader.utility.SharedPref


/**
 * A simple [Fragment] subclass.
 * Use the [WebFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WebFragment : Fragment(), DefaultLifecycleObserver {

    companion object {

    }

    private val TAG: String = "WEBFRAGMENT"

    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!

    private var LoginDetails: Boolean = false
    private var sendRequest: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }


    override fun onDestroy() {
        super<Fragment>.onDestroy()
        lifecycle.removeObserver(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded && _binding != null) {

            LoginDetails = SharedPref.cookies != null


            binding.instaWebview.apply {

                this.settings.apply {
                    // Enable JavaScript for Instagram functionality
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false

                    javaScriptCanOpenWindowsAutomatically = true
                    // Enable safe browsing
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        safeBrowsingEnabled = true
                    }
                    // Enable image loading for a better user experience
                    loadsImagesAutomatically = true
                    // Cache settings
                    cacheMode = WebSettings.LOAD_DEFAULT

                    // Compatibility settings for newer Android versions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }
                    SharedPref.userAgent = userAgentString
                }


                // Clear any previous data (optional)
//                clearCache(true) // Clear cache including disk cache
//                clearHistory() // Clear history
//                clearFormData() // Clear form data
//                clearSslPreferences()
//                clearMatches()

                webViewClient = MyWebViewClient()
                webChromeClient = WebChromeClient()

            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.instaWebview, true)
            CookieManager.getInstance().setAcceptCookie(true)




            binding.refreshWeb.setOnRefreshListener {
                binding.instaWebview.reload()
            }

            binding.refreshWeb.setColorSchemeColors(
                requireContext().resources.getColor(
                    R.color.md_theme_primary,
                    requireContext().theme
                ),
                requireContext().resources.getColor(R.color.logo_one, requireContext().theme),
                requireContext().resources.getColor(R.color.logo_two, requireContext().theme),
                requireContext().resources.getColor(R.color.logo_three, requireContext().theme)
            )

            loadWebView()

        }
    }

    private fun loadWebView() {
        _binding?.let {
            if (isAdded && binding.instaWebview.url == null) {

                Log.d(TAG, "loadwebview: ")
                if (SharedPref.cookies == null) {
                    binding.instaWebview.loadUrl(ApiHelper.LOGIN_URL)
                } else {
                    binding.instaWebview.loadUrl(ApiHelper.BASE_URL)
                }


            }
        }
    }

    fun refresh() {
        _binding?.let {
            if (isAdded) {
                binding.instaWebview.clearHistory()
                binding.instaWebview.loadUrl(ApiHelper.BASE_URL)
                LoginDetails = SharedPref.cookies != null
                sendRequest = LoginDetails
            }
        }
    }


    inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            binding.wfCurrentUrl.text = url
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, str: String?) {
            super.onPageFinished(view, str)
            binding.refreshWeb.isRefreshing = false

            binding.wfCurrentUrl.text = str
            SharedPref.isLoadAgain = false
            var cookies = CookieManager.getInstance().getCookie(str)

            SharedPref.tempCookies = cookies

            Log.d("WebView", "onPageFinished Cookies: $cookies")
            if (cookies != null && cookies.contains("ds_user_id") && cookies.contains("sessionid")) {
                SharedPref.cookies = cookies
                //Log.d("logindetails", "onPageStarted Cookies: userid contains " + cookies);

                try {
                    cookies = Helper.extractValue(cookies, "ds_user_id")
                    Log.d("WebView", "userid:  $cookies")
                    SharedPref.userIdIsSave = cookies
                } catch (e: Exception) {
                    Log.d("WebView", "onPageFinished: ${e.message}")
                }
                SharedPref.isUserLogin = true
                if (!sendRequest) {
                    sendRequest = true
                }
                if (!LoginDetails) {
                    LoginDetails = true
                }
            } else {
                Log.d(TAG, "onPageFinished: user logout")
            }

        }


        override fun onLoadResource(view: WebView?, url: String?) {
            //            Log.d("WebViewUrl", "webpage onLoadResource.. " + str);
            binding.loginActivityToolText.text = url
            super.onLoadResource(view, url)
        }


        override fun onReceivedError(
            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
        }


        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val response = super.shouldInterceptRequest(view, request)
            if (response == null) {
                val headers = request!!.requestHeaders
                for ((key, value) in headers) {
                    when (key) {
                        "X-IG-App-ID" -> {
                            SharedPref.xIgAppId = value
                            Log.d("WebViewHeaders", "$key: $value")
                        }

                        "X-ASBD-ID" -> {
                            SharedPref.xAsbID = value
                            Log.d("WebViewHeaders", "$key: $value")
                        }

                        "X-IG-WWW-Claim" -> {
                            if (value != "0") {
                                SharedPref.xClaimId = value
                                Log.d("WebViewHeaders", "$key: $value")
                            }
                        }
                    }
                }
            }
            return response
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


}