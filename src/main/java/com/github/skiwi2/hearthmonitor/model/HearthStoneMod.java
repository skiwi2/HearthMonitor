package com.github.skiwi2.hearthmonitor.model;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.resources.ECSResource;

/**
 * A HearthStone mod implementation using the Cardshifter framework.
 *
 * @author Frank van Heeswijk
 */
public class HearthStoneMod implements ECSMod {
    public enum HearthStoneResources implements ECSResource {
        ATTACK, HEALTH;
    }

    @Override
    public void setupGame(final ECSGame ecsGame) {

    }
}
