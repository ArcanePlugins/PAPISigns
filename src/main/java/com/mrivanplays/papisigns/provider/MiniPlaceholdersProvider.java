package com.mrivanplays.papisigns.provider;

import com.mrivanplays.papisigns.data.SingleSignData;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class MiniPlaceholdersProvider implements PlaceholderProvider {
    @Override
    public String name() {
        return "MiniPlaceholders";
    }

    @Override
    public Component parse(final Player player, final SingleSignData data) {
        final var resolver = MiniPlaceholders.getAudienceGlobalPlaceholders(player);
        final Component result = miniMessage().deserialize(data.placeholder(), resolver);
        if (data.color() == null) {
            return result;
        } else {
            return result.applyFallbackStyle(data.color());
        }
    }
}
