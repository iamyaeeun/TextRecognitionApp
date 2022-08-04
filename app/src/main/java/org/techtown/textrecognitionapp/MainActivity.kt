package org.techtown.textrecognitionapp

import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.TextView
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.os.Bundle
import org.techtown.textrecognitionapp.R
import android.content.Intent
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import org.techtown.textrecognitionapp.MainActivity
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import android.widget.Toast
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock
import java.util.*

class MainActivity : AppCompatActivity(), OnInitListener {
      var imageView:ImageView?=null
      var textView:TextView?=null
      var imageBitmap: Bitmap? = null
      var tts: TextToSpeech? = null
      var msg:String = "식품을 찍어주시면 유통기한을 음성 안내 해드립니다." +
            "각각 식품 사진 찍기 버튼과 유통기한 음성 안내 버튼 2개가 화면 가장자리에 위아래로 있으며" +
            "메인으로 가기 버튼은 왼쪽 하단에 있습니다."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var captureImageBtn= findViewById<Button>(R.id.capture_image_btn)
        var detectTextBtn= findViewById<Button>(R.id.detect_text_image_btn)
        imageView = findViewById<ImageView>(R.id.image_view)
        textView= findViewById<TextView>(R.id.text_display)
        tts = TextToSpeech(this, this)
        captureImageBtn.setOnClickListener(View.OnClickListener {
            dispatchTakePictureIntent()
            //textView.setText("")
        })
        detectTextBtn.setOnClickListener(View.OnClickListener { detectTextFromImage() })
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras = data!!.extras
            imageBitmap = extras!!["data"] as Bitmap?
            imageView!!.setImageBitmap(imageBitmap)
        }
    }

    private fun detectTextFromImage() {
        val inputImage = InputImage.fromBitmap(imageBitmap!!, 0)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text -> displayTextFromImage(text) }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity, "Error" + e.message, Toast.LENGTH_SHORT).show()
                Log.d("Error: ", e.message!!)
            }
    }

    private fun displayTextFromImage(text: Text) {
        val blockList = text.textBlocks
        if (blockList.size == 0) {
            msg = "글자를 인식하지 못했습니다. 다시 찍어주세요."
            textView!!.text = msg //Toast.makeText(this,"No Text Found in image.",Toast.LENGTH_SHORT).show();
            displayVoiceFromText() //새로 추가
        } else {
            for (block in text.textBlocks) {
                msg = block.text
                textView!!.text = msg
                displayVoiceFromText() //새로 추가
            }
        }
    }

    private fun displayVoiceFromText() {
        val text: CharSequence = msg
        tts!!.setPitch(0.6.toFloat())
        tts!!.setSpeechRate(0.1.toFloat())
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.KOREA)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                //btn_speak.setEnabled(true);
                displayVoiceFromText() //speakOut();
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}