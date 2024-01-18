package com.rendrapcx.tts.ui

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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.currentUserId
import com.rendrapcx.tts.constant.Const.Companion.isSignedIn
import com.rendrapcx.tts.databinding.ActivityMainBinding
import com.rendrapcx.tts.databinding.DialogLoginBinding
import com.rendrapcx.tts.databinding.DialogMenuPlayBinding
import com.rendrapcx.tts.databinding.DialogSignOutBinding
import com.rendrapcx.tts.helper.Dialog
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel
import com.rendrapcx.tts.model.Data.Companion.listPartial
import com.rendrapcx.tts.model.Data.Companion.listQuestion
import com.rendrapcx.tts.model.Data.Companion.listUser
import com.rendrapcx.tts.model.Data.Companion.listUserPreferences
import com.rendrapcx.tts.ui.trial.TestActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private var resultQRDecoded = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Helper().apply { hideSystemUI() }

        loadUserPreferences()

        loadCurrentUser()

        getData()

        animLogo()

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
                playMenuDialog(this@MainActivity, lifecycle)
            }

            btnGoWiw.setOnClickListener() {
                val intent = Intent(this@MainActivity, TestActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            btnGoTBK.setOnClickListener() {
                Toast.makeText(
                    this@MainActivity,
                    "${listUserPreferences[0].integratedKeyboard}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun playMenuDialog(
        context: Context,
        lifecycle: Lifecycle
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogMenuPlayBinding.inflate(inflater)
        val builder = AlertDialog.Builder(context).setView(binding.root)
        val dialog = builder.create()

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogBottomAnim
        dialog.window!!.attributes.gravity = Gravity.BOTTOM
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        fun changeListFiltered(category: String) {
            binding.apply {

                val filteredList =
                    listLevel.sortedBy { it.title }.filter { it.category == category }
                        .toMutableList()
                val adapter = PlayMenuTitleAdapter()
                myRecView.layoutManager = GridLayoutManager(context, 3)
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
                myRecView.layoutManager = LinearLayoutManager(context)
                myRecView.adapter = adapter
                adapter.setListItem(listLevel.distinctBy { it.category }.sortedBy { it.category }
                    .toMutableList())

                adapter.setOnClickView {
                    changeListFiltered(it.category)
                    binding.tvPlayMenuHeader.text = it.category
                }
            }
        }

        binding.tvPlayMenuHeader.text = "Select Category"

        binding.btnBackToCategoryAdapter.setOnClickListener() {
            binding.tvPlayMenuHeader.text = "Select Category"
            showListByCategory()
        }

        showListByCategory()

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

    private fun loadUserPreferences() {
        lifecycleScope.launch {
            val isEmpty =
                DB.getInstance(applicationContext)
                    .userPreferences().getAllUserPreferences().isEmpty()

            if (isEmpty) writeDefaultPreferences()

            listUserPreferences =
                DB.getInstance(applicationContext)
                    .userPreferences().getAllUserPreferences()
        }
    }

    private fun writeDefaultPreferences() {
        lifecycleScope.launch {
            DB.getInstance(applicationContext).userPreferences().insertUserPref(
                Data.UserPreferences(
                    id = "0",
                    isLogin = false,
                    showFinished = false,
                    sortOrderByAuthor = false,
                    integratedKeyboard = false,
                    isMusic = true,
                    isSound = true,
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadCurrentUser() {
        lifecycle.coroutineScope.launch {
            listUser = DB.getInstance(applicationContext).user().getAllUser()
            if (listUser.isEmpty()) {
                loginDialog()
                return@launch
            } else {
                val guest = listUser[0].isGuest
                if (guest) binding.btnLogin.text = "Guest"
                else binding.btnLogin.text = listUser[0].username
                currentUserId = listUser[0].id
                isSignedIn = true
            }
        }
    }

    private fun animLogo() {
        YoYo.with(Techniques.Tada)
            .duration(2000)
            //.repeat(1)
            .playOn(binding.imgLogo);
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun signOutDialog(
    ) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bind = DialogSignOutBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this).setView(bind.root)
        val dialog = builder.create()
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
//        val window = dialog.window

//        val windowInsetsController =
//            WindowCompat.getInsetsController(window!!, window.decorView)
//        windowInsetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
//            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
//                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
//            ) {
//                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//            }
//            view.onApplyWindowInsets(windowInsets)
//        }

        extracted(dialog)

        dialog.window!!.attributes.windowAnimations = R.style.DialogTopAnim
        dialog.window!!.attributes.gravity = Gravity.TOP
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        fun createUserGuest() {
            val id = UUID.randomUUID().toString().substring(0, 10)
            val name = "Guest-$id"
            lifecycle.coroutineScope.launch {
                DB.getInstance(applicationContext).user().insertUser(
                    Data.User(
                        id = id,
                        username = name,
                        password = "secret",
                        isGuest = true
                    )
                )
                delay(1000L)
                loadCurrentUser()
            }
        }

        bind.btnLogin.setOnClickListener() {
            dialog.dismiss()
        }

        bind.btnLoginGuest.setOnClickListener() {
            createUserGuest()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getData() {
        lifecycleScope.launch {
            try {
                listLevel = DB.getInstance(applicationContext).level().getAllLevel()
                    .ifEmpty { return@launch }
            } finally {

            }
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