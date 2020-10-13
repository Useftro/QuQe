package com.example.quqe

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Base64.encodeToString
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random
import kotlin.reflect.typeOf

private var buttonUsed: Boolean = false
private var output: String? = null
private var mediaRecorder: MediaRecorder? = null
private var outputName = Random.nextInt(0, 2048) // Name of our file, f.e. 211.mp3 ("mp3" added further)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        val url = "http://9c2add012d49.ngrok.io/uploadfile" // Заменять каждые 8 ЧАСОВ, АУ
        //http://127.0.0.1:5000/uploadfile

        startButton.setOnClickListener{
            // Checking permissions to save audio and to record using microphone
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)

            }
            else{
                if(!buttonUsed){
                    startRecording()
                }
                else{
                    stopRecording()
                    var mhm = fileToBytes()
                    var encoded = Base64.encodeToString(mhm, 0)
                    val params = HashMap<String, String>()
                    params["sound"] = encoded

                    requestToServer(url, params)

                    // запись в файл для теста
//                    var fos: FileOutputStream = FileOutputStream(Environment.getExternalStorageDirectory().absolutePath + "/test.txt")
//                    fos.write(mhm)
//                    fos.close()
                }
            }
        }
    }

    private fun requestToServer(url: String, parameters: HashMap<String, String>){
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject(parameters as Map<*, *>)

        textView2.text = jsonObject::class.simpleName

        val stringRequest = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                // Display the first 50 characters of the response string.
                try {
                    textView.text = "Response: $response"
                }catch (e:Exception){
                    textView.text = "Exception: $e"
                }
            },
            Response.ErrorListener { textView.text = "That didn't work!" })
        queue.add(stringRequest)
    }

    private fun fileToBytes(): ByteArray {
        var file = File(output)
        var bytes: ByteArray = file.readBytes()
        // var encoded = encodeToString(bytes, 0)
        return bytes
    }

    private fun startRecording(){
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()

//            textView.text = getString(R.string.button_used)
            startButton.text = getString(R.string.stop_recording)
            buttonUsed = true

            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
//        textView.text = getString(R.string.button_not_used)
        startButton.text = getString(R.string.start_recording)
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
        Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
        buttonUsed = false
    }


}