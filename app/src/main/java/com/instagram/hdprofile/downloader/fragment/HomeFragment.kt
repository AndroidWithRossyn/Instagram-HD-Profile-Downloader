package com.instagram.hdprofile.downloader.fragment

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import com.instagram.hdprofile.downloader.MyApplication
import com.instagram.hdprofile.downloader.R
import com.instagram.hdprofile.downloader.api.ApiHelper
import com.instagram.hdprofile.downloader.api.ApiHelper.getUrlWithoutParameters
import com.instagram.hdprofile.downloader.customview.CustomDialog
import com.instagram.hdprofile.downloader.databinding.FragmentHomeBinding
import com.instagram.hdprofile.downloader.model.ResponseModel
import com.instagram.hdprofile.downloader.model.UserInfoForSingleStoryDownload
import com.instagram.hdprofile.downloader.utility.BaseViewModel
import com.instagram.hdprofile.downloader.utility.Helper
import com.instagram.hdprofile.downloader.utility.Helper.showToastShort
import com.instagram.hdprofile.downloader.utility.SharedPref
import com.instagram.hdprofile.downloader.utility.Utils
import io.reactivex.rxjava3.observers.DisposableObserver
import java.net.URI
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), DefaultLifecycleObserver, View.OnClickListener,
    OnEditorActionListener {

    companion object {

    }

    private var _binding: FragmentHomeBinding? = null

    /**
     * Throws an exception if accessed when `_binding` is null
     */
    private val binding get() = _binding!!


    private val homeViewModel: HomeViewModel by viewModels()

    private var clipboardManager: ClipboardManager? = null


    private var fragmentAttached: Boolean = false

    val TAG = "homeFragment"


    fun focusChanged(hasFocus: Boolean) {
        Log.d("LifeCheckAutoSaveHome", "HomeFragment onWindowFocusChanged $hasFocus")
        if (isAdded && hasFocus && _binding != null && fragmentAttached) {
            if ((clipboardManager?.primaryClip?.itemCount ?: 0) > 0) {
                val str = clipboardManager!!.primaryClip!!.getItemAt(0).text.toString()
                binding.fasSearchView.setText(str)
            }
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (v == binding.fasSearchView) {
                onClick(binding.searchNow)
                Helper.hideSoftKeyboard(v)
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }


    override fun onDestroy() {
        super<Fragment>.onDestroy()
        lifecycle.removeObserver(this)
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * Initializes click listeners and sets up the search functionality.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (isAdded) {
            clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager



            binding.searchNow.setOnClickListener(this)
            binding.btnDownload.setOnClickListener(this)
            binding.apiResultJson.setOnClickListener(this)
            binding.copyResultJson.setOnClickListener(this)
            binding.fasSearchView.setOnEditorActionListener(this)


            binding.fasSearchView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                    //  Log.d("EditTextCheck", "beforeTextChanged: " + "CharSequence " + s + "\tint start " + start + "\tint count " + count + "\tint after " + after);
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    Log.d(
                        "EditTextCheck", "onTextChanged: CharSequence $s\t"
                    )

                    if (s.isNotEmpty()) {
                        binding.searchNow.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(), R.color.md_theme_onSurface
                            ), PorterDuff.Mode.SRC_IN
                        )

                    } else {
                        binding.searchNow.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(), R.color.md_theme_outline
                            ), PorterDuff.Mode.SRC_IN
                        )
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    //  Log.d("EditTextCheck", "afterTextChanged: " + "Editable \t" + s);
//                getSearchResult(String.valueOf(s));
                }
            })

            homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) CustomDialog.showProgressDialog(requireContext()) else CustomDialog.hideProgressDialog()
            }

            homeViewModel.snackBarMessage.observe(viewLifecycleOwner) {
                it?.let { Helper.showSnackBarShort(binding.root, it) }
            }


            homeViewModel.profileOther.observe(viewLifecycleOwner) {
                homeViewModel.setLoading(false)
                when (it) {
                    is BaseViewModel.Result.Success -> {
                        if (it.result != null) {
                            try {
                                if (it.result.user != null) {
                                    val user = it.result.user

//                                    Helper.URLCopy(Gson().toJson(user))
                                    binding.apiResultJson.text =
                                        Helper.formatJson(Gson().toJson(user))

                                    /*
                                    * If the user has uploaded their profile in high quality, only then will the HD quality image be saved in Instagram's database.
                                    * Only in that case will the 'hd_profile_pic_url_info' JSON tag appear in the result, otherwise, the normal profile will be shown.
                                    * */
                                    this.profileURL = try {
                                        if (user.hd_profile_pic_url_info != null) {
                                            user.hd_profile_pic_url_info.url
                                        } else user.profile_pic_url_hd ?: user.profile_pic_url
                                    } catch (e: Exception) {
                                        user.profile_pic_url
                                    }

                                    if (user.hd_profile_pic_url_info != null) {
                                        binding.imageSize.text = buildString {
                                            append("[")
                                            append(user.hd_profile_pic_url_info.height)
                                            append(" x ")
                                            append(user.hd_profile_pic_url_info.width)
                                            append("]")
                                        }
                                        binding.imageSize.visibility = View.VISIBLE
                                    } else binding.imageSize.visibility = View.GONE

                                    this.username = user.username

                                    Glide.with(requireContext()).load(profileURL)
                                        .placeholder(R.drawable.user_img_error)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .skipMemoryCache(false)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(binding.maProfileImg).apply {
                                            homeViewModel.setLoading(false)
                                        }

                                    homeViewModel.alreadyLoaded = homeViewModel.pasteLoadUrl

                                }

                            } catch (e: Exception) {
                                Log.d(TAG, "onViewCreated: ${e.message}")
                                binding.apiResultJson.text = buildString {
                                    append("onViewCreated: ")
                                    append(e.message)
                                }
                            }
                        } else {
                            binding.apiResultJson.text = "Result is null"
                        }
                    }

                    is BaseViewModel.Result.Error -> {
                        requireContext().showToastShort(it.exception.message.toString())
                    }
                }

            }
        }

    }

    private var profileURL: String? = null
    private var username: String? = null


    override fun onClick(v: View?) {
        when (v) {
            binding.searchNow -> {
                val url = binding.fasSearchView.getText().toString()
                try {
                    if (!TextUtils.isEmpty(url)) {
                        if (url.contains("https://instagram.com/") || url.contains("https://www.instagram.com/")) {
                            homeViewModel.callApiResult(url)
                        } else {
                            if (url.matches("""^[a-zA-Z0-9_](?!.*\.\.)(?:[a-zA-Z0-9._]*[a-zA-Z0-9_])?$""".toRegex())) {
                                homeViewModel.callApiResult(
                                    "https://www.instagram.com/${
                                        url.lowercase(
                                            Locale.getDefault()
                                        )
                                    }"
                                )
                            } else {
                                homeViewModel.showSnackBar(requireContext().resources.getString(R.string.contains_special))
                            }
                        }
                    } else if ((clipboardManager?.primaryClip?.itemCount ?: 0) > 0) {
                        val str = clipboardManager!!.primaryClip!!.getItemAt(0).text.toString()
                        binding.fasSearchView.setText(str)
                        homeViewModel.callApiResult(str)
                    }
                } catch (e: Exception) {
                    homeViewModel.callApiResult(url)
                }
                Helper.hideSoftKeyboard(binding.fasSearchView)
            }

            binding.btnDownload -> {

                if (profileURL != null && username != null) {
                    Utils.downloadProfile(
                        requireContext(), profileURL!!, username, homeViewModel.pasteLoadUrl
                    )

                } else homeViewModel.showSnackBar("Download URL Not Available")
            }

            binding.copyResultJson -> {
                Helper.URLCopy(binding.apiResultJson.text.toString())
            }
        }

    }


}

