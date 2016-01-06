package Crafting;

import Blocks.BlockStone;
import Blocks.BlockTorch;
import Blocks.BlockWood;
import Items.ItemAxe;
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

		for (ItemStack tem : MainFile.currentWorld.player.inventoryItems) {
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

		for (ItemStack tem : MainFile.currentWorld.player.inventoryItems) {
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
		addRecipe(new ItemStack[]{ new ItemStack(new BlockWood(), 2)}, new ItemStack(new BlockTorch(), 5));

		addRecipe(new ItemStack[]{new ItemStack(new BlockWood(), 1), new ItemStack(new BlockStone(), 2)}, new ItemStack(new ItemAxe()));
	}

}