package org.pirozhok.sniper.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.network.NetworkEvent;
import org.pirozhok.sniper.system.SecuritySystem;

import java.util.UUID;
import java.util.function.Supplier;

public class CheatPacket {
    public final int cheatId;

    private static final UUID HEALTH_BOOST_ID = UUID.fromString("a3d7a0e2-8b0a-4c8a-9e77-abc123def456");

    public CheatPacket(int cheatId) {
        this.cheatId = cheatId;
    }

    public static void encode(CheatPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.cheatId);
    }

    public static CheatPacket decode(FriendlyByteBuf buffer) {
        return new CheatPacket(buffer.readInt());
    }

    public static void handle(CheatPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && SecuritySystem.canUseCheats(player)) {
                switch (packet.cheatId) {
                    case 1 -> { // Невидимость
                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false));
                    }
                    case 2 -> { // Дополнительные сердца
                        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
                        // Удаляем старый модификатор если есть
                        var oldModifier = healthAttribute.getModifier(HEALTH_BOOST_ID);
                        if (oldModifier != null) {
                            healthAttribute.removeModifier(HEALTH_BOOST_ID);
                        }
                        // Добавляем новый
                        healthAttribute.addTransientModifier(new AttributeModifier(
                                HEALTH_BOOST_ID, "health_boost", 20.0, AttributeModifier.Operation.ADDITION
                        ));
                        // Восстанавливаем здоровье
                        player.setHealth(player.getMaxHealth());
                    }
                    case 3 -> { // Скорость
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1, false, false));
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}