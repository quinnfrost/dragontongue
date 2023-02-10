Dragon tongue
=============

[Ice and Fire mod](https://github.com/AlexModGuy/Ice_and_Fire)

[中文](https://github.com/quinnfrost/dragontongue/blob/master/README_CN.md)

A Minecraft forge mod to enhance the experience in interaction with dragons from Ice and Fire, as well as some other
tamables, with some other modifications.

I'm very new to modding and I built up this mod from a tutorial mod. Backup your save.

Features
--------

### Commanding
- Use custom keybindings to command your pets, mainly for dragons in Ice and Fire, vanilla tameables and hippogryphs should work too.
- Tell your pets where to go, and for dragons where to hover.
- Command your pets to attack mobs, and for dragons to breath at a specific area.
- Use dragon staff from a distance, cycle the command mode from the ground, no dragon flute is ever needed.
- Original dragon staff behavior is modified, from far distance right click on the dragon will open the gui, from close remain the same.
- A new Guarding state, your pet will try to attack any hostile mob in sight.
- Dragon specific griefing rules, attack options, flight options and more. Each dragon have independent settings. Configurable in the original shift right click gui.

### Other modification
- Fix dragon flight CFIT issue, they will now fly higher to pass the terrain if they can't see the target directly.
- Use totem of undying to resurrect dragons.
- The dragon scale set and steel set will now immune to its correspond damage, such as ice set to dragon ice spikes, lightning set to lightning strike.
- Elder dragons will not take damage from minor sources, such as cactus and sweet berry bushes.
- Elder dragons tend to fly higher when wandering/escorting.
- Elder dragons can walk up fences, according to their stages at most 3 blocks can be walked up.
- Other minor bug fixes for Ice and Fire that has not been in the release yet.

Other stuff
-----------

- Trident hit can make your pets to attack, just like what bows and arrows does. It can also teleport you to the hit
point if you are sneaking (Default disabled).
- Creepers and TNTs no longer damage terrains.
- A wand that teleport you to anywhere you pointed.
- A simple damage feedback system migrated from [这里](https://www.mcbbs.net/forum.php?mod=viewthread&tid=795249)

How to use
----------

Left mouse Button (LB), Right mouse Button (RB), Middle mouse Button (MB)

Entity is select via the crosshair

Select key (default V)
- V + LB: add pointed tamed to the command list, most command will influence the tamed in the command list.
- V + RB: remove tamed from command list and reset it to vanilla behavior.
- V + MB: clear the command list and add the pointed tamed to the list, the behavior is not reset.
- V + LB + MB: clear the command list.
- V + scroll: modify the nearby range.

Command key (default G)
- G + LB: tamed in the list will attack the pointed entity. If the list is empty, all nearby tamed will attack (range configure by the nearby range V + scroll)
- G + RB: tamed in the list will try reach and stay at pointed location.
- G + MB: the pointed tamed will stop attacking or moving. If nothing was pointed, all nearby tamed will stop.
- G + LB + MB: (dragons only) tamed dragon will try to breath at the pointed location. If the list is empty, all nearby dragons will breathe.
- G + scroll: modify the command range, all the command key related action is influenced by this value, if nothing in range is selected, a mark in the crosshair will show. This is useful when guiding dragons to fly.

Status key (default H)
- H + LB: tamed will stand/follow.
- H + RB: tamed will sit.
- H + MB: tamed will enter Guarding mode, any hostile in sight will be its target.
