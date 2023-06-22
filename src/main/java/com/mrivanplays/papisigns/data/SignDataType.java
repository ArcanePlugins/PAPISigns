package com.mrivanplays.papisigns.data;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.sign.Side;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class SignDataType implements PersistentDataType<byte[], SignData> {

  public static final SignDataType INSTANCE = new SignDataType();

  public static final int VERSION = 101;

  private SignDataType() {}

  @Override
  public @NotNull Class<byte[]> getPrimitiveType() {
    return byte[].class;
  }

  @Override
  public @NotNull Class<SignData> getComplexType() {
    return SignData.class;
  }

  @Override
  public byte @NotNull [] toPrimitive(
      @NotNull SignData complex, @NotNull PersistentDataAdapterContext context) {
    var out = ByteStreams.newDataOutput();
    // since 1.0.0's format read starts with an integer too, make this also an integer, in order
    // for 1.0.1's read method to determine whether it's dealing with the old or the new format
    // and since the line numbers on signs can't go over 4, a number above 4 is suitable for a mark
    out.writeInt(VERSION);
    var twoSidesOccupied = complex.areTwoSidesOccupied();
    out.writeBoolean(twoSidesOccupied);
    var frontSideMap = complex.data().get(Side.FRONT);
    if (frontSideMap != null && !frontSideMap.isEmpty()) {
      if (!twoSidesOccupied) {
        out.writeUTF("FRONT");
      }
      this.writeEntries(frontSideMap, out);
    }
    var backSideMap = complex.data().get(Side.BACK);
    if (backSideMap != null && !backSideMap.isEmpty()) {
      if (!twoSidesOccupied) {
        out.writeUTF("BACK");
      }
      this.writeEntries(backSideMap, out);
    }
    return out.toByteArray();
  }

  private void writeEntries(Map<Integer, SingleSignData> map, ByteArrayDataOutput out) {
    out.writeInt(map.size());
    for (var entry : map.entrySet()) {
      out.writeInt(entry.getKey());
      out.writeBoolean(entry.getValue().placeholder() != null);
      if (entry.getValue().placeholder() != null) {
        out.writeUTF(entry.getValue().placeholder());
      }
      out.writeBoolean(entry.getValue().color() != null);
      if (entry.getValue().color() != null) {
        out.writeUTF(entry.getValue().color().asHexString());
      }
    }
  }

  @Override
  public @NotNull SignData fromPrimitive(
      byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
    var in = ByteStreams.newDataInput(primitive);
    var version = in.readInt();
    if (version == VERSION) {
      // new format
      Map<Side, Map<Integer, SingleSignData>> map = new HashMap<>();
      if (in.readBoolean()) {
        // when both sides are occupied, Side is not sent, as the front side is always written
        // first, and then the back side
        var frontSize = in.readInt();
        map.put(Side.FRONT, this.readSignData(in, frontSize));
        var backSize = in.readInt();
        map.put(Side.BACK, this.readSignData(in, backSize));
      } else {
        var side = Side.valueOf(in.readUTF());
        var size = in.readInt();
        map.put(side, this.readSignData(in, size));
      }
      return new SignData(map);
    } else {
      // old format
      Map<Side, Map<Integer, SingleSignData>> map = new HashMap<>();
      map.put(Side.FRONT, this.readSignData(in, version));
      return new SignData(map);
    }
  }

  private Map<Integer, SingleSignData> readSignData(ByteArrayDataInput in, int size) {
    Map<Integer, SingleSignData> map = new HashMap<>(size);
    for (int i = 0; i < size; i++) {
      var line = in.readInt();
      String placeholder = null;
      if (in.readBoolean()) {
        placeholder = in.readUTF();
      }
      TextColor color = null;
      if (in.readBoolean()) {
        color = TextColor.fromHexString(in.readUTF());
      }
      map.put(line, new SingleSignData(placeholder, color));
    }
    return map;
  }
}
