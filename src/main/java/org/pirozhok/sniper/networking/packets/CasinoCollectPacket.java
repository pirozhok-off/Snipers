package org.pirozhok.sniper.networking.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.pirozhok.sniper.inventory.CasinoMenu;

import java.util.function.Supplier;

public class CasinoCollectPacket {
    private final BlockPos pos;

    public CasinoCollectPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(CasinoCollectPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static CasinoCollectPacket decode(FriendlyByteBuf buf) {
        return new CasinoCollectPacket(buf.readBlockPos());
    }

    public static void handle(CasinoCollectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof CasinoMenu) {
                CasinoMenu menu = (CasinoMenu) player.containerMenu;
                menu.collectWinnings();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}