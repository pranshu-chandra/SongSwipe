package com.example.songswipe

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track

enum class PlayingState {
    PAUSED, PLAYING, STOPPED
}
object SpotifyService {
    var trackname:String="blah blah"
    var artistname:String="blah blah"
    private const val CLIENT_ID = "86851deaee554edabbc9d4b87735c81d"
    private const val  REDIRECT_URI = "http://localhost:8888/callback"

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams: ConnectionParams = ConnectionParams.Builder(CLIENT_ID)
        .setRedirectUri(REDIRECT_URI)
        .showAuthView(true)
        .build()
    fun connect(context: Context, handler: (connected: Boolean) -> Unit) {
        if (spotifyAppRemote?.isConnected == true) {
            handler(true)
            return
        }
        val connectionListener = object : Connector.ConnectionListener {
            override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                this@SpotifyService.spotifyAppRemote = spotifyAppRemote
                Toast.makeText(context,"Hello",Toast.LENGTH_SHORT).show()
                handler(true)
            }
            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyService", throwable.message, throwable)
                handler(false)
            }
        }
        SpotifyAppRemote.connect(context, connectionParams, connectionListener)
    }



        fun play(uri: String) {
            spotifyAppRemote?.playerApi?.play(uri)
            Handler
            spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                val track: Track = it.track
                artistname = track.artist.name
                trackname=track.name
            }
        }

        fun resume() {
            spotifyAppRemote?.playerApi?.resume()
        }

        fun pause() {
            spotifyAppRemote?.playerApi?.pause()
        }

        fun playingState(handler: (PlayingState) -> Unit) {
            spotifyAppRemote?.playerApi?.playerState?.setResultCallback { result ->
                if (result.track.uri == null) {
                    handler(PlayingState.STOPPED)
                } else if (result.isPaused) {
                    handler(PlayingState.PAUSED)
                } else {
                    handler(PlayingState.PLAYING)
                }
            }
        }

    fun playNext() {
        spotifyAppRemote?.playerApi?.skipNext()
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
            val track: Track = it.track
            artistname = track.artist.name
            trackname=track.name
        }
    }

    fun getCurrentTrack(handler: (track: Track) -> Unit) {
        spotifyAppRemote?.playerApi?.playerState?.setResultCallback { result ->
            handler(result.track)
        }
    }

    fun getName():String{

        return trackname
    }

    fun getArtist():String{
        return artistname
    }

    fun getImage(imageUri: ImageUri, handler: (Bitmap) -> Unit)  {
        spotifyAppRemote?.imagesApi?.getImage(imageUri)?.setResultCallback {
            handler(it)
            spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                val track: Track = it.track
                artistname = track.artist.name
                trackname=track.name
            }
        }

    }

    fun getCurrentTrackImage(handler: (Bitmap) -> Unit) {
        getCurrentTrack {
            getImage(it.imageUri) {
                handler(it)
            }
        }
    }
    fun suscribeToChanges(handler: (Track) -> Unit) {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
            handler(it.track)
        }

    }

}