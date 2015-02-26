package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.base.ECSGame;
import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.ActionStartLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.ShowEntityLogEntry;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Command to execute the ActionStartCommand in the game.
 *
 * @author Frank van Heeswijk
 */
public class ActionStartCommand extends AbstractCommand {
    private static final Map<Class<?>, BiFunction<CommandContext, LogEntry, Command>> COMMAND_MAP = new HashMap<>();
    static {
        COMMAND_MAP.put(FullEntityLogEntry.class, (commandContext, logEntry) -> new FullEntityCommand(commandContext, (FullEntityLogEntry)logEntry));
        COMMAND_MAP.put(TagChangeLogEntry.class, (commandContext, logEntry) -> new TagChangeCommand(commandContext, (TagChangeLogEntry)logEntry));
        //disabled TransitioningLogEntry as it should be covered by tag updates
//        COMMAND_MAP.put(TransitioningLogEntry.class, (commandContext, logEntry) -> new TransitioningCommand(commandContext, (TransitioningLogEntry)logEntry));
        COMMAND_MAP.put(ShowEntityLogEntry.class, (commandContext, logEntry) -> new ShowEntityCommand(commandContext, (ShowEntityLogEntry)logEntry));
        COMMAND_MAP.put(ActionStartLogEntry.class, (commandContext, logEntry) -> new ActionStartCommand(commandContext, (ActionStartLogEntry)logEntry));
    }
    //TODO refactor to a common class to hold the COMMAND_MAP

    private final CommandContext commandContext;
    private final ActionStartLogEntry actionStartLogEntry;

    private List<Command> logEntryCommands;

    /**
     * Constructs a new ActionStartCommand instance.
     *
     * @param commandContext   The command context
     * @param actionStartLogEntry   The log entry
     * @throws  java.lang.NullPointerException  If actionStartLogEntry is null.
     */
    public ActionStartCommand(final CommandContext commandContext, final ActionStartLogEntry actionStartLogEntry) {
        this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
        this.actionStartLogEntry = Objects.requireNonNull(actionStartLogEntry, "actionStartLogEntry");
    }

    @Override
    protected void executeImpl() {
        List<LogEntry> logEntries = actionStartLogEntry.getLogEntries();
        logEntryCommands = logEntries.stream()
            .map(logEntry -> {
                if (!COMMAND_MAP.containsKey(logEntry.getClass())) {
                    //ignore
                    System.out.println("No mapping has been found for " + logEntry.getClass());
                    return null;
                }
                return COMMAND_MAP.get(logEntry.getClass()).apply(commandContext, logEntry);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        logEntryCommands.forEach(Command::execute);
    }

    @Override
    protected void undoImpl() {
        Collections.reverse(logEntryCommands);
        logEntryCommands.forEach(Command::undo);
    }
}
