package com.github.skiwi2.hearthmonitor;

import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.ActionStartEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.CreateGameEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.FullEntityEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.ShowEntityEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.TagChangeEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.zone.TransitioningEntryParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to get default entry parser sets.
 *
 * @author Frank van Heeswijk
 */
public final class EntryParsers {
    private EntryParsers() {
        throw new UnsupportedOperationException();
    }

    private static final Set<EntryParser.Factory<? extends EntryParser>> HEARTHSTONE_ENTRY_PARSER_FACTORIES =
        new HashSet<>(Arrays.<EntryParser.Factory<? extends EntryParser>>asList(
            CreateGameEntryParser.createFactory(),
            FullEntityEntryParser.createFactory(),
            TagChangeEntryParser.createFactory(),
            //disabled TransitioningLogEntry as it should be covered by tag updates
//            TransitioningEntryParser.createFactory(),
            ShowEntityEntryParser.createFactory(),
            ActionStartEntryParser.createFactory(
                new HashSet<>(Arrays.<EntryParser.Factory<? extends EntryParser>>asList(
                    CreateGameEntryParser.createFactory(),
                    FullEntityEntryParser.createFactory(),
                    TagChangeEntryParser.createFactory(),
                    //disabled TransitioningLogEntry as it should be covered by tag updates
//                    TransitioningEntryParser.createFactory(),
                    ShowEntityEntryParser.createFactory()
                ))
            )
        ));

    private static final Set<EntryParser> HEARTHSTONE_ENTRY_PARSERS = HEARTHSTONE_ENTRY_PARSER_FACTORIES.stream()
        .map(factory -> factory.create(0))
        .collect(Collectors.<EntryParser>toSet());

    public static Set<EntryParser.Factory<? extends EntryParser>> getHearthstoneEntryParserFactories() {
        return new HashSet<>(HEARTHSTONE_ENTRY_PARSER_FACTORIES);
    }

    public static Set<EntryParser> getHearthStoneEntryParsers() {
        return new HashSet<>(HEARTHSTONE_ENTRY_PARSERS);
    }
}
