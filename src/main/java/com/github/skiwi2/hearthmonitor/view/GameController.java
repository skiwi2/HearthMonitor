package com.github.skiwi2.hearthmonitor.view;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.commands.Command;
import com.github.skiwi2.hearthmonitor.model.CardDataComponent;
import com.github.skiwi2.hearthmonitor.model.Game;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod.HearthStoneAttribute;
import com.github.skiwi2.hearthmonitor.model.HearthStoneMod.HearthStoneResource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author Frank van Heeswijk
 */
public class GameController implements Initializable {
    @FXML
    private VBox playerBox;

    @FXML
    private VBox playerHandBox;

    @FXML
    private VBox playerFieldBox;

    @FXML
    private VBox opponentFieldBox;

    @FXML
    private VBox opponentHandBox;

    @FXML
    private VBox opponentBox;

    @FXML
    private Label turnInfoLabel;

    private final Game game;
    private final ECSGame ecsGame;
    private final int maxTurns;
    private final ListIterator<Command> commandIterator;

    private int currentTurn = 0;

    public GameController(final Game game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ecsGame = game.getInitialGame();
        this.maxTurns = game.getCommands().size();
        this.commandIterator = game.getCommands().listIterator();
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        refresh();
    }

    @FXML
    private void handlePreviousButtonKeyPressed(final KeyEvent keyEvent) {
        handlePreviousButton();
    }

    @FXML
    private void handlePreviousButtonMousePressed(final MouseEvent mouseEvent) {
        handlePreviousButton();
    }

    private void handlePreviousButton() {
        if (currentTurn <= 0) {
            return;
        }
        currentTurn--;
        commandIterator.previous().undo();
        refresh();
    }

    @FXML
    private void handleNextButtonKeyPressed(final KeyEvent keyEvent) {
        handleNextButton();
    }

    @FXML
    private void handleNextButtonMousePressed(final MouseEvent mouseEvent) {
        handleNextButton();
    }

    private void handleNextButton() {
        if (currentTurn >= maxTurns) {
            return;
        }
        currentTurn++;
        commandIterator.next().execute();
        refresh();
    }

    private void refresh() {
        turnInfoLabel.setText(currentTurn + " / " + maxTurns);

        refreshPlayerBox();
        refreshPlayerHandBox();
        refreshPlayerFieldBox();
        refreshOpponentFieldBox();
        refreshOpponentHandBox();
        refreshOpponentBox();
    }

    private void refreshPlayerBox() {
        refreshBox(playerBox, 1);
    }

    private void refreshPlayerHandBox() {
        refreshHandBox(playerHandBox, 1);
    }

    private void refreshPlayerFieldBox() {
        refreshFieldBox(playerFieldBox, 1);
    }

    private void refreshOpponentFieldBox() {
        refreshFieldBox(opponentFieldBox, 2);
    }

    private void refreshOpponentHandBox() {
        refreshHandBox(opponentHandBox, 2);
    }

    private void refreshOpponentBox() {
        refreshBox(opponentBox, 2);
    }

