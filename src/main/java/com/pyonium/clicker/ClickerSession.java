package com.pyonium.clicker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
public class ClickerSession {
    private final Map<Skill, Integer> sessionExperience = new EnumMap<>(Skill.class);

    public ClickerSession()
    {
        for(int i = 0; i < Skill.values().length; i++)
        {
            sessionExperience.put(Skill.values()[i], 0);
        }
    }

    public void xpDrop(Skill skill, int oldExperience, int newExperience)
    {
        sessionExperience.put(skill, sessionExperience.get(skill) + newExperience - oldExperience);
    }

    public int getExperience(Skill skill)
    {
        return sessionExperience.get(skill);
    }
}
