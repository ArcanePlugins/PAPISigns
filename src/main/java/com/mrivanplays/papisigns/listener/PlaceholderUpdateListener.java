package com.mrivanplays.papisigns.listener;

import com.mrivanplays.papisigns.data.SignDataType;
import com.mrivanplays.papisigns.loader.PapiSigns;
import java.util.ArrayList;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlaceholderUpdateListener implements Listener {

  private final PapiSigns plugin;

  public PlaceholderUpdateListener(PapiSigns plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    var player = event.getPlayer();
    var loc = event.getTo();
    for (var state : loc.getChunk().getTileEntities()) {
      if (!(state instanceof Sign sign)) {
        continue;
      }
      var stateLoc = state.getBlock().getLocation();
      if (loc.distance(stateLoc) > plugin.getPSConfig().getMaxDistance()) {
        continue;
      }
      var dataContainer = sign.getPersistentDataContainer();
      if (!dataContainer.has(PapiSigns.TAGGED_SIGNS_KEY)) {
        continue;
      }
      var signData = dataContainer.get(PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE);
      var lines = new ArrayList<>(sign.lines());
      for (var entry : signData.data().entrySet()) {
        var singleData = entry.getValue();
        if (singleData.placeholder() == null) {
          continue;
        }
        var toSet = plugin.provider().parse(player, singleData);
        lines.set(entry.getKey(), toSet);
      }
      player.sendSignChange(stateLoc, lines);
    }
  }
}
