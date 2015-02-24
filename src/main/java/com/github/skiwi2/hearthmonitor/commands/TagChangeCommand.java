package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.TagChangeLogEntry;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod;

import java.util.Objects;

/**
 * Command to execute the TagChangeLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class TagChangeCommand extends AbstractCommand {
    private final ECSGame ecsGame;
    private final TagChangeLogEntry tagChangeLogEntry;

    private int oldResourceValue;
    private String oldAttributeValue;

    /**
     * Constructs a new TagChangeCommand instance.
     *
     * @param ecsGame   The game instance
     * @param tagChangeLogEntry    The log entry
     * @throws NullPointerException   If tagChangeLogEntry is null.
     */
    public TagChangeCommand(final ECSGame ecsGame, final TagChangeLogEntry tagChangeLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.tagChangeLogEntry = Objects.requireNonNull(tagChangeLogEntry, "tagChangeLogEntry");
    }

    @Override
    protected void executeImpl() {
        ResourceRetriever entityIdRetriever = ResourceRetriever.forResource(HearthStoneMod.HearthStoneResource.ENTITY_ID);
        Entity logEntity = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> {
                int entityId = entityIdRetriever.getFor(entity);
                EntityLogObject entityLogObject = tagChangeLogEntry.getEntity();
                if (entityLogObject instanceof PlayerEntityLogObject) {
                    return false;
                }
                CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
                int tagChangeEntityId = Integer.parseInt(cardEntityLogObject.getId());
                return (entityId == tagChangeEntityId);
            })
            .findFirst().orElse(null);
        if (logEntity == null) {
            return; //TODO fix when ActionStartLogEntry entries are getting processed
        }

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
        ResourceRetriever entityIdRetriever = ResourceRetriever.forResource(HearthStoneMod.HearthStoneResource.ENTITY_ID);
        Entity logEntity = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> {
                int entityId = entityIdRetriever.getFor(entity);
                EntityLogObject entityLogObject = tagChangeLogEntry.getEntity();
                if (entityLogObject instanceof PlayerEntityLogObject) {
                    return false;
                }
                CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
                int tagChangeEntityId = Integer.parseInt(cardEntityLogObject.getId());
                return (entityId == tagChangeEntityId);
            })
            .findFirst().orElse(null);
        if (logEntity == null) {
            return; //TODO fix when ActionStartLogEntry entries are getting processed
        }

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
    }
}
