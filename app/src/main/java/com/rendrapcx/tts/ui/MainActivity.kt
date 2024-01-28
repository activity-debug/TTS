package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.currentUser
import com.rendrapcx.tts.constant.Const.Companion.currentUserId
import com.rendrapcx.tts.constant.Const.Companion.isSignedIn
import com.rendrapcx.tts.constant.Const.Companion.progress
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.databinding.DialogExitAppBinding
import com.rendrapcx.tts.databinding.DialogLoginBinding
import com.rendrapcx.tts.databinding.DialogMenuPlayBinding
import com.rendrapcx.tts.databinding.DialogSignOutBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.MyState
import com.rendrapcx.tts.helper.NetworkStatusTracker
import com.rendrapcx.tts.helper.NetworkStatusViewModel
import com.rendrapcx.tts.helper.Progress
import com.rendrapcx.tts.helper.Sound
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listUser
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModelNet: NetworkStatusViewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val networkStatusTracker = NetworkStatusTracker(this@MainActivity)
                    return NetworkStatusViewModel(networkStatusTracker) as T
                }
            },
        )[NetworkStatusViewModel::class.java]
    }


    private lateinit var mAdView: AdView


    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        loadBannerAds()

        binding.btnExitApp.visibility = View.GONE
        binding.btnShop.visibility = View.GONE
        binding.btnUserSecret.visibility = View.GONE
        binding.btnDatabase.visibility = View.GONE
        binding.textView7.visibility = View.GONE
        binding.btnLogin.visibility = View.INVISIBLE
        binding.btnGoListQuestion.visibility = View.INVISIBLE

        viewModelNet.state.observe(this) { state ->
            binding.apply {
                when (state) {
                    MyState.Fetched -> textView7.text = "fetch"
                    MyState.Error -> textView7.text = "error"
                    else -> {}
                }
            }
        }
        binding.textView7.text = "error"

        lifecycleScope.launch {
            val job = async {
                val isEmpty =
                    DB.getInstance(applicationContext)
                        .userPreferences().getAllUserPreferences().isEmpty()

                if (isEmpty) UserRef().writeDefaultPreferences(applicationContext, lifecycle)

                userPreferences =
                    DB.getInstance(applicationContext.applicationContext).userPreferences()
                        .getAllUserPreferences()
            }
            job.await()
            val job1 = async {

                listUser = DB.getInstance(applicationContext).user().getAllUser()
                currentUser = UserRef().getCurrentUser()

                progress = Progress().getUserProgress(this@MainActivity, lifecycle)
            }
            job1.await()

            /*NANTI AKTIFKAN LAGI*/
            //loadCurrentUser()

            initIsEditor()

            getDataLevel() //init for playMenuTTS

            animLogo()

        }
        binding.apply {

            btnOnline.setOnClickListener() {
                Toast.makeText(this@MainActivity, "${progress}", Toast.LENGTH_SHORT).show()
            }

            btnTrophy.setOnClickListener() {
                Dialog().aboutDialog(this@MainActivity)
            }

            btnGoListQuestion.setOnClickListener() {
                val i = Intent(this@MainActivity, QuestionActivity::class.java)
                startActivity(i)
                //finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            btnSettingMain.setOnClickListener() {
                Dialog().apply { settingDialog(this@MainActivity, lifecycle) }
                UserRef().getIsEditor()
            }

            btnUserSecret.setOnClickListener() {
                Dialog().apply { userProfile(this@MainActivity) }
            }

            btnLogin.setOnClickListener() {
                if (currentUserId.isNotEmpty()) signOutDialog()
                else loginDialog()
            }

            btnGoTTS.setOnClickListener() {
                playMenuTTSDialog()
            }

            /*Play Random*/
            btnGoAcak.setOnClickListener() {
                if (listLevel.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Belum ada data, silakan scan soal terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

//                val count = listLevel.size
//
//                val arr = arrayListOf<Int>()
//                for (i in 0 until count) {
//                    val x = (0 until count).random()
//                    if (!progress.contains(listLevel[x].id)) {
//                        currentLevel = listLevel[x].id
//                        break
//                    }
//                    arr.add(i)
//                }
//                if (arr.size == count) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Silakan selesaikan dulu soal untuk bisa menjalankan secara acak",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }

                if (progress.isEmpty()) {
                    Dialog().showDialog(
                        this@MainActivity,
                        "Belum ada soal yang Anda selesaikan,\n" +
                                "Silakan bermain dan selesaikan beberapa soal terlebih dahulu, " +
                                "untuk bisa memankan secara acak"
                    )
                    return@setOnClickListener
                }

                boardSet = Const.BoardSet.PLAY_RANDOM
                val intent = Intent(this@MainActivity, BoardActivity::class.java)
                startActivity(intent)
                //finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }


            /*BARCODE*/
            btnGoScan.setOnClickListener() {
                val intent = Intent(this@MainActivity, BarcodeActivity::class.java)
                startActivity(intent)
                //finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            btnExitApp.setOnClickListener() {
                exitDialog()
                loadBannerAds()
            }
        }
    }

    private fun initIsEditor() {
        val isEditor = UserRef().getIsEditor()
        if (isEditor) binding.btnGoListQuestion.visibility = View.VISIBLE
        else binding.btnGoListQuestion.visibility = View.INVISIBLE
    }

    /* ADMOB BANNER*/
    private fun loadBannerAds() {
        MobileAds.initialize(this@MainActivity) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Toast.makeText(this@MainActivity, "${adError.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                //Toast.makeText(this@MainActivity, "adLoaded", Toast.LENGTH_SHORT).show()
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }

    }

    private fun animLogo() {
        YoYo.with(Techniques.Tada).duration(1000)
            .onEnd {
                Sound().soundOpeningApp(this@MainActivity)
                YoYo.with(Techniques.RubberBand).duration(2000).playOn(binding.imgLogo)
            }
            .playOn(binding.imgLogo)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun exitDialog() {
        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogExitAppBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.attributes.gravity = Gravity.NO_GRAVITY
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        bind.btnOK.setOnClickListener() {
            exitProcess(-1)
        }

        bind.btnCancel.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }
//    private fun getUserProgress(): ArrayList<String>{
//        val arr = arrayListOf<String>()
//        lifecycleScope.launch {
//            Data.userAnswerTTS = DB.getInstance(applicationContext).userAnswerTTS().getAllUserAnswer()
//            Data.userAnswerTTS.filter { it.status == Const.AnswerStatus.DONE }.forEach {
//                arr.add(it.levelId)
//            }
//        }
//        return arr
//    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun playMenuTTSDialog() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogMenuPlayBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogBottomAnim
        dialog.window!!.attributes.gravity = Gravity.BOTTOM
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        fun changeListFiltered(category: String) {
            binding.apply {
                val filteredListLevel = listLevel
                    .filter { it.category == category && it.status == Const.FilterStatus.POST }
                    .toMutableList()

                val adapter = PlayMenuTitleAdapter()
                myRecView.layoutManager = GridLayoutManager(this@MainActivity, 3)
                myRecView.adapter = adapter
                adapter.setListItem(filteredListLevel)

                adapter.setOnClickView {
                    lifecycle.coroutineScope.launch {
                        boardSet = Const.BoardSet.PLAY_USER
                        currentLevel = it.id

                        if (progress.contains(currentLevel)) {
                            Dialog().apply {
                                showDialogYesNo(
                                    "Info",
                                    "Anda sudah menyelesaikan level ini. \nMau reset ulang level ini?",
                                    "Tidak",
                                    "Ya",
                                    lifecycle
                                )
                            }
                        } else {
                            val i = Intent(this@MainActivity, BoardActivity::class.java)
                            startActivity(i)
                            //finish()
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            dialog.dismiss()
                        }


                    }

                }
            }
        }

        fun showListByCategory() {
            binding.apply {
                val adapter = PlayMenuAdapter()
                myRecView.layoutManager = LinearLayoutManager(this@MainActivity)
                myRecView.adapter = adapter
                adapter.setListItem(listLevel.distinctBy { it.category }.sortedBy { it.category }
                    .toMutableList())

                adapter.setOnClickView {
                    changeListFiltered(it.category)
                    binding.tvPlayMenuHeader.text = it.category
                    Const.currentCategory = it.category
                }
            }
        }

        //Init Dialog Komponen
        binding.tvPlayMenuHeader.text = "Pilih Kategori"
        showListByCategory()

        //Actions
        binding.btnBackToCategoryAdapter.setOnClickListener() {
            binding.tvPlayMenuHeader.text = "Pilih Kategori"
            showListByCategory()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun extracted(dialog: AlertDialog) {
        val window = dialog.window

        val windowInsetsController =
            WindowCompat.getInsetsController(window!!, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadCurrentUser() {
        lifecycle.coroutineScope.launch {
            var isEmpty = true
            val job1 =
                async { isEmpty = DB.getInstance(applicationContext).user().getAllUser().isEmpty() }
            job1.await()
            if (isEmpty) {
                loginDialog()
                return@launch
            } else {
                val load = async {
                    listUser = DB.getInstance(applicationContext).user().getAllUser()
                }
                load.await()

                currentUser = UserRef().getCurrentUser()

                val guest = listUser[currentUser].isGuest
                if (guest) {
                    binding.btnLogin.text = "Guest"
                    binding.btnGoListQuestion.visibility = View.INVISIBLE
                } else {
                    binding.btnLogin.text = listUser[currentUser].username
                    binding.btnGoListQuestion.visibility = View.VISIBLE
                }

                currentUserId = listUser[currentUser].id
                isSignedIn = true
            }
        }
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun signOutDialog(
    ) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogSignOutBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogFadeAnim
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        bind.btnTwo.setOnClickListener() {
            isSignedIn = false
            currentUserId = ""
            binding.btnLogin.text = "Login"
            loginDialog()
            dialog.dismiss()
        }

        bind.btnOne.setOnClickListener() {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun loginDialog(
    ) {
        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogLoginBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(bind.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        fun createUserGuest() {
            val id = UUID.randomUUID().toString().substring(0, 10)
            val username = "Guest-$id"
            lifecycle.coroutineScope.launch {
                val job = async {
                    DB.getInstance(applicationContext).user().insertUser(
                        Data.User(
                            id = id,
                            username = username,
                            password = "secret",
                            isGuest = true
                        )
                    )
                    listUser = DB.getInstance(applicationContext).user().getAllUser()
                }
                job.await()
                currentUser = listUser.indexOfFirst { it.id == id }
                UserRef().setCurrentUser("0", currentUser, this@MainActivity, lifecycle)
                loadCurrentUser()
                dialog.dismiss()
            }
        }

        bind.btnLogin.setOnClickListener() {
            lifecycleScope.launch {
                val username = bind.editUser.text.toString()
                val password = bind.editPassword.text.toString()

                if (username.isEmpty()) {
                    bind.editUser.error = "silakan lengkapi data"
                }
                if (password.isEmpty()) {
                    bind.editPassword.error = "silakan isi password"
                }

                val job = async {
                    listUser = DB.getInstance(applicationContext).user().getAllUser()
                }
                job.await()

                val index =
                    listUser.indexOfFirst { it.username == username && it.password == password }
                if (index == -1) {
                    bind.editPassword.error = "Tidak ada ada"
                } else {
                    currentUser = index
                    UserRef().setCurrentUser("0", currentUser, this@MainActivity, lifecycle)
                    loadCurrentUser()
                    dialog.dismiss()
                }
            }
        }

        bind.btnLoginGuest.setOnClickListener() {
            createUserGuest()
        }

        dialog.show()
    }

    private fun getDataLevel() {
        lifecycleScope.launch {
            listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                .ifEmpty { return@launch }
        }
    }

}