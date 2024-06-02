package org.tensorflow.lite.examples.soundclassifier

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import org.tensorflow.lite.examples.soundclassifier.databinding.ActivityMainBinding
import org.tensorflow.lite.task.audio.classifier.AudioClassifier


class MainActivity : AppCompatActivity() {
  private val probabilitiesAdapter by lazy { ProbabilitiesAdapter() }
  private var audioClassifier: AudioClassifier? = null
  private var audioRecord: AudioRecord? = null
  private var classificationInterval = 500L // как часто классификация будет срабатывать
  private lateinit var handler: Handler

  class BirdDetailsActivity {

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)



    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)


    with(binding) {
      recyclerView.apply {
        setHasFixedSize(false)
        adapter = probabilitiesAdapter
      }
      val openBirdDetailsButton = findViewById<Button>(R.id.secondbutton) // Replace with your button ID
      openBirdDetailsButton.setOnClickListener {
        val intent = Intent(this@MainActivity, BirdEncyclopedia::class.java)
        startActivity(intent)
      }

      var isRecording = true
      class StatusHolder(var status: Boolean)
      // Создать кнопку
     // Обработать нажатия кнопки
      myButton.setOnClickListener {
        // Изменить состояние записи
        isRecording = !isRecording

        // Обновить текст кнопки
        myButton.text = if (isRecording) "Остановить" else "Начать"

        // Запустить или остановить классификацию аудио
        if (isRecording) {
          startAudioClassification()
          keepScreenOn(true)
          val statusHolder = StatusHolder(true)
        } else {
          stopAudioClassification()
          keepScreenOn(false)
          val statusHolder = StatusHolder(false)
                  }
      }


      // Ползунок который отображает как часто срабатывает классификация
      classificationIntervalSlider.value = classificationInterval.toFloat()
      classificationIntervalSlider.setLabelFormatter { value: Float ->
        "${value.toInt()} ms"
      }
      classificationIntervalSlider.addOnChangeListener { _, value, _ ->
        classificationInterval = value.toLong()
        stopAudioClassification()
        startAudioClassification()
      }
    }




    val handlerThread = HandlerThread("backgroundThread")
    handlerThread.start()
    handler = HandlerCompat.createAsync(handlerThread.looper)

    // запрос разрешения на использование микрофона
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestMicrophonePermission()
    } else {
      startAudioClassification()
    }
  }


  private fun startAudioClassification() {
        if (audioClassifier != null) return;

    // Initialize the audio classifier
    val classifier = AudioClassifier.createFromFile(this, MODEL_FILE)
    val audioTensor = classifier.createInputTensorAudio()

    // инициализация аудиорекордера
    val record = classifier.createAudioRecord()
    record.startRecording()


    val run = object : Runnable {
      override fun run() {
        val startTime = System.currentTimeMillis()

        audioTensor.load(record)
        val output = classifier.classify(audioTensor)
        val filteredModelOutput = output[0].categories.filter {
          it.score > MINIMUM_DISPLAY_THRESHOLD
        }.sortedBy {
          -it.score
        }

        val finishTime = System.currentTimeMillis()

        Log.d(TAG, "Latency = ${finishTime - startTime}ms")
        runOnUiThread {
          probabilitiesAdapter.categoryList = filteredModelOutput
          probabilitiesAdapter.notifyDataSetChanged()
        }
        handler.postDelayed(this, classificationInterval)
      }
    }

    // запуск процесса классификации
    handler.post(run)
    audioClassifier = classifier
    audioRecord = record
  }

  private fun stopAudioClassification() {
    handler.removeCallbacksAndMessages(null)
    audioRecord?.stop()
    audioRecord = null
    audioClassifier = null
  }

  override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
    // Handles "top" resumed event on multi-window environment
    if (isTopResumedActivity && isRecordAudioPermissionGranted()) {
      startAudioClassification()
    } else {
      stopAudioClassification()
    }
  }

  override fun onRequestPermissionsResult(
          requestCode: Int,
          permissions: Array<out String>,
          grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_RECORD_AUDIO) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.i(TAG, "Audio permission granted :)")
        startAudioClassification()
      } else {
        Log.e(TAG, "Audio permission not granted :(")
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun requestMicrophonePermission() {
    if (isRecordAudioPermissionGranted()) {
      startAudioClassification()
    } else {
      requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
    }
  }

  private fun isRecordAudioPermissionGranted(): Boolean {
      return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO
      ) == PackageManager.PERMISSION_GRANTED
  }

  private fun keepScreenOn(enable: Boolean) =
    if (enable) {
      window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
      window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

  companion object {
    const val REQUEST_RECORD_AUDIO = 1337
    private const val TAG = "AudioDemo"
    private const val MODEL_FILE = "soundclassifier_with_metadata.tflite"
    private const val MINIMUM_DISPLAY_THRESHOLD: Float = 0.3f
  }
}
