package com.mrivanplays.papisigns.loader;

import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.MinecraftHelp.HelpColors;
import cloud.commandframework.paper.PaperCommandManager;
import com.mrivanplays.annotationconfig.core.resolver.settings.ACDefaultSettings;
import com.mrivanplays.annotationconfig.core.resolver.settings.NullReadHandleOption;
import com.mrivanplays.annotationconfig.core.resolver.settings.Settings;
import com.mrivanplays.annotationconfig.core.serialization.SerializerRegistry;
import com.mrivanplays.annotationconfig.yaml.YamlConfig;
import com.mrivanplays.papisigns.listener.PlaceholderUpdateListener;
import com.mrivanplays.papisigns.command.BaseCommand;
import com.mrivanplays.papisigns.data.PSConfig;
import java.io.File;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.mrivanplays.papisigns.provider.MiniPlaceholdersProvider;
import com.mrivanplays.papisigns.provider.PlaceholderAPIProvider;
import com.mrivanplays.papisigns.provider.PlaceholderProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class PapiSigns extends JavaPlugin {

  public static final NamespacedKey TAGGED_SIGNS_KEY =
      new NamespacedKey("papisigns", "tagged-sign");

  private static final Settings CONFIG_SETTINGS =
      new Settings()
          .put(ACDefaultSettings.NULL_READ_HANDLER, NullReadHandleOption.USE_DEFAULT_VALUE)
          .put(ACDefaultSettings.SHOULD_REVERSE_FIELDS, true)
          .put(ACDefaultSettings.GENERATE_NEW_OPTIONS, true)
          .put(ACDefaultSettings.FIND_PARENT_FIELDS, false);

  private PSConfig config;
  private MinecraftHelp<CommandSender> helpMenu;
  private PlaceholderProvider placeholderProvider;

  @Override
  public void onEnable() {
    this.placeholderProvider = Stream.of(
        new MiniPlaceholdersProvider(),
        new PlaceholderAPIProvider()
    )
        .filter(PlaceholderProvider::available)
        .findFirst()
        .orElse(null);

    if (placeholderProvider == null) {
        getLogger().info("No compatible placeholder provider plugin found; disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    } else {
        getLogger().info("Using " + placeholderProvider + " as PlaceholderProvider");
    }

    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }
    SerializerRegistry serializerRegistry = SerializerRegistry.INSTANCE;
    if (!serializerRegistry.hasSerializer(Component.class)) {
      serializerRegistry.registerSerializer(Component.class, new PSConfig.ComponentSerializer());
    }
    if (!serializerRegistry.hasSerializer(HelpColors.class)) {
      serializerRegistry.registerSerializer(HelpColors.class, new PSConfig.HelpColorsSerializer());
    }
    config = new PSConfig();
    YamlConfig.getConfigResolver()
        .loadOrDump(config, new File(getDataFolder(), "config.yml"), CONFIG_SETTINGS);

    try {
      PaperCommandManager<CommandSender> commandManager =
          new PaperCommandManager<>(
              this,
              CommandExecutionCoordinator.simpleCoordinator(),
              Function.identity(),
              Function.identity());

      if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
        commandManager.registerBrigadier();
      }
      if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
        commandManager.registerAsynchronousCompletions();
      }

      BaseCommand.register(this, commandManager);

      this.helpMenu = new MinecraftHelp<>("/papisigns help", sender -> sender, commandManager);

      this.helpMenu.messageProvider(
          (sender, key, args) -> config.getMessages().getHelpCmd().getMessage(key));
      this.helpMenu.setHelpColors(config.getMessages().getHelpCmd().getHelpColors());
    } catch (Exception e) {
      getLogger()
          .log(
              Level.SEVERE,
              "Something went wrong with command manager initialization, shutting down",
              e);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    getServer().getPluginManager().registerEvents(new PlaceholderUpdateListener(this), this);
    getLogger().info("Enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info("Disabled");
  }

  public void reload() {
    YamlConfig.getConfigResolver()
        .loadOrDump(config, new File(getDataFolder(), "config.yml"), CONFIG_SETTINGS);
  }

  public PSConfig getPSConfig() {
    return this.config;
  }

  public MinecraftHelp<CommandSender> getHelpMenu() {
    return this.helpMenu;
  }

  public PlaceholderProvider provider() {
      return this.placeholderProvider;
  }
}
