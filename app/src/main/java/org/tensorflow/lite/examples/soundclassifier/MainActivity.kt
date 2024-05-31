package org.tensorflow.lite.examples.soundclassifier

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
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
  private var classificationInterval = 500L // how often should classification run in milli-secs
  private lateinit var handler: Handler // background thread handler to run classification

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    with(binding) {
      recyclerView.apply {
        setHasFixedSize(false)
        adapter = probabilitiesAdapter
      }

      // Input switch to turn on/off classification
    /*  keepScreenOn(inputSwitch.isChecked)
      inputSwitch.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) startAudioClassification() else stopAudioClassification()
        keepScreenOn(isChecked)
      }*/

      var isRecording = false
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

      // Slider which control how often the classification task should run
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



    // Create a handler to run classification in a background thread
    val handlerThread = HandlerThread("backgroundThread")
    handlerThread.start()
    handler = HandlerCompat.createAsync(handlerThread.looper)

    // Request microphone permission and start running classification
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestMicrophonePermission()
    } else {
      startAudioClassification()
    }
  }

  private fun startAudioClassification() {
    // If the audio classifier is initialized and running, do nothing.
    if (audioClassifier != null) return;

    // Initialize the audio classifier
    val classifier = AudioClassifier.createFromFile(this, MODEL_FILE)
    val audioTensor = classifier.createInputTensorAudio()

    // Initialize the audio recorder
    val record = classifier.createAudioRecord()
    record.startRecording()

    // Define the classification runnable
    val run = object : Runnable {
      override fun run() {
        val startTime = System.currentTimeMillis()

        // Load the latest audio sample
        audioTensor.load(record)
        val output = classifier.classify(audioTensor)

        // Filter out results above a certain threshold, and sort them descendingly
        val filteredModelOutput = output[0].categories.filter {
          it.score > MINIMUM_DISPLAY_THRESHOLD
        }.sortedBy {
          -it.score
        }

        val finishTime = System.currentTimeMillis()

        Log.d(TAG, "Latency = ${finishTime - startTime}ms")

        // Updating the UI
        runOnUiThread {
          probabilitiesAdapter.categoryList = filteredModelOutput
          probabilitiesAdapter.notifyDataSetChanged()
        }

        // Rerun the classification after a certain interval
        handler.postDelayed(this, classificationInterval)
      }
    }

    // Start the classification process
    handler.post(run)

    // Save the instances we just created for use later
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