package com.github.skiwi2.hearthmonitor;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Frank van Heeswijk
 */
public class CardData {
    private static final CardDataLoader CARD_DATA_LOADER = new CardDataLoader();

    private final String id;
    private final String name;

    public CardData(final String id, final String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static CardData getForCardId(final String cardId) {
        Objects.requireNonNull(cardId, "cardId");
        return CARD_DATA_LOADER.load(cardId);
    }

    private static class CardDataLoader {
        private final Map<String, CardData> cardDataMap = new HashMap<>();
        private final ScriptEngine scriptEngine;

        private CardDataLoader() {
            try {
                Path setListPath = Paths.get(CardData.class.getResource("SetList.json").toURI());
                String setList = new String(Files.readAllBytes(setListPath), StandardCharsets.UTF_8);

                Path allSetsPath = Paths.get(CardData.class.getResource("AllSets.json").toURI());
                String allSets = new String(Files.readAllBytes(allSetsPath), StandardCharsets.UTF_8);

                scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
                scriptEngine.eval(new InputStreamReader(new FileInputStream(Paths.get(CardData.class.getResource("CardData.js").toURI()).toFile()), StandardCharsets.UTF_8));

                Invocable invocable = (Invocable)scriptEngine;
                invocable.invokeFunction("setSetList", setList);
                invocable.invokeFunction("setAllSets", allSets);
            } catch (URISyntaxException | IOException | ScriptException | NoSuchMethodException ex) {
                throw new IllegalStateException("an error has occurred when initializing the card data loader", ex);
            }
        }

        private CardData load(final String cardId) {
            if (cardDataMap.containsKey(cardId)) {
                return cardDataMap.get(cardId);
            }
            try {
                CardData cardData = load0(cardId);
                cardDataMap.put(cardId, cardData);
                return cardData;
            } catch (ScriptException | NoSuchMethodException ex) {
                throw new IllegalStateException("an error occurred when loading cardId = " + cardId, ex);
            }
        }

        private CardData load0(final String cardId) throws ScriptException, NoSuchMethodException {
            Invocable invocable = (Invocable)scriptEngine;
            CardData cardData = (CardData)invocable.invokeFunction("getCardData", cardId);
            if (cardData == null) {
                throw new IllegalStateException("could not find card for cardId = " + cardId);
            }
            return cardData;
        }
    }
}
