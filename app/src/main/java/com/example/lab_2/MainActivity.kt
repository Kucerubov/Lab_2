package com.example.lab_2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.lab_2.databinding.ActivityMainBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var latestUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnSeePicture.setOnClickListener {
                cameraApprovedForUse()
                latestUri = getFile()
                getCameraImage.launch(latestUri)
            }
            btnSendPhoto.setOnClickListener {
                sendMail()
            }
        }
    }


    private val getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicture())
    { success ->
        if (success) {
            Log.i("INFO", "Изображение успешно снято")
            latestUri?.let { uri -> binding.viewPhoto.setImageURI(uri) }
        } else { Log.i("INFO", "Ошибка!") }
    }


    private fun cameraApprovedForUse() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
            ) -> { Log.i("TAG", "Разрешения для камеры уже предоставлены") }
            else -> { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { isGranted: Boolean ->
        if (isGranted) {
            Log.i("INFO", "Разрешение использовать камеру полученно!")
        } else {
            Log.i("INFO", "Вы не разрешили использовать камеру!")
        }
    }

    private fun getFile(): Uri {
        val image = File.createTempFile(
            "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date()) + "_",
            ".jpg",
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        return FileProvider.getUriForFile(this, "com.example.lab_2.fileprovider", image)
    }

    private fun sendMail() {
        latestUri?.let {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(it, contentResolver.getType(it))
                putExtra(Intent.EXTRA_STREAM, it)
                putExtra(Intent.EXTRA_EMAIL, arrayOf("kucerubov365@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "КПП АИ-194 Куцерубов И.И.")
            }
            startActivity(Intent.createChooser(emailIntent, "Отправка..."))
        }
    }
}