package com.github.skiwi2.hearthmonitor.commands;

import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.logapi.power.CardEntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.EntityLogObject;
import com.github.skiwi2.hearthmonitor.logapi.power.PlayerEntityLogObject;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod.HearthStoneResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Frank van Heeswijk
 */
public class CommandContext {
    private final ECSGame ecsGame;
    private final Map<String, Integer> playerEntityIdMap = new HashMap<>();
    private final Map<String, List<Command>> queuedPlayerCommands = new HashMap<>();

    {
        playerEntityIdMap.put("GameEntity", 1);
    }

    public CommandContext(final ECSGame ecsGame) {
        this.ecsGame = Objects.requireNonNull(ecsGame, "ecsGame");
    }

    public ECSGame getEcsGame() {
        return ecsGame;
    }

    public boolean shouldHaveEntity(final EntityLogObject entityLogObject) {
        if (entityLogObject instanceof PlayerEntityLogObject) {
            String name = ((PlayerEntityLogObject)entityLogObject).getName();
            if (name.chars().allMatch(Character::isDigit)) {
                //refers to an entity id
                return true;
            }
            return playerEntityIdMap.containsKey(name);
        }
        else if (entityLogObject instanceof CardEntityLogObject) {
            return true;
        } else {
            throw new IllegalArgumentException("cannot handle class of entityLogObject: " + entityLogObject.getClass());
        }
    }

    public boolean hasEntity(final EntityLogObject entityLogObject) {
        if (entityLogObject instanceof PlayerEntityLogObject) {
            String name = ((PlayerEntityLogObject)entityLogObject).getName();
            if (name.chars().allMatch(Character::isDigit)) {
                //refers to an entity id
                return hasEntityWithId(Integer.parseInt(name));
            }
            return playerEntityIdMap.containsKey(name);
        }
        else if (entityLogObject instanceof CardEntityLogObject) {
            return hasEntityWithId(Integer.parseInt(((CardEntityLogObject)entityLogObject).getId()));
        } else {
            throw new IllegalArgumentException("cannot handle class of entityLogObject: " + entityLogObject.getClass());
        }
    }

    public Command createAddEntityCommand(final EntityLogObject entityLogObject, final Command command) {
        if (hasEntity(entityLogObject)) {
            throw new IllegalArgumentException("an entity already exists for the given entityLogObject, can not add new entity");
        }
        if (entityLogObject instanceof PlayerEntityLogObject) {
            PlayerEntityLogObject playerEntityLogObject = (PlayerEntityLogObject)entityLogObject;
            String name = playerEntityLogObject.getName();
            if (name.chars().allMatch(Character::isDigit)) {
                //refers to an entity id
                return new EmptyCommand();
            }
            return new AbstractCommand() {
                @Override
                protected void executeImpl() {
                    queuedPlayerCommands.putIfAbsent(name, new ArrayList<>());
                    queuedPlayerCommands.get(name).add(command);
                }

                @Override
                protected void undoImpl() {
                    queuedPlayerCommands.get(name).remove(command);
                }
            };
        }
        else if (entityLogObject instanceof CardEntityLogObject) {
            return new EmptyCommand();
        }
        else {
            throw new IllegalArgumentException("cannot handle class of entityLogObject: " + entityLogObject.getClass());
        }
    }

    public Entity getEntity(final EntityLogObject entityLogObject) {
        if (entityLogObject instanceof PlayerEntityLogObject) {
            PlayerEntityLogObject playerEntityLogObject = (PlayerEntityLogObject)entityLogObject;
            String playerName = playerEntityLogObject.getName();
            if (playerName.chars().allMatch(Character::isDigit)) {
                //refers to an entity id
                return getEntityWithId(Integer.parseInt(playerName));
            }
            if (!playerEntityIdMap.containsKey(playerName)) {
                throw new IllegalStateException("player " + playerName + " has no known entity id");
            }
            int entityId = playerEntityIdMap.get(playerName);
            return getEntityWithId(entityId);
        } else if (entityLogObject instanceof CardEntityLogObject) {
            CardEntityLogObject cardEntityLogObject = (CardEntityLogObject)entityLogObject;
            int entityId = Integer.parseInt(cardEntityLogObject.getId());
            return getEntityWithId(entityId);
        } else {
            throw new IllegalArgumentException("cannot handle class of entityLogObject: " + entityLogObject.getClass());
        }
    }

    public Entity getEntityWithId(final int entityId) {
        return ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> ResourceRetriever.forResource(HearthStoneResource.ENTITY_ID).getOrDefault(entity, 0) == entityId)
            .findFirst().get();
    }

    public boolean hasEntityWithId(final int entityId) {
        return ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .anyMatch(entity -> ResourceRetriever.forResource(HearthStoneResource.ENTITY_ID).getOrDefault(entity, 0) == entityId);
    }

    public Command createDiscoveredEntityCommand(final EntityLogObject entityLogObject, final int entityId) {
        if (entityLogObject instanceof PlayerEntityLogObject) {
            String name = ((PlayerEntityLogObject)entityLogObject).getName();
            if (name.chars().allMatch(Character::isDigit)) {
                //refers to an entity id, ignore this
                return new EmptyCommand();
            }
            if (playerEntityIdMap.containsKey(name)) {
                //nothing to update
                return new EmptyCommand();
            }
            return new AbstractCommand() {
                private String oldName = "";
                private List<Command> commands = new ArrayList<>();

                @Override
                protected void executeImpl() {
                    playerEntityIdMap.put(name, entityId);
                    Entity entity = getEntityWithId(entityId);
                    if (entity.hasComponent(PlayerComponent.class)) {
                        PlayerComponent playerComponent = entity.getComponent(PlayerComponent.class);
                        oldName = playerComponent.getName();
                        playerComponent.setName(name);
                    }
                    commands.addAll(queuedPlayerCommands.get(name));
                    commands.forEach(Command::execute);
                    queuedPlayerCommands.get(name).clear();
                }

                @Override
                protected void undoImpl() {
                    queuedPlayerCommands.get(name).addAll(commands);
                    Collections.reverse(commands);
                    commands.forEach(Command::undo);
                    commands.clear();
                    Entity entity = getEntityWithId(entityId);
                    if (entity.hasComponent(PlayerComponent.class)) {
                        PlayerComponent playerComponent = entity.getComponent(PlayerComponent.class);
                        playerComponent.setName(oldName);
                    }
                    playerEntityIdMap.remove(name);
                }
            };
        } else if (entityLogObject instanceof CardEntityLogObject) {
            //nothing to update
            return new EmptyCommand();
        } else {
            throw new IllegalArgumentException("cannot handle class of entityLogObject: " + entityLogObject.getClass());
        }
    }

    //TODO add an easy way to add a tag value pair to an entity?
}
