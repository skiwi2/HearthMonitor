package com.github.skiwi2.hearthmonitor;

import com.github.skiwi2.hearthmonitor.logreader.EntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.CreateGameEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.FullEntityEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.power.TagChangeEntryParser;
import com.github.skiwi2.hearthmonitor.logreader.hearthstone.zone.TransitioningEntryParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to get default entry parser sets.
 *
 * @author Frank van Heeswijk
 */
public final class EntryParsers {
    private EntryParsers() {
        throw new UnsupportedOperationException();
    }

    private static final Set<EntryParser> HEARTHSTONE_ENTRY_PARSERS =
        new HashSet<>(Arrays.asList(
            CreateGameEntryParser.createForIndentation(0),
            FullEntityEntryParser.createForIndentation(0),
            TagChangeEntryParser.createForIndentation(0),
            TransitioningEntryParser.createForIndentation(0)
        ));

    public static Set<EntryParser> getHearthStoneEntryParsers() {
        return new HashSet<>(HEARTHSTONE_ENTRY_PARSERS);
    }
}
