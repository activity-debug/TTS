package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentUser
import com.rendrapcx.tts.constant.Const.Companion.currentUserId
import com.rendrapcx.tts.constant.Const.Companion.isSignedIn
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
import com.rendrapcx.tts.helper.Sound
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listPartial
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import com.rendrapcx.tts.model.Data.Companion.listUser
import com.rendrapcx.tts.model.Data.Companion.userPreferences
import com.rendrapcx.tts.ui.trial.TestActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.UUID
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var resultQRDecoded = ""
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


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        loadBannerAds()

        binding.btnScanQRCode.visibility = View.GONE
        binding.btnExitApp.visibility = View.GONE
        binding.btnShop.visibility = View.GONE
        binding.btnTrophy.visibility = View.GONE
        binding.btnUserSecret.visibility = View.GONE
        binding.btnDatabase.visibility = View.GONE
        binding.textView7.visibility = View.GONE


        viewModelNet.state.observe(this) { state ->
            binding.apply {
                when (state) {
                    MyState.Fetched -> textView7.text = "fetch"
                    MyState.Error -> textView7.text = "error"
                }
            }
        }
        binding.textView7.text = "error"

        lifecycleScope.launch {
            val job1 = async {
                UserRef().checkUserPref(this@MainActivity, lifecycle)
                userPreferences =
                    DB.getInstance(applicationContext).userPreferences().getAllUserPreferences()
                listUser = DB.getInstance(applicationContext).user().getAllUser()
                currentUser = UserRef().getCurrentUser()
            }
            job1.await()

            loadCurrentUser()

            getDataLevel() //init for playMenuTTS

            animLogo()

        }
        binding.apply {
            btnGoListQuestion.setOnClickListener() {
                val i = Intent(this@MainActivity, QuestionActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            btnSettingMain.setOnClickListener() {
                Dialog().apply { settingDialog(this@MainActivity, lifecycle) }
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

            btnGoTBK.setOnClickListener() {
                Toast.makeText(this@MainActivity, "Belum tersedia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGoWiw.setOnClickListener() {
                Toast.makeText(this@MainActivity, "Belum tersedia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

                val intent = Intent(this@MainActivity, TestActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            btnExitApp.setOnClickListener() {
                exitDialog()
                loadBannerAds()
            }
        }
    }

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
                Toast.makeText(this@MainActivity, "${adError.message}", Toast.LENGTH_SHORT).show()
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

                val filteredList =
                    listLevel.sortedBy { it.title }
                        .filter { it.category == category && it.status == Const.FilterStatus.POST }
                        .toMutableList()
                val adapter = PlayMenuTitleAdapter()
                myRecView.layoutManager = GridLayoutManager(this@MainActivity, 3)
                myRecView.adapter = adapter
                adapter.setListItem(filteredList)

                adapter.setOnClickView {
                    lifecycle.coroutineScope.launch {
                        Const.boardSet = Const.BoardSet.PLAY_USER
                        Const.currentLevel = it.id

                        listLevel =
                            DB.getInstance(applicationContext).level().getLevel(Const.currentLevel)
                        listQuestion =
                            DB.getInstance(applicationContext).question()
                                .getQuestion(Const.currentLevel)
                        listPartial = DB.getInstance(applicationContext).partial().getPartial(
                            Const.currentLevel
                        )

                        val i = Intent(this@MainActivity, BoardActivity::class.java)
                        startActivity(i)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        dialog.dismiss()
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
        binding.tvPlayMenuHeader.text = "Select Category"
        showListByCategory()

        //Actions
        binding.btnBackToCategoryAdapter.setOnClickListener() {
            binding.tvPlayMenuHeader.text = "Select Category"
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

        bind.btnSignOut.setOnClickListener() {
            isSignedIn = false
            currentUserId = ""
            binding.btnLogin.text = "Login"
            loginDialog()
            dialog.dismiss()
        }

        bind.btnCancelSignOut.setOnClickListener() {
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

                val index = listUser.indexOfFirst { it.username == username && it.password == password  }
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

    private fun getQRContent(file: File): String {
        val inputStream: InputStream = BufferedInputStream(FileInputStream(file))
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return decodeQRImage(bitmap)!!
    }

    private fun decodeQRImage(bMap: Bitmap): String? {
        var contents: String? = null
        val intArray = IntArray(bMap.width * bMap.height)
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource = RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        try {
            val result = reader.decode(bitmap)
            contents = result.text

        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding qr code", e)
            Toast.makeText(
                this,
                "Error decoding QR Code, Mohon pilih gambar QR Code yang benar!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return contents
    }

    private val resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data!!.data!!
                //binding.imgCoder.setImageURI(imageUri)

                val imagePath = convertMediaUriToPath(imageUri)
                val imgFile = File(imagePath)
                //binding.textResultContent.text = getQRContent(imgFile)
                resultQRDecoded = getQRContent(imgFile)
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
            }
        }

    private fun openAlbums() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultLauncherGallery.launch(galleryIntent)
    }

    private fun convertMediaUriToPath(uri: Uri): String {
        val proj = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

}