package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.base.Entity;
import com.github.skiwi2.hearthmonitor.CardData;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;
import com.github.skiwi2.hearthmonitor.model.CardDataComponent;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod.HearthStoneAttribute;

import java.util.Objects;

/**
 * Command to execute the TransitioningLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class TransitioningCommand extends AbstractCommand {
    private final CommandContext commandContext;
    private final TransitioningLogEntry transitioningLogEntry;

    private Command addNewEntityCommand = new EmptyCommand();

    private String oldZone;
    private CardData oldCardData;

    /**
     * Constructs a new TransitioningCommand instance.
     *
     * @param commandContext   The command context
     * @param transitioningLogEntry    The log entry
     * @throws NullPointerException   If transitioningLogEntry is null.
     */
    public TransitioningCommand(final CommandContext commandContext, final TransitioningLogEntry transitioningLogEntry) {
        this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
        this.transitioningLogEntry = Objects.requireNonNull(transitioningLogEntry, "transitioningLogEntry");
    }

    @Override
    protected void executeImpl() {
        if (!commandContext.shouldHaveEntity(transitioningLogEntry.getEntity())) {
            addNewEntityCommand = commandContext.createAddEntityCommand(transitioningLogEntry.getEntity(), new AbstractCommand() {
                @Override
                protected void executeImpl() {
                    TransitioningCommand.this.executeImpl();
                }

                @Override
                protected void undoImpl() {
                    TransitioningCommand.this.undoImpl();
                }
            });
            addNewEntityCommand.execute();
            return;
        }
        Entity logEntity = commandContext.getEntity(transitioningLogEntry.getEntity());

        AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE);
        oldZone = attributeRetriever.getFor(logEntity);
        attributeRetriever.attrFor(logEntity).set(transitioningLogEntry.getTargetZone());

        oldCardData = logEntity.getComponent(CardDataComponent.class).getCardData();
        if (transitioningLogEntry.getEntity() instanceof CardEntityLogObject) {
            String cardId = ((CardEntityLogObject)transitioningLogEntry.getEntity()).getCardId();
            if (!cardId.isEmpty()) {
                logEntity.getComponent(CardDataComponent.class).setCardData(CardData.getForCardId(cardId));
            }
        }
    }

    @Override
    protected void undoImpl() {
        Entity logEntity = commandContext.getEntity(transitioningLogEntry.getEntity());

        AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE);
        attributeRetriever.attrFor(logEntity).set(oldZone);

        logEntity.getComponent(CardDataComponent.class).setCardData(oldCardData);

        addNewEntityCommand.undo();
    }
}
