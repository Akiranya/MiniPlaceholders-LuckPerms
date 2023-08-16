package io.github.miniplaceholders.expansion.luckperms.common;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static io.github.miniplaceholders.api.utils.Components.FALSE_COMPONENT;
import static io.github.miniplaceholders.api.utils.Components.TRUE_COMPONENT;
import static io.github.miniplaceholders.api.utils.LegacyUtils.parsePossibleLegacy;

public record CommonExpansion(LuckPerms luckPerms) {
    private static final Component UNDEFINED_COMPONENT = Component.text("undefined", NamedTextColor.GRAY);

    public Expansion.Builder commonBuilder() {
        return Expansion.builder("luckperms")
            .audiencePlaceholder("prefix", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                Component content = parsePossibleLegacy(user.getCachedData().getMetaData().getPrefix());
                return optionalSelfClosing(aud, queue, ctx, content);
            })
            .audiencePlaceholder("suffix", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                Component content = parsePossibleLegacy(user.getCachedData().getMetaData().getSuffix());
                return optionalSelfClosing(aud, queue, ctx, content);
            })
            .audiencePlaceholder("has_permission", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String permission = queue.popOr(() -> "you need to introduce an permission").value();
                Tristate result = user.getCachedData().getPermissionData().checkPermission(permission);
                return Tag.selfClosingInserting(result.asBoolean()
                    ? TRUE_COMPONENT
                    : FALSE_COMPONENT
                );
            })
            .audiencePlaceholder("check_permission", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                final String permission = queue.popOr(() -> "you need to introduce an permission").value();
                final Tristate result = user.getCachedData().getPermissionData().checkPermission(permission);
                return Tag.selfClosingInserting(switch (result) {
                    case TRUE -> TRUE_COMPONENT;
                    case FALSE -> FALSE_COMPONENT;
                    case UNDEFINED -> UNDEFINED_COMPONENT;
                });
            })
            .audiencePlaceholder("inherited_groups", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                final Component groups = user.getInheritedGroups(user.getQueryOptions()).stream()
                    .map(group -> parsePossibleLegacy(group.getDisplayName()))
                    .collect(Component.toComponent(Component.text(", ")));
                return Tag.selfClosingInserting(groups);
            })
            .audiencePlaceholder("primary_group_name", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                final String primaryGroup = user.getCachedData().getMetaData().getPrimaryGroup();
                if (primaryGroup == null) return null;

                return Tag.preProcessParsed(primaryGroup);
            })
            .audiencePlaceholder("inherits_group", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                Group group = luckPerms.getGroupManager().getGroup(queue.popOr("you need to provide a group").value());
                return Tag.selfClosingInserting(group != null && user.getInheritedGroups(user.getQueryOptions()).contains(group)
                    ? TRUE_COMPONENT
                    : FALSE_COMPONENT
                );
            })
            .audiencePlaceholder("meta", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String value = user.getCachedData().getMetaData().getMetaValue(queue.popOr("you need to provide a metadata key").value());
                if (value == null) return null;

                Component content = ctx.deserialize(value);
                return optionalSelfClosing(aud, queue, ctx, content);
            })
            .audiencePlaceholder("context", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String value = luckPerms.getContextManager()
                    .getContext(user)
                    .orElseGet(() -> luckPerms.getContextManager().getStaticContext()) // fallback to static context
                    .getAnyValue(queue.popOr("you need to provide a context key").value())
                    .orElse(null);
                if (value == null) return null;

                Component content = ctx.deserialize(value);
                return optionalSelfClosing(aud, queue, ctx, content);
            })
            .audiencePlaceholder("static_context", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String value = luckPerms.getContextManager()
                    .getStaticContext()
                    .getAnyValue(queue.popOr("you need to provide a context key").value())
                    .orElse(null);
                if (value == null) return null;

                Component content = ctx.deserialize(value);
                return optionalSelfClosing(aud, queue, ctx, content);
            })
            .audiencePlaceholder("expiry_time", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String node = queue.popOr("you need to provide a node").value();
                String accuracy = queue.popOr("you need to provide a unit").value();
                QueryOptions queryOptions = user.getQueryOptions();
                String value = user.getNodes().stream()
                    .filter(Node::hasExpiry)
                    .filter(n -> n.getKey().equals(node))
                    .filter(n -> queryOptions.satisfies(n.getContexts()))
                    .map(Node::getExpiryDuration)
                    .filter(Objects::nonNull)
                    .filter(d -> !d.isNegative())
                    .findFirst()
                    .map(duration -> formatDuration(duration, accuracy))
                    .orElse(null);
                if (value == null) return null;

                return Tag.preProcessParsed(value);
            })
            .audiencePlaceholder("inherited_expiry_time", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String node = queue.popOr("you need to provide a node").value();
                String accuracy = queue.popOr("you need to provide a unit").value();
                QueryOptions queryOptions = user.getQueryOptions();
                String value = user.resolveInheritedNodes(queryOptions).stream()
                    .filter(Node::hasExpiry)
                    .filter(n -> n.getKey().equals(node))
                    .map(Node::getExpiryDuration)
                    .filter(Objects::nonNull)
                    .filter(d -> !d.isNegative())
                    .findFirst()
                    .map(duration -> formatDuration(duration, accuracy))
                    .orElse(null);
                if (value == null) return null;

                return Tag.preProcessParsed(value);
            })
            .audiencePlaceholder("group_expiry_time", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String group = queue.popOr("you need to provide a group").value();
                String accuracy = queue.popOr("you need to provide a unit").value();
                QueryOptions queryOptions = user.getQueryOptions();
                String value = user.getNodes(NodeType.INHERITANCE).stream()
                    .filter(Node::hasExpiry)
                    .filter(n -> n.getGroupName().equals(group))
                    .filter(n -> queryOptions.satisfies(n.getContexts()))
                    .map(Node::getExpiryDuration)
                    .filter(Objects::nonNull)
                    .filter(d -> !d.isNegative())
                    .findFirst()
                    .map(duration -> formatDuration(duration, accuracy))
                    .orElse(null);
                if (value == null) return null;

                return Tag.preProcessParsed(value);
            })
            .audiencePlaceholder("inherited_group_expiry_time", (aud, queue, ctx) -> {
                final User user = user(aud);
                if (user == null) return null;

                String group = queue.popOr("you need to provide a group").value();
                String accuracy = queue.popOr("you need to provide a unit").value();
                QueryOptions queryOptions = user.getQueryOptions();
                String value = user.resolveInheritedNodes(queryOptions).stream()
                    .filter(Node::hasExpiry)
                    .filter(NodeType.INHERITANCE::matches)
                    .map(NodeType.INHERITANCE::cast)
                    .filter(n -> n.getGroupName().equals(group))
                    .map(Node::getExpiryDuration)
                    .filter(Objects::nonNull)
                    .filter(d -> !d.isNegative())
                    .findFirst()
                    .map(duration -> formatDuration(duration, accuracy))
                    .orElse(null);
                if (value == null) return null;

                return Tag.preProcessParsed(value);
            });
    }

    private User user(final Audience audience) {
        final UUID uuid = audience.get(Identity.UUID).orElse(null);
        if (uuid == null) {
            return null;
        }
        return luckPerms.getUserManager().getUser(uuid);
    }

    /**
     * This should be used at last as it reads the queue.
     */
    private @NotNull Tag optionalSelfClosing(Audience audience, ArgumentQueue queue, Context context, Component content) {
        Tag.Argument peek = queue.peek();
        if (peek != null && peek.value().equals("close")) {
            return Tag.selfClosingInserting(content);
        }
        return Tag.inserting(content);
    }

    /**
     * Format a duration using the LuckPerms formatter.
     *
     * @param duration the duration
     * @return a formatted version of the duration
     */
    private @NotNull String formatDuration(final @NotNull Duration duration, String accuracy) {
        return switch (accuracy) {
            case "y" -> DurationFormatter.YEARS.format(duration);
            case "mo" -> DurationFormatter.MONTHS.format(duration);
            case "w" -> DurationFormatter.WEEKS.format(duration);
            case "d" -> DurationFormatter.DAYS.format(duration);
            case "h" -> DurationFormatter.HOURS.format(duration);
            case "m" -> DurationFormatter.MINUTES.format(duration);
            case "s" -> DurationFormatter.SECONDS.format(duration);
            default -> throw new IllegalArgumentException("unknown argument: " + accuracy);
        };
    }
}
