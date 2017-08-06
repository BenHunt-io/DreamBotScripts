DragonNotes -- As soon as the NPC dies (i.e health gets to 0) interactedWith() turns false. While the NPC is dying .exists() returns true. When the NPC is gone .exists() returns false.

-------------------- Anti PK --- Running in background thread --------------------------------------------------------------------------------------------------------
Anti-PK -- Constantly check if nearby players are skulled, then check combat level and wildy level to see if they can attack. Teleport if they can. (Hop worlds)
If player is interacting with somebody other than the green dragon, teleport. if can't teleport run south.

if player is not interacting with dragon, and is standing still for atleast 5-10 seconds and can attack and is close, teleport
------------------------------------------------------------------------------------------------------------------------------------------------------------------------


--------- Inventory ------------
If players inventory is full and is trying to loot, eat a lobster for each item.


------------ If Died ---------------------------------------------------------
-should have atleast one teleport left in glory, use that to teleport to edgeville and do some kind of regear function/hop worlds, then basically restart script



TODO:(problems/bugs)

- Doesn't withdrawal the amt to = starting ammo -- Withdraws correct amt
- Fix NPE with walking (multiple clicks ect.) (Fixed)
- Add world hopping
- Add Death walk
- Dragon combat works.. but it's a not full proof
- No matter what if interacting with anything other than dragon... I'm getting pk'd and should tele.. That should work fine.. It should run south and eat if getting attacked though.
(if teleported because of danger.. it should be able to restart easily..)
- Deposits all items even if inventory Problem
- Loot Bag support (should deposit into loot bag.. Need to add banking support still)
- Need to pick up the damn dragon head
- Did not bank glory when switched to new one


------------- new bugs 8/2/2017 -----------------------

-When selecting a new dragon, it can click 3 times till I assume the bolt fires off and player is registered to be in combat.
-Still pauses outside corp cave.. see screenshot on chrome
-Add profit/time running as well
-Add Pkwatcher to the walking phase too

------------- new bugs 8/4/2017 -----------------------
- pause outside corp cave
- still slightly too fast for eating lobsters 
-


