package WorldFiles;

import Blocks.BlockAir;
import Blocks.Util.Block;
import Blocks.Util.ILightSource;
import Blocks.Util.ITickBlock;
import EntityFiles.Entities.EntityPlayer;
import EntityFiles.Entity;
import EntityFiles.EntityItem;
import Items.Utils.ItemStack;
import Main.MainFile;
import Render.Renders.MinimapRender;
import Threads.WorldEntityUpdateThread;
import Threads.WorldGenerationThread;
import Threads.WorldLightUpdateThread;
import Threads.WorldUpdateThread;
import Utils.ConfigValues;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

//TODO Make sure there is no code left that is hardcoded to one player
public class World {
	public WorldGenerationThread worldGenerationThread = new WorldGenerationThread();
	public WorldUpdateThread worldUpdateThread = new WorldUpdateThread();
	public WorldEntityUpdateThread worldEntityUpdateThread = new WorldEntityUpdateThread();
	public WorldLightUpdateThread worldLightUpdateThread = new WorldLightUpdateThread();

	public HashMap<String, Object> worldProperties = new HashMap<>();

	public ArrayList<Entity> Entities = new ArrayList<>();
	public ArrayList<Entity> RemoveEntities = new ArrayList<>();

	public Block[][] Blocks;
	public ArrayList<Point> tickableBlocks = new ArrayList<>();

	public String worldName;
	public EnumWorldSize worldSize;
	public EnumWorldTime worldTimeOfDay = EnumWorldTime.DAY;

	public int WorldTime = worldTimeOfDay.timeBegin, WorldTimeDayEnd = EnumWorldTime.NIGHT.timeEnd;
	public int WorldDay = 1;

	public boolean generating = false;

	public World( String name, EnumWorldSize size ) {
		this.worldName = name;
		this.worldSize = size;

		resetValues();
	}

	public EnumWorldTime getNextWorldTime() {
		boolean t = false;
		for (EnumWorldTime en : EnumWorldTime.values()) {
			if (!t) {
				if (WorldTime > en.timeBegin && WorldTime < en.timeEnd) {
					if (en == EnumWorldTime.NIGHT) {
						return EnumWorldTime.MORNING;
					} else {
						t = true;
					}
				}
			} else {
				return en;
			}
		}

		return EnumWorldTime.MORNING;
	}

	public void setTimeOfDay( EnumWorldTime time ) {
		worldTimeOfDay = time;
		WorldTime = time.timeBegin;
	}

	public void start() {
		MainFile.getClient().setPlayer(new EntityPlayer(0,0, MainFile.getClient().playerId));

		worldUpdateThread.start();
	}

	public void resetValues() {
		Blocks = new Block[ worldSize.xSize ][ worldSize.ySize ];

		worldTimeOfDay = EnumWorldTime.MORNING;
		WorldTime = worldTimeOfDay.timeBegin;
		WorldDay = 1;
	}

	public void generate() {
		worldGenerationThread.start();
		worldEntityUpdateThread.start();
		worldLightUpdateThread.start();
	}
	public void doneGenerating() {
		spawnPlayer(MainFile.getClient().getPlayer());
	}

	public void stop() {
		MainFile.getClient().setPlayer(null);

		worldUpdateThread.stop();
		worldEntityUpdateThread.stop();
		worldLightUpdateThread.stop();
	}


	public void updateTime(){
		for (EnumWorldTime en : EnumWorldTime.values()) {
			if(WorldTime > en.timeBegin){
				worldTimeOfDay = en;
			}
		}

		WorldTime += 1;

		if (WorldTime > WorldTimeDayEnd) {
			WorldTime = 0;
			WorldDay += 1;
		}
	}

	public boolean isAirBlock( int x, int y ) {
		if (getBlock(x, y, true) instanceof BlockAir) {
			return true;
		}

		return false;
	}


	public Block getBlock( int x, int y ) {
		return getBlock(x, y, false);
	}

