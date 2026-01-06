package org.pirozhok.sniper.networking.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.pirozhok.sniper.inventory.CasinoMenu;

import java.util.function.Supplier;

public class CasinoBetPacket {
    private final BlockPos pos;

    public CasinoBetPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(CasinoBetPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static CasinoBetPacket decode(FriendlyByteBuf buf) {
        return new CasinoBetPacket(buf.readBlockPos());
    }

    public static void handle(CasinoBetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof CasinoMenu) {
                CasinoMenu menu = (CasinoMenu) player.containerMenu;
                menu.handleBet();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}