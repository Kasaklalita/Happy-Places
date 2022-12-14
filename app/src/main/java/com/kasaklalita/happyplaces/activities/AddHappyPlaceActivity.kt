package com.kasaklalita.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kasaklalita.happyplaces.R
import com.kasaklalita.happyplaces.database.DatabaseHandler
import com.kasaklalita.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(tbAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tbAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"
            etTitle.setText(mHappyPlaceDetails!!.title)
            etDescription.setText(mHappyPlaceDetails!!.description)
            etDate.setText(mHappyPlaceDetails!!.date)
            etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            ivPlaceImage.setImageURI(saveImageToInternalStorage)
            btnSave.text = "UPDATE"
        }

        etDate.setOnClickListener(this)
        tvAddImage.setOnClickListener(this)
        btnSave.setOnClickListener {
            when {
                etTitle.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter the title!", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter the description!", Toast.LENGTH_SHORT)
                        .show()
                }
                etLocation.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter the location!", Toast.LENGTH_SHORT)
                        .show()
                }
                saveImageToInternalStorage == null -> {
                    Toast.makeText(this, "Please an image!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val happyPlaceModel = HappyPlaceModel(
                        if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                        etTitle.text.toString(),
                        saveImageToInternalStorage.toString(),
                        etDescription.text.toString(),
                        etDate.text.toString(),
                        etLocation.text.toString(),
                        mLatitude,
                        mLongitude
                    )
                    val dbHandler = DatabaseHandler(this)
                    if (mHappyPlaceDetails == null) {
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                        if (addHappyPlace > 0) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Adding failed $addHappyPlace",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                        if (updateHappyPlace > 0) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Updating failed $updateHappyPlace",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }



                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.etDate -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tvAddImage -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems =
                    arrayOf("Select photo from Gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
//            R.id.btnSave -> {
//                when {
//                    etTitle.text.isNullOrEmpty() -> {
//                        Toast.makeText(this, "Please enter the title!", Toast.LENGTH_SHORT).show()
//                    }
//                    etDescription.text.isNullOrEmpty() -> {
//                        Toast.makeText(this, "Please enter the description!", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    etLocation.text.isNullOrEmpty() -> {
//                        Toast.makeText(this, "Please enter the location!", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    saveImageToInternalStorage == null -> {
//                        Toast.makeText(this, "Please an image!", Toast.LENGTH_SHORT).show()
//                    }
//                    else -> {
//                        val happyPlaceModel = HappyPlaceModel(
//                            0,
//                            etTitle.text.toString(),
//                            saveImageToInternalStorage.toString(),
//                            etDescription.text.toString(),
//                            etDate.text.toString(),
//                            etLocation.text.toString(),
//                            mLatitude,
//                            mLongitude
//                        )
//                        val dbHandler = DatabaseHandler(this)
//                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
//
//                        if (addHappyPlace > 0) {
//                            Toast.makeText(
//                                this,
//                                "The happy place details are inserted successfully!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            finish()
//                        }
//                    }
//                }
//            }
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(sdf.format(cal.time).toString())
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")
                        ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Failed to load an image from the gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                ivPlaceImage.setImageBitmap(thumbnail)
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permission required for this feature. It can be enabled under the Application Settings")
            .setPositiveButton("Go to settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpeg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}