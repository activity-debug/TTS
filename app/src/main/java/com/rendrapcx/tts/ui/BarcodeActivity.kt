package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.Const.Companion.isEditor
import com.rendrapcx.tts.constant.Const.Companion.isEnableClick
import com.rendrapcx.tts.constant.RequestCode
import com.rendrapcx.tts.databinding.ActivityBarcodeBinding
import com.rendrapcx.tts.helper.GoogleMobileAdsConsentManager
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.helper.MPlayer
import com.rendrapcx.tts.helper.Sora
import com.rendrapcx.tts.helper.UserRef
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean


class BarcodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeBinding
    private var myClipboard: ClipboardManager? = null
    //private var myClip: ClipData? = null

    private var counterClearInput = 0

    private var qrShare = mutableListOf<Data.QRShare>()
    private var qrListLevel = mutableListOf<Data.Level>()
    private var qrListQuestion = mutableListOf<Data.Question>()

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val initialLayoutComplete = AtomicBoolean(false)
    private lateinit var adView: AdView
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Helper().apply { hideSystemUI() }

        adView = AdView(this)
        binding.adViewContainer.addView(adView)

        // Log the Mobile Ads SDK version.
        Log.d("JACK", "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(this) { error ->
            if (error != null) {
                // Consent not obtained in current session.
                Log.d("JACK", "${error.errorCode}: ${error.message}")
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds) {
            initializeMobileAdsSdk()
        }

        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete.getAndSet(true) && googleMobileAdsConsentManager.canRequestAds) {
                loadBanner()
            }
        }
        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build()
        )



        myClipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?




        binding.textResultContent.text = ""
        binding.editPaste.setText("")
        binding.editPaste.visibility = View.INVISIBLE
        binding.btnSaveSoal.visibility = View.INVISIBLE
        qrShare.clear()
        qrListLevel.clear()
        qrListQuestion.clear()

        binding.includeHeader.apply {
            tvLabelTop.text = "Scan Data Questioner"
            btnBack.setOnClickListener {
                val i = Intent(this@BarcodeActivity, MainActivity::class.java)
                startActivity(i)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }

            btnSettingPlay.setOnClickListener {
                /*ENABLE EDITOR*/
                counterClearInput++
                if (counterClearInput > 9 && !isEditor) {
                    isEditor = true
                    UserRef().setIsEditor(isEditor, applicationContext, lifecycle)
                    YoYo.with(Techniques.RubberBand).playOn(it)
                    Toast.makeText(
                        this@BarcodeActivity,
                        "Editor Aktif",
                        Toast.LENGTH_SHORT
                    ).show()
                    MPlayer().sound(applicationContext, Sora.SUCCESS)
                }
            }

            btnSettingPlay.setOnLongClickListener {
                /*RESET USER ANSWER*/
                lifecycleScope.launch {
                    isEditor = false
                    UserRef().setIsEditor(isEditor, applicationContext, lifecycle)
                    MPlayer().sound(applicationContext, Sora.SUCCESS)
                }
                return@setOnLongClickListener true
            }
        }

        binding.apply {

            btnDecodeGallery.setOnClickListener {
                openAlbums()
            }

            btnDecodeFromCamera.setOnClickListener {
                openCamera()
            }

            btnSaveSoal.setOnClickListener {
                isEnableClick = false
                saveQRToDB()
                val i = Intent(this@BarcodeActivity, MainActivity::class.java)
                startActivity(i)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }

            btnPasteSoal.setOnClickListener {
                var error = false
                try {
                    val abc = myClipboard?.primaryClip
                    val item = abc?.getItemAt(0)

                    binding.editPaste.setText("")
                    binding.editPaste.setText(item?.text.toString())

                    val decodeString =
                        String(Base64.getDecoder().decode(binding.editPaste.text.toString()))

                    qrShare.clear()

                    qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(decodeString)
                    qrListLevel = qrShare[0].level
                    qrListQuestion = qrShare[0].question

                } catch (e: Exception) {
                    error = true
                    Toast.makeText(this@BarcodeActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    if (error) {
                        Toast.makeText(
                            applicationContext,
                            "Bukan data soal Terka TTS",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.btnSaveSoal.visibility = View.VISIBLE
                        binding.textResultContent.text =
                            "ID: ${qrListLevel[0].id} \n" +
                                    "Category: ${qrListLevel[0].category} \n" +
                                    "Title: ${qrListLevel[0].title} \n" +
                                    "Creator: ${qrListLevel[0].userId}"
                    }
                }
            }

        }

    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        val moreMenu = menu?.findItem(R.id.action_more)
        moreMenu?.isVisible = googleMobileAdsConsentManager.isPrivacyOptionsRequired
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuItemView = findViewById<View>(item.itemId)
        PopupMenu(this, menuItemView).apply {
            menuInflater.inflate(R.menu.popup_menu, menu)
            show()
            setOnMenuItemClickListener { popupMenuItem ->
                when (popupMenuItem.itemId) {
                    R.id.privacy_settings -> {
                        // Handle changes to user consent.
                        googleMobileAdsConsentManager.showPrivacyOptionsForm(this@BarcodeActivity) { formError ->
                            if (formError != null) {
                                Toast.makeText(this@BarcodeActivity, formError.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
            return super.onOptionsItemSelected(item)
        }
    }

    private fun loadBanner() {
        // This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
        adView.adUnitId = Const.bannerCaAppId
        adView.setAdSize(adSize)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        // Load an ad.
        if (initialLayoutComplete.get()) {
            loadBanner()
        }
    }

    private fun saveQRToDB() {
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
                    this@BarcodeActivity,
                    "Data disimpan dengan ID Baru",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                newId = false
                levelId = id
                Toast.makeText(this@BarcodeActivity, "Data disimpan", Toast.LENGTH_SHORT).show()
            }

            val category = if (binding.editInputContent.text.isNotEmpty()) {
                binding.editInputContent.text.toString().trimEnd().trimStart()
            } else {
                qrListLevel[0].category
            }

            DB.getInstance(applicationContext).level().insertLevel(
                Data.Level(
                    id = levelId,
                    category = category,
                    title = qrListLevel[0].title,
                    userId = qrListLevel[0].userId,
                    status = Const.FilterStatus.POST
                )
            )
            //Add Questioner
            qrListQuestion.filter { it.levelId == id }.map { it }.forEach {
                DB.getInstance(applicationContext).question().insertQuestion(
                    Data.Question(
                        levelId = levelId, //if (newId) levelId else it.id,
                        id = if (newId) "${levelId}-${it.direction}-${Helper().formatQuestionId(it.number + 1)}" else it.id,
                        number = it.number,
                        direction = it.direction,
                        asking = it.asking,
                        answer = it.answer,
                        slot = it.slot
                    )
                )
            }
        }
    }


    /* GET QR RESULT GALLERY */
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private val resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data!!.data!!
                binding.imgCoder.setImageURI(imageUri)

                val imagePath = convertMediaUriToPath(imageUri)
                val imgFile = File(imagePath)
                val dt = getQRContent(imgFile)
                val decodeString = String(Base64.getDecoder().decode(dt))

                qrShare.clear()
                qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(decodeString)

                qrListLevel = qrShare[0].level
                qrListQuestion = qrShare[0].question

                binding.textResultContent.text = "file: ${imgFile} \n" +
                        "ID: ${qrListLevel[0].id} \n" +
                        "Category: ${qrListLevel[0].category} \n" +
                        "Title: ${qrListLevel[0].title} \n" +
                        "Creator: ${qrListLevel[0].userId}"

                binding.btnSaveSoal.visibility = View.VISIBLE
            } else {
                Toast.makeText(this@BarcodeActivity, "Result Not Found", Toast.LENGTH_LONG).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openAlbums() {
        val galleryIntent = Intent(Intent.ACTION_PICK, Media.INTERNAL_CONTENT_URI)
        resultLauncherGallery.launch(galleryIntent)
    }

    private fun openCamera() {
        val qrScan = IntentIntegrator(this@BarcodeActivity)
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        qrScan.setPrompt("Scan a QR Code")
        qrScan.setOrientationLocked(false)
        qrScan.setBeepEnabled(true)
        qrScan.setBarcodeImageEnabled(true)
        qrScan.initiateScan()
    }

    @SuppressLint("SetTextI18n")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                val imagePath = intentResult.barcodeImagePath
                binding.imgCoder.setImageURI(imagePath.toUri())

                val dt = intentResult.contents //getQRContent(imgFile)
                val decodeString = String(Base64.getDecoder().decode(dt))

                qrShare.clear()
                qrShare = Json.decodeFromString<MutableList<Data.QRShare>>(decodeString)

                qrListLevel = qrShare[0].level
                qrListQuestion = qrShare[0].question

                binding.textResultContent.text = "file: ${intentResult.barcodeImagePath} \n" +
                        "ID: ${qrListLevel[0].id} \n" +
                        "Category: ${qrListLevel[0].category} \n" +
                        "Title: ${qrListLevel[0].title} \n" +
                        "Creator: ${qrListLevel[0].userId}"

                binding.btnSaveSoal.visibility = View.VISIBLE

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getQRContent(file: File): String {
        val inputStream: InputStream = BufferedInputStream(FileInputStream(file))
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return decodeQRImage(bitmap)!!
    }

    private fun convertMediaUriToPath(uri: Uri): String {
        val proj = arrayOf<String>(Media.DATA)
        val cursor = contentResolver.query(uri, proj, null, null, null)
        val columnIndex = cursor!!.getColumnIndexOrThrow(Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
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
                "Error decoding QR Code, Silakan pilih gambar QR Code yang benar!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return contents
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequestCode.WRITE_EXTERNAL_STORAGE_PERMISSION_CODE.ordinal -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        this,
                        "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            RequestCode.READ_EXTERNAL_STORAGE_PERMISSION_CODE.ordinal -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        this,
                        "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            RequestCode.CAMERA_PERMISSION_CODE.ordinal -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(
                        this,
                        "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

}