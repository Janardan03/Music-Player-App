package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(private var musicList : MutableList<Music>,private var itemClicked : ItemClicked) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_items, parent, false)

        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {

        val item = musicList[position]
        holder.bindMusic(item)
    }

    inner class MusicViewHolder(view : View) : RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var music : Music
        private var songName : TextView = view.findViewById(R.id.song_text_view)
        private var artistName : TextView = view.findViewById(R.id.artist_text_view)

        init {
            view.setOnClickListener(this)
        }

        fun bindMusic(music : Music){

            this.music = music

            artistName.text = music.artistName
            songName.text = music.songName
        }

        override fun onClick(v: View?) {
                itemClicked.itemClicked(adapterPosition)
        }
    }
}