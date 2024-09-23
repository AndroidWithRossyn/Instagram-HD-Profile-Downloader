package com.instagram.hdprofile.downloader.activitys

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import com.instagram.hdprofile.downloader.R
import com.instagram.hdprofile.downloader.adapter.ViewPagerAdapter
import com.instagram.hdprofile.downloader.databinding.ActivityMainBinding
import com.instagram.hdprofile.downloader.fragment.HomeFragment
import com.instagram.hdprofile.downloader.fragment.WebFragment
import com.instagram.hdprofile.downloader.utility.BaseViewModel
import com.instagram.hdprofile.downloader.utility.PermissionUtility
import com.instagram.hdprofile.downloader.utility.SharedPref

class MainActivity : AppCompatActivity(), DefaultLifecycleObserver,
    OnSharedPreferenceChangeListener {

    private val TAG = "MainActivityTAG"

    private lateinit var binding: ActivityMainBinding


    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var permissionUtility: PermissionUtility? = null


    companion object {
        var homeFragment: HomeFragment? = null
        var webFragment: WebFragment? = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            when (key) {

            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d("MainActivityLife", "MainActivity onWindowFocusChanged: $hasFocus")
        window.decorView.rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (binding.mainFrameLayout.currentItem == 0) {
                        Log.d("homeFragment", "MainActivity onRe foucus: not equals WebFragment")
                        Handler(Looper.getMainLooper()).postDelayed({
                            homeFragment?.focusChanged(
                                true
                            )
                        }, 500)
                    }
                    // Remove the listener to avoid multiple calls
                    window.decorView.rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                resources.getColor(
                    R.color.md_theme_background, this.theme
                ), resources.getColor(R.color.md_theme_background, this.theme)
            ), navigationBarStyle = SystemBarStyle.light(
                resources.getColor(
                    R.color.md_theme_background, this.theme
                ), resources.getColor(R.color.md_theme_background, this.theme)
            )
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNevigationBar) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }


        lifecycle.addObserver(this)

        SharedPref.registerOnSharedPreferenceChangeListener(this)
        homeFragment = HomeFragment()
        webFragment = WebFragment()

        permissionUtility = PermissionUtility(this, requestPermissionLauncher)
        requestPermission()


        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(homeFragment!!)
        viewPagerAdapter.addFragment(webFragment!!)

        binding.mainFrameLayout.setAdapter(viewPagerAdapter)
        binding.mainFrameLayout.setOffscreenPageLimit(2)
        binding.mainFrameLayout.setPagingEnabled(false)

        binding.mainFrameLayout.setCurrentItem(0, true)
        binding.bottomNevigationBar.menu.getItem(0).setChecked(true)

        // Handle item selection in BottomNavigationView
        binding.bottomNevigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.home_main_icon -> {
                    binding.mainFrameLayout.setCurrentItem(0, true)
                    return@setOnItemSelectedListener true
                }

                R.id.web_main_icon -> {
                    binding.mainFrameLayout.setCurrentItem(1, true)
                    return@setOnItemSelectedListener true
                }

                else -> {
                    false
                }
            }
        }


        binding.mainFrameLayout.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.bottomNevigationBar.menu.getItem(position).setChecked(true)
                when (position) {
                    0 -> {

                    }

                    1 -> {

                    }


                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (isEnabled) {
                    isEnabled = false
                    finish()
                }
            }
        })

    }


    private fun requestPermission() {
        if (permissionUtility != null && !permissionUtility!!.isPermissionGranted() && !this.isFinishing) {
            permissionUtility!!.requestPermissions()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val blockPermissionCheck = mutableListOf<String>()
        for ((key, value) in result) {
            if (!value) {
                blockPermissionCheck.add(PermissionUtility.getPermissionStatus(this, key))
            }
        }
        // if (blockPermissionCheck.contains("blocked")) {
        //     showPermissionDialog("Permission Required", "This App requires for Particular features to work as expected as Save Photos and Videos")
        // }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d("MainActivityLife", "MainActivity onDestroy: ")
        // When status updates are no longer needed, unregister the listener.
        SharedPref.unregisterOnSharedPreferenceChangeListener(this)
        super<DefaultLifecycleObserver>.onDestroy(owner)
    }

    override fun onDestroy() {
        lifecycle.removeObserver(this)
        super<AppCompatActivity>.onDestroy()
    }

    override fun onResume(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onResume(owner)
    }
}

class MainViewModel(application: Application) : BaseViewModel(application) {
    val TAG = "MainViewModel"
}