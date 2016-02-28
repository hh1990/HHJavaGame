package WorldFiles;

import BlockFiles.Blocks;
import BlockFiles.Util.Block;
import BlockFiles.Util.ILightSource;
import BlockFiles.Util.ITickBlock;
import BlockFiles.Util.LightUnit;
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
import Utils.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

//TODO Make sure there is no code left that is hardcoded to one player
public class World {
	public WorldGenerationThread worldGenerationThread = new WorldGenerationThread();
	public WorldUpdateThread worldUpdateThread = new WorldUpdateThread();
	public WorldEntityUpdateThread worldEntityUpdateThread = new WorldEntityUpdateThread();
	public WorldLightUpdateThread worldLightUpdateThread = new WorldLightUpdateThread();

	public HashMap<String, Object> worldProperties = new HashMap<>();

	public ArrayList<Entity> Entities = new ArrayList<>();
	public ArrayList<Entity> RemoveEntities = new ArrayList<>();


	public HashMap<Point, Chunk> worldChunks;

	public String worldName;
	public EnumWorldSize worldSize;
	public EnumWorldTime worldTimeOfDay = EnumWorldTime.DAY;

	public int WorldTime = worldTimeOfDay.timeBegin, WorldTimeDayEnd = EnumWorldTime.NIGHT.timeEnd;
	public int WorldDay = 1;

	public long timePlayed;

	public boolean generating = false;
	public boolean loaded = false;
	public boolean isLive = false;

	public World( String name, EnumWorldSize size ) {
		while(FileUtil.isThereWorldWithName(name)){
			name += "-";
		}

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
		if(!loaded) {
			MainFile.game.getClient().setPlayer(new EntityPlayer(0, 0, MainFile.game.getClient().playerId));
		}else{
			loadPlayer();
		}

		worldUpdateThread = new WorldUpdateThread();
		worldUpdateThread.start();


		TimeTaker.startTimeTaker("worldTimePlayed:" + worldName);
		isLive = true;
	}

	public void resetValues() {
		if(worldSize == null){
			LoggerUtil.out.log(Level.SEVERE, "worldSize was null! Unable to resetValues in world! [" + worldName + "]");
		}


		if(worldSize != null) {
			worldChunks = new HashMap<>();
		}

		worldTimeOfDay = EnumWorldTime.MORNING;
		WorldTime = worldTimeOfDay.timeBegin;
		WorldDay = 1;
	}

	public void generate() {
		if(!loaded) {
			saveWorld();

			worldGenerationThread = new WorldGenerationThread();
			worldGenerationThread.start();
		}

		worldEntityUpdateThread = new WorldEntityUpdateThread();
		worldLightUpdateThread = new WorldLightUpdateThread();

		worldEntityUpdateThread.start();
		worldLightUpdateThread.start();
	}
	public void doneGenerating() {
		if(!loaded) {
			spawnPlayer(MainFile.game.getClient().getPlayer());
		}else{
			loadPlayer();
		}
	}

	public void stop() {
		saveWorld();
		MainFile.game.getClient().setPlayer(null);

		worldUpdateThread.stop();
		worldEntityUpdateThread.stop();
		worldLightUpdateThread.stop();

		worldChunks.clear();
	}

