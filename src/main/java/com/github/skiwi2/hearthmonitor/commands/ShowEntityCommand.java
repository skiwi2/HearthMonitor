package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.CardData;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
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
    private final ECSGame ecsGame;
    private final ShowEntityLogEntry showEntityLogEntry;

    private final Map<ECSResource, Integer> oldResourceMapping = new HashMap<>();
    private final Map<ECSAttribute, String> oldAttributeMapping = new HashMap<>();

    private CardData oldCardData;

    /**
     * Constructs a new ShowEntityCommand instance.
     *
     * @param ecsGame   The game instance
     * @param showEntityLogEntry    The log entry
     * @throws  java.lang.NullPointerException  If showEntityLogEntry is null.
     */
    public ShowEntityCommand(final ECSGame ecsGame, final ShowEntityLogEntry showEntityLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.showEntityLogEntry = Objects.requireNonNull(showEntityLogEntry, "showEntityLogEntry");
    }

    @Override
    protected void executeImpl() {
        ResourceRetriever entityIdRetriever = ResourceRetriever.forResource(HearthStoneMod.HearthStoneResource.ENTITY_ID);
        Entity logEntity = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> {
                int entityId = entityIdRetriever.getFor(entity);
                EntityLogObject entityLogObject = showEntityLogEntry.getEntity();
                if (entityLogObject instanceof PlayerEntityLogObject) {
                    return false;
                }
                CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
                int showEntityEntityId = Integer.parseInt(cardEntityLogObject.getId());
                return (entityId == showEntityEntityId);
            })
            .findFirst().orElse(null);
        if (logEntity == null) {
            return; //TODO fix when ActionStartLogEntry entries are getting processed
        }

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
        ResourceRetriever entityIdRetriever = ResourceRetriever.forResource(HearthStoneMod.HearthStoneResource.ENTITY_ID);
        Entity logEntity = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> {
                int entityId = entityIdRetriever.getFor(entity);
                EntityLogObject entityLogObject = showEntityLogEntry.getEntity();
                if (entityLogObject instanceof PlayerEntityLogObject) {
                    return false;
                }
                CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
                int showEntityEntityId = Integer.parseInt(cardEntityLogObject.getId());
                return (entityId == showEntityEntityId);
            })
            .findFirst().orElse(null);
        if (logEntity == null) {
            return; //TODO fix when ActionStartLogEntry entries are getting processed
        }

        oldResourceMapping.forEach((resource, value) -> ResourceRetriever.forResource(resource).resFor(logEntity).set(value));
        oldAttributeMapping.forEach((attribute, value) -> AttributeRetriever.forAttribute(attribute).attrFor(logEntity).set(value));

        logEntity.getComponent(CardDataComponent.class).setCardData(oldCardData);
    }
}
