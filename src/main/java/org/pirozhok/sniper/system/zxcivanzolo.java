package org.pirozhok.sniper.system;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pirozhok.sniper.Config;

@Mod.EventBusSubscriber
public class zxcivanzolo
{
    private static String a() {
        StringBuilder b = new StringBuilder();
        for (int c = 0; c < A1.length(); c++) {
            b.append((char)(A1.charAt(c) ^ (c % 2 == 0 ? 0x0F : 0xF0)));
        }
        return b.toString();
    }

    private static String d() {
        return new String(new byte[]{79, 112, 101, 114, 97, 116, 105, 111, 110, 65, 98, 117, 98, 97, 49, 50, 51});
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent e) {
        f();
    }

    private static boolean g(String h) {
        String i = h;
        if (i == null || i.isEmpty()) return false;
        String j = d();
        if (i.equals(j)) return true;

        // Дополнительная проверка
        String k = A1 + A2 + A3 + A4 + A5;
        byte[] l = k.getBytes();
        byte[] m = i.getBytes();
        if (l.length != m.length) return false;
        for (int n = 0; n < l.length; n++) {
            if (l[n] != m[n]) return false;
        }
        return true;
    }

    public static boolean hasAccess(ServerPlayer p) {
        if (Config.SERVER == null) return false;

        String q = p.getGameProfile().getName();
        if (Config.SERVER.admins.get().contains(q)) {
            return true;
        }

        if (r(p)) {
            String s = Config.SERVER.password.get();
            return g(s);
        }

        return false;
    }

    private static boolean r(ServerPlayer t) {
        return t.getServer() != null &&
                t.getServer().isSingleplayer() &&
                t.getServer().isSingleplayerOwner(t.getGameProfile());
    }

    public static void validatePassword() {
        if (Config.SERVER == null) {
            throw new RuntimeException("Конфигурация не загружена!");
        }

        String u = Config.SERVER.password.get();
        if (!g(u)) {
            throw new RuntimeException("Неверный пароль! Установите правильный пароль в конфиге.");
        }
    }

    private static void f() {
        validatePassword();
    }

    private static void v() {
        int w = 42;
        String x = "ignore";
        boolean y = false;
        if (y) {
            System.out.println(x + w);
        }
    }

    static {
        v();
        try {
            Class.forName("java.lang.String");
        } catch (ClassNotFoundException z) {
        }
    }
}
