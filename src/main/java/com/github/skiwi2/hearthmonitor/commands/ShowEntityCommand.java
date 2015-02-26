package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.CardData;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.ShowEntityLogEntry;
import com.github.skiwi2.hearthmonitor.model.CardDataComponent;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Command to execute the ShowEntityLogEntity on a game.
 *
 * @author Frank van Heeswijk
 */
public class ShowEntityCommand extends AbstractCommand {
    private final CommandContext commandContext;
    private final ShowEntityLogEntry showEntityLogEntry;

    private final Map<ECSResource, Integer> oldResourceMapping = new HashMap<>();
    private final Map<ECSAttribute, String> oldAttributeMapping = new HashMap<>();

    private Command addNewEntityCommand = new EmptyCommand();

    private CardData oldCardData;

    /**
     * Constructs a new ShowEntityCommand instance.
     *
     * @param commandContext   The command context
     * @param showEntityLogEntry    The log entry
     * @throws  java.lang.NullPointerException  If showEntityLogEntry is null.
     */
    public ShowEntityCommand(final CommandContext commandContext, final ShowEntityLogEntry showEntityLogEntry) {
        this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
        this.showEntityLogEntry = Objects.requireNonNull(showEntityLogEntry, "showEntityLogEntry");
    }

    @Override
    protected void executeImpl() {
        if (!commandContext.hasEntity(showEntityLogEntry.getEntity())) {
            addNewEntityCommand = commandContext.createAddEntityCommand(showEntityLogEntry.getEntity(), new AbstractCommand() {
                @Override
                protected void executeImpl() {
                    ShowEntityCommand.this.executeImpl();
                }

                @Override
                protected void undoImpl() {
                    ShowEntityCommand.this.undoImpl();
                }
            });
            addNewEntityCommand.execute();
            return;
        }
        Entity logEntity = commandContext.getEntity(showEntityLogEntry.getEntity());

        showEntityLogEntry.getTagValues().forEach((tag, value) -> {
            if (HearthStoneMod.isHearthStoneResource(tag)) {
                ECSResource resource = HearthStoneMod.getHearthStoneResource(tag);
                ResourceRetriever resourceRetriever = ResourceRetriever.forResource(resource);
                int oldValue = resourceRetriever.getOrDefault(logEntity, 0);
                oldResourceMapping.put(resource, oldValue);
                resourceRetriever.resFor(logEntity).set(Integer.parseInt(value));   //TODO catch NPE?
            } else if (HearthStoneMod.isHearthStoneAttribute(tag)) {
                ECSAttribute attribute = HearthStoneMod.getHearthStoneAttribute(tag);
                AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(attribute);
                String oldValue = attributeRetriever.getOrDefault(logEntity, "");
                oldAttributeMapping.put(attribute, oldValue);
                attributeRetriever.attrFor(logEntity).set(value);
            } else {
                System.out.println("Tag " + tag + " matches neither a resource nor an attribute.");
            }
        });

        oldCardData = logEntity.getComponent(CardDataComponent.class).getCardData();
        if (showEntityLogEntry.getEntity() instanceof CardEntityLogObject) {
            String cardId = ((CardEntityLogObject)showEntityLogEntry.getEntity()).getCardId();
            if (!cardId.isEmpty()) {
                logEntity.getComponent(CardDataComponent.class).setCardData(CardData.getForCardId(cardId));
            }
        }
    }

    @Override
    protected void undoImpl() {
        Entity logEntity = commandContext.getEntity(showEntityLogEntry.getEntity());

        oldResourceMapping.forEach((resource, value) -> ResourceRetriever.forResource(resource).resFor(logEntity).set(value));
        oldAttributeMapping.forEach((attribute, value) -> AttributeRetriever.forAttribute(attribute).attrFor(logEntity).set(value));

        logEntity.getComponent(CardDataComponent.class).setCardData(oldCardData);

        addNewEntityCommand.undo();
    }
}
