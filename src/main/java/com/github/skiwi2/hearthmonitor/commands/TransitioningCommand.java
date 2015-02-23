package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.CardData;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.zone.TransitioningLogEntry;
import com.github.skiwi2.hearthmonitor.model.CardDataComponent;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod.HearthStoneAttribute;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod.HearthStoneResource;

import java.util.Objects;
import java.util.Optional;

/**
 * Command to execute the TransitioningLogEntry on a game.
 *
 * @author Frank van Heeswijk
 */
public class TransitioningCommand extends AbstractCommand {
    private final ECSGame ecsGame;
    private final TransitioningLogEntry transitioningLogEntry;

    private String oldZone;
    private Optional<CardDataComponent> oldCardDataComponent;

    /**
     * Constructs a new TransitioningCommand instance.
     *
     * @param ecsGame   The game instance
     * @param transitioningLogEntry    The log entry
     * @throws NullPointerException   If transitioningLogEntry is null.
     */
    public TransitioningCommand(final ECSGame ecsGame, final TransitioningLogEntry transitioningLogEntry) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
        this.transitioningLogEntry = Objects.requireNonNull(transitioningLogEntry, "transitioningLogEntry");
    }

    @Override
    protected void executeImpl() {
        ResourceRetriever entityIdRetriever = ResourceRetriever.forResource(HearthStoneResource.ENTITY_ID);
        Entity logEntity = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> {
                int entityId = entityIdRetriever.getFor(entity);
                EntityLogObject entityLogObject = transitioningLogEntry.getEntity();
                if (entityLogObject instanceof PlayerEntityLogObject) {
                    return false;
                }
                CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
                int transitioningEntityId = Integer.parseInt(cardEntityLogObject.getId());
                return (entityId == transitioningEntityId);
            })
            .findFirst().orElse(null);
        if (logEntity == null) {
            return; //TODO fix when ActionStartLogEntry entries are getting processed
        }
        AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE);
        oldZone = attributeRetriever.getFor(logEntity);
        attributeRetriever.attrFor(logEntity).set(transitioningLogEntry.getTargetZone());

        oldCardDataComponent = Optional.ofNullable(logEntity.hasComponent(CardDataComponent.class) ? logEntity.getComponent(CardDataComponent.class) : null);
        if (transitioningLogEntry.getEntity() instanceof CardEntityLogObject) {
            String cardId = ((CardEntityLogObject)transitioningLogEntry.getEntity()).getCardId();
            if (!cardId.isEmpty()) {
                if (logEntity.hasComponent(CardDataComponent.class)) {
                    logEntity.removeComponent(CardDataComponent.class);
                }
                logEntity.addComponent(new CardDataComponent(CardData.getForCardId(cardId)));
            }
        }
    }

    @Override
    protected void undoImpl() {
        ResourceRetriever entityIdRetriever = ResourceRetriever.forResource(HearthStoneResource.ENTITY_ID);
        Entity logEntity = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> {
                int entityId = entityIdRetriever.getFor(entity);
                EntityLogObject entityLogObject = transitioningLogEntry.getEntity();
                if (entityLogObject instanceof PlayerEntityLogObject) {
                    return false;
                }
                CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
                int transitioningEntityId = Integer.parseInt(cardEntityLogObject.getId());
                return (entityId == transitioningEntityId);
            })
            .findFirst().get();
        AttributeRetriever attributeRetriever = AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE);
        attributeRetriever.attrFor(logEntity).set(oldZone);

        if (logEntity.hasComponent(CardDataComponent.class)) {
            logEntity.removeComponent(CardDataComponent.class);
        }
        oldCardDataComponent.ifPresent(logEntity::addComponent);
    }
}
