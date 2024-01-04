package com.pyonium.clicker;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.runelite.client.RuneLite;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
	name = "Clicker Training"
)
public class ClickerPlugin extends Plugin
{
	@Inject
	private Client client;

	private final Map<Skill, Integer> oldExperience = new EnumMap<>(Skill.class);

	private static final File CUSTOM_SOUNDS_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "clicker");
	private static final File CLICKER_SOUND_FILE = new File(CUSTOM_SOUNDS_DIR, "clicker.wav");

	private static final File[] SOUND_FILES = new File[]{
			CLICKER_SOUND_FILE
	};

	private Clip clip = null;

	@Override
	protected void startUp()
	{
		initSoundFiles();
	}

	@Override
	protected void shutDown()
	{
		clip.close();
		clip = null;
	}

	//@Inject
	//private BloodShardNotifierConfig config;
	
	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		final Skill skill = statChanged.getSkill();

		// Modified from Nightfirecat's virtual level ups plugin as this info isn't (yet?) built in to statChanged event
		final int xpAfter = client.getSkillExperience(skill);
		final int levelAfter = Experience.getLevelForXp(xpAfter);
		final int xpBefore = oldExperience.getOrDefault(skill, -1);
		final int levelBefore = xpBefore == -1 ? -1 : Experience.getLevelForXp(xpBefore);

		oldExperience.put(skill, xpAfter);

		// Do not proceed if any of the following are true:
		//  * xpBefore == -1              (don't fire when first setting new known value)
		//  * xpAfter <= xpBefore         (do not allow 200m -> 200m exp drops)
		//  * levelBefore >= levelAfter   (stop if if we're not actually reaching a new level)
		//  * levelAfter > MAX_REAL_LEVEL && config says don't include virtual (level is virtual and config ignores virtual)
		if (xpBefore == -1 || xpAfter <= xpBefore || levelBefore >= levelAfter ||
				(levelAfter > Experience.MAX_REAL_LEVEL)) {
			return;
		}
		playSound(CLICKER_SOUND_FILE);
	}


	private void initSoundFiles()
	{
		if (!CUSTOM_SOUNDS_DIR.exists())
		{
			CUSTOM_SOUNDS_DIR.mkdirs();
		}

		for (File f : SOUND_FILES)
		{
			try
			{
				if (f.exists()) {
					continue;
				}
				InputStream stream = ClickerPlugin.class.getClassLoader().getResourceAsStream(f.getName());
				OutputStream out = new FileOutputStream(f);
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = stream.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				out.close();
				stream.close();
			}  catch (Exception e) {
				log.debug(e + ": " + f);
			}
		}
	}

	private void playSound(File f)
	{
		try
		{
			/* Leaving this removed for now. Calling this too many times causes client to hang.
			if (clip != null)
			{
				clip.close();
			}
			 */

			log.warn(f.getPath());
			AudioInputStream is = AudioSystem.getAudioInputStream(f);
			AudioFormat format = is.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(is);

			//set volume
			int volume = 100;
			float vol = volume/100.0f;
			FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(20.0f * (float) Math.log10(vol));

			clip.start();
		}
		catch (LineUnavailableException | UnsupportedAudioFileException | IOException e)
		{
			log.warn(f.getName());
			log.warn("Sound file error", e);
		}
	}
}
