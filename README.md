# rizk
Risk in Clojure

## Terms
- Tile : a node in the graph/map
- Group : a collection of territories with a special bonus

## Gameplay
### Starting out
Players are randomly assigned territories.  Territories are evenly distributed amongst the players -- the greatest gap
in territory count between any two players is 1.  Each player takes turns placing `k` troops in any subset of territories
that they own.  Once all players have exhausted their initial troop allotment, the game begins.

### Turns
A turn consists of 3 phases: (1) reinforcement, (2) attack, (3) movement.

#### Reinforcement Phase
Every player receives some number of troops at the start of their turn every round.
The number of troops received is determined by

```
reinforcements = max { 3, ; minimum allotment
                       (num_territories / 3) } + region_bonuses
```
A player must place all of these troops into territories that they own before they may move on to the attack phase. 

#### Attack Phase
During the attack phase, a player may move troops from any territory with 2 or more units to attack an adjacent territory owned by another player.  Players may not move units from one friendly territory to another at this time (this is done during the coordination phase).

Combat proceeds as follows:
- The attacker rolls `min { 3, num-troops-attacking }` dice.  
- The defender rolls `min { 2, num-troops-defending }` dice.
- The highest roll of the attacker is matched with the highest roll of the defender, and so on with the second-highest rolls for both.
  The highest roll in each pair wins; ties go to the defender.
- The losing troops are removed from their respective nodes.

The attacker may end the attack, run out of troops with which to attack, or kill all defending troops.  In this last case,
the attacker conquers the territory and must move at least 1 troop into the node.

#### Movement Phase
A player may move any number of troops to adjacent nodes.  Each troop may only move up to 1 node away.

### Region Bonuses
Determined by the map.  Usually, regions which are large and less defensible have higher bonuses.
Bonuses are applied *right before the start of a player's turn*, for all regions that a player fully controls.
This means that a player must control a region for a full round before they can claim its bonus.

Region bonuses scale throughout the course of the game.

## Interesting extensions
- [ ] Fog of war
- [ ] Randomly generated maps.  (Use graph cluster-detection algorithms to assign region bonuses.)
- [ ] node-relative resources/boosts.  (Certain territories give additional bonus troops or enhance the fighting power of troops within a certain radius.)
- [ ] Economy management.  (Each territory outputs a certain amount of $, and troops cost $ depending on how spread out / concentrated they are.  Combat penalties for going negative.)
- [ ] Random events.  (Every few turns, some event occurs, modifying combat coefficients, reinforcement coefficients, or further game stats.)
- [ ] Team play.

## Notes
- Basic strategy: [http://web.mit.edu/sp.268/www/2010/risk.pdf](http://web.mit.edu/sp.268/www/2010/risk.pdf)

# Sprints

## Sprint 1: Attack
- [x] `attack-once`
- [x] `attack-k-times`
- [x] `attack-till-end`: attack until either the attacker can no longer attack (<2 troops) or the defender has no troops remaining
- [x] `attack-with-k`: attack until either territory taken or `k` friendly troops dead
- [x] conquer a territory once no defenders remaining
- [ ] troop movement after attack completed
  - prompt player? need inputs specifying how many troops should be moved

## Sprint 2: Movement / Reinforcement
- [x] troop movement between nodes
- [x] reinforcing nodes with additional troops
- [ ] track total number of reinforcements for a player in a round

## Sprint 3: Setup phase
- set up the game by having players take turns seeding troops

## Sprint 4: Game API
- [ ] phase- and player-turn- transitions

## Miscellaneous
- [x] (Optional) better testing map (shorter names, simpler layout, easy to understand)
