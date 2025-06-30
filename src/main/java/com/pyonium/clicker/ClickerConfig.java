package com.pyonium.clicker;

import net.runelite.client.config.*;

@ConfigGroup("clicker")
public interface ClickerConfig extends Config
{

    @ConfigSection(
            name = "Operation",
            description = "Settings about the operation of the clicker",
            position = 1
    )
    String OPERATION = "Operation";

    @ConfigItem(
            section = OPERATION,
            keyName = "mode",
            name = "Operating Mode",
            description = "Set the mode of operation: on session experience, or by (part of a) level",
            position = 1
    )
    default ClickerMode mode()
    {
        return ClickerMode.LEVEL;
    }

    @ConfigItem(
            section = OPERATION,
            keyName = "clickerVolume",
            name = "Volume",
            description = "How loud do you want want your reward?",
            position = 2
    )
    default int volume()
    {
        return 65;
    }

    @ConfigItem(
            section = OPERATION,
            keyName = "chatMessages",
            name = "Chat Messages",
            description = "Do you want chat messages?",
            position = 3
    )
    default boolean chatMessages()
    {
        return true;
    }

    @ConfigItem(
            section = OPERATION,
            keyName = "praise",
            name = "Praise",
            description = "The term of endearment used",
            position = 4
    )
    default String praise()
    {
        return "Good puppy!";
    }

    @ConfigSection(
            name = "Level mode settings",
            description = "Settings for clicking (parts of) level",
            position = 10
    )
    String LEVEL = "LevelMode";

    @ConfigItem(
            section = LEVEL,
            keyName = "onVirtualLevel",
            name = "Click On Virtual Level",
            description = "For all the maxed puppies out there!",
            position=12
    )
    default boolean onVirtualLevel()
    {
        return true;
    }

    @ConfigItem(
            section = LEVEL,
            keyName = "onPartLevel",
            name = "Click On Partial Level",
            description = "For if it takes too long!",
            position=13
    )
    default boolean onPartLevel()
    {
        return false;
    }

    @ConfigItem(
            section = LEVEL,
            keyName = "levelPartSize",
            name = "Partial Levels",
            description = "How many times do you want to be rewarded per level?",
            position=14
    )
    default int partialDivisor()
    {
        return 2;
    }

    @ConfigSection(
            name = "Interval mode settings",
            description = "Settings for clicking on an interval of experience",
            position = 30
    )
    String INTERVAL = "IntervalMode";

    @ConfigItem(
            section = INTERVAL,
            keyName = "xpInterval",
            name = "Experience Interval",
            description = "How much experience do you want in between clicks?",
            position=31
    )
    default int absoluteInterval()
    {
        return 10000;
    }

    @ConfigItem(
            keyName = "onClog",
            name = "Click On Collection Log",
            description = "Rewards Collection Log Slots",
            position=50
    )
    default boolean onClog()
    {
        return true;
    }




}