	//TODO Add auto saving (Save the world every 5 min or so)
	public void saveWorld(){
		DataHandler handlerSets = MainFile.game.saveUtil.getDataHandler("saves/" + worldName + "/world.data");
		handlerSets.setObject("worldSize", worldSize);
		handlerSets.setObject("worldTimeOfDay", worldTimeOfDay);
		handlerSets.setObject("worldTime", WorldTime);
		handlerSets.setObject("dayNumber", WorldDay);
		handlerSets.setObject("timeStart", TimeTaker.getTime("worldTimePlayed:" + worldName));

		DataHandler handlerProperties = MainFile.game.saveUtil.getDataHandler("saves/" + worldName + "/worldProperties.data");
		handlerProperties.setObject("properties", worldProperties);



		//TODO For some reason only one chunk is saved?
		//TODO Save chunks sepratly
		HashMap<Point, Chunk> blStore = new HashMap<>();
		for(int x = 0; x < (worldSize.xSize / Chunk.chunkSize); x += 1){
			for(int y = 0; y < (worldSize.ySize / Chunk.chunkSize); y += 1){
				MainFile.game.saveUtil.saveObjectFile(worldChunks.get(new Point(x, y)), "saves/" + worldName + "/chunks/" + "chunk_" + x + "_" + y + ".data");
			}
		}

		MainFile.game.saveUtil.saveObjectFile(Entities, "saves/" + worldName + "/worldEntities.data");
	}

	public void loadPlayer(){
		//TODO Make it where if it cant find the player in the world just add the one that was created before checking
		for(Entity ent : Entities){
			if(ent instanceof EntityPlayer){
				EntityPlayer pl = (EntityPlayer)ent;

				if(pl.name.equalsIgnoreCase(MainFile.game.getClient().playerId)){
					MainFile.game.getClient().setPlayer(pl);
					return;
				}
			}
		}

		//If player loading failed add created player
		if(MainFile.game.getClient().getPlayer() != null){
			Entities.add(MainFile.game.getClient().getPlayer());
		}
	}

	//TODO Blocks are not being loaded!
	public void loadWorld(String name){
		DataHandler handlerSets = MainFile.game.saveUtil.getDataHandler("saves/" + name + "/world.data");
		worldName = name;
		WorldTime = handlerSets.getInteger("worldTime");
		WorldDay = handlerSets.getInteger("dayNumber");
		timePlayed = handlerSets.getLong("timeStart");
		TimeTaker.startTimeTaker("worldTimePlayed:" + worldName, System.currentTimeMillis() - timePlayed);

		String t = handlerSets.getString("worldSize");
		for(EnumWorldSize ee : EnumWorldSize.values()) {
			if (ee.name().equals(t)) {
				worldSize = ee;
				break;
			}
		}
		String tt = handlerSets.getString("worldTimeOfDay");
		for(EnumWorldTime ee : EnumWorldTime.values()) {
			if (ee.name().equals(tt)) {
				worldTimeOfDay = ee;
				break;
			}
		}

		DataHandler handlerProperties = MainFile.game.saveUtil.getDataHandler("saves/" + worldName + "/worldProperties.data");
		worldProperties = (HashMap<String, Object>)handlerProperties.getObject("properties");


//		Object ob = MainFile.game.saveUtil.loadObjectFile("saves/" + name + "/worldBlocks.data");
//		HashMap<Point, Chunk> bl = (HashMap<Point, Chunk>)ob;
//
//		if(bl != null && bl.size() > 0) {
//			for (Map.Entry<Point, Chunk> ent : bl.entrySet()) {
////				ent.getValue().loadTextures();
////				setBlock(ent.getValue(), ent.getKey().x, ent.getKey().y);
//				worldChunks[ent.getKey().x][ent.getKey().y] = ent.getValue();
//			}
//		}
		Entities = (ArrayList<Entity>)MainFile.game.saveUtil.loadObjectFile("saves/" + worldName + "/worldEntities.data");
		loadPlayer();

		if(MainFile.game.getClient().getPlayer() != null) {
			loadChunksNear((int) MainFile.game.getClient().getPlayer().getEntityPostion().x, (int) MainFile.game.getClient().getPlayer().getEntityPostion().y);
		}

		loaded = true;
	}

	public void loadChunksNear(int xx, int yy){
		for(int x = -1; x < 2; x++){
			for(int y = -1; y < 2; y++){
				loadChunk((xx / 16) + (x), (yy / 16) + (y));
			}
		}
	}

