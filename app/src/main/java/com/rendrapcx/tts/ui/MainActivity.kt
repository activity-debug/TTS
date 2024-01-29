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
import com.rendrapcx.tts.constant.Const.Companion.isEditor
import com.rendrapcx.tts.constant.Const.Companion.progress
import com.rendrapcx.tts.constant.Const.Companion.selesai
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.databinding.DialogMenuPlayBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.MyState
import com.rendrapcx.tts.helper.NetworkStatusTracker
import com.rendrapcx.tts.helper.NetworkStatusViewModel
import com.rendrapcx.tts.helper.Progress
import com.rendrapcx.tts.helper.Sound
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


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

        binding.btnGoListQuestion.visibility = View.INVISIBLE

        viewModelNet.state.observe(this) { state ->
            binding.apply {
                when (state) {
                    MyState.Fetched -> btnOnline.visibility = View.VISIBLE
                    MyState.Error -> btnOnline.visibility = View.INVISIBLE
                    else -> {}
                }
            }
        }
        binding.btnOnline.visibility = View.INVISIBLE

        lifecycleScope.launch {
            /*INIT DATABASE*/
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

                isEditor = UserRef().getIsEditor()
                selesai = Progress().getUserSelesai(this@MainActivity, lifecycle)
                progress = Progress().getUserProgress(this@MainActivity, lifecycle)
            }
            job1.await()

            getDataLevel() //init for playMenuTTS

            animLogo()

            initEditorMenu()

        }
        binding.apply {

            btnOnline.setOnClickListener {
                Toast.makeText(this@MainActivity, "\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7\nsedang dalam perbaikan", Toast.LENGTH_SHORT).show()
            }

            btnTrophy.setOnClickListener {
                Dialog().aboutDialog(this@MainActivity)
            }

            btnGoListQuestion.setOnClickListener {
                val i = Intent(this@MainActivity, QuestionActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            btnSettingMain.setOnClickListener {
                Dialog().apply { settingDialog(this@MainActivity, lifecycle) }
                UserRef().getIsEditor()
            }

            btnGoTTS.setOnClickListener {
                playMenuTTSDialog()
            }

            btnGoAcak.setOnClickListener {
                if (listLevel.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Belum ada data, silakan scan soal terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (selesai.isEmpty()) {
                    Dialog().showDialog(
                        this@MainActivity,
                        "Belum ada soal yang Anda selesaikan,\n" +
                                "Silakan bermain dan selesaikan beberapa soal terlebih dahulu, " +
                                "untuk bisa memainkan secara acak"
                    )
                    return@setOnClickListener
                }

                boardSet = Const.BoardSet.PLAY_RANDOM
                val intent = Intent(this@MainActivity, BoardActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            btnGoScan.setOnClickListener {
                val intent = Intent(this@MainActivity, BarcodeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }


    private fun initEditorMenu(){
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

                        if (selesai.contains(currentLevel)) {
                            Dialog().apply {
                                showDialogYesNo(
                                    "Info",
                                    "Anda sudah menyelesaikan level ini. \nMau reset ulang level ini?",
                                    "Tidak",
                                    "Ya",
                                )
                            }
                        } else {
                            val i = Intent(this@MainActivity, BoardActivity::class.java)
                            startActivity(i)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
        binding.btnBackToCategoryAdapter.setOnClickListener {
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

    private fun getDataLevel() {
        lifecycleScope.launch {
            listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                .ifEmpty { return@launch }
        }
    }

}