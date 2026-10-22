package pelletstove.usefulrecipes.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("removal")
public class UsefulRecipesTests {
	/** 86 always load; the spruce bridge pier recipe only loads when Macaw's Bridges is installed. */
	private static final int EXPECTED_RECIPES = 86 + (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("mcwbridges") ? 1 : 0);

	private static RecipeManager recipes(GameTestHelper helper) {
		return helper.getLevel().getServer().getRecipeManager();
	}

	private static ResourceKey<Recipe<?>> key(String namespace, String path) {
		return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(namespace, path));
	}

	/** Lays items out row by row in a width x height crafting grid and returns the crafted result (empty when nothing matches). */
	private static ItemStack craft(GameTestHelper helper, int width, int height, Item... items) {
		List<ItemStack> grid = new ArrayList<>();
		for (Item item : items) {
			grid.add(item == null ? ItemStack.EMPTY : new ItemStack(item));
		}
		CraftingInput input = CraftingInput.of(width, height, grid);
		Optional<RecipeHolder<CraftingRecipe>> found = recipes(helper).getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		return found.map(h -> h.value().assemble(input)).orElse(ItemStack.EMPTY);
	}

	/** Every stonecutter result for this input, as "item xcount". */
	private static List<String> stonecutting(GameTestHelper helper, Item input) {
		SingleRecipeInput in = new SingleRecipeInput(new ItemStack(input));
		List<String> out = new ArrayList<>();
		for (RecipeHolder<?> holder : recipes(helper).getRecipes()) {
			if (holder.value().getType() == RecipeType.STONECUTTING && holder.value() instanceof SingleItemRecipe r && r.matches(in, helper.getLevel())) {
				ItemStack result = r.assemble(in);
				out.add(BuiltInRegistries.ITEM.getKey(result.getItem()).getPath() + " x" + result.getCount());
			}
		}
		return out;
	}

	private static ItemStack cook(GameTestHelper helper, RecipeType<?> type, Item input) {
		SingleRecipeInput in = new SingleRecipeInput(new ItemStack(input));
		for (RecipeHolder<?> holder : recipes(helper).getRecipes()) {
			if (holder.value().getType() == type && holder.value() instanceof AbstractCookingRecipe r && r.matches(in, helper.getLevel())) {
				return r.assemble(in);
			}
		}
		return ItemStack.EMPTY;
	}

	@GameTest
	public void allRecipesLoaded(GameTestHelper helper) {
		long count = recipes(helper).getRecipes().stream().filter(h -> h.id().identifier().getNamespace().equals("useful_recipes")).count();
		helper.assertTrue(count == EXPECTED_RECIPES, "expected " + EXPECTED_RECIPES + " useful_recipes recipes but " + count + " loaded; the rest failed to parse");
		helper.succeed();
	}

	@GameTest
	public void vanillaRecipesAreRemoved(GameTestHelper helper) {
		for (String name : List.of("quartz_block", "iron_bars", "rail")) {
			helper.assertTrue(recipes(helper).byKey(key("minecraft", name)).isEmpty(), "vanilla recipe minecraft:" + name + " should be gone");
		}
		helper.assertTrue(recipes(helper).byKey(key("minecraft", "powered_rail")).isPresent(), "powered rail must still exist");
		helper.succeed();
	}

	@GameTest
	public void railsComeInSixtyFours(GameTestHelper helper) {
		ItemStack rails = craft(helper, 3, 3, Items.STICK, null, Items.STICK, Items.STICK, null, Items.STICK, Items.STICK, null, Items.STICK);
		helper.assertTrue(rails.is(Items.RAIL) && rails.getCount() == 64, "6 sticks in two columns should give 64 rails, got " + rails);
		ItemStack vanilla = craft(helper, 3, 3, Items.IRON_INGOT, null, Items.IRON_INGOT, Items.IRON_INGOT, Items.STICK, Items.IRON_INGOT, Items.IRON_INGOT, null, Items.IRON_INGOT);
		helper.assertTrue(vanilla.isEmpty(), "the vanilla iron rail recipe must not work, got " + vanilla);
		helper.succeed();
	}

	@GameTest
	public void ironBarsComeInSixtyFours(GameTestHelper helper) {
		ItemStack bars = craft(helper, 3, 2, Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT);
		helper.assertTrue(bars.is(Items.IRON_BARS) && bars.getCount() == 64, "6 iron ingots should give 64 iron bars, got " + bars);
		helper.succeed();
	}

	@GameTest
	public void quartzBlockMakesFour(GameTestHelper helper) {
		ItemStack blocks = craft(helper, 2, 2, Items.QUARTZ, Items.QUARTZ, Items.QUARTZ, Items.QUARTZ);
		helper.assertTrue(blocks.is(Items.QUARTZ_BLOCK) && blocks.getCount() == 4, "4 quartz should give 4 quartz blocks, got " + blocks);
		helper.succeed();
	}

	@GameTest
	public void lodestoneAndArrowsAndGunpowder(GameTestHelper helper) {
		Item cb = Items.CHISELED_STONE_BRICKS;
		ItemStack lodestone = craft(helper, 3, 3, cb, cb, cb, cb, Items.IRON_INGOT, cb, cb, cb, cb);
		helper.assertTrue(lodestone.is(Items.LODESTONE), "lodestone recipe failed, got " + lodestone);
		ItemStack arrows = craft(helper, 1, 3, Items.IRON_NUGGET, Items.STICK, Items.FEATHER);
		helper.assertTrue(arrows.is(Items.ARROW) && arrows.getCount() == 4, "nugget arrow should give 4 arrows, got " + arrows);
		ItemStack gunpowder = craft(helper, 1, 1, Items.TNT);
		helper.assertTrue(gunpowder.is(Items.GUNPOWDER) && gunpowder.getCount() == 4, "TNT should give 4 gunpowder, got " + gunpowder);
		helper.succeed();
	}

	@GameTest
	public void chainmailFromNuggets(GameTestHelper helper) {
		Item n = Items.IRON_NUGGET;
		helper.assertTrue(craft(helper, 3, 2, n, n, n, n, null, n).is(Items.CHAINMAIL_HELMET), "chainmail helmet");
		helper.assertTrue(craft(helper, 3, 3, n, null, n, n, n, n, n, n, n).is(Items.CHAINMAIL_CHESTPLATE), "chainmail chestplate");
		helper.assertTrue(craft(helper, 3, 3, n, n, n, n, null, n, n, null, n).is(Items.CHAINMAIL_LEGGINGS), "chainmail leggings");
		helper.assertTrue(craft(helper, 3, 2, n, null, n, n, null, n).is(Items.CHAINMAIL_BOOTS), "chainmail boots");
		helper.succeed();
	}

	@GameTest
	public void enchantedGoldenAppleFromGoldBlocks(GameTestHelper helper) {
		Item g = Items.GOLD_BLOCK;
		ItemStack apple = craft(helper, 3, 3, g, g, g, g, Items.APPLE, g, g, g, g);
		helper.assertTrue(apple.is(Items.ENCHANTED_GOLDEN_APPLE), "8 gold blocks + apple should give an enchanted golden apple, got " + apple);
		helper.succeed();
	}

	@GameTest
	public void stonecutterWood(GameTestHelper helper) {
		List<String> planks = stonecutting(helper, Items.OAK_PLANKS);
		helper.assertTrue(planks.contains("oak_stairs x1") && planks.contains("oak_slab x2"), "oak planks should cut into stairs x1 and slabs x2, got " + planks);
		List<String> logs = stonecutting(helper, Items.SPRUCE_LOG);
		for (String expected : List.of("chest x1", "spruce_trapdoor x2", "spruce_boat x1", "spruce_fence x3", "spruce_fence_gate x2", "spruce_door x2", "spruce_sign x2")) {
			helper.assertTrue(logs.contains(expected), "spruce log should cut into " + expected + ", got " + logs);
		}
		helper.succeed();
	}

	@GameTest
	public void stonecutterIronGlassBricks(GameTestHelper helper) {
		helper.assertTrue(stonecutting(helper, Items.IRON_BLOCK).contains("iron_door x2"), "iron block -> 2 iron doors");
		helper.assertTrue(stonecutting(helper, Items.IRON_INGOT).contains("iron_bars x3"), "iron ingot -> 3 iron bars");
		helper.assertTrue(stonecutting(helper, Items.GLASS).contains("glass_pane x3"), "glass -> 3 panes");
		helper.assertTrue(stonecutting(helper, Items.BRICK).contains("bricks x1"), "brick -> bricks");
		helper.succeed();
	}

	@GameTest
	public void charcoalInSmokerAndBlastFurnace(GameTestHelper helper) {
		for (Item log : List.of(Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG)) {
			helper.assertTrue(cook(helper, RecipeType.SMOKING, log).is(Items.CHARCOAL), BuiltInRegistries.ITEM.getKey(log) + " should smoke into charcoal");
			helper.assertTrue(cook(helper, RecipeType.BLASTING, log).is(Items.CHARCOAL), BuiltInRegistries.ITEM.getKey(log) + " should blast into charcoal");
		}
		helper.succeed();
	}

	@GameTest
	public void blastFurnaceExtras(GameTestHelper helper) {
		helper.assertTrue(cook(helper, RecipeType.BLASTING, Items.SAND).is(Items.GLASS), "sand -> glass");
		helper.assertTrue(cook(helper, RecipeType.BLASTING, Items.COBBLESTONE).is(Items.STONE), "cobblestone -> stone");
		helper.assertTrue(cook(helper, RecipeType.BLASTING, Items.STONE).is(Items.SMOOTH_STONE), "stone -> smooth stone");
		helper.assertTrue(cook(helper, RecipeType.BLASTING, Items.CLAY_BALL).is(Items.BRICK), "clay ball -> brick");
		helper.assertTrue(cook(helper, RecipeType.BLASTING, Items.NETHERRACK).is(Items.NETHER_BRICK), "netherrack -> nether brick");
		helper.succeed();
	}
}