	public void unloadChunk(int chunkX, int chunkY){
		if(worldChunks.containsKey(new Point(chunkX, chunkY))){
			MainFile.game.saveUtil.saveObjectFile(worldChunks.get(new Point(chunkX, chunkY)), "saves/" + worldName + "/chunks/" + "chunk_" + chunkX + "_" + chunkY + ".data");
			worldChunks.remove(new Point(chunkX, chunkY));
		}
	}

	public boolean isChunkLoaded(int chunkX, int chunkY){
		return worldChunks.containsKey(new Point(chunkX, chunkY));
	}


	public void loadChunk(int chunkX, int chunkY){
		if(!isChunkLoaded(chunkX, chunkY)){
			Chunk chunk = (Chunk)MainFile.game.saveUtil.loadObjectFile("saves/" + worldName + "/chunks/" + "chunk_" + chunkX + "_" + chunkY + ".data");

			if(chunk != null){
				worldChunks.put(new Point(chunkX, chunkY), chunk);
			}else{
				createChunk(chunkX, chunkY);
			}
		}
	}

	public void createChunk(int chunkX, int chunkY){
		worldChunks.put(new Point(chunkX, chunkY), new Chunk(this, chunkX, chunkY));
	}


	public void updateTime(){
		if(MainFile.game.getClient().getPlayer() != null) {
			loadChunksNear((int) MainFile.game.getClient().getPlayer().getEntityPostion().x, (int) MainFile.game.getClient().getPlayer().getEntityPostion().y);
		}

		for(Chunk chunk : new HashMap<Point, Chunk>(worldChunks).values()){
			if(!chunk.shouldBeLoaded() && isChunkLoaded(chunk.chunkX, chunk.chunkY)){
				unloadChunk(chunk.chunkX, chunk.chunkY);
			}
		}

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


	public Chunk getChunk(int x, int y){
		int xx = (x / Chunk.chunkSize);
		int yy = (y / Chunk.chunkSize);

		if(!isChunkLoaded(xx, yy))
		loadChunk(xx, yy);

		if(xx >= 0 && yy >= 0) {
			if (xx < (worldSize.xSize / Chunk.chunkSize) && yy < (worldSize.ySize / Chunk.chunkSize)) {
				return worldChunks.get(new Point(x / Chunk.chunkSize, y / Chunk.chunkSize));
			}
		}

		return null;
	}


	public Block getBlock( int x, int y ) {
		return getBlock(x, y, false);
	}

	public Block getBlock( int x, int y, boolean allowAir ) {
		if(getChunk(x, y) == null) return Blocks.blockAir;
		Block b = getChunk(x, y).getBlock(x,y, allowAir);

		return b != null ? b : null;
	}

	public void setBlock( Block block, int x, int y ) {
		if(getChunk(x, y) != null) {
			getChunk(x, y).setBlock(block, x, y);
		}
	}

	public void removeTickBlock(int x, int y){
		if(getChunk(x, y) != null)
		getChunk(x, y).removeTickBlock(x, y);
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

		MainFile.game.getClient().hasSpawnedPlayer = true;
	}

	public void updateBlocks() {
		try {
			if(worldChunks != null) {
				for(Map.Entry<Point, Chunk> ent : new HashMap<Point, Chunk>(worldChunks).entrySet()) {

					if(ent.getValue() == null || ent.getValue().tickableBlocks == null)
						continue;

					//TODO ConcurrentModificationError
					for (Point p : new ArrayList<Point>(ent.getValue().tickableBlocks)) {
						Block block = getBlock(p.x, p.y);

						if (block != null) {
							if (block instanceof ITickBlock) {
								ITickBlock up = (ITickBlock) block;

								int x = p.x, y = p.y;

								if (MainFile.game.getClient().getPlayer().getEntityPostion().distance(x, y) <= (ConfigValues.renderDistance * 2) || up.updateOutofBounds()) {
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
			}
		}catch (Exception e){
			LoggerUtil.exception(e);
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
	
	public LightUnit getLightUnit( int x, int y){
		if(x >= 0 && y >= 0){
			if(x < worldSize.xSize && y < worldSize.ySize){
				if(getChunk(x, y) != null) {
					return getChunk(x, y).getLightUnit(x, y);
				}
			}
		}
		
		return new LightUnit(ILightSource.DEFAULT_LIGHT_COLOR, ILightSource.MAX_LIGHT_STRENGTH);
	}

	public void updateLightForBlock( int xx, int yy ) {
		Block block = getBlock(xx, yy, true);

		if (block != null) {
			getLightUnit(xx,yy).setLightValue(0);
			getLightUnit(xx,yy).setLightColor(ILightSource.DEFAULT_LIGHT_COLOR);

			if (block instanceof ILightSource) {
				getLightUnit(xx,yy).setLightValue(((ILightSource) block).getOutputStrength());
				getLightUnit(xx,yy).setLightColor(((ILightSource) block).getLightColor());
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
							getLightUnit(xx,yy).setLightValue(b.getLightValue(this, xPos, yPos) - 1);
							if (getLightUnit(xx,yy).getLightColor() != getLightUnit(xx,yy).getLightColor()) {
								getLightUnit(xx,yy).setLightColor(getLightUnit(xx,yy).getLightColor());
							}

						}
					}
				}
			}

			if (!hasLight && !(block instanceof ILightSource)) {
				getLightUnit(xx,yy).setLightValue(0);
				getLightUnit(xx,yy).setLightColor(ILightSource.DEFAULT_LIGHT_COLOR);
			}

		}
	}

	public void updateLightForBlocks() {
		updateLightForBlocks(MainFile.game.getClient().getPlayer() != null);
	}

	public void updateLightForBlocks(Boolean t ) {
		if (t) {
			for (int x = -(ConfigValues.lightUpdateRenderRange / 2); x < (ConfigValues.lightUpdateRenderRange / 2); x++) {
				for (int y = -(ConfigValues.lightUpdateRenderRange / 2); y < (ConfigValues.lightUpdateRenderRange / 2); y++) {

					int xPos = (int) MainFile.game.getClient().getPlayer().getEntityPostion().x + x, yPos = (int) MainFile.game.getClient().getPlayer().getEntityPostion().y + y;
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
		return worldTimeOfDay == world.worldTimeOfDay;

	}

	//TODO Getting tired of this... Still takes time when game is paused... Maybe is should just change to a tick system
	public String getTimePlayed(){
		if(isLive && !MainFile.game.gameContainer.isPaused()) {
			timePlayed = System.currentTimeMillis() - TimeTaker.getStartTime("worldTimePlayed:" + worldName);
		}
		String t = TimeTaker.getText("worldTimePlayed:" + worldName, 0,(timePlayed), "<days><hours><mins><secs>", false);

		return t;
	}

	@Override
	public int hashCode() {
		int result = worldName.hashCode();
		result = 31 * result + worldSize.hashCode();
		result = 31 * result + (worldProperties != null ? worldProperties.hashCode() : 0);
		result = 31 * result + (Entities != null ? Entities.hashCode() : 0);
		result = 31 * result + WorldTime;
		result = 31 * result + WorldTimeDayEnd;
		result = 31 * result + (worldTimeOfDay != null ? worldTimeOfDay.hashCode() : 0);
		result = 31 * result + WorldDay;
		result = 31 * result + (generating ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		String t = getTimePlayed();

		return "World{" +
				"worldName='" + worldName + '\'' +
				", worldSize=" + worldSize +
				", entities=" + (Entities != null ? Entities.size() : 0) +
				", timePlayed= " + (t != null && t.length() > 0  ? t.substring(0, t.length()-1) : "") +
				", properties=" + worldProperties +
				", loaded=" + loaded +
				", generating=" + generating +
				", WorldDay=" + WorldDay +
				", WorldTime=" + WorldTime +
				", worldTimeOfDay=" + worldTimeOfDay +
				'}';
	}
}
