package org.pirozhok.sniper.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.pirozhok.sniper.system.SecuritySystem;

import java.util.function.Supplier;

public class OpenCheatGUIPacket {

    public OpenCheatGUIPacket() {}

    public static void encode(OpenCheatGUIPacket packet, FriendlyByteBuf buffer) {
        // Пустой пакет
    }

    public static OpenCheatGUIPacket decode(FriendlyByteBuf buffer) {
        return new OpenCheatGUIPacket();
    }

    public static void handle(OpenCheatGUIPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && SecuritySystem.canUseCheats(player)) {
                // Отправляем пакет обратно для открытия GUI на клиенте
                ModNetwork.sendToPlayer(new OpenCheatGUIPacket(), player);
            }
        });
        context.get().setPacketHandled(true);
    }
}