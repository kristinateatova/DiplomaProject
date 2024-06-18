package org.tensorflow.lite.examples.soundclassifier

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader

class BirdEncyclopedia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bird_encyclopedia)
        val birdind = intent.getStringExtra("DELTA_KEY")

        data class Bird(
            val id:String,
            val title: String,
            val description: String,
            val imageResId: Int
        )

        var birdDescription: String

        fun readFromAssetFile(context: Context, fileName: String): String {
            val stringBuilder = StringBuilder()
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append('\n')
                    }
                }
            }
            return stringBuilder.toString()
        }

        fun parseConfigData(configData: String): Map<String, String> {
            val configMap = mutableMapOf<String, String>()
            val lines = configData.split("\n")
            for (line in lines) {
                val parts = line.split("=")
                if (parts.size == 2) {
                    configMap[parts[0].trim()] = parts[1].trim()
                }
            }
            return configMap
        }

        val configData = readFromAssetFile(this, birdind+".txt")
        val configMap = parseConfigData(configData)
        birdDescription = configMap["bird_description"] ?: "Описание отсутствует"

        val birds = listOf(
            Bird("0 Воробей","Воробей",birdDescription , R.drawable.vorob),
            Bird("1 Глухарь","Глухарь", birdDescription, R.drawable.gluhar),
            Bird("2 Дятел","Дятел", birdDescription, R.drawable.detel),
            Bird("3 Коростель","Коростель", "", R.drawable.korostel),
            Bird("4 Кряква","Кряква", birdDescription, R.drawable.krakva),
            Bird("5 Кукушка","Кукушка",birdDescription, R.drawable.kykyshka),
            Bird("6 Ласточка","Ласточка", birdDescription, R.drawable.lastochka),
            Bird("7 Снегирь","Снегирь", birdDescription, R.drawable.snegir),
            Bird("8 Сова","Сова",birdDescription , R.drawable.sova),
            Bird("9 Фоновый шум","Фоновый шум", birdDescription, R.drawable.backnoise),
        )

        val selectedBird = birds.find { it.id == birdind }
        if (selectedBird != null) {
            val birdTitleTextView = findViewById<TextView>(R.id.BirdName)
            val birdDescriptionTextView = findViewById<TextView>(R.id.BirdDescr)
            val birdImageView = findViewById<ImageView>(R.id.BirdView)
            birdTitleTextView.text = "Возможно это "+selectedBird.title
            birdDescriptionTextView.text = selectedBird.description
            birdImageView.setImageResource(selectedBird.imageResId)
        } else {
            Toast.makeText(this, "Птица не найдена", Toast.LENGTH_LONG).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}
