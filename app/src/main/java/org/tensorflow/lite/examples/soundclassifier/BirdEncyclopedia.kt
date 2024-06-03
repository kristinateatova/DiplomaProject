package org.tensorflow.lite.examples.soundclassifier

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BirdEncyclopedia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bird_encyclopedia)
        val birdind = intent.getStringExtra("DELTA_KEY")
       // Toast.makeText(this, birdind, Toast.LENGTH_LONG).show()

        data class Bird(
            val id:String,
            val title: String,
            val description: String,
            val imageResId: Int
        )

        val birds = listOf(
            Bird("0 Воробей","Воробей", "Маленькая птица, известная своим пением.", R.drawable.vorob),
            Bird("1 Глухарь","Глухарь", "Крупная хищная птица с острым зрением.", R.drawable.gluhar),
            Bird("2 Дятел","Дятел", "Птица с яркой окраской, встречается в лесах.", R.drawable.detel),
            Bird("3 Коростель","Коростель", "Маленькая птица, известная своим пением.", R.drawable.korostel),
            Bird("4 Кряква","Кряква", "Крупная хищная птица с острым зрением.", R.drawable.krakva),
            Bird("5 Кукушка","Кукушка", "Птица с яркой окраской, встречается в лесах.", R.drawable.kykyshka),
            Bird("6 Ласточка","Ласточка", "Маленькая птица, известная своим пением.", R.drawable.lastochka),
            Bird("7 Снегирь","Снегирь", "Крупная хищная птица с острым зрением.", R.drawable.snegir),
            Bird("8 Сова","Сова", "Птица с яркой окраской, встречается в лесах.", R.drawable.sova),
            Bird("9 Фоновый шум","Фоновый шум", "Фоновый шум — это та загадочная птица, чье присутствие всегда ощущается, но ее увидеть невозможно. Она обитает повсюду, будто таинственный фантом, невидимый, но всегда рядом. ", R.drawable.backnoise),
        )

        val selectedBird = birds.find { it.id == birdind }
        if (selectedBird != null) {
            val birdTitleTextView = findViewById<TextView>(R.id.BirdName)
            val birdDescriptionTextView = findViewById<TextView>(R.id.BirdDescr)
            val birdImageView = findViewById<ImageView>(R.id.BirdView)
            birdTitleTextView.text = selectedBird.title
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
    }
}