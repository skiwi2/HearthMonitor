package com.github.skiwi2.hearthmonitor.model;

import com.cardshifter.modapi.base.Component;
import com.github.skiwi2.hearthmonitor.CardData;

import java.util.Optional;

/**
 * @author Frank van Heeswijk
 */
public class CardDataComponent extends Component {
    private CardData cardData;

    public CardDataComponent() {

    }

    public CardDataComponent(final CardData cardData) {
        this.cardData = cardData;
    }

    public void setCardData(final CardData cardData) {
        this.cardData = cardData;
    }

    public CardData getCardData() {
        return cardData;
    }

    public boolean hasCardData() {
        return (cardData != null);
    }
}
