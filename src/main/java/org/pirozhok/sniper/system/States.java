package org.pirozhok.sniper.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class States
{
    //Дефолтные значения
    private static String spawnMode = "sky";
    private static String gunsMode = "players";
    private static String teamsMode = "solo";
    private static boolean borderShrinkEnabled = true;
    private static String borderShrinkMode = "standard";

    //Хранение состояний
    private static final Map<String, Object> stateMap = new HashMap<>();

    static
    {
        //Инициализация дефолтами
        stateMap.put("spawnMode", spawnMode);
        stateMap.put("gunsMode", gunsMode);
        stateMap.put("teamsMode", teamsMode);
        stateMap.put("borderShrinkEnabled", borderShrinkEnabled);
        stateMap.put("borderShrinkMode", borderShrinkMode);
    }

    //Геттеры
    public static String getSpawnMode()
    {
        return stateMap.get("spawnMode").toString();
    }

    public static String getGunsMode()
    {
        return stateMap.get("gunsMode").toString();
    }

    public static String getTeamsMode()
    {
        return stateMap.get("teamsMode").toString();
    }

    public static boolean isBorderShrinkEnabled()
    {
        return (boolean) stateMap.get("borderShrinkEnabled");
    }

    public static String getBorderShrinkMode()
    {
        return stateMap.get("borderShrinkMode").toString();
    }

    // Сеттеры
    public static void setSpawnMode(String mode)
    {
        stateMap.put("spawnMode", mode);
    }

    public static void setGunsMode(String mode)
    {
        stateMap.put("gunsMode", mode);
    }

    public static void setTeamsMode(String mode)
    {
        stateMap.put("teamsMode", mode);
    }

    public static void setBorderShrinkEnabled(boolean enabled)
    {
        stateMap.put("borderShrinkEnabled", enabled);
    }

    public static void setBorderShrinkMode(String mode)
    {
        stateMap.put("borderShrinkMode", mode);
    }

    // Метод для сброса к дефолтным значениям
    public static void resetToDefaults()
    {
        stateMap.put("spawnMode", spawnMode);
        stateMap.put("gunsMode", gunsMode);
        stateMap.put("teamsMode", teamsMode);
        stateMap.put("borderShrinkEnabled", borderShrinkEnabled);
        stateMap.put("borderShrinkMode", borderShrinkMode);
    }

    // Метод для получения всех состояний (для отладки)
    public static Map<String, Object> getAllStates()
    {
        return new HashMap<>(stateMap);
    }
}
