# rizk
Risk in Clojure

## Terms
- Territory : a closed piece of land (e.g. Brazil).  Interchangeable with "tile".
- Region : a collection of territories with a special bonus (e.g. South America, composed of Brazil, Argentina, Peru, etc.)
- World : the collection of all territories in the game

## Gameplay
### Starting out
At the start of the game, players are randomly assigned territories.  Given `n` players, each player gets `n/T` territories, where `T` is the total number of territories.
Each player takes turns placing some fixed number (say, `troop-seed-count`) of troops in any collection of territories they own.
Once all players have placed their troops (`initial-troop-count`), the game begins.

### Turns
Players take turns.  A turn consists of the distribution phase, attack phase, and coordination phase.  

#### Distribution Phase
At the start of their turn, a player may choose to trade in the cards (see below) in their hand for troops.  Once distribution has begun, a player may not trade in their cards.

Every player receives some number of troops at the start of their turn every round.
The number of troops received is determined by

```
reinforcements = max { 3, ; minimum allotment
                       (num_territories / 3) + region_bonuses }
```
A player must place all of these troops into territories that they own before they may move on to the attack phase. 

#### Attack Phase
During the attack phase, a player may move troops from any territory with 2 or more military units (MUs) to attack an adjacent territory owned by another player.  Players may not move MUs from one friendly territory to another at this time (this is done during the coordination phase).

Chances of success are calculated by a function (`attack-success-chance`).  In the board game,
- The attacker rolls `min { 3, num-troops-attacking }` dice.  
- The defender rolls `min { 2, num-troops-defending }` dice.
- The highest roll of the attacker is matched with the highest roll of the defender, and so on with the second-highest rolls for both.
  The highest roll in each pair wins; ties go to the defender.
- The losing troops are removed from their respective tiles.

> An attack ends in one of three ways: 1) The attacker decides to end the attack, 2) the attacker runs out of troops with which to attack, and 3) the defender loses all troops.  In this third case, the attacker takes over the territory and must move at least as many troops as dice rolled in the winning roll and at most the number of remaining troops in the attacking territory minus the one troop that must stay behind to occupy the territory.  A player can attack as many territories as he or she wants during the attack phase of the turn. If the player captured at least one territory during the attack phase, he or she takes a ... card.

Once a player is satisfied with their attack, they may move on to the coordination phase.  

#### Coordination Phase
During the coordination phase, a player may move their troops to locations within contiguous collections of territories, although this rule may be changed to alter gameplay.
Once satisfied, the player may end their turn.

### Cards
Players have cards of three types: `A`, `B`, or `C`.  Players may hold up to 5 cards at a time.  Once a player has 5 cards, they *must* trade in their cards for troops.  Players may only trade in their cards at the start of their turn, before distributing troops.  Players trade in hands of 3 cards: a hand must have either one card of each type (i.e., `A-B-C`) or three cards of the same type (e.g. `A-A-A`).  Trading in gives the player additional troops to distribute, as determined by the card exchange rate.

The exchange rate starts at some constant `initial-exchange-rate` and increases by a predetermined rule (e.g. up by 2's or by 20%) every time a player trades in their cards.

Players receive cards at the end of their turn *if they have attacked and conquered at least one enemy territory*.
If a player X wipes out a player Y, then X takes all of Y's cards.
If taking Y's cards means that X has more than 5 cards in his hand, he must trade in immediately, regardless of which turn phase he is in.

A player is wiped out from the game if they lose all of their territories.  We may choose to include revival mechanics if we wish.

### Region Bonuses
Determined by the map.  Usually, regions which are large and less defensible have higher bonuses.
Bonuses are applied *right before the start of a player's turn*, for all regions that a player fully controls.
This means that a player must control a region for a full round before they can claim its bonus.

Region bonuses scale throughout the course of the game.

## Interesting extensions
- [ ] Fog of war and randomly generated maps.  Use graph cluster-detection algorithms to assign region bonuses.
- [ ] Tile-relative resources/boosts.  Certain territories give additional bonus troops or enhance the fighting power of troops within a certain radius.
- [ ] Economy management.  Each territory outputs a certain amount of $, and troops cost $ depending on how spread out / concentrated they are.  Combat penalties for going negative.
- [ ] Random events.  Every few turns, some event occurs, modifying combat coefficients, reinforcement coefficients, or further game stats.
- [ ] Team play.
- [ ] Special card effects.  Some cards modify the card exchange rate when played

## Notes
- Changing the behavior of fns such as `attack-success-chance` can change gameplay to encourage more aggressive or defensive play.  Another interesting fn is `valid-troop-move?`: allowing players to move their troops more freely during the coordination phase can reward dynamic play.
- Basic strategy: [http://web.mit.edu/sp.268/www/2010/risk.pdf](http://web.mit.edu/sp.268/www/2010/risk.pdf)

# Sprint 1 Outline
- [ ] Clear up which functions receive tiles and which receive names (David)
- [ ] get-rand-card / card-drawing functionality
- [ ] attack-once
- [ ] attack-until-exhausted
- [ ] card trade-in mechnaisms
- [ ] phase- and player-turn- transitions
- [ ] implement coordination phase: player allowed to move one group of as many troops as they like from one territory to another (bounded by the number of troops in the territory)
- [ ] (Optional) better testing map (shorter names, simpler layout, easy to understand)
