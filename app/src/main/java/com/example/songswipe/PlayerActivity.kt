package com.example.songswipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.Thread.sleep
import java.util.*


class PlayerActivity : AppCompatActivity() {
    private lateinit var mDetector: GestureDetectorCompat
    lateinit var imageView :ImageView
    lateinit var pause :Button
    lateinit var resume :Button
    lateinit var title: TextView
    lateinit var artist: TextView
    private val DEBUG_TAG = "Gestures"
    var timer=false
    lateinit var ProgressBar:ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        imageView = findViewById<ImageView>(R.id.imageView)
        ProgressBar=findViewById<ProgressBar>(R.id.progressBar2)
        pause = findViewById<Button>(R.id.pauseButton)
        resume = findViewById<Button>(R.id.resumeButton)
        title = findViewById<TextView>(R.id.songname)
        artist = findViewById<TextView>(R.id.artist)
            setupViews()
            setupListeners()
        ProgressBar.setVisibility(View.INVISIBLE)
    }
    var flag=false

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action: Int = MotionEventCompat.getActionMasked(event)

        if (flag)return false
        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(DEBUG_TAG, "Action was DOWN")
                ProgressBar.setVisibility(View.VISIBLE)
                SpotifyService.playNext()
                setupListeners()
                flag=true
                Handler().postDelayed({
                    flag=false
                }, 1000)
                true
            }
            MotionEvent.ACTION_UP -> {
                Log.d(DEBUG_TAG, "Action was UP")
                ProgressBar.setVisibility(View.VISIBLE)
                SpotifyService.playNext()

                setupListeners()
                flag=true
                Handler().postDelayed({
                    flag=false
                }, 1000)
                true
            }
            else -> super.onTouchEvent(event)
        }


    }
    private fun setupViews() {
        SpotifyService.playingState {
            when (it) {
                PlayingState.PLAYING -> showPauseButton()
                PlayingState.PAUSED -> showResumeButton()
            }
        }
    }
    fun resumeSong(view: View) {
        SpotifyService.resume()
        showPauseButton()
    }
    fun pauseSong(view: View) {
        SpotifyService.pause()
        showResumeButton()
    }
    private fun showResumeButton() {
        resume.visibility = View.VISIBLE
        pause.visibility = View.GONE
        pause.setEnabled(false)
        resume.setEnabled(true)
    }
    private fun showPauseButton() {
        pause.visibility = View.VISIBLE
        resume.visibility = View.GONE
        pause.setEnabled(true)
        resume.setEnabled(false)
    }
    override fun onStart() {
        super.onStart()
        ProgressBar=findViewById<ProgressBar>(R.id.progressBar2)
        ProgressBar.setVisibility(View.VISIBLE)
        SpotifyService.play("spotify:album:5L8VJO457GXReKVVfRhzyM")
        showPauseButton()
    }
    private fun setupListeners() {
        SpotifyService.suscribeToChanges {
            SpotifyService.getImage(it.imageUri){
                imageView.setImageBitmap(it)
                ProgressBar.setVisibility(View.INVISIBLE)

            }
        }
        Handler().postDelayed({
            title.text=SpotifyService.getName()
            artist.text=SpotifyService.getArtist()
        }, 200)




        showPauseButton()
    }
}