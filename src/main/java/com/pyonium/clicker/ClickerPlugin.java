package com.pyonium.clicker;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.StatChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.runelite.client.RuneLite;
import net.runelite.client.util.Text;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
		name = "Clicker Training"
)
public class ClickerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClickerConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	private final Map<Skill, Integer> oldExperience = new EnumMap<>(Skill.class);

	private static final File CUSTOM_SOUNDS_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "clicker");
	private static final File CLICKER_SOUND_FILE = new File(CUSTOM_SOUNDS_DIR, "clicker.wav");

	private static final File[] SOUND_FILES = new File[]{
			CLICKER_SOUND_FILE
	};

	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log:.*");

	private Clip clip = null;

	private boolean onLevel;
	private boolean onPartLevel;
	private boolean onClog;
	private int volume;
	private boolean onVirtualLevel;
	private int levelPartSize;
	private boolean chatMessages;

	@Override
	protected void startUp()
	{
		initSoundFiles();
		this.onLevel = config.onLevel();
		this.onClog = config.onClog();
		this.volume = config.volume();
		this.onPartLevel = config.onPartLevel();

	}

	@Override
	protected void shutDown()
	{
		clip.close();
		clip = null;
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		if (!onLevel) {
			return;
		}
		final Skill skill = statChanged.getSkill();

		// Modified from Nightfirecat's virtual level ups plugin as this info isn't (yet?) built in to statChanged event
		final int xpAfter = client.getSkillExperience(skill);
		final int levelAfter = Experience.getLevelForXp(xpAfter);
		final int xpBefore = oldExperience.getOrDefault(skill, -1);
		final int levelBefore = xpBefore == -1 ? -1 : Experience.getLevelForXp(xpBefore);

		oldExperience.put(skill, xpAfter);

		boolean fire = true;

		//fire when threshold is between old xp and new xp (this xp drop passed it or reached it)

		// Do not proceed if any of the following are true (sanity checks):
		//  * xpBefore == -1              (don't fire when first setting new known value)
		//  * xpAfter <= xpBefore         (do not allow 200m -> 200m exp drops)
		//  * levelAfter > MAX_REAL_LEVEL && config says don't include virtual (level is virtual and config ignores virtual)
		if (xpBefore == -1 || xpAfter <= xpBefore || (levelAfter > Experience.MAX_REAL_LEVEL && !onVirtualLevel)) {
			return;
		}

		//fire if half level is reached
		if(onPartLevel)
		{
			for(int i = 0; i < levelPartSize - 1; i++) {

				int partway = Experience.getXpForLevel(levelBefore) + (Experience.getXpForLevel(levelBefore + 1) - Experience.getXpForLevel(levelBefore)) / levelPartSize * (i+1);

				if (xpBefore < partway && xpAfter >= partway) {
					String message = "You're " + (i + 1) + "/" + levelPartSize + " of the way to " + statChanged.getSkill().getName() + " level " + (levelBefore + 1) + "! Good puppy!";
					sendHighlightedMessage(message);
					playSound(CLICKER_SOUND_FILE);
					return;
				}
			}
		}

		// * levelBefore >= levelAfter (new level is reached)
		if(levelBefore < levelAfter)
		{
			playSound(CLICKER_SOUND_FILE);
			return;
		}



	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if(!onClog)
		{
			return;
		}
		ChatMessageType msgType = chatMessage.getType();
		if(!msgType.equals(ChatMessageType.GAMEMESSAGE))
		{
			return;
		}

		String outputMessage = Text.removeTags(chatMessage.getMessage());
		if(COLLECTION_LOG_ITEM_REGEX.matcher(outputMessage).matches())
		{
			sendHighlightedMessage("A new item in your collection log! Good puppy!");
			playSound(CLICKER_SOUND_FILE);
		}
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

	@Provides
	ClickerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClickerConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		this.onLevel = config.onLevel();
		this.onClog = config.onClog();
		this.volume = config.volume() > 100 ? 100 : config.volume();
		this.onPartLevel = config.onPartLevel();
		this.levelPartSize = config.levelPartSize();
		this.onVirtualLevel = config.onVirtualLevel();
		this.chatMessages = config.chatMessages();
	}

	private void sendHighlightedMessage(String message) {
		if(!chatMessages)
		{
			return;
		}
		String highlightedMessage = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(message)
				.build();

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(highlightedMessage)
				.build());
	}
}