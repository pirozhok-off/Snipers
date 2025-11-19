package org.pirozhok.sniper.cheats;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler
{
    public static final KeyMapping CHEATS_KEY = new KeyMapping(
            "key.sniper.cheats",
            GLFW.GLFW_KEY_MINUS,
            "category.sniper"
    );

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event)
    {
        if (CHEATS_KEY.consumeClick())
        {
            //открываем GUI читов
        }
    }
}
