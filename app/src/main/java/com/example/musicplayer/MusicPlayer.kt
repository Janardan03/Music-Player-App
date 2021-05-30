package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_music_player.*
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class MusicPlayer : AppCompatActivity(), ItemClicked {

    private var mediaPlayer : MediaPlayer? = null
    private lateinit var musicList : MutableList<Music>
    private var currPosition : Int = 0
    private var state = false
    // state = false means that player is stopped and true means that player is playing

    companion object{

        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        musicList = mutableListOf()

        if(Build.VERSION.SDK_INT >= 23)
            checkPermission()

        fab_play.setOnClickListener{

            play(currPosition)
        }

        fab_next.setOnClickListener{

            mediaPlayer?.stop()
            state = false
            if(currPosition < musicList.size - 1)
            currPosition += 1
            else
                currPosition = 0
            play(currPosition)
        }

        fab_previous.setOnClickListener {

            mediaPlayer?.stop()
            state = false
            if(currPosition > 0)
                currPosition--
            else
                currPosition = musicList.size - 1
            play(currPosition)
        }

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if(fromUser){
                    mediaPlayer?.seekTo(progress*1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


    private fun play(currPosition : Int){

        try {
            if (!state) {
                fab_play.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_stop, null))   // if it does not works change it to previous
                state = true

                mediaPlayer = MediaPlayer().apply {

                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setDataSource(this@MusicPlayer, Uri.parse(musicList[currPosition].songUri))
                    prepare()
                    start()
                }

                val mHandler = Handler()
                this@MusicPlayer.runOnUiThread(object : Runnable{         // to run some task continuously on UI thread

                    override fun run() {

                        val playerPosition = mediaPlayer?.currentPosition!! / 1000
                        val totalDuration = mediaPlayer?.duration!! / 1000

                        seek_bar.max = totalDuration
                        seek_bar.progress = playerPosition

                        past_text_view.text = timerFormat(playerPosition.toLong())
                        remain_text_view.text = timerFormat((totalDuration - playerPosition).toLong())

                        mHandler.postDelayed(this, 1000)         // this will move seek bar by 1 second on playing
                    }
                })

            } else {

                state = false
                mediaPlayer?.stop()
                fab_play.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play_arrow, null))
            }
        }catch(e : Exception){
            Toast.makeText(this, "error is $e", Toast.LENGTH_LONG).show()
        }
    }

    private fun timerFormat(time : Long) : String{

        val result = String.format("%02d:%02d", TimeUnit.SECONDS.toMinutes(time),
            TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))

        var convert = ""

        for(i in 0 until result.length)
            convert += result[i]

        return convert
    }

    private fun getSongs(){      // we are calling this function only when we have got the permissions

        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projection = arrayOf(
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA
        )

        val cursor : Cursor? = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, null)

        while(cursor!!.moveToNext()){

            musicList.add(Music(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }

        cursor.close()

        recycler_view.layoutManager = LinearLayoutManager(this)
        val adapter = MusicAdapter(musicList, this)

        recycler_view.adapter = adapter
    }

    private fun checkPermission(){

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            getSongs()

        }else{
            // false -> user asked not to show me anymore or permission is disabled
                // true -> rejected before want to use the feature again
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this, "MusicPlayer needs to access your files", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode){
            REQUEST_CODE_READ_EXTERNAL_STORAGE -> if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                getSongs()

            }else{
                Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun itemClicked(position: Int) {

        mediaPlayer?.stop()
        state = false
        this.currPosition = position
        play(currPosition)
    }
}