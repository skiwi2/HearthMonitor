package com.github.skiwi2.hearthmonitor.model;

import com.cardshifter.modapi.base.Component;
import com.github.skiwi2.hearthmonitor.CardData;

import java.util.Objects;

/**
 * @author Frank van Heeswijk
 */
public class CardDataComponent extends Component {
    private final CardData cardData;

    public CardDataComponent(final CardData cardData) {
        this.cardData = Objects.requireNonNull(cardData, "cardData");
    }

    public CardData getCardData() {
        return cardData;
    }
}
