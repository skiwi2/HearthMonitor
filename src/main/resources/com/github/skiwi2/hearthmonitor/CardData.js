/**
 * Created by Frank van Heeswijk on 23-2-2015.
 */

var CardData = Java.type("com.github.skiwi2.hearthmonitor.CardData");
var Optional = Java.type("java.util.Optional");
var setList;
var allSets;

function setSetList(setListRaw) {
    setList = JSON.parse(setListRaw);
}

function setAllSets(allSetsRaw) {
    allSets = JSON.parse(allSetsRaw);
}

function getCardData(cardId) {
    for (var i = 0; i < setList.length; i++) {
        var set = setList[i];
        for (var j = 0; j < allSets[set].length; j++) {
            var card = allSets[set][j];
            if (card.id == cardId) {
                return new CardData(card.id, card.name, Optional.ofNullable(card.playerClass));
            }
        }
    }
    return null;
}