class HomeViewModel(application: Application) : BaseViewModel(application) {

    val TAG = "ViewModelHome"
    private var _errorUrl: String? = null

    private var _pasteLoadUrl: String? = null
    var pasteLoadUrl: String?
        get() = _pasteLoadUrl
        set(value) {
            _pasteLoadUrl = value
        }

    private var _alreadyLoaded: String? = null
    var alreadyLoaded: String?
        get() = _alreadyLoaded
        set(value) {
            _alreadyLoaded = value
        }


    fun callApiResult(urlDownload: String) {
        Log.d(TAG, "callApiResult: $urlDownload")
        val context = getApplication<MyApplication>().applicationContext
        val urlWithoutQP = getUrlWithoutParameters(urlDownload)

        if (urlWithoutQP.isEmpty()) {
            showSnackBar(context.resources.getString(R.string.enter_valid_url))
        } else {
            try {
                if (urlWithoutQP.length > 24) {
                    val authority = URI(urlDownload).rawAuthority
                    Log.d(TAG, "authority : $authority")
                    if ("instagram.com" == authority) {
                        if (urlWithoutQP.length >= 30 && !urlWithoutQP.contains("/stories/")) {
                            _errorUrl = ApiHelper.profileByUsername(urlWithoutQP.substring(22))
                            Log.d(TAG, "callApiResult: $_errorUrl")
                            setLoading(true)
                            ApiHelper.callResult(
                                instaObserver, _errorUrl!!, SharedPref.orgCookies()!!
                            )
                        }
                    } else if ("www.instagram.com" == authority) {
                        if (urlWithoutQP.length >= 30 && !urlWithoutQP.contains("/tv/") && !urlWithoutQP.contains(
                                "/stories/"
                            ) && !urlWithoutQP.contains("/reel/") && !urlWithoutQP.contains(
                                "/reels/"
                            ) && !urlWithoutQP.contains(
                                "/tv/"
                            ) && !urlWithoutQP.contains("/p/")
                        ) {
                            val username = urlWithoutQP.substring(26).replace("/$".toRegex(), "")
                            Log.d(TAG, "callApiResult: $username")
                            setLoading(true)
                            ApiHelper.callResult(
                                instaObserver,
                                ApiHelper.profileByUsername(username),
                                SharedPref.orgCookies()!!
                            )
                        }
                    }
                }
            } catch (ignored: Exception) {
                setLoading(false)
                Log.d(TAG, "callApiResult: Exception $ignored")
                _profileOther.value = Result.Error(ignored)
            }
        }
        _alreadyLoaded = _pasteLoadUrl
    }

