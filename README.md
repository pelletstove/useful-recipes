# Useful Recipes

The recipes from Don's old CraftTweaker script (`scripts/usefull_recipes.zs`, 1.20.4), as a data-only Fabric mod for 26.2.
No scripting mod is needed any more.

- **Crafting:** quartz block x4, 6 iron ingots -> 64 iron bars, lodestone from chiseled stone bricks + iron ingot, 6 sticks -> 64 rails,
  4 arrows from iron nugget + stick + feather, chainmail armour from iron nuggets, gunpowder from TNT, enchanted golden apple
  from 8 gold blocks + apple.
- **Removed vanilla recipes:** quartz block, iron bars, rail (a copy with a failing `fabric:false` load condition sits on top of each).
- **Stonecutter:** planks and logs into stairs, slabs, chests, doors, fences, gates, trapdoors, boats and signs for oak, spruce, birch, jungle, acacia and
  dark oak; bricks from bricks; iron door/bars from iron; glass panes from glass; the spruce bridge pier (needs Macaw's Bridges).
- **Smoker and blast furnace:** logs into charcoal, plus glass, stone, smooth stone, brick and nether brick in the blast furnace.

The JSON in `src/main/resources/data/useful_recipes/recipe` was generated from the script by a converter, then checked by GameTests.