    private void refreshBox(final VBox box, final int controllerId) {
        box.getChildren().clear();

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), "PLAY"))
            .filter(entity -> ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getOrDefault(entity, 0) == controllerId)
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), "HERO"))
            .forEach(entity -> {
                int controller = ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getFor(entity);
                Entity playerEntity = ecsGame.findEntities(innerEntity -> innerEntity.hasComponent(PlayerComponent.class) && ResourceRetriever.forResource(HearthStoneResource.PLAYER_ID).getFor(innerEntity) == controller).get(0);
                box.getChildren().add(new Label(playerEntity.getComponent(PlayerComponent.class).getName() + System.lineSeparator() + getPlayerClass(entity) + System.lineSeparator() + getHeroData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), "PLAY"))
            .filter(entity -> ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getOrDefault(entity, 0) == controllerId)
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), "HERO_POWER"))
            .forEach(entity -> {
                box.getChildren().add(new Label(getName(entity) + System.lineSeparator() + getAttackAndHitPointsData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });

        long deckRemainingSize = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), "DECK"))
            .filter(entity -> ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getOrDefault(entity, 0) == controllerId)
            .count();
        box.getChildren().add(new Label("Deck " + deckRemainingSize));
    }

    private void refreshHandBox(final VBox box, final int controllerId) {
        box.getChildren().clear();

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), "HAND"))
            .filter(entity -> ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getOrDefault(entity, 0) == controllerId)
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), "MINION")
                || Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), "ABILITY")
                || Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), ""))
            .sorted(Comparator.comparingInt(entity -> ResourceRetriever.forResource(HearthStoneResource.ZONE_POSITION).getOrDefault(entity, 0)))
            .forEach(entity -> {
                box.getChildren().add(new Label(getName(entity) + System.lineSeparator() + getAttackAndHitPointsData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });

    }

    private void refreshFieldBox(final VBox box, final int controllerId) {
        box.getChildren().clear();

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), "PLAY"))
            .filter(entity -> ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getOrDefault(entity, 0) == controllerId)
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), "MINION")
                || Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.CARDTYPE).getOrDefault(entity, ""), "ABILITY"))
            .sorted(Comparator.comparingInt(entity -> ResourceRetriever.forResource(HearthStoneResource.ZONE_POSITION).getOrDefault(entity, 0)))
            .forEach(entity -> {
                box.getChildren().add(new Label(getName(entity) + System.lineSeparator() + getAttackAndHitPointsData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });
    }

    private static String getName(final Entity entity) {
        CardDataComponent cardDataComponent = entity.getComponent(CardDataComponent.class);
        if (!cardDataComponent.hasCardData()) {
            return "???";
        }
        return cardDataComponent.getCardData().getName();
    }

    private static String getPlayerClass(final Entity entity) {
        CardDataComponent cardDataComponent = entity.getComponent(CardDataComponent.class);
        if (!cardDataComponent.hasCardData()) {
            return "???";
        }
        return cardDataComponent.getCardData().getPlayerClass().orElse("???");
    }

    private static String getHeroData(final Entity entity) {
        int attack = ResourceRetriever.forResource(HearthStoneResource.ATK).getOrDefault(entity, 0);
        int health = ResourceRetriever.forResource(HearthStoneResource.HEALTH).getOrDefault(entity, 0);
        int damage = ResourceRetriever.forResource(HearthStoneResource.DAMAGE).getOrDefault(entity, 0);
        int armor = ResourceRetriever.forResource(HearthStoneResource.ARMOR).getOrDefault(entity, 0);
        return "ATK " + attack + " / HP " + (health - damage) + " / ARMOR " + armor;
    }

    private static String getAttackAndHitPointsData(final Entity entity) {
        int attack = ResourceRetriever.forResource(HearthStoneResource.ATK).getOrDefault(entity, 0);
        int health = ResourceRetriever.forResource(HearthStoneResource.HEALTH).getOrDefault(entity, 0);
        int damage = ResourceRetriever.forResource(HearthStoneResource.DAMAGE).getOrDefault(entity, 0);
        return "ATK " + attack + " / HP " + (health - damage);
    }

    private static String getSpecialEffects(final Entity entity) {
        int justPlayed = ResourceRetriever.forResource(HearthStoneResource.JUST_PLAYED).getOrDefault(entity, 0);
        int deathrattle = ResourceRetriever.forResource(HearthStoneResource.DEATHRATTLE).getOrDefault(entity, 0);
        int taunt = ResourceRetriever.forResource(HearthStoneResource.TAUNT).getOrDefault(entity, 0);
        int divineShield = ResourceRetriever.forResource(HearthStoneResource.DIVINE_SHIELD).getOrDefault(entity, 0);
        int charge = ResourceRetriever.forResource(HearthStoneResource.CHARGE).getOrDefault(entity, 0);
        int frozen = ResourceRetriever.forResource(HearthStoneResource.FROZEN).getOrDefault(entity, 0);
        int stealth = ResourceRetriever.forResource(HearthStoneResource.STEALTH).getOrDefault(entity, 0);
        int enraged = ResourceRetriever.forResource(HearthStoneResource.ENRAGED).getOrDefault(entity, 0);
        int windfury = ResourceRetriever.forResource(HearthStoneResource.WINDFURY).getOrDefault(entity, 0);
        int exhausted = ResourceRetriever.forResource(HearthStoneResource.EXHAUSTED).getOrDefault(entity, 0);

        List<String> effects = new ArrayList<>();
        if (justPlayed == 1) {
            effects.add("Just Played");
        }
        if (deathrattle == 1) {
            effects.add("Deathrattle");
        }
        if (taunt == 1) {
            effects.add("Taunt");
        }
        if (divineShield == 1) {
            effects.add("Divine Shield");
        }
        if (charge == 1) {
            effects.add("Charge");
        }
        if (frozen == 1) {
            effects.add("Frozen");
        }
        if (stealth == 1) {
            effects.add("Stealth");
        }
        if (enraged == 1) {
            effects.add("Enrage");
        }
        if (windfury == 1) {
            effects.add("Windfury");
        }
        if (exhausted == 1) {
            effects.add("Exhaused");
        }

        return effects.stream().collect(Collectors.joining(", "));
    }
}
