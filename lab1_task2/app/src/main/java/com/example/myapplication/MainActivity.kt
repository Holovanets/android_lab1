package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var scrambledWordTextView: TextView
    private lateinit var counter: TextView
    private lateinit var inputWordEditText: EditText
    private lateinit var checkButton: Button
    private lateinit var hintButton: Button
    private var currentWord: String = ""
    private var counterNum: Int = 0
    private var hintsGiven: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scrambledWordTextView = findViewById(R.id.scrambledWordTextView)
        counter = findViewById(R.id.counter)
        inputWordEditText = findViewById(R.id.inputWordEditText)
        checkButton = findViewById(R.id.checkButton)
        hintButton = findViewById(R.id.hintButton)

        startNewGame()

        checkButton.setOnClickListener {
            checkAnswer()
        }
        hintButton.setOnClickListener {
            provideHint()
        }
    }

    private fun startNewGame() {
        hintsGiven = 0
        FetchWordTask().execute()
    }
    private fun checkAnswer() {
        val userInput = inputWordEditText.text.toString().trim().lowercase()
        if (userInput == currentWord) {
            Toast.makeText(this, "Вітаю! Ви вгадали слово!", Toast.LENGTH_SHORT).show()
            if(hintsGiven <= 4){
                counterNum++
            }
            counter.text = "Вгаданих слів: $counterNum"
            startNewGame()
        } else {
            Toast.makeText(this, "Неправильно. Спробуйте ще раз!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun provideHint() {
        hintsGiven++
        val hint = when (hintsGiven) {
            1 -> "Перша буква: ${currentWord[0]}"
            2 -> "Кількість букв у слові: ${currentWord.length}"
            3 -> "Перша та остання букви: ${currentWord[0]} - ${currentWord.last()}"
            4 -> "Перша частина слова: ${currentWord.take(3)}"
            5 -> "Слово: ${currentWord}"
            else -> "Не можу більше допомогти, але спробуйте ще раз!"
        }
        Toast.makeText(this, "Підказка: $hint", Toast.LENGTH_SHORT).show()
    }

    private inner class FetchWordTask : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg params: Void?): String? {
            try {
                val url = URL("https://random-word-api.herokuapp.com/word?lang=en&number=1")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()
                    val words = JSONArray(response)
                    return words.getString(0)
                } else {
                    return null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: String?) {
            if (!result.isNullOrEmpty()) {
                currentWord = result
                val scrambledWord = currentWord.toList().shuffled().joinToString("")
                scrambledWordTextView.text = scrambledWord
                inputWordEditText.text.clear()
            } else {
                Toast.makeText(this@MainActivity, "Не вдалося отримати слово. Спробуйте пізніше!", Toast.LENGTH_LONG).show()
                checkButton.isEnabled = false
            }
        }
    }
}
