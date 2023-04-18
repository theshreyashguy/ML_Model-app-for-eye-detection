package com.example.ml_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.ml_model.ml.MyModel
import kotlinx.coroutines.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileDescriptor
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var clickImageId: ImageView
    lateinit var clickuri : Uri
    private var text1 : TextView? = null
    var imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224,224,ResizeOp.ResizeMethod.BILINEAR))
        .build()



    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        clickImageId.setImageURI(null)
        clickImageId.setImageURI((clickuri))

    }

   suspend fun predict(clickuri: Uri) {
        val model = MyModel.newInstance(applicationContext)
       var tensorImage = TensorImage(DataType.FLOAT32)
        val bitmap = applicationContext.contentResolver.openInputStream(clickuri).use { data ->
            BitmapFactory.decodeStream(data)
        }
       tensorImage.load(bitmap)
        tensorImage  = imageProcessor.process(tensorImage)
// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)
// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray


         runOnUiThread{
             var op : String? = ""
             if( outputFeature0[0]<0.5){
               op = "healthy"
             }
             else{
                 op = "infected"
             }


             op?.let {
                 text1?.setText(it)
                 Toast.makeText(this,outputFeature0[0].toString(),Toast.LENGTH_SHORT).show()
             }



         }
// Releases model resources if no longer used.
        model.close()

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
        text1 = findViewById<TextView>(R.id.result)
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
        val pre = findViewById<Button>(R.id.predict22)
        pre.setOnClickListener {
             GlobalScope.launch {
                 predict(clickuri)
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




