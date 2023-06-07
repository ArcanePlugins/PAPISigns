package com.mrivanplays.papisigns.data;

import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.MinecraftHelp.HelpColors;
import com.mrivanplays.annotationconfig.core.annotations.ConfigObject;
import com.mrivanplays.annotationconfig.core.annotations.Ignore;
import com.mrivanplays.annotationconfig.core.annotations.Key;
import com.mrivanplays.annotationconfig.core.annotations.comment.Comment;
import com.mrivanplays.annotationconfig.core.serialization.AnnotationAccessor;
import com.mrivanplays.annotationconfig.core.serialization.DataObject;
import com.mrivanplays.annotationconfig.core.serialization.FieldTypeSerializer;
import com.mrivanplays.annotationconfig.core.serialization.SerializationContext;
import com.mrivanplays.annotationconfig.core.utils.AnnotationUtils;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Comment("PAPISigns configuration")
public class PSConfig {

  public static class ComponentSerializer implements FieldTypeSerializer<Component> {

    @Override
    public Component deserialize(
        DataObject data, SerializationContext<Component> context, AnnotationAccessor annotations) {
      if (annotations.getAnnotation(PlainComponent.class).isPresent()) {
        return PlainTextComponentSerializer.plainText().deserialize(data.getAsString());
      }
      return MiniMessage.miniMessage().deserialize(data.getAsString());
    }

    @Override
    public DataObject serialize(
        Component value, SerializationContext<Component> context, AnnotationAccessor annotations) {
      if (annotations.getAnnotation(PlainComponent.class).isPresent()) {
        return new DataObject(PlainTextComponentSerializer.plainText().serialize(value));
      }
      return new DataObject(MiniMessage.miniMessage().serialize(value));
    }
  }

  public static class HelpColorsSerializer implements FieldTypeSerializer<HelpColors> {

    @Override
    public HelpColors deserialize(
        DataObject data, SerializationContext<HelpColors> context, AnnotationAccessor annotations) {
      if (!data.has("primary")
          || !data.has("highlight")
          || !data.has("alternateHighlight")
          || !data.has("text")
          || !data.has("accent")) {
        throw new IllegalArgumentException(
            "Missing help colors ; cannot parse config ; please delete this section of the config (it will automatically regenerate)");
      }
      var primaryStr = data.get("primary").getAsString();
      var highlightStr = data.get("highlight").getAsString();
      var alternateHighlightStr = data.get("alternateHighlight").getAsString();
      var textStr = data.get("text").getAsString();
      var accentStr = data.get("accent").getAsString();
      return HelpColors.of(
          toTextColor(primaryStr),
          toTextColor(highlightStr),
          toTextColor(alternateHighlightStr),
          toTextColor(textStr),
          toTextColor(accentStr));
    }

    private TextColor toTextColor(String str) {
      if (str.contains("#")) {
        return TextColor.fromHexString(str);
      }
      return NamedTextColor.NAMES.value(str);
    }

    @Override
    public DataObject serialize(
        HelpColors value,
        SerializationContext<HelpColors> context,
        AnnotationAccessor annotations) {
      var ret = new DataObject();
      for (var field : value.getClass().getDeclaredFields()) {
        var key = field.getName();
        field.setAccessible(true);
        TextColor color;
        try {
          color = (TextColor) field.get(value);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        String serialized;
        if (color instanceof NamedTextColor named) {
          serialized = named.toString();
        } else {
          serialized = color.asHexString();
        }
        ret.put(key, serialized);
      }
      return ret;
    }
  }

  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface PlainComponent {}

  @Comment("The maximum distance to be searched for a sign.")
  @Comment("Making this value very high can result in lag")
  private int maxDistance = 5;

  @Comment("Placeholders which shall not be replaced")
  private List<String> forbiddenPlaceholders = Arrays.asList("%balance%", "%balance_MrIvanPlays%");

  @ConfigObject private Messages messages = new Messages();

  @Comment("All configurable messages")
  @Comment("MiniMessage format is supported, LEGACY FORMAT IS NOT")
  public static class Messages {

