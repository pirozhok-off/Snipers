package org.pirozhok.sniper.cheats;

import com.sun.jna.platform.win32.DdemlUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
        Minecraft minecraft = Minecraft.getInstance();

        //Проверка активна ли игра и игрок в мире
        if (minecraft.player == null || minecraft.screen != null)
        {
            return;
        }

        //Проверка нажатой клавиши "-" (код 12)
        if (event.getKey() == 12 && event.getAction() == 1) //1 = нажатие
        {
            //Открываем GUI
             minecraft.setScreen(new CheatsScreen());
        }
    }
}
