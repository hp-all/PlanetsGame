package musicPlayer;

import javax.swing.*;

import main.SocketScreen;

import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class MusicComponent extends SwingWorker<Boolean, Boolean>{
	public Clip themeClip;
	public Clip fightClip;
	public long startTime = 0;
	public long songDuration = 0;
	public final int THEME_SONG_BPM = 135;
	public final int FIGHT_SONG_BPM = 135;
	public final double THEME_SONG_LENGTH = 66.05; 
	public final int FIGHT_A = 16;
	public final int FIGHT_B = 8;
	public final int FIGHT_T = 2;
	public boolean mute = false;
	private String themeSong = "";
	private String fightSong = "";
	public volatile boolean firstLoop = true;
	private volatile boolean startNextPhase = false;
	public volatile int fightPhase = -1;
	private volatile long ellapse = 0;
	private volatile boolean fightPlaying = false;

	public MusicComponent(boolean m)
	{
		mute = !m;
		String folder = "/musicPlayer/";
		themeSong = folder + "ThemeSong.wav";
		fightSong = folder + "FightSong.wav";
	}
	@Override
	protected Boolean doInBackground() throws Exception {
		boolean smile = true;
		while(smile) {
			long nowTime = System.nanoTime()/1000;
			if(fightPhase == -1)
			{
				if(fightClip != null)
				{
					fightClip.stop();
					fightClip = null;
					songDuration = 0;
					fightPlaying = false;
				}
				playTheme();
			}
			else if(fightPhase >= 0)
			{
				if(themeClip != null)
				{
					themeClip.stop();
					themeClip = null;
					songDuration = 0;
				}
				playFightSong();
			}
			else
			{
				if(themeClip != null)
				{
					themeClip.stop();
					themeClip = null;
					songDuration = 0;
				}
				if(fightClip != null)
				{
					fightClip.stop();
					fightClip = null;
					songDuration = 0;
				}
				fightPlaying = false;
			}
			ellapse = nowTime - startTime;
		}
		return null;
	}
	private void playTheme()
	{
		if(ellapse >= songDuration && !mute)
		{			
			fightPlaying = false;
			if(firstLoop)
			{
				themeClip = playFile(themeSong, themeClip, 0);
				firstLoop = false;
			}
			else
			{
				themeClip = playFile(themeSong, themeClip, 4, THEME_SONG_LENGTH - 4.05);
			}
		}
	}
	private void playFightSong()
	{
		double extra = .05;
		if(((startNextPhase && barChange(ellapse, FIGHT_SONG_BPM)) || (startNextPhase && fightPhase == 0)) && !mute)
		{
			fightPlaying = true;
			if(fightClip != null)
				fightClip.stop();
			startNextPhase = false;
			int barsPlayed = FIGHT_A*(fightPhase) + FIGHT_B*(fightPhase) + FIGHT_T*(fightPhase);
			
			fightClip = playFile(fightSong, fightClip, barsPlayed, FIGHT_A + FIGHT_B + FIGHT_T - extra);
		}
		else if(ellapse > songDuration && !mute)
		{
			if(fightClip != null)
				fightClip.stop();
			int barsPlayed = FIGHT_A*(fightPhase) + FIGHT_B*(fightPhase) + FIGHT_T*(fightPhase+1);
			fightClip = playFile(fightSong, fightClip, barsPlayed, FIGHT_A + FIGHT_B - extra);
		}
	}
	private boolean barChange(long ellapse, int BPM)
	{
		double extra = .05;
		return ((int)((getBarTime(ellapse, BPM)*10000)-extra)/10000.)%1 == 0;
	}
	public void nextPhase(int i)
	{
		synchronized(this) {
			if(i <= 2)
			{
				fightPhase = i;
				startNextPhase = true;
				System.out.println("Next Phase " + fightPhase);
			}
		}
	}
	public void stopMusic()
	{
		synchronized(this)
		{
			if(mute)
			{
				if(fightPhase == -1)
					themeClip.stop();
				else
					fightClip.stop();
				songDuration = 0;
				System.out.println("Muted");
			}
			else
				System.out.println("Unmuted");
		}
	}
	private long getClipTime(double bars, int BPM)
	{
		return (long)(bars*4./BPM*60*1000000);
	}
	public double getBarTime(long time, int BPM)
	{
		return (double)(time*BPM/60./1000000/4);
	}
	public Clip playFile(String musicFile, Clip clip)
	{
		return playFile(musicFile, clip, 0);
	}
	public Clip playFile(String musicFile, Clip clip, double startPos)
	{
		return playFile(musicFile, clip, startPos, 0);
	}
	public Clip playFile(String musicFile, Clip clip, double startPos, double bars)
	{
		try
		{
			int BPM = THEME_SONG_BPM;
			if(clip == fightClip)
				BPM = FIGHT_SONG_BPM;

			long startMilli = getClipTime(startPos, BPM);
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(MusicComponent.class.getResource(musicFile)));
			clip.setMicrosecondPosition(startMilli);
			if(bars == 0)
			{
				songDuration = getClipTime(THEME_SONG_LENGTH, BPM) - startMilli;
			}
			else
				songDuration = getClipTime(bars, BPM);
			clip.start();
			startTime = System.nanoTime()/1000;
			return clip;
		}
		catch(Exception e)
		{
			e.getStackTrace();
		}
		return null;
	}
}
