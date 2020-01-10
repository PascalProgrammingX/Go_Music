package com.pca.gomusic.Activities


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.SearchEvent
import com.facebook.FacebookSdk
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.perf.metrics.AddTrace
import com.pca.gomusic.Adapters.SongAdapter
import com.pca.gomusic.Adapters.ViewPagerAdapter
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.Fragments.SongsListFragment
import com.pca.gomusic.Handlers.PlayerHandler.Companion.mediaPlayer
import com.pca.gomusic.Handlers.UserPreferenceHandler
import com.pca.gomusic.ModelClass.Song
import com.pca.gomusic.R
import com.pca.gomusic.Services.PlayerService
import com.pca.gomusic.Services.PlayerService.Companion.ACTION_NEXT_SONG
import com.pca.gomusic.Services.PlayerService.Companion.ACTION_PAUSE_SONG
import com.pca.gomusic.Services.PlayerService.Companion.ACTION_PREV_SONG
import com.pca.gomusic.Services.PlayerService.Companion.ACTION_REPEAT
import com.pca.gomusic.ui.ZoomOutPageTransformer
import com.pca.gomusic.utils.Utils
import de.hdodenhof.circleimageview.CircleImageView
import io.fabric.sdk.android.services.common.Crash
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.content_main_frame.*
import kotlinx.android.synthetic.main.nav_header.*
import java.util.*


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var mAlertDialog: AlertDialog? = null
    private var behavior: BottomSheetBehavior<*>? = null
    private var running: Boolean = false
    private var noSongPlaying = true
    private var songDisplayName: String? = null
    private var duration: Long = 0
    private var albumID: Long = 0
    private var artist: String? = null
    private var dialog: ShareDialog? = null
    private var content: ShareLinkContent? = null
    private var userPreferenceHandler: UserPreferenceHandler? = null
    private lateinit var rewardedAd: RewardedAd
    private lateinit var callback: RewardedAdCallback
    private lateinit var filter: IntentFilter

    private lateinit var auth: FirebaseAuth
    private lateinit var mCredentialsClient: CredentialsClient
    private lateinit var mCredentialRequest: CredentialRequest

    private lateinit var profile:CircleImageView
    private lateinit var siginTextView: AppCompatTextView

    private lateinit var appUpdateManager: AppUpdateManager

    private val songInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            artist = intent.getStringExtra("_art0")
            songDisplayName = intent.getStringExtra("songName")
            albumID = intent.getLongExtra("albumId", 0)
            duration = intent.getLongExtra("duration", 0)
            running = intent.getBooleanExtra("running", false)

            when (Objects.requireNonNull<String>(action)) {
                SongAdapter.ACTION_ID_RECIEVED -> {

                    if(bottom.visibility != View.VISIBLE){
                        bottom.visibility = View.VISIBLE
                    }

                    noSongPlaying = false
                    songTitle.text = songDisplayName
                    songTime.text = Song().convertingDuration(duration)
                    if (artist == "<unknown>") {
                        artistName.setText(R.string.unkown_artist)
                    } else {
                        artistName.text = artist
                    }
                    seekbar?.max = duration.toInt()
                    val seekBarHandler = Handler(Looper.getMainLooper())
                    seekBarHandler.post(object : Runnable {
                        override fun run() {
                            runOnUiThread {
                                timer.text = Song().convertingDuration(mediaPlayer!!.currentPosition.toLong())
                            }
                            seekBarHandler.postDelayed(this, 1000)
                        }
                    })

                    Glide.with(context).load(Utils.getAlbumArtUri(albumID)).placeholder(R.drawable.ic).into(songArtBig)
                    Glide.with(context).load(Utils.getAlbumArtUri(albumID)).placeholder(R.drawable._art000).into(songArt)
                    if (running) {
                        play_pause!!.setBackgroundResource(R.drawable.ic_pause_accent)
                    } else {
                        play_pause!!.setBackgroundResource(R.drawable.ic_play_accent)
                    }

                    if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        if (running) {
                            play_bottom!!.setBackgroundResource(R.drawable.ic_pause_accent)
                        } else {
                            play_bottom!!.setBackgroundResource(R.drawable.ic_play_accent)
                        }
                    } else {
                        play_bottom!!.visibility = View.GONE
                    }
                }

                ACTION_CHANGE_BUTTON -> if (running) {
                    play_pause!!.setBackgroundResource(R.drawable.ic_pause_accent)
                    play_bottom!!.setBackgroundResource(R.drawable.ic_pause_accent)
                } else {
                    play_pause!!.setBackgroundResource(R.drawable.ic_play_accent)
                    play_bottom!!.setBackgroundResource(R.drawable.ic_play_accent)
                }

                IS_SHUFFLING -> {
                    val isShuffling = intent.getBooleanExtra("isShuffling", false)
                    if (isShuffling) {
                        shuffle!!.setBackgroundResource(R.drawable.ic_shuffle)
                    } else {
                        shuffle!!.setBackgroundResource(R.drawable.ic_shuffle_off)
                    }
                }

                SIGNED_IN ->{
                    sign_text.visibility = View.GONE
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                    val pUrl = sharedPref.getString(Constant.PROFILE_URL, "")
                    Glide.with(context).load(pUrl).placeholder(R.drawable.ic_profile).into(profile)
                    val email = sharedPref.getString(Constant.USER_EMAIL, resources.getString(R.string.sign_in))
                    siginTextView.text = email
                }
            }
        }
    }


    private val isAppInstalled: Boolean
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        get() {
            val pm = packageManager
            val installed: Boolean
            installed = try {
                pm.getPackageInfo("com.facebook.katana",
                        PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            return installed
        }



    override fun onStart() {
        super.onStart()
        val playerIntent = Intent(this, PlayerService::class.java)
        startService(playerIntent)
        isRepeatEnabled()
        isShuffleEnabled()
    }

    private val emojiByUnicode: String
        get() = String(Character.toChars(0x1F3A7))



    //Loading Rewarded ads
    private fun createAndLoadRewardedAd(): RewardedAd {
         rewardedAd = RewardedAd(this, resources.getString(R.string.video_ad))
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                rewardedAd.show(this@MainActivity, callback)
            }
            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                onRewardedAdClosedOrFailed()
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        return rewardedAd
    }
    fun onRewardedAdClosedOrFailed() {
        this.rewardedAd = createAndLoadRewardedAd()
    }


    private fun initViews() {
        behavior = BottomSheetBehavior.from(bottom as View)

        val navHeaderView = nav_view.inflateHeaderView(R.layout.nav_header)
        profile = navHeaderView.findViewById(R.id.profile_img)
        siginTextView = navHeaderView.findViewById(R.id.sign_text)

        toolbar.title = "Enjoy it!"
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)

        filter = IntentFilter()
        filter.addAction(ACTION_RECIEVE_SONG)
        filter.addAction(ACTION_CHANGE_BUTTON)
        filter.addAction(SongAdapter.ACTION_ID_RECIEVED)
        filter.addAction(IS_SHUFFLING)
        filter.addAction(SIGNED_IN)
        registerReceiver(songInfoReceiver, filter)
        userPreferenceHandler = UserPreferenceHandler(this)

        auth = FirebaseAuth.getInstance()
        mCredentialsClient = Credentials.getClient(this)

        mCredentialRequest = CredentialRequest.Builder()
    .setPasswordLoginSupported(true)
    .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.TWITTER)
    .build()

    }


    /* override fun onDestroy() {
         super.onDestroy()
         Log.d("debugger", "State: OnDestroy")
         unregisterReceiver(songInfoReceiver)
     }*/


    @AddTrace(name = "onCreateTrace", enabled = true /* optional */)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val currentTheme = sharedPref.getString(Constant.THEME, "default")
        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> {
                setTheme(R.style.AppTheme)
            }
        }


        MobileAds.initialize(this, resources.getString(R.string.home_interstial))
        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(this)
        // Hides statusBar
        /* requestWindowFeature(Window.FEATURE_NO_TITLE)
         window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
 */
        setContentView(R.layout.activity_main)
        initViews()

        when{
            currentTheme.equals("night") -> {
                toolbar.setTitleTextColor(resources.getColor(R.color.colorAccent))
                next.setBackgroundResource(R.drawable.ic_next_white)
                previous.setBackgroundResource(R.drawable.ic_previous_white)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white)
            }
            else ->{
                toolbar.setTitleTextColor(resources.getColor(R.color.black))
                next.setBackgroundResource(R.drawable.ic_next_black)
                previous.setBackgroundResource(R.drawable.ic_previous_black)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black)
            }
        }


        //Load rewarded ads
        createAndLoadRewardedAd()
        checkUpdate()

        Handler().post {
            val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this)
            viewPager.setPageTransformer(true, ZoomOutPageTransformer())
            viewPager!!.offscreenPageLimit = 4
            viewPager!!.isSaveEnabled = true
            viewPager!!.adapter = viewPagerAdapter
        }
        tabLayout!!.setUnboundedRipple(true)
        tabLayout!!.setupWithViewPager(viewPager, false)



        val toggle = ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_closed)
        toggle.syncState()
        drawer.addDrawerListener(toggle)


        toolbar.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        val pUrl = sharedPref.getString(Constant.PROFILE_URL,"")
        val email = sharedPref.getString(Constant.USER_EMAIL, resources.getString(R.string.sign_in))
        Glide.with(this).load(Uri.parse(pUrl)).placeholder(R.drawable.ic_profile).into(profile)
        siginTextView.text = email
        nav_view.setNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.setting -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    drawer.closeDrawers()
                }
                R.id.feedback ->{
                    startActivity(Intent(this, FeedbackActivity::class.java))
                    drawer.closeDrawers()
                }
            }
            true
        }



        if (auth.currentUser == null){
            mCredentialsClient.request(mCredentialRequest).addOnCompleteListener(
                    object: OnCompleteListener<CredentialRequestResponse> {
                        override fun onComplete(@NonNull task: Task<CredentialRequestResponse>) {
                            if (task.isSuccessful) {
                                // See "Handle successful credential requests"
                                onCredentialRetrieved(task.result!!.credential)
                                return
                            }
                        }
                    })
        }


        profile.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser!= null){
                showMenu(profile)
            }else{
                startActivity(Intent(this, SignInActivity::class.java))
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_from_left)
            }
        }


        bottom.setOnClickListener {
            if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }


        seekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, position: Int, b: Boolean) {
                val seekIntent = Intent()
                seekIntent.action = PlayerService.ACTION_SEEK_SONG
                seekIntent.putExtra("seek", position)
                sendBroadcast(seekIntent)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })




        behavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SetTextI18n")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    songArt.visibility = View.VISIBLE
                    play_bottom.visibility = View.VISIBLE
                    if (running) {
                        play_bottom!!.setBackgroundResource(R.drawable.ic_pause_accent)
                    } else {
                        play_bottom!!.setBackgroundResource(R.drawable.ic_play_accent)
                    }
                } else {
                    play_bottom.visibility = View.GONE
                    songArt.visibility = View.GONE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })


        next!!.setOnClickListener {
            if (!noSongPlaying) {
                val nextIntente = Intent()
                nextIntente.action = ACTION_NEXT_SONG
                sendBroadcast(nextIntente)
            }
        }


        previous!!.setOnClickListener {
            if (!noSongPlaying) {
                val previous = Intent()
                previous.action = ACTION_PREV_SONG
                sendBroadcast(previous)
            }
        }


        play_pause!!.setOnClickListener {
            if (!noSongPlaying) {
                if (running) {
                    play_pause!!.setBackgroundResource(R.drawable.ic_play_accent)
                    setPlayPause()
                } else {
                    play_pause!!.setBackgroundResource(R.drawable.ic_pause_accent)
                    setPlayPause()
                }
            }

        }


        repeat!!.setOnClickListener {
            if (userPreferenceHandler!!.isRepeatOneEnabled) {
                repeat!!.setBackgroundResource(R.drawable.ic_repeat_off)
                userPreferenceHandler!!.setRepeatOneEnable(false)
                val repeatIntent = Intent()
                repeatIntent.action = ACTION_REPEAT
                sendBroadcast(repeatIntent)
            } else {
                repeat!!.setBackgroundResource(R.drawable.ic_repeat_one)
                userPreferenceHandler!!.setRepeatOneEnable(true)
                val repeatIntent = Intent()
                repeatIntent.action = ACTION_REPEAT
                sendBroadcast(repeatIntent)
            }
        }


        play_bottom!!.setOnClickListener {
            if (!noSongPlaying) {
                if (running) {
                    play_bottom!!.setBackgroundResource(R.drawable.ic_play_accent)
                    setPlayPause()
                } else {
                    play_bottom!!.setBackgroundResource(R.drawable.ic_pause_accent)
                    setPlayPause()
                }
            }
        }


        //Let friends know what music you like and your current playlist.
        facebook_share!!.setOnClickListener { FacebookShare() }


        shuffle!!.setOnClickListener {
            // Random play
            if (userPreferenceHandler!!.isShuffleEnabled) {
                userPreferenceHandler!!.isShuffleEnabled = false
                shuffle!!.setBackgroundResource(R.drawable.ic_shuffle_off)
                val shuffleIntent = Intent()
                shuffleIntent.action = PlayerService.SHUFFLE_OFF
                sendBroadcast(shuffleIntent)
            } else {
                userPreferenceHandler!!.isShuffleEnabled = true
                shuffle!!.setBackgroundResource(R.drawable.ic_shuffle)
                val shuffleIntent = Intent()
                shuffleIntent.action = PlayerService.SHURFFLE_ON
                sendBroadcast(shuffleIntent)
            }
        }


    }


    private fun isRepeatEnabled() {
        if (userPreferenceHandler!!.isRepeatOneEnabled) {
            repeat!!.setBackgroundResource(R.drawable.ic_repeat_one)
        } else {
            repeat!!.setBackgroundResource(R.drawable.ic_repeat_off)
        }
    }


    private fun isShuffleEnabled() {
        if (userPreferenceHandler!!.isShuffleEnabled) {
            shuffle!!.setBackgroundResource(R.drawable.ic_shuffle)
        } else {
            shuffle!!.setBackgroundResource(R.drawable.ic_shuffle_off)
        }
    }


    private fun setPlayPause() {
        val playerIntent = Intent()
        playerIntent.action = ACTION_PAUSE_SONG
        sendBroadcast(playerIntent)
    }



    private fun showMenu(v:View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.actions)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when(item?.itemId){
                R.id.signOut ->{
                    auth.signOut()
                    Glide.with(this).load(R.drawable.ic_profile).into(profile)
                    siginTextView.text = resources.getString(R.string.sign_in)
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    val editor = sharedPreferences.edit()
                    editor.remove(Constant.PROFILE_URL)
                    editor.apply()
                }
            }
            true
        }
        popup.show()
    }



    @SuppressLint("ResourceAsColor")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val mSearchView = MenuItemCompat.getActionView(menu.findItem(R.id.search)) as SearchView
        mSearchView.queryHint = "Search a song"
        mSearchView.setIconifiedByDefault(false)
        mSearchView.isIconified = false
        mSearchView.setBackgroundResource(R.color.white)
        mSearchView.clearFocus()
        mSearchView.setOnQueryTextListener(this@MainActivity)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        val intent = Intent()
        intent.action = SongsListFragment.ACTION_SEARCH
        intent.putExtra("search", query)
        sendBroadcast(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Answers.getInstance().logSearch(SearchEvent().putQuery(query))
        }
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }




    private fun onCredentialRetrieved(credential:Credential) {
        val accountType = credential.accountType
         if (accountType == IdentityProviders.GOOGLE) {
            // The user has previously signed in with Google Sign-In. Silently
            // sign in the user with the same ID.
            // See https://developers.google.com/identity/sign-in/android/
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            val signInClient = GoogleSignIn.getClient(this, gso)
            val task = signInClient.silentSignIn()
        }
    }



    /*private fun shareApplication() {
        try {
            var nowPlaying: String? = null
            if (songDisplayName!!.isNotEmpty()) {
                nowPlaying = songDisplayName
            }
            val url = "https://play.google.com/store/apps/details?id=com.pca.gomusic"
            val shareContent = resources.getString(R.string.listening) + "  " + nowPlaying + "\n" + url
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareContent)
            startActivity(Intent.createChooser(intent, resources.getString(R.string.app_link)))
        } catch (e: Exception) {
            Toast.makeText(this, resources.getString(R.string.unsupported), Toast.LENGTH_LONG).show()
        }

    }*/


    override fun onBackPressed() {
        if (behavior?.state == BottomSheetBehavior.STATE_EXPANDED)
            behavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        else if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawers()
        }

        else
            startActivity(Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }





    private fun checkUpdate(){
         appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                val listener = InstallStateUpdatedListener { state->
                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate() } }
                appUpdateManager.registerListener(listener)
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            UPDATE_CODE)
                }catch (e: Exception){ }
                appUpdateManager.unregisterListener(listener)
            }
        }
    }



    private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(bottom,
                "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("RESTART") {appUpdateManager.completeUpdate()}
        snackbar.setActionTextColor(
                resources.getColor(R.color.colorAccent))
        snackbar.show()
    }



    override fun onResume() {
        super.onResume()
        appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                IMMEDIATE,
                                this,
                                UPDATE_CODE)
                    } } }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPDATE_CODE) {
            if (resultCode != RESULT_OK) {
                Crashlytics.getInstance().answers.onException(Crash.LoggedException("Failed to update App"))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }



    private fun FacebookShare() {
        if (isAppInstalled) {
            @SuppressLint("InflateParams") val inflater12 = layoutInflater.inflate(R.layout.facebook_share_dialog, null)
            val builder = AlertDialog.Builder(this)
            builder.setView(inflater12)
                    .setCancelable(true)
            mAlertDialog = builder.show()

            val text = inflater12.findViewById<AppCompatEditText>(R.id.text)
            val yeah = inflater12.findViewById<AppCompatButton>(R.id.yeah)
            yeah.setOnClickListener {
                mAlertDialog!!.dismiss()
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null && networkInfo.isConnected) {
                    //Loading Alert Dialog.
                    @SuppressLint("InflateParams") val inflater1 = layoutInflater.inflate(R.layout.loading, null)
                    val builder1 = AlertDialog.Builder(this)
                    builder1.setView(inflater1)
                            .setCancelable(true)
                    mAlertDialog = builder1.show()

                    content = ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://www.youtube.com/results?search_query=" + artist!!))
                            .setQuote(Objects.requireNonNull<Editable>(text.text).toString().trim { it <= ' ' })
                            .setShareHashtag(ShareHashtag.Builder()
                                    .setHashtag("#GOMusic\n " + resources.getString(R.string.listening) + "  " + songDisplayName + " \n"
                                            + resources.getString(R.string.by) + "   " + artist + "   " + emojiByUnicode)
                                    .build())
                            .build()
                    dialog = ShareDialog(this)
                    if (dialog!!.canShow(content, ShareDialog.Mode.AUTOMATIC)) {
                        dialog!!.show(content)
                    }
                    dialog!!.shouldFailOnDataError = true
                    Handler().postDelayed({ mAlertDialog!!.dismiss() }, 8000)
                } else {
                    Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Install facebook to use this feature.", Toast.LENGTH_LONG).show()
            val params = Bundle()
            val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            params.putString("facebook_share", "Facebook not installed")
            firebaseAnalytics.logEvent("facebook_share", params)
        }
    }

    companion object {
        const val ACTION_RECIEVE_SONG = "ACTION_RECIEVE_SONG"
        const val ACTION_CHANGE_BUTTON = "ACTION_CHANGE_BUTTON"
        const val IS_SHUFFLING = "IS_SHUFFLING"
        const val UPDATE_CODE = 112
        const val SIGNED_IN = "signed_in"
    }


}


