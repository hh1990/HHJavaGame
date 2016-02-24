package Crafting;

import BlockFiles.*;
import Items.ItemAxe;
import Items.ItemPickaxe;
import Items.ItemShovel;
import Items.ItemStick;
import Items.Utils.ItemStack;
import Main.MainFile;

import java.util.ArrayList;
import java.util.Arrays;

public class CraftingRegister {

	public static ArrayList<CraftingRecipe> recipes = new ArrayList<>();

	public static CraftingRecipe getRecipeFromInput( ItemStack[] input ) {
		for (CraftingRecipe res : recipes) {
			if (Arrays.equals(res.input, input)) {
				return res;
			}
		}

		return null;
	}

	public static void addRecipe( ItemStack[] input, ItemStack output ) {
		try {
			if (input.length > 4) {
				throw new Exception("Recipes cant be longer then 4 input items!");
			}

			recipes.add(new CraftingRecipe(input, output));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getAmount(ItemStack item){
		boolean hasItem = false;
		int hasSize = 0;

		for (ItemStack tem : MainFile.game.getClient().getPlayer().inventoryItems.values()) {
			if(tem != null && item != null) {
				if (tem.equals(item)) {
					hasSize += tem.getStackSize();
				}
			}
		}

		return hasSize;
	}

	public static boolean hasMaterialFor( CraftingRecipe recipe ) {

		if(recipe != null)
		for (ItemStack item : recipe.input) {
			if(item != null)
			if (!hasMaterial(item)) {
				return false;
			}
		}


		return true;
	}

	public static boolean hasMaterial( ItemStack item ) {
		boolean hasItem = false;
		int hasSize = 0;

		for (ItemStack tem : MainFile.game.getClient().getPlayer().inventoryItems.values()) {
			if (!hasItem && hasSize >= item.getStackSize()) {
				hasItem = true;
				break;
			}

			if (item.equals(tem) && tem.getStackSize() >= item.getStackSize()) {
				hasItem = true;
				hasSize = item.getStackSize();
			} else if (item.equals(tem) && tem.getStackSize() < item.getStackSize()) {
				hasSize += tem.getStackSize();
				continue;
			}
		}

		return hasItem;
	}

	public static void registerRecipes() {
		addRecipe(new ItemStack[]{ new ItemStack(Blocks.blockWood)}, new ItemStack(Blocks.blockPlanks, 3));
		addRecipe(new ItemStack[]{ new ItemStack(Blocks.blockPlanks, 2)}, new ItemStack(new ItemStick(), 4));

		addRecipe(new ItemStack[]{ new ItemStack(new ItemStick(), 1), new ItemStack(Blocks.blockPlanks)}, new ItemStack(Blocks.blockTorch, 5));

		addRecipe(new ItemStack[]{new ItemStack(new ItemStick(), 2), new ItemStack(Blocks.blockStone, 1)}, new ItemStack(new ItemShovel()));
		addRecipe(new ItemStack[]{new ItemStack(new ItemStick(), 2), new ItemStack(Blocks.blockStone, 2)}, new ItemStack(new ItemAxe()));
		addRecipe(new ItemStack[]{new ItemStack(new ItemStick(), 2), new ItemStack(Blocks.blockStone, 3)}, new ItemStack(new ItemPickaxe()));
	}

}
