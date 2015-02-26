package com.github.skiwi2.hearthmonitor.model;

import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.resources.ECSResource;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A HearthStone mod implementation using the Cardshifter framework.
 *
 * @author Frank van Heeswijk
 */
public class HearthStoneMod implements ECSMod {
    public enum HearthStoneResource implements ECSResource {
        ATK, HEALTH, CONTROLLER, ENTITY_ID, COST, CREATOR, ZONE_POSITION, CANT_PLAY, BATTLECRY,
        PREMIUM, DEATHRATTLE, TAUNT, DIVINE_SHIELD, CHARGE, TRIGGER_VISUAL, FORGETFUL, EXHAUSTED,
        AURA, SPELLPOWER, IGNORE_DAMAGE, IGNORE_DAMAGE_OFF, CANT_BE_TARGETED_BY_ABILITIES, CANT_BE_TARGETED_BY_HERO_POWERS,
        NUM_TURNS_IN_PLAY, JUST_PLAYED, NUM_ATTACKS_THIS_TURN, DAMAGE, FROZEN, SECRET, POWERED_UP, CARD_TARGET,
        ATTACHED, LAST_AFFECTED_BY, FREEZE, DISPLAYED_CREATOR, STEALTH, CANT_BE_ATTACKED,
        ELITE, CANT_BE_TARGETED_BY_OPPONENTS, STARTHANDSIZE, MAXHANDSIZE, HERO_ENTITY, NUM_TURNS_LEFT, FIRST_PLAYER,
        CURRENT_PLAYER, PLAYER_ID, TIMEOUT, TEAM_ID, MAXRESOURCES, ENRAGED, WINDFURY, ADJACENT_BUFF, ARMOR,
        TURN, NUM_MINIONS_KILLED_THIS_TURN, TURN_START,
        _10{
            @Override
            public String toString() {
                return "10";
            }
        };

        private static final Map<String, HearthStoneResource> MAPPING =
            Arrays.stream(HearthStoneResource.values())
                .collect(Collectors.toMap(HearthStoneResource::toString, i -> i));

        public static boolean hasResource(final String resource) {
            Objects.requireNonNull(resource, "resource");
            return MAPPING.containsKey(resource);
        }

        public static HearthStoneResource getResource(final String resource) {
            Objects.requireNonNull(resource, "resource");
            return MAPPING.get(resource);
        }
    }

    public enum HearthStoneAttribute implements ECSAttribute {
        ZONE, FACTION, CARDTYPE, RARITY, CLASS, PLAYSTATE, NEXT_STEP, STATE, STEP;

        private static final Map<String, HearthStoneAttribute> MAPPING =
            Arrays.stream(HearthStoneAttribute.values())
                .collect(Collectors.toMap(HearthStoneAttribute::toString, i -> i));

        public static boolean hasAttribute(final String attribute) {
            Objects.requireNonNull(attribute, "attribute");
            return MAPPING.containsKey(attribute);
        }

        public static HearthStoneAttribute getAttribute(final String attribute) {
            Objects.requireNonNull(attribute, "attribute");
            return MAPPING.get(attribute);
        }
    }

    public static boolean isHearthStoneResource(final String resource) {
        return HearthStoneResource.hasResource(resource);
    }

    public static HearthStoneResource getHearthStoneResource(final String resource) {
        return HearthStoneResource.getResource(resource);
    }

    public static boolean isHearthStoneAttribute(final String attribute) {
        return HearthStoneAttribute.hasAttribute(attribute);
    }

    public static HearthStoneAttribute getHearthStoneAttribute(final String attribute) {
        return HearthStoneAttribute.getAttribute(attribute);
    }

    @Override
    public void setupGame(final ECSGame ecsGame) {

    }
}
