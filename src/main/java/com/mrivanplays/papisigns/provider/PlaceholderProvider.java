package com.mrivanplays.papisigns.provider;

import com.mrivanplays.papisigns.data.SingleSignData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public interface PlaceholderProvider {

  String name();

  default boolean available() {
    return Bukkit.getPluginManager().isPluginEnabled(name());
  }

  Component parse(Player player, SingleSignData data);

}
