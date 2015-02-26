package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.github.skiwi2.hearthmonitor.CardData;
import com.github.skiwi2.hearthmonitor.logapi.power.FullEntityLogEntry;
import com.github.skiwi2.hearthmonitor.model.CardDataComponent;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod;

import java.util.Objects;

/**
 * Command to execute the FullEntityLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class FullEntityCommand extends AbstractCommand {
    private final CommandContext commandContext;
    private final FullEntityLogEntry fullEntityLogEntry;

    private Entity newEntity;

    /**
     * Constructs a new FullEntityCommand instance.
     *
     * @param commandContext   The command context
     * @param fullEntityLogEntry    The log entry
     * @throws java.lang.NullPointerException   If fullEntityLogEntry is null.
     */
    public FullEntityCommand(final CommandContext commandContext, final FullEntityLogEntry fullEntityLogEntry) {
        this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
        this.fullEntityLogEntry = Objects.requireNonNull(fullEntityLogEntry, "fullEntityLogEntry");
    }

    @Override
    protected void executeImpl() {
        newEntity = commandContext.getEcsGame().newEntity();
        ECSResourceMap ecsResourceMap = ECSResourceMap.createFor(newEntity);
        ECSAttributeMap ecsAttributeMap = ECSAttributeMap.createFor(newEntity);
        CardDataComponent cardDataComponent = new CardDataComponent();
        if (!fullEntityLogEntry.getCardId().isEmpty()) {
            cardDataComponent.setCardData(CardData.getForCardId(fullEntityLogEntry.getCardId()));
        }
        newEntity.addComponent(cardDataComponent);
        fullEntityLogEntry.getTagValues().forEach((tag, value) -> {
            if (HearthStoneMod.isHearthStoneResource(tag)) {
                ecsResourceMap.set(HearthStoneMod.getHearthStoneResource(tag), Integer.parseInt(value));    //TODO catch NFE for robustness?
            } else if (HearthStoneMod.isHearthStoneAttribute(tag)) {
                ecsAttributeMap.set(HearthStoneMod.getHearthStoneAttribute(tag), value);
            } else {
                System.out.println("Tag " + tag + " matches neither a resource nor an attribute.");
            }
        });
    }

    @Override
    protected void undoImpl() {
        newEntity.destroy();
    }
}
