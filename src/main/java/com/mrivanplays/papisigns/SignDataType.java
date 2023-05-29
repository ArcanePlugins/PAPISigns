package com.mrivanplays.papisigns;

import com.google.common.io.ByteStreams;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class SignDataType implements PersistentDataType<byte[], SignData> {

  public static final SignDataType INSTANCE = new SignDataType();

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
    out.writeInt(complex.data().size());
    for (var entry : complex.data().entrySet()) {
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
    return out.toByteArray();
  }

  @Override
  public @NotNull SignData fromPrimitive(
      byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
    var in = ByteStreams.newDataInput(primitive);
    var size = in.readInt();
    Map<Integer, SingleSignData> map = new HashMap<>();
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
    return new SignData(map);
  }
}