    @Key("not-a-sign")
    private Component notASign =
        Component.text("The block you're facing is not a sign!", NamedTextColor.RED);

    @Key("forbidden-placeholder")
    private Component forbiddenPlaceholder =
        Component.text("That placeholder is forbidden!", NamedTextColor.RED);

    @Key("line-change-success")
    private Component lineChangedSuccess =
        Component.text("Line changed successfully!", NamedTextColor.GREEN);

    @Key("color-changed-success")
    private Component colorChangedSuccess =
        Component.text("Color changed successfully!", NamedTextColor.GREEN);

    @Key("warning-placeholder-not-set")
    private Component warningNotSet =
        Component.text(
            "WARNING: Placeholder not set. You may want to set a placeholder, otherwise the changes won't affect the sign in any way.");

    @Key("reload-success")
    private Component reloadSuccess =
        Component.text("Configuration reloaded successfully!", NamedTextColor.GREEN);

    @ConfigObject
    @Key("help-command")
    private HelpMessages helpCmd = new HelpMessages();

    @Comment("Messages of the /papisigns help command")
    @Comment("Colors are NOT supported")
    public static class HelpMessages {

      @PlainComponent private Component help = Component.text("Help");

      @PlainComponent private Component command = Component.text("Command");

      @PlainComponent private Component description = Component.text("Description");

      @PlainComponent
      @Key("no_description")
      private Component noDescription = Component.text("No Description");

      @PlainComponent private Component arguments = Component.text("Arguments");

      @PlainComponent private Component optional = Component.text("Optional");

      @PlainComponent
      @Key("showing_results_for_query")
      private Component showingResultsForQuery = Component.text("Showing results for query");

      @PlainComponent
      @Key("no_results_for_query")
      private Component noResultsForQuery = Component.text("No results for query");

      @PlainComponent
      @Key("available_commands")
      private Component availableCommands = Component.text("Available Commands");

      @PlainComponent
      @Key("click_to_show_help")
      private Component clickToShowHelp = Component.text("Click to show help for this command");

      @PlainComponent
      @Key("page_out_of_range")
      private Component pageOutOfRange =
          Component.text("Error: Page <page> is not in range. Must be in range [1, <max_pages>]");

      @PlainComponent
      @Key("click_for_next_page")
      private Component clickForNextPage = Component.text("Click for next page");

      @PlainComponent
      @Key("click_for_previous_page")
      private Component clickForPreviousPage = Component.text("Click for previous page");

      @Comment("The colors of the /papisigns help are controlled here")
      private HelpColors helpColors = MinecraftHelp.DEFAULT_HELP_COLORS;

      @Ignore private Map<String, Component> cachedMessages;

      public Component getMessage(String key) {
        if (cachedMessages == null) {
          cachedMessages = new HashMap<>();
          for (var field : this.getClass().getDeclaredFields()) {
            if (!Component.class.isAssignableFrom(field.getType())) {
              continue;
            }
            var fieldKey = AnnotationUtils.getKey(field);
            field.setAccessible(true);
            try {
              cachedMessages.put(fieldKey, (Component) field.get(this));
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }
        }
        return cachedMessages.get(key);
      }

      public HelpColors getHelpColors() {
        return helpColors;
      }
    }

    public Component getNotASign() {
      return notASign;
    }

    public Component getForbiddenPlaceholder() {
      return forbiddenPlaceholder;
    }

    public Component getLineChangedSuccess() {
      return lineChangedSuccess;
    }

    public Component getColorChangedSuccess() {
      return colorChangedSuccess;
    }

    public Component getWarningNotSet() {
      return warningNotSet;
    }

    public Component getReloadSuccess() {
      return reloadSuccess;
    }

    public HelpMessages getHelpCmd() {
      return helpCmd;
    }
  }

  public int getMaxDistance() {
    return this.maxDistance;
  }

  public List<String> getForbiddenPlaceholders() {
    return this.forbiddenPlaceholders;
  }

  public Messages getMessages() {
    return messages;
  }
}