    private val instaObserver: DisposableObserver<ResponseModel> =
        object : DisposableObserver<ResponseModel>() {
            override fun onNext(response: ResponseModel) {
                try {
//                    Helper.URLCopy(Gson().toJson(response))
                    if (response.graphql?.user != null) {
                        val user = response.graphql.user

                        val userInfoForSingleStoryDownload = UserInfoForSingleStoryDownload(user)
                        _profileOther.value = Result.Success(userInfoForSingleStoryDownload)

                        callProfileUser(
                            ApiHelper.profileByUserId(
                                user.id
                            ), SharedPref.orgCookies()!!
                        )
                    } else if (response.data != null) {
                        //  Log.d("TAG", "response.getGraphql().user != null");
                        val user = response.data.user!!

                        val userInfoForSingleStoryDownload = UserInfoForSingleStoryDownload(user)
                        _profileOther.value = Result.Success(userInfoForSingleStoryDownload)

                        callProfileUser(
                            ApiHelper.profileByUserId(
                                user.id
                            ), SharedPref.orgCookies()!!
                        )
                    } else {
                        setLoading(false)
                    }

                    Log.d(TAG, "onNext: instaObserver")
                } catch (e: Exception) {
                    _profileOther.value = Result.Error(e)
                }
            }

            override fun onError(e: Throwable) {
                _profileOther.value = Result.Error(e)
                Log.d(TAG, "onError: instaObserver ${e.message}")
                if (_errorUrl != null) {
                    ApiHelper.callResult(
                        this, _errorUrl!!, SharedPref.orgCookies()!!
                    )
                    _errorUrl = null
                } else {
                    if (SharedPref.cookies != null) {
                        showSnackBar(
                            getApplication<MyApplication>().applicationContext.resources.getString(
                                R.string.something_wrong
                            )
                        )
                    } else {
                        setLoading(false)
                    }
                }


            }

            override fun onComplete() {

            }
        }


    fun callProfileUser(url: String, cookies: String) {
        ApiHelper.getStoryUserIdForDownload(
            aboutUser, url, cookies
        )
    }

    private val _profileOther = MutableLiveData<Result<UserInfoForSingleStoryDownload?>>()
    val profileOther: LiveData<Result<UserInfoForSingleStoryDownload?>> get() = _profileOther

    private val aboutUser: DisposableObserver<UserInfoForSingleStoryDownload> =
        object : DisposableObserver<UserInfoForSingleStoryDownload>() {
            override fun onNext(t: UserInfoForSingleStoryDownload) {
                if (t.user != null) _profileOther.value = Result.Success(t)
                Log.d(TAG, "onNext: AboutUser")
            }

            override fun onError(e: Throwable) {
                _profileOther.value = Result.Error(e)
                Log.d(TAG, "onError: AboutUser ${e.message}")
                if (SharedPref.cookies != null) {
                    showSnackBar(
                        getApplication<MyApplication>().applicationContext.resources.getString(R.string.something_wrong)
                    )
                } else {
                    setLoading(false)
                }


            }

            override fun onComplete() {

            }
        }

}