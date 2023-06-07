package com.mrivanplays.papisigns.provider;

import com.mrivanplays.papisigns.data.SingleSignData;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public final class PlaceholderAPIProvider implements PlaceholderProvider {

  @Override
  public String name() {
    return "PlaceholderAPI";
  }

  @Override
  public Component parse(final Player player, final SingleSignData data) {
    final String parsed = PlaceholderAPI.setPlaceholders(player, data.placeholder());
    final Component result = LegacyComponentSerializer.legacyAmpersand().deserialize(parsed);
    if (data.color() == null) {
      return result;
    } else {
      return result.applyFallbackStyle(data.color());
    }
  }
}
