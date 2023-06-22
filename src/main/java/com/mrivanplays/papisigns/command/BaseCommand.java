package com.mrivanplays.papisigns.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.TextColorArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.mrivanplays.papisigns.data.PSConfig;
import com.mrivanplays.papisigns.data.SignData;
import com.mrivanplays.papisigns.data.SignDataType;
import com.mrivanplays.papisigns.data.SingleSignData;
import com.mrivanplays.papisigns.loader.PapiSigns;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaseCommand {

  public static void register(PapiSigns plugin, PaperCommandManager<CommandSender> manager) {
    PSConfig config = plugin.getPSConfig();
    manager.command(
        manager
            .commandBuilder("papisign", ArgumentDescription.of("Base PAPISigns command"))
            .literal(
                "set", ArgumentDescription.of("Set placeholder/color of a line of a facing sign"))
            .permission("papisign.set")
            .literal(
                "placeholder",
                ArgumentDescription.of("Set a line of a facing sign to the placeholder inputted"))
            .senderType(Player.class)
            .argument(
                EnumArgument.<CommandSender, Side>builder(Side.class, "Side").asRequired().build(),
                ArgumentDescription.of(
                    "The side of the sign of which you want the placeholder to apply"))
            .argument(
                IntegerArgument.<CommandSender>builder("line")
                    .withMin(1)
                    .withMax(4)
                    .withSuggestionsProvider(lineSuggestions())
                    .asRequired()
                    .build(),
                ArgumentDescription.of("The line to manipulate"))
            .argument(
                StringArgument.<CommandSender>builder("placeholder").single().asRequired().build(),
                ArgumentDescription.of("The placeholder you want to be displayed"))
            .handler(
                context -> {
                  var player = (Player) context.getSender();
                  Side signSide = context.get("Side");
                  var block = player.getTargetBlockExact(config.getMaxDistance());
                  if (!(block.getState() instanceof Sign sign)) {
                    player.sendMessage(plugin.getPSConfig().getMessages().getNotASign());
                    return;
                  }
                  int lineRaw = context.get("line");
                  var line = lineRaw - 1;
                  String placeholder = context.get("placeholder");
                  if (config.getForbiddenPlaceholders().contains(placeholder)) {
                    player.sendMessage(
                        plugin.getPSConfig().getMessages().getForbiddenPlaceholder());
                    return;
                  }

                  var dataContainer = sign.getPersistentDataContainer();
                  if (dataContainer.has(PapiSigns.TAGGED_SIGNS_KEY)) {
                    var signData =
                        dataContainer.get(PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE);
                    if (signData.data().containsKey(signSide)) {
                      var signDataData = signData.data().get(signSide);
                      if (signDataData.containsKey(line)) {
                        var singleSignData = signDataData.get(line);
                        signDataData.replace(
                            line, new SingleSignData(placeholder, singleSignData.color()));
                      } else {
                        signDataData.put(line, new SingleSignData(placeholder, null));
                      }
                      signData.data().replace(signSide, signDataData);
                    } else {
                      Map<Integer, SingleSignData> map = new HashMap<>();
                      map.put(line, new SingleSignData(placeholder, null));
                      signData.data().put(signSide, map);
                    }
                    dataContainer.set(PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE, signData);
                  } else {
                    Map<Integer, SingleSignData> map = new HashMap<>();
                    map.put(line, new SingleSignData(placeholder, null));
                    Map<Side, Map<Integer, SingleSignData>> mapRet = new HashMap<>();
                    mapRet.put(signSide, map);
                    dataContainer.set(
                        PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE, new SignData(mapRet));
                  }
                  sign.update();

                  player.sendMessage(plugin.getPSConfig().getMessages().getLineChangedSuccess());
                }));

    manager.command(
        manager
            .commandBuilder("papisign", ArgumentDescription.of("Base PAPISigns command"))
            .literal(
                "set", ArgumentDescription.of("Set placeholder/color of a line of a facing sign"))
            .permission("papisign.set")
            .literal(
                "color",
                ArgumentDescription.of("Set the specified line's color of the facing sign"))
            .senderType(Player.class)
            .argument(
                EnumArgument.<CommandSender, Side>builder(Side.class, "Side").asRequired().build(),
                ArgumentDescription.of(
                    "The side of the sign of which you want the placeholder to apply"))
            .argument(
                IntegerArgument.<CommandSender>builder("line")
                    .withMin(1)
                    .withMax(4)
                    .withSuggestionsProvider(lineSuggestions())
                    .asRequired()
                    .build(),
                ArgumentDescription.of("The line to manipulate"))
            .argument(TextColorArgument.of("color"), ArgumentDescription.of("The color to set"))
            .handler(
                context -> {
                  var player = (Player) context.getSender();
                  Side signSide = context.get("Side");
                  var block = player.getTargetBlockExact(config.getMaxDistance());
                  if (!(block.getState() instanceof Sign sign)) {
                    player.sendMessage(plugin.getPSConfig().getMessages().getNotASign());
                    return;
                  }
                  int lineRaw = context.get("line");
                  var line = lineRaw - 1;
                  TextColor textColor = context.get("color");

                  var dataContainer = sign.getPersistentDataContainer();
                  if (dataContainer.has(PapiSigns.TAGGED_SIGNS_KEY)) {
                    var signData =
                        dataContainer.get(PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE);
                    if (signData.data().containsKey(signSide)) {
                      var signDataData = signData.data().get(signSide);
                      if (signDataData.containsKey(line)) {
                        var singleSignData = signDataData.get(line);
                        signDataData.replace(
                            line, new SingleSignData(singleSignData.placeholder(), textColor));
                      } else {
                        player.sendMessage(plugin.getPSConfig().getMessages().getWarningNotSet());
                        signDataData.put(line, new SingleSignData(null, textColor));
                      }
                      signData.data().replace(signSide, signDataData);
                    } else {
                      player.sendMessage(plugin.getPSConfig().getMessages().getWarningNotSet());
                      Map<Integer, SingleSignData> map = new HashMap<>();
                      map.put(line, new SingleSignData(null, textColor));
                      signData.data().put(signSide, map);
                    }
                    dataContainer.set(PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE, signData);
                  } else {
                    player.sendMessage(plugin.getPSConfig().getMessages().getWarningNotSet());
                    Map<Integer, SingleSignData> map = new HashMap<>();
                    map.put(line, new SingleSignData(null, textColor));
                    Map<Side, Map<Integer, SingleSignData>> mapRet = new HashMap<>();
                    mapRet.put(signSide, map);
                    dataContainer.set(
                        PapiSigns.TAGGED_SIGNS_KEY, SignDataType.INSTANCE, new SignData(mapRet));
                  }
                  sign.update();

                  player.sendMessage(plugin.getPSConfig().getMessages().getColorChangedSuccess());
                }));

    manager.command(
        manager
            .commandBuilder("papisign", ArgumentDescription.of("Base PAPISigns command"))
            .literal("reload", ArgumentDescription.of("Reload the configuration"))
            .permission("papisign.reload")
            .handler(
                context -> {
                  plugin.reload();
                  context
                      .getSender()
                      .sendMessage(plugin.getPSConfig().getMessages().getReloadSuccess());
                }));

    manager.command(
        manager
            .commandBuilder("papisign", ArgumentDescription.of("Base PAPISigns command"))
            .literal("help")
            .permission("papisign.help")
            .argument(
                StringArgument.<CommandSender>builder("query")
                    .greedy()
                    .asOptionalWithDefault("")
                    .withSuggestionsProvider(
                        (context, input) ->
                            manager
                                .createCommandHelpHandler()
                                .queryRootIndex(context.getSender())
                                .getEntries()
                                .stream()
                                .map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString)
                                .collect(Collectors.toList()))
                    .build())
            .handler(
                context ->
                    plugin
                        .getHelpMenu()
                        .queryCommands(context.getOrDefault("query", ""), context.getSender())));
  }

  private static BiFunction<CommandContext<CommandSender>, String, List<String>> lineSuggestions() {
    return (context, input) -> {
      var possible = Arrays.asList("1", "2", "3", "4");
      if (input.isEmpty()) {
        return possible;
      }
      return possible.stream()
          .filter(value -> value.toLowerCase().startsWith(input.toLowerCase()))
          .collect(Collectors.toList());
    };
  }
}
