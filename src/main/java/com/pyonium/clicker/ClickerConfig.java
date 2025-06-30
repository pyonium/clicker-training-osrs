package com.pyonium.clicker;

import net.runelite.client.config.*;

@ConfigGroup("clicker")
public interface ClickerConfig extends Config
{
    @ConfigItem(
            keyName = "clickerVolume",
            name = "Volume",
            description = "How loud does puppy want its reward?",
            position = 10
    )
    default int volume()
    {
        return 65;
    }

    @ConfigItem(
            keyName = "chatMessages",
            name = "Chat Messages",
            description = "Do you want chat messages?",
            position = 20
    )
    default boolean chatMessages()
    {
        return true;
    }

    @ConfigItem(
            keyName = "onLevel",
            name = "Click On Level Up",
            description = "Rewards level ups",
            position=30
    )
    default boolean onLevel()
    {
        return true;
    }

    @ConfigItem(
            keyName = "onVirtualLevel",
            name = "Click On Virtual Level",
            description = "For all the maxed puppies out there!",
            position=35
    )
    default boolean onVirtualLevel()
    {
        return true;
    }

    @ConfigItem(
            keyName = "onPartLevel",
            name = "Click On Partial Level",
            description = "For if it takes too long!",
            position=40
    )
    default boolean onPartLevel()
    {
        return false;
    }

    @ConfigItem(
            keyName = "levelPartSize",
            name = "Partial Levels",
            description = "How many times do you want to be rewarded per level?",
            position=41
    )
    default int levelPartSize()
    {
        return 2;
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