package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.lifecycle.Observer
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
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.BoardSet
import com.rendrapcx.tts.constant.Const.Companion.boardSet
import com.rendrapcx.tts.constant.Const.Companion.currentLevel
import com.rendrapcx.tts.constant.Const.Companion.dbApp
import com.rendrapcx.tts.constant.Const.Companion.dbRefQuestions
import com.rendrapcx.tts.constant.Const.Companion.isEditor
import com.rendrapcx.tts.constant.Const.Companion.isEnableClick
import com.rendrapcx.tts.constant.Const.Companion.koinUser
import com.rendrapcx.tts.constant.Const.Companion.lastAcak
import com.rendrapcx.tts.constant.Const.Companion.listProgress
import com.rendrapcx.tts.constant.Const.Companion.listSelesai
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.databinding.DialogMenuOnlineBinding
import com.rendrapcx.tts.databinding.DialogMenuPlayBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.MPlayer
import com.rendrapcx.tts.helper.MyState
import com.rendrapcx.tts.helper.NetworkStatusTracker
import com.rendrapcx.tts.helper.NetworkStatusViewModel
import com.rendrapcx.tts.helper.Progress
import com.rendrapcx.tts.helper.Sora
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listOnlineList
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Base64


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
    var qrShare = mutableListOf<Data.QRShare>()
    var qrListLevel = mutableListOf<Data.Level>()
    var qrListQuestion = mutableListOf<Data.Question>()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        viewModelNet.state.observe(this, Observer {
            when (it) {
                MyState.Fetched -> {
                    binding.btnOnline.visibility = View.VISIBLE
                }

                MyState.Error -> {
                    binding.btnOnline.visibility = View.INVISIBLE
                }
            }
        })

        loadBannerAds()

        isEnableClick = true

        binding.btnGoListQuestion.visibility = View.INVISIBLE
        binding.btnOnline.visibility = View.INVISIBLE


        lifecycleScope.launch {
            /*INIT DATABASE*/
            val job = async {
                try {
                    val isEmpty =
                        DB.getInstance(applicationContext)
                            .userPreferences().getAllUserPreferences().isEmpty()

                    if (isEmpty) UserRef().writeDefaultPreferences(applicationContext, lifecycle)

                    userPreferences =
                        DB.getInstance(applicationContext.applicationContext).userPreferences()
                            .getAllUserPreferences()

                } catch (e: Exception) {
                    UserRef().writeDefaultPreferences(applicationContext, lifecycle)
                } finally {
                    userPreferences =
                        DB.getInstance(applicationContext.applicationContext).userPreferences()
                            .getAllUserPreferences()
                }
            }
            job.await()
            val job1 = async {
                listSelesai = Progress().getUserSelesai(applicationContext, lifecycle)
                listProgress = Progress().getUserProgress(applicationContext, lifecycle)
                isEditor = UserRef().getIsEditor()
                koinUser = UserRef().getKoin()
                lastAcak = UserRef().getLastAcak()
            }

            job1.await()

            getDataLevel()

            animLogo()

            initEditorMenu()

            loadOnlineList()
            loadKoin()
            loadLastAcak()
        }

        binding.apply {

            btnOnline.setOnClickListener {
                if (isEnableClick) {
                    onlineMenu()
                    isEnableClick = false
                }
            }

            btnTrophy.setOnClickListener {
                if (isEnableClick) {
                    Dialog().aboutDialog(this@MainActivity)
                    isEnableClick = false
                }
            }

            btnGoListQuestion.setOnClickListener {
                val i = Intent(this@MainActivity, QuestionActivity::class.java)
                startActivity(i)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            btnSettingMain.setOnClickListener {
                if (isEnableClick) {
                    Dialog().apply { settingDialog(this@MainActivity, lifecycle) }
                    isEnableClick = false
                    UserRef().getIsEditor()
                }
            }

            btnGoTTS.setOnClickListener {
                if (isEnableClick) {
                    getDataLevel()
                    playMenuTTSDialog()
                    isEnableClick = false
                }
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
                if (listSelesai.isEmpty()) {
                    Dialog().showDialog(
                        this@MainActivity,
                        "Belum ada soal yang Anda selesaikan,\n" +
                                "Silakan bermain dan selesaikan beberapa soal terlebih dahulu, " +
                                "untuk bisa memainkan secara acak"
                    )
                    return@setOnClickListener
                }

                boardSet = BoardSet.PLAY_RANDOM
                val intent = Intent(this@MainActivity, BoardActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            btnGoScan.setOnClickListener {
                val intent = Intent(this@MainActivity, BarcodeActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    /*NANTI GANTI LOAD FROM DB*/
    private fun loadKoin() {
        binding.componentKoin.tvKoin.text = koinUser.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun loadLastAcak() {
        if (lastAcak.isEmpty()) binding.btnGoAcak.text = "Acak"
        else binding.btnGoAcak.text = "Lanjutkan"
    }

    /* ADMOB BANNER*/
    private fun loadBannerAds() {
        MobileAds.initialize(this@MainActivity) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {}
            override fun onAdClosed() {}
            override fun onAdFailedToLoad(adError: LoadAdError) {}
            override fun onAdImpression() {}
            override fun onAdLoaded() {}
            override fun onAdOpened() {}
        }
    }

    private fun initEditorMenu() {
        if (isEditor) binding.btnGoListQuestion.visibility = View.VISIBLE
        else binding.btnGoListQuestion.visibility = View.INVISIBLE
    }

    private fun animLogo() {
        YoYo.with(Techniques.Tada).duration(1000)
            .onEnd {
                MPlayer().sound(this, Sora.START_APP)
                YoYo.with(Techniques.RubberBand).duration(2000).playOn(binding.imgLogo)
            }
            .playOn(binding.imgLogo)
    }

    private fun loadOnlineList() {
        lifecycleScope.launch {
            val database = Firebase.database(dbApp)
            val myRef = database.getReference(dbRefQuestions)
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listOnlineList.clear()
                    for (item in dataSnapshot.children) {
                        item.getValue(Data.OnlineLevelList::class.java)
                            ?.let { data ->
                                listOnlineList.add(data)
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        applicationContext,
                        databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun saveOnlineData() {
        lifecycleScope.launch {
            val id = qrListLevel[0].id
            var levelId = ""
            val data = DB.getInstance(applicationContext).level().getAllLevel()
            val ids = data.map { it.id }
            val newId: Boolean

            if (id in ids) {
                newId = true
                levelId = Helper().generateLevelId(ids.size)
                Toast.makeText(
                    this@MainActivity,
                    "Soal disimpan dengan ID Baru",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                newId = false
                levelId = id
                Toast.makeText(this@MainActivity, "Soal disimpan", Toast.LENGTH_SHORT).show()
            }

            DB.getInstance(applicationContext).level().insertLevel(
                Data.Level(
                    id = levelId,
                    category = qrListLevel[0].category,
                    title = qrListLevel[0].title,
                    userId = qrListLevel[0].userId,
                    status = Const.FilterStatus.POST
                )
            )
            //Add Questioner
            qrListQuestion.filter { it.levelId == id }.map { it }.forEach {
                DB.getInstance(applicationContext).question().insertQuestion(
                    Data.Question(
                        levelId = levelId,
                        id = if (newId) "${levelId}-${it.direction}-${Helper().formatQuestionId(it.number + 1)}" else it.id,
                        number = it.number,
                        direction = it.direction,
                        asking = it.asking,
                        answer = it.answer,
                        slot = it.slot
                    )
                )
            }

            getDataLevel()
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun onlineMenu() {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogMenuOnlineBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogBottomAnim
        dialog.window!!.attributes.gravity = Gravity.BOTTOM
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        YoYo.with(Techniques.FlipInY)
            .onEnd { isEnableClick = true }
            .playOn(binding.editCariKategori)

        val adapter = OnlineAdapter()

        if (listOnlineList.isEmpty()) {
            binding.loading.root.visibility = View.VISIBLE
            binding.loading.tvLoadingInfo.text = "Loading..."
            loadOnlineList()
        } else {
            binding.loading.root.visibility = View.INVISIBLE
        }

        fun filter(str: String) {
            if (listOnlineList.isEmpty()) return
            val listLevelFilter = listOnlineList
            val result = listLevelFilter.filter {
                it.category!!.lowercase().contains(str.lowercase()) ||
                        it.id!!.lowercase().contains(str.lowercase()) ||
                        it.editor!!.lowercase().contains(str.lowercase())
            }.toMutableList()


            if (result.isEmpty()) {
                binding.apply {
                    rcViewOnline.layoutManager = LinearLayoutManager(this@MainActivity)
                    rcViewOnline.adapter = adapter
                    adapter.setListItem(listOnlineList)
                }
            } else {
                binding.apply {
                    rcViewOnline.layoutManager = LinearLayoutManager(this@MainActivity)
                    rcViewOnline.adapter = adapter
                    adapter.setListItem(result)
                }
            }
        }

        binding.apply {
            rcViewOnline.layoutManager = LinearLayoutManager(this@MainActivity)
            rcViewOnline.adapter = adapter
            adapter.setListItem(listOnlineList)

            binding.editCariKategori.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {
                    filter(s.toString())
                }
            })

            adapter.setOnClickDownload {
                binding.loading.root.visibility = View.VISIBLE
                binding.loading.tvLoadingInfo.text = "Downloading..."
                lifecycleScope.launch(Dispatchers.IO) {
                    val database = Firebase.database(dbApp)
                    val refQuestion = database.getReference(dbRefQuestions)
                        .child(it.id.toString()).child("encodeString")

                    refQuestion.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val value = dataSnapshot.getValue<String>().toString()
                            val data = String(Base64.getDecoder().decode(value))
                            qrShare.clear()
                            qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(data)
                            qrListLevel = qrShare[0].level
                            qrListQuestion = qrShare[0].question

                            saveOnlineData()

                            binding.loading.root.visibility = View.INVISIBLE
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(
                                applicationContext, databaseError.message, Toast.LENGTH_SHORT
                            ).show()
                            binding.loading.root.visibility = View.INVISIBLE
                            dialog.dismiss()
                        }
                    })
                }
            }

            adapter.setOnClickRemove { eta ->
                binding.loading.root.visibility = View.VISIBLE
                binding.loading.tvLoadingInfo.text = "Erasing..."

                lifecycleScope.launch {
                    val job = async {
                        val database = Firebase.database(dbApp)
                        val myRef = database.getReference(dbRefQuestions)
                        myRef.child(eta.id.toString()).removeValue() //.setValue(null)
                            .addOnCompleteListener() {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Removed Completed",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                binding.loading.root.visibility = View.INVISIBLE
                            }
                            .addOnFailureListener() {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Failed to Remove",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                binding.loading.root.visibility = View.INVISIBLE
                            }
                    }
                    job.await()

                    val job1 = async {
                        loadOnlineList()
                    }
                    job1.await()

                    rcViewOnline.layoutManager = LinearLayoutManager(this@MainActivity)
                    rcViewOnline.adapter = adapter
                    adapter.setListItem(listOnlineList)
                }
            }
        }

        dialog.show()
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
            isEnableClick = true
            binding.apply {
                val filteredListLevel = listLevel
                    .filter { it.category == category && it.status == Const.FilterStatus.POST }
                    .toMutableList()

                val pmtAdapter = PlayMenuTitleAdapter()
                myRecView.layoutManager = GridLayoutManager(this@MainActivity, 3)
                myRecView.adapter = pmtAdapter
                pmtAdapter.setListItem(filteredListLevel)

                pmtAdapter.setOnClickView {
                    lifecycle.coroutineScope.launch {
                        boardSet = BoardSet.PLAY_KATEGORI
                        currentLevel = it.id

                        if (currentLevel in listSelesai) {
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
                            finish()
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            dialog.dismiss()
                        }
                    }

                }
            }
        }

        fun showListByCategory() {
            binding.apply {
                val pmAdapter = PlayMenuAdapter()
                myRecView.layoutManager = LinearLayoutManager(this@MainActivity)
                myRecView.adapter = pmAdapter
                pmAdapter.setListItem(listLevel.distinctBy { it.category }.sortedBy { it.category }
                    .toMutableList())

                pmAdapter.setOnClickView {
                    changeListFiltered(it.category)
                    binding.tvPlayMenuHeader.text = it.category
                    Const.currentCategory = it.category
                }
            }
        }

        //Init Dialog Komponen
        binding.tvPlayMenuHeader.text = "Pilih Kategori"
        YoYo.with(Techniques.Landing)
            .onEnd { isEnableClick = true }
            .playOn(binding.tvPlayMenuHeader)
        showListByCategory()
        //Actions
        binding.btnBackToCategoryAdapter.setOnClickListener {
            if (binding.tvPlayMenuHeader.text == "Pilih Kategori") {
                dialog.dismiss()
            }
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