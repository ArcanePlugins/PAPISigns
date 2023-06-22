package com.mrivanplays.papisigns.listener;

import com.mrivanplays.papisigns.data.SignDataType;
import com.mrivanplays.papisigns.loader.PapiSigns;
import com.mrivanplays.papisigns.util.SignUpdates;
import java.util.ArrayList;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
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
      for (var entry : signData.data().entrySet()) {
        var signSide = sign.getSide(entry.getKey());
        var lines = signSide.lines();
        for (var signDataEntry : entry.getValue().entrySet()) {
          var singleData = signDataEntry.getValue();
          if (singleData.placeholder() == null) {
            continue;
          }
          var toSet = plugin.provider().parse(player, singleData);
          lines.set(signDataEntry.getKey(), toSet);
        }
        SignUpdates.sendSignChange(player, lines, stateLoc, entry.getKey());
      }
    }
  }
}
