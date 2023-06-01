package com.mrivanplays.papisigns.provider;

import com.mrivanplays.papisigns.data.SingleSignData;
import net.kyori.adventure.text.Component;

// TODO: Impl
public interface PlaceholderProvider {

  boolean available();

  Component parse(SingleSignData data);

}
