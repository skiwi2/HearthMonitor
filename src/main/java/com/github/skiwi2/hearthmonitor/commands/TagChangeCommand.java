package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod;

import java.util.Objects;

/**
 * Command to execute the TagChangeLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class TagChangeCommand extends AbstractCommand {
    private final CommandContext commandContext;
    private final TagChangeLogEntry tagChangeLogEntry;

    private Command addNewEntityCommand = new EmptyCommand();
    private Command discoveredEntityCommand = new EmptyCommand();

    private int oldResourceValue;
    private String oldAttributeValue;

    /**
     * Constructs a new TagChangeCommand instance.
     *
     * @param commandContext   The command context
     * @param tagChangeLogEntry    The log entry
     * @throws NullPointerException   If tagChangeLogEntry is null.
     */
    public TagChangeCommand(final CommandContext commandContext, final TagChangeLogEntry tagChangeLogEntry) {
        this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
        this.tagChangeLogEntry = Objects.requireNonNull(tagChangeLogEntry, "tagChangeLogEntry");
    }

    @Override
    protected void executeImpl() {
        if (!commandContext.shouldHaveEntity(tagChangeLogEntry.getEntity())) {
            addNewEntityCommand = commandContext.createAddEntityCommand(tagChangeLogEntry.getEntity(), new AbstractCommand() {
                @Override
                protected void executeImpl() {
                    TagChangeCommand.this.executeImpl();
                }

                @Override
                protected void undoImpl() {
                    TagChangeCommand.this.undoImpl();
                }
            });
            addNewEntityCommand.execute();

            String tag = tagChangeLogEntry.getTag();
            String value = tagChangeLogEntry.getValue();
            if (HearthStoneMod.isHearthStoneResource(tag)) {
                ECSResource resource = HearthStoneMod.HearthStoneResource.getResource(tag);
                if (resource == HearthStoneMod.HearthStoneResource.ENTITY_ID) {
                    discoveredEntityCommand = commandContext.createDiscoveredEntityCommand(tagChangeLogEntry.getEntity(), Integer.parseInt(value));
                    discoveredEntityCommand.execute();
                }
            }
            return;
        }
        Entity logEntity = commandContext.getEntity(tagChangeLogEntry.getEntity());

        String tag = tagChangeLogEntry.getTag();
        String value = tagChangeLogEntry.getValue();
        if (HearthStoneMod.isHearthStoneResource(tag)) {
            ECSResource resource = HearthStoneMod.getHearthStoneResource(tag);
            ResourceRetriever resourceRetriever = ResourceRetriever.forResource(resource);
            oldResourceValue = resourceRetriever.getOrDefault(logEntity, 0);
            resourceRetriever.resFor(logEntity).set(Integer.parseInt(value));  //TODO catch NPE?
        } else if (HearthStoneMod.isHearthStoneAttribute(tag)) {
            ECSAttribute attribute = HearthStoneMod.getHearthStoneAttribute(tag);
            AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(attribute);
            oldAttributeValue = attributeRetriever.getOrDefault(logEntity, "");
            attributeRetriever.attrFor(logEntity).set(value);
        } else {
            System.out.println("Tag " + tag + " matches neither a resource nor an attribute.");
        }
    }

    @Override
    protected void undoImpl() {
        Entity logEntity = commandContext.getEntity(tagChangeLogEntry.getEntity());

        String tag = tagChangeLogEntry.getTag();
        if (HearthStoneMod.isHearthStoneResource(tag)) {
            ECSResource resource = HearthStoneMod.getHearthStoneResource(tag);
            ResourceRetriever resourceRetriever = ResourceRetriever.forResource(resource);
            resourceRetriever.resFor(logEntity).set(oldResourceValue);
        } else if (HearthStoneMod.isHearthStoneAttribute(tag)) {
            ECSAttribute attribute = HearthStoneMod.getHearthStoneAttribute(tag);
            AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(attribute);
            attributeRetriever.attrFor(logEntity).set(oldAttributeValue);
        } else {
            System.out.println("Tag " + tag + " matches neither a resource nor an attribute.");
        }

        discoveredEntityCommand.undo();
        addNewEntityCommand.undo();
    }
}
