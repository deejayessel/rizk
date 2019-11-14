### Cards
Players have cards of three types: `A`, `B`, or `C`.  Players may hold up to 5 cards at a time.  Once a player has 5 cards, they *must* trade in their cards for troops.  Players may only trade in their cards at the start of their turn, before distributing troops.  Players trade in hands of 3 cards: a hand must have either one card of each type (i.e., `A-B-C`) or three cards of the same type (e.g. `A-A-A`).  Trading in gives the player additional troops to distribute, as determined by the card exchange rate.

The exchange rate starts at some constant `initial-exchange-rate` and increases by a predetermined rule (e.g. up by 2's or by 20%) every time a player trades in their cards.

Players receive cards at the end of their turn *if they have attacked and conquered at least one enemy territory*.
If a player X wipes out a player Y, then X takes all of Y's cards.
If taking Y's cards means that X has more than 5 cards in his hand, he must trade in immediately, regardless of which turn phase he is in.

## Implementation: Card-trading
- [x] determine valid trades
- [ ] determine card trade-in value (formula & impl)
  - trade-in value should go up by some rule:
    - increase by constant (e.g. 2 cards) at every trade?
    - increase by compounding rate (e.g. 10%) at every trade?
    - increase at every turn?
    - set as some fraction of the total number of troops on the board?
- [ ] when a player captures the last territory of another player (i.e., when a player removes another player from the game), the attacker takes the defender's cards
