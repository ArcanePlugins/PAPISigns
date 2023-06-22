package com.mrivanplays.papisigns.util;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.bukkit.Location;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Player;

public class SignUpdates {

  public static void sendSignChange(
      Player player, List<Component> lines, Location location, Side side) {
    var craft = (CraftPlayer) player;
    if (craft.getHandle().connection != null) {
      var components =
          new net.minecraft.network.chat.Component[4];
      if (lines.isEmpty()) {
        for (int i = 0; i < 4; i++) {
          components[i] = net.minecraft.network.chat.Component.empty();
        }
      } else {
        for (int i = 0; i < 4; i++) {
          if (i < lines.size() && lines.get(i) != null) {
            components[i] =
                CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(lines.get(i)));
          } else {
            components[i] = net.minecraft.network.chat.Component.empty();
          }
        }
        var sign =
            new SignBlockEntity(
                CraftLocation.toBlockPosition(location), Blocks.OAK_SIGN.defaultBlockState());
        SignText signText;
        if (side == Side.FRONT) {
          signText = sign.getFrontText();
        } else {
          signText = sign.getBackText();
        }
        // TODO: Back side not really working how it should
        signText = signText.setColor(DyeColor.BLACK);
        signText = signText.setHasGlowingText(false);
        for (int i = 0; i < components.length; i++) {
          signText = signText.setMessage(i, components[i]);
        }
        sign.setText(signText, true);
        craft.getHandle().connection.send(sign.getUpdatePacket());
      }
    }
  }
}
