# rizk
A clojure clone of Risk

## Terms
- Territory : a closed piece of land (e.g. Brazil)
- Region : a collection of territories with a special bonus (e.g. South America)
- World : the collection of all territories in the game (to disambiguate with the map fn)

## Gameplay
### Starting out
At the start of the game, players are randomly assigned a set of territories.  Player 1 places 4 troops in any collection of territories that they own.  Player 2 proceeds to do the same.  Once all players have placed troops, loop back to Player 1 and repeat until each player has placed 20 (TODO) troops.  

### Turns
Players take turns.  A turn consists of the distribution phase, attack phase, and coordination phase.  

#### Distribution Phase
Every player receives some number K of troops at the start of their turn every round, where K is determined by (1) the number of territories held by the player and (2) the number of regions held by the player. A player must place all of the troops received (during the distribution phase) into territories they own before they may move on to the attack phase. 

A player may choose to trade in the cards in their hand once troops are distributed.  If the player moves on to the attack phase, they may no longer trade in their cards.

#### Attack Phase
During the attack phase, a player may move troops from any territory with more than one military unit (MU) to a territory owned by another player.  Chances of success are calculated (TODO).  Once a player is satisfied with their attack, they may move on to the coordination phase.  

#### Coordination Phase
During the coordination phase, a player may move their troops to locations within contiguous collections of territories (TODO).  Then the player may end their turn.

### Cards
Players have cards of three types: A, B, or C.  Players may hold up to 5 cards at a time.  Once a player has 5 cards, they must trade in their cards for troops.  Players may trade in any hand of 3-of-a-kind or 1-of-each-kind to receive N MUs, where N is the trade-in rate.  The trade-in rate (TODO) starts at 4 and goes up by a predetermined rule (e.g. up by 2s or by 20%) every time a player trades in their cards for troops.

Players receive cards at the end of every round where they have attacked and conquered at least one enemy territory.  If a player X wipes out a player Y, then X takes all the cards of Y.  If taking Y's cards causes X to have more than 5 cards in his hand, he must trade in immediately, regardless of which turn phase he is in.

A player is wiped out from the game if they lose all of their territories.  We may choose to include revival mechanics if we wish (TODO).

## Notes
- Changing the behavior of fns such as `attack-success-chance` can change gameplay to encourage more aggressive or defensive play.  Another interesting fn is `valid-troop-move?`: allowing players to move their troops more freely during the coordination phase can reward dynamic play.
- Region bonus depends on the region.  Usually, regions which are large and less defensible have higher bonuses.
- We can further extend the game by adding random events, economy management, special card effects, and more rules to turn this from a Risk clone into something closer to Civ.
