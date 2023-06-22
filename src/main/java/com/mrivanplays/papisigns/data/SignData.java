package com.mrivanplays.papisigns.data;

import java.util.Map;
import org.bukkit.block.sign.Side;

public record SignData(Map<Side, Map<Integer, SingleSignData>> data) {

  public boolean areTwoSidesOccupied() {
    return this.data.containsKey(Side.FRONT) && this.data.containsKey(Side.BACK);
  }
}
