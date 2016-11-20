package com.rem.otl.pc.gui.music;
import java.io.File;
import java.io.IOException;

import com.rem.core.gui.music.ITrack;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class Track extends PlaybackListener implements Runnable, ITrack {

	private static final int PAUSED = -1;
	private static final int READY = 0;
	private static final int PLAYING = 1;
	private static final int FINISHED = 2;
	
	private int state = READY;

	private String copyright;
	private String name;
	private String artist;
	private String artistAndName;
	private String mp3Name;
	private String httpLink = null;
	private String feature;

	private String filePath;

	private AdvancedPlayer player;
	private Thread playerThread;
	private int pausedAt = 0;

	
	private JavaSoundAudioDevice volumeAdjustableAudio;
	private float volume;

	public Track(String mp3, String name, String artist, String copyright) {
		super();
		this.mp3Name = mp3;
		this.name = name;
		this.artist = artist;
		this.artistAndName = name+" - "+artist;
		this.copyright = copyright;

		this.filePath = "res/music/"+mp3Name;


	}

	public void play(float volume)  {
		try {
		File file = new File(this.filePath);
		if(!file.exists()){
			state = FINISHED;
			return;
		}		
		this.volume = volume;
		String urlAsString = 
				"file:///" 
						+ file.getCanonicalPath(); 

		AudioDevice audio = FactoryRegistry.systemRegistry().createAudioDevice();

		if (audio instanceof JavaSoundAudioDevice)
		{
			volumeAdjustableAudio = (JavaSoundAudioDevice)audio;
		}
		this.player = new AdvancedPlayer(new java.net.URL(urlAsString).openStream(),audio);

		this.player.setPlayBackListener(this);

		this.playerThread = new Thread(this);

		state = READY;
		this.playerThread.start();
		}
		catch (JavaLayerException e){
			e.printStackTrace();
		}
		catch (IOException e2){
			e2.printStackTrace();
		}
	}

	public void pause(){
		if(state==PLAYING){
			state = PAUSED;
			player.stop();
		}
		else {
			state = PAUSED;
		}
	}
	public void skip(){
		state = PAUSED;
		player.stop();
		pausedAt = 0;
	}
	public void reset(){
		pausedAt = 0;
	}

	public void playbackStarted(PlaybackEvent playbackEvent) {
		if(state==PAUSED){
			state = PLAYING;
			adjustVolume(volume);
			pause();
		}
		else {
			state = PLAYING;
			adjustVolume(volume);
		}
	}

	public void playbackFinished(PlaybackEvent playbackEvent) {
		pausedAt  += playbackEvent.getFrame()/16;
		if(state == PLAYING){
			state = FINISHED;
			pausedAt=0;
		}

	}    

	public void run() {
		try
		{	
			if(state==READY){
				this.player.play(pausedAt,Integer.MAX_VALUE);
			}
		}
		catch (javazoom.jl.decoder.JavaLayerException ex)
		{
			ex.printStackTrace();
		}

	}

	public String getName() {
		return name;
	}

	public void setHttpLink(String link) {
		this.httpLink = link;
	}

	public String getHttpLink() {
		return this.httpLink;
	}
	public void setFeature(String ft) {
		this.feature = ft;
	}

	public String getFeature() {
		return this.feature;
	}

	public boolean isFinished() {
		return state==FINISHED;
	}

	public void adjustVolume(float volume){
		if(volumeAdjustableAudio!=null){
			volumeAdjustableAudio.setVolume(volume);
			this.volume = volume;
		}
	}

	public String getLicense() {
		return copyright;
	}

	public String getArtist() {		
		return artist;
	}

	public String getFullName(){
		return artistAndName;
	}
}