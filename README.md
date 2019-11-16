# rizk
Risk in Clojure

## Terms
- Tile : a node in the graph/map
- Group : a collection of territories with a special bonus
- Unit: a contingent of troops

## Gameplay
### Starting out
Players are randomly assigned territories.  Territories are evenly distributed amongst the players -- the greatest gap
in territory count between any two players is 1.  Each player takes turns placing `k` units in any subset of territories
that they own.  Once all players have exhausted their initial unit allotment, the game begins.

### Turns
A turn consists of 3 phases: (1) reinforcement, (2) attack, (3) movement.

#### Reinforcement Phase
Every player receives some number of units at the start of their turn every round.
The number of units received is determined by

```
reinforcements = max { 3, ; minimum allotment
                       (num_territories / 3) } + region_bonuses
```
A player must place all of these units into territories that they own before they may move on to the attack phase. 

#### Attack Phase
During the attack phase, a player may move units from any territory with 2 or more units to attack an adjacent territory owned by another player.  

Combat proceeds as follows:
- The attacker rolls `min { 3, num-units-attacking }` dice.  
- The defender rolls `min { 2, num-units-defending }` dice.
- The highest roll of the attacker is matched with the highest roll of the defender, and so on with the second-highest rolls for both.
  The highest roll in each pair wins; ties go to the defender.
- The losing units are removed from their respective nodes.

The attacker may end the attack, run out of units with which to attack, or kill all defending units.  In this last case,
the attacker conquers the territory and must move at least 1 unit into the node.

#### Movement Phase
A player may move any number of units to adjacent nodes.  Each unit may only move up to 1 node away.

### Region Bonuses
Determined by the map.  Usually, regions which are large and less defensible have higher bonuses.
Bonuses are applied *right before the start of a player's turn*, for all regions that a player fully controls.
This means that a player must control a region for a full round before they can claim its bonus.

Region bonuses scale throughout the course of the game.

## Interesting extensions
- [ ] Fog of war
- [ ] Randomly generated maps.  (Use graph cluster-detection algorithms to assign region bonuses.)
- [ ] node-relative resources/boosts.  (Certain territories give additional bonus units or enhance the fighting power of units within a certain radius.)
- [ ] Economy management.  (Each territory outputs a certain amount of $, and units cost $ depending on how spread out / concentrated they are.  Combat penalties for going negative.)
- [ ] Random events.  (Every few turns, some event occurs, modifying combat coefficients, reinforcement coefficients, or further game stats.)
- [ ] Team play.

## Notes
- Basic strategy: [http://web.mit.edu/sp.268/www/2010/risk.pdf](http://web.mit.edu/sp.268/www/2010/risk.pdf)

# Implementation TODOs

## Attack
- [x] `attack-once`
- [x] `attack-k-times`
- [x] `attack-till-end`: attack until either the attacker can no longer attack (<2 units) or the defender has no units remaining
- [x] `attack-with-k`: attack until either territory taken or `k` friendly units dead
- [x] conquer a territory once no defenders remaining
- [ ] unit movement after attack completed
  - prompt player? need inputs specifying how many units should be moved

## Movement / Reinforcement
- [x] unit movement between nodes
- [x] reinforcing nodes with additional units
- [ ] track total number of reinforcements for a player in a round

## Infrastructure
- [ ] set up the game by having players take turns seeding units
- [x] phase- and player-turn- transitions

## Miscellaneous
- [x] (Optional) better testing map (shorter names, simpler layout, easy to understand)
