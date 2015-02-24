package com.github.skiwi2.hearthmonitor.view;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.github.skiwi2.hearthmonitor.CardData;
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
        refreshBox(playerBox, "FRIENDLY");
    }

    private void refreshPlayerHandBox() {
        refreshHandBox(playerHandBox, "FRIENDLY");
    }

    private void refreshPlayerFieldBox() {
        refreshFieldBox(playerFieldBox, "FRIENDLY");
    }

    private void refreshOpponentFieldBox() {
        refreshFieldBox(opponentFieldBox, "OPPOSING");
    }

    private void refreshOpponentHandBox() {
        refreshHandBox(opponentHandBox, "OPPOSING");
    }

    private void refreshOpponentBox() {
        refreshBox(opponentBox, "OPPOSING");
    }

    private void refreshBox(final VBox box, final String controllerType) {
        box.getChildren().clear();

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), controllerType + " PLAY (Hero)"))
            .forEach(entity -> {
                int controller = ResourceRetriever.forResource(HearthStoneResource.CONTROLLER).getFor(entity);
                box.getChildren().add(new Label("Hero " + controller + System.lineSeparator() + getPlayerClass(entity) + System.lineSeparator() + getAttackAndHitPointsData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), controllerType + " PLAY (Hero Power)"))
            .forEach(entity -> {
                box.getChildren().add(new Label(getName(entity) + System.lineSeparator() + getAttackAndHitPointsData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });

        Long deckRemainingSize = ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .collect(Collectors.groupingBy(entity -> AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getFor(entity), Collectors.counting()))
            .get(controllerType + " DECK");
        if (deckRemainingSize != null) {
            box.getChildren().add(new Label("Deck " + deckRemainingSize.intValue()));
        }
    }

    private void refreshHandBox(final VBox box, final String controllerType) {
        box.getChildren().clear();

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), controllerType + " HAND"))
            .sorted(Comparator.comparingInt(entity -> ResourceRetriever.forResource(HearthStoneResource.ZONE_POSITION).getOrDefault(entity, 0)))
            .forEach(entity -> {
                box.getChildren().add(new Label(getName(entity) + System.lineSeparator() + getAttackAndHitPointsData(entity) + System.lineSeparator() + getSpecialEffects(entity)));
            });

    }

    private void refreshFieldBox(final VBox box, final String controllerType) {
        box.getChildren().clear();

        ecsGame.findEntities(entity -> (entity.hasComponent(ECSResourceMap.class) && entity.hasComponent(ECSAttributeMap.class)))
            .stream()
            .filter(entity -> Objects.equals(AttributeRetriever.forAttribute(HearthStoneAttribute.ZONE).getOrDefault(entity, ""), controllerType + " PLAY"))
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

    private static String getAttackAndHitPointsData(final Entity entity) {
        int attack = ResourceRetriever.forResource(HearthStoneResource.ATK).getOrDefault(entity, 0);
        int health = ResourceRetriever.forResource(HearthStoneResource.HEALTH).getOrDefault(entity, 0);
        return "ATK " + attack + " / HP " + health;
    }

    private static String getSpecialEffects(final Entity entity) {
        int justPlayed = ResourceRetriever.forResource(HearthStoneResource.JUST_PLAYED).getOrDefault(entity, 0);
        int deathrattle = ResourceRetriever.forResource(HearthStoneResource.DEATHRATTLE).getOrDefault(entity, 0);
        int taunt = ResourceRetriever.forResource(HearthStoneResource.TAUNT).getOrDefault(entity, 0);
        int divineShield = ResourceRetriever.forResource(HearthStoneResource.DIVINE_SHIELD).getOrDefault(entity, 0);
        int charge = ResourceRetriever.forResource(HearthStoneResource.CHARGE).getOrDefault(entity, 0);
        int frozen = ResourceRetriever.forResource(HearthStoneResource.FROZEN).getOrDefault(entity, 0);

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

        return effects.stream().collect(Collectors.joining(", "));
    }
}
