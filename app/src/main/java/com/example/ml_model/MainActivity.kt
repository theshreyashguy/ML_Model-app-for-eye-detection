package com.example.ml_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileDescriptor
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var clickImageId: ImageView
    lateinit var clickuri : Uri

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        clickImageId.setImageURI(null)
        clickImageId.setImageURI((clickuri))
    }

    private val camerapermission : ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if(it){
            diag()

        }
        else{
            Toast.makeText(this,"Application require permission",Toast.LENGTH_SHORT).show()

        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         clickImageId = findViewById(R.id.imagever)
        clickuri = createImageUri()!!
        val f = findViewById<Button>(R.id.capture)
            f.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                    )
                ) {

                } else {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    camerapermission.launch(
                        Manifest.permission.CAMERA
                    )

                }
            }

        }
    private fun createImageUri(): Uri? {
        val Image = File(applicationContext.filesDir,"camera_photo.png")
        return FileProvider.getUriForFile(applicationContext,
            "com.example.ml_model.fileProvider",
            Image)
    }

    private fun diag(){
        val dd = Dialog(this)
        dd.setContentView(R.layout.cusdiag)
        dd.setTitle("Demo")
        val ff = dd.findViewById<Button>(R.id.btnstop)
        ff.setOnClickListener {
            Toast.makeText(this,"Let's go then,ready",Toast.LENGTH_SHORT)
                .show()
            dd.dismiss()
            contract.launch(clickuri)
        }

        dd.show()

    }

}