	public Block getBlock( int x, int y, boolean allowAir ) {
		if (Blocks != null) {
			if (x >= 0 && y >= 0 && x < worldSize.xSize && y < worldSize.ySize) {
				if (!allowAir && Blocks[ x ][ y ] instanceof BlockAir) {
					return null;
				}
				return Blocks[ x ][ y ];
			}
		}
		return null;
	}

	public void removeTickBlock(int x, int y){
		tickableBlocks.remove(new Point(x, y));
	}

	public void setBlock( Block block, int x, int y ) {
		if(!generating)
		removeTickBlock(x, y);

		if (Blocks != null) {
			if (x >= 0 && y >= 0) {
				if (x < worldSize.xSize && y < worldSize.ySize) {

					if (x >= 0 && y >= 0) {
						if (block != null) {
							Blocks[ x ][ y ] = block;

							if(block instanceof ITickBlock){
								tickableBlocks.add(new Point(x,y));
							}
						} else {
							Blocks[ x ][ y ] = new BlockAir();
						}

						if(!generating) {
							getBlock(x, y, true).updateBlock(this, x, y, x, y);
							updateNearbyBlocks(x, y);
						}
					}
				}
			}
		}
	}

	public void breakBlock(int x, int y){
		if(getBlock(x, y) != null){
			ItemStack stack = getBlock(x, y).getItemDropped(this, x, y);

			if(stack != null){
				EntityItem item = new EntityItem(x, y, stack);
				Entities.add(item);
			}

			setBlock(null, x, y);
		}
	}

	public void spawnPlayer(EntityPlayer player) {
		int xx = 1 + MainFile.random.nextInt(worldSize.xSize - 1), yy = 0;

		for (int y = 0; y < worldSize.ySize; y++) {
			Block block = getBlock(xx, y);

			if (block != null) {
				yy = y - 1;
				break;
			}
		}

		player.setEntityPosition(xx, yy);

		if(!Entities.contains(player))
		Entities.add(player);
		MinimapRender.reset();

		MainFile.getClient().hasSpawnedPlayer = true;
	}

