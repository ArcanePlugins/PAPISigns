package com.mrivanplays.papisigns.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_20_R1.block.sign.CraftSignSide;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Player;

public class SignUpdates {

  private static final Field signTextField;

  static {
    try {
      signTextField = CraftSignSide.class.getDeclaredField("signText");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    signTextField.setAccessible(true);
  }

  public static void sendSignChange(
      Player player, Map<Side, List<Component>> updatesMap, Location location, Sign bukkitSign) {
    var craft = (CraftPlayer) player;
    if (craft.getHandle().connection != null) {
      var sign =
          new SignBlockEntity(
              CraftLocation.toBlockPosition(location),
              ((CraftSign) bukkitSign)
                  .getBlock()
                  .getHandle()
                  .getBlockState(CraftLocation.toBlockPosition(location)));
      Side modifiedSide = null;
      for (Map.Entry<Side, List<Component>> entry : updatesMap.entrySet()) {
        var side = entry.getKey();
        SignText modifiedSignText;
        if (side == Side.FRONT) {
          modifiedSignText = sign.getFrontText();
        } else {
          modifiedSignText = sign.getBackText();
        }
        var components = sanitizeToInternalComponents(entry.getValue());
        modifiedSignText = modifiedSignText.setColor(DyeColor.BLACK);
        modifiedSignText = modifiedSignText.setHasGlowingText(false);
        for (int i = 0; i < components.length; i++) {
          modifiedSignText = modifiedSignText.setMessage(i, components[i]);
        }
        sign.setText(modifiedSignText, side == Side.FRONT);
        if (updatesMap.size() == 1) {
          modifiedSide = side;
        }
      }
      if (modifiedSide != null) {
        var unmodifiedSide = modifiedSide == Side.FRONT ? Side.BACK : Side.FRONT;
        sign.setText(getSignText(bukkitSign.getSide(unmodifiedSide)), unmodifiedSide == Side.FRONT);
      }
      craft.getHandle().connection.send(sign.getUpdatePacket());
    }
  }

  private static SignText getSignText(SignSide signSide) {
    try {
      return (SignText) signTextField.get(((CraftSignSide) signSide));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static net.minecraft.network.chat.Component[] sanitizeToInternalComponents(
      List<Component> lines) {
    var components = new net.minecraft.network.chat.Component[4];
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
    }
    return components;
  }
}