	public void updateBlocks() {
		try {
			if(tickableBlocks != null) {
				//TODO ConcurrentModificationError
				for (Point p : new ArrayList<Point>(tickableBlocks)) {
					Block block = getBlock(p.x, p.y);

					if(block != null) {
						if (block instanceof ITickBlock) {
							ITickBlock up = (ITickBlock) block;

							int x = p.x, y = p.y;

							if (MainFile.getClient().getPlayer().getEntityPostion().distance(x, y) <= (ConfigValues.renderDistance * 2) || up.updateOutofBounds()) {
								if (up.shouldupdate(this, x, y)) {
									if (up.getTimeSinceUpdate() == up.blockupdateDelay()) {
										up.updateBlock(this, x, y);
										up.setTimeSinceUpdate(0);
									} else {
										up.setTimeSinceUpdate(up.getTimeSinceUpdate() + 1);
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	public void updateNearbyBlocks(int xx, int yy ) {
		for (int x = -1; x < 2; x++) {
			for (int y = -1; y < 2; y++) {
				if (x != 0 && y != 0)
					continue;

				int xPos = xx + x, yPos = yy + y;
				Block b = getBlock(xPos, yPos, true);

				if (b != null) {
					if (xPos != xx || yPos != yy) {
						b.updateBlock(this, xx, yy, xPos, yPos);
					}
				}
			}
		}
	}

	public Block[] getNearbyBlocks( int xx, int yy ) {
		Block[] bl = new Block[ 4 ];

		for (int x = -1; x < 2; x++) {
			for (int y = -1; y < 2; y++) {
				if (x != 0 && y != 0) continue;

				int xPos = xx + x, yPos = yy + y;
				Block b = getBlock(xPos, yPos, true);

				if (b != null) {
					if (xPos != xx || yPos != yy) {
						bl[ (x + 1) + (y + 1) ] = b;
					}
				}
			}
		}

		return bl;
	}

	public void updateLightForBlock( int xx, int yy ) {
		Block block = getBlock(xx, yy, true);

		if (block != null) {
			block.setLightValue(0);
			block.getLightUnit().setLightColor(ILightSource.DEFAULT_LIGHT_COLOR);

			if (block instanceof ILightSource) {
				block.setLightValue(((ILightSource) block).getOutputStrength());
				block.getLightUnit().setLightColor(((ILightSource) block).getLightColor());
			}

			boolean hasLight = false;

			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					if (x != 0 && y != 0) {
						continue;
					}

					int xPos = xx + x, yPos = yy + y;
					Block b = getBlock(xPos, yPos, true);

					if (b != null) {

						if (b.getLightValue(this, xPos, yPos) > 0) {
							hasLight = true;
						}

						if (block.getLightValue(this, xx, yy) < b.getLightValue(this, xPos, yPos)) {
							block.setLightValue(b.getLightValue(this, xPos, yPos) - 1);
							if (block.getLightUnit().getLightColor() != b.getLightUnit().getLightColor()) {
								block.getLightUnit().setLightColor(b.getLightUnit().getLightColor());
							}

						}
					}
				}
			}

			if (!hasLight && !(block instanceof ILightSource)) {
				block.setLightValue(0);
				block.getLightUnit().setLightColor(ILightSource.DEFAULT_LIGHT_COLOR);
			}

		}
	}

	public void updateLightForBlocks() {
		if (MainFile.getClient().getPlayer() != null) {
			for (int x = -(ConfigValues.lightUpdateRenderRange / 2); x < (ConfigValues.lightUpdateRenderRange / 2); x++) {
				for (int y = -(ConfigValues.lightUpdateRenderRange / 2); y < (ConfigValues.lightUpdateRenderRange / 2); y++) {

					int xPos = (int) MainFile.getClient().getPlayer().getEntityPostion().x + x, yPos = (int) MainFile.getClient().getPlayer().getEntityPostion().y + y;
					Block b = getBlock(xPos, yPos, true);

					if (b != null) {
						updateLightForBlock(xPos, yPos);
						b.updateBlock(this, xPos, yPos, xPos, yPos);
					}
				}
			}

		} else {
			for(int x = 0; x < worldSize.xSize; x++){
				for(int y = 0; y < worldSize.ySize; y++){
					Block b = getBlock(x, y);
					if (b != null) {
						updateLightForBlock(x, y);
					}
				}
			}
		}
	}


	@Override
	public boolean equals( Object o ) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof World)) {
			return false;
		}

		World world = (World) o;

		if (WorldTime != world.WorldTime) {
			return false;
		}
		if (WorldTimeDayEnd != world.WorldTimeDayEnd) {
			return false;
		}
		if (WorldDay != world.WorldDay) {
			return false;
		}
		if (!worldName.equals(world.worldName)) {
			return false;
		}
		if (worldSize != world.worldSize) {
			return false;
		}
		if (worldProperties != null ? !worldProperties.equals(world.worldProperties) : world.worldProperties != null) {
			return false;
		}
		if (!Blocks.equals(world.Blocks)) {
			return false;
		}
		return worldTimeOfDay == world.worldTimeOfDay;

	}

	@Override
	public int hashCode() {
		int result = worldName.hashCode();
		result = 31 * result + worldSize.hashCode();
		result = 31 * result + (worldProperties != null ? worldProperties.hashCode() : 0);
		result = 31 * result + (Entities != null ? Entities.hashCode() : 0);
		result = 31 * result + Blocks.hashCode();
		result = 31 * result + WorldTime;
		result = 31 * result + WorldTimeDayEnd;
		result = 31 * result + (worldTimeOfDay != null ? worldTimeOfDay.hashCode() : 0);
		result = 31 * result + WorldDay;
		result = 31 * result + (generating ? 1 : 0);
		return result;
	}
}
