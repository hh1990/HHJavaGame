package Render.Renders;

import BlockFiles.BlockRender.DefaultBlockRendering;
import BlockFiles.Util.Block;
import Items.Utils.ItemStack;
import Main.MainFile;
import Render.EnumRenderMode;
import Rendering.AbstractWindowRender;
import Utils.ConfigValues;
import WorldFiles.Chunk;
import com.sun.javafx.geom.Vec2d;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BlockRendering extends AbstractWindowRender {

	@Override
	public void render( org.newdawn.slick.Graphics g2 ) {
		Vec2d plPos = new Vec2d(MainFile.game.getClient().getPlayer().getEntityPostion().x, MainFile.game.getClient().getPlayer().getEntityPostion().y);

		org.newdawn.slick.geom.Rectangle c = g2.getClip();
		g2.setClip(MainFile.blockRenderBounds);

		int xxx = (ConfigValues.renderXSize * ConfigValues.size), yyy = (ConfigValues.renderYSize * ConfigValues.size);
		int j = ((xxx) / ConfigValues.size), g = ((yyy) / ConfigValues.size);

		for(int i = (ConfigValues.renderMod == EnumRenderMode.render2D || ConfigValues.simpleBlockRender ? 2 : 0); i < 3; i++) {
		HashMap<Point, Block> b = new HashMap<>();

			for (int x = -(j / 2) - 2; x < (j / 2) + 2; x++) {
				for (int y = -(g / 2) - 2; y < (g / 2) + 2; y++) {

					int xx = (int) (x + plPos.x);
					int yy = (int) (y + plPos.y);

					if(!Chunk.shouldRangeLoad(xx / Chunk.chunkSize, yy / Chunk.chunkSize))
						continue;

					if(MainFile.game.getServer().getWorld().isChunkLoaded(xx / Chunk.chunkSize, yy / Chunk.chunkSize) &&
							MainFile.game.getServer().getWorld().getChunk(xx, yy) != null && MainFile.game.getServer().getWorld().getChunk(xx, yy).shouldBeLoaded()) {

						float blockX = (float) (((xx) - plPos.x) + ConfigValues.renderRange);
						float blockY = (float) (((yy) - plPos.y) + ConfigValues.renderRange);

						if (MainFile.game.getServer().getWorld().getBlock(xx, yy) != null) {
							Block block = MainFile.game.getServer().getWorld().getBlock(xx, yy);

							if (block != null)
								if (block.isBlockSolid()) {
									((DefaultBlockRendering) block.getRender()).renderBlock(g2, (int) ((blockX) * ConfigValues.size), (int) ((blockY) * ConfigValues.size), block.getRenderMode(), new ItemStack(block), MainFile.game.getServer().getWorld(), xx, yy, i);
								} else {
									b.put(new Point(xx, yy), block);
								}
						}

					}
				}
			}

			//Non-solid block rendering is delayed to prevent overlay issues
			for (Map.Entry<Point, Block> bb : b.entrySet()) {
				float blockX = (float) (((bb.getKey().x) - plPos.x) + ConfigValues.renderRange);
				float blockY = (float) (((bb.getKey().y) - plPos.y) + ConfigValues.renderRange);

				((DefaultBlockRendering) bb.getValue().getRender()).renderBlock(g2,(int) ((blockX) * ConfigValues.size), (int) ((blockY) * ConfigValues.size), bb.getValue().getRenderMode(), new ItemStack(bb.getValue()), MainFile.game.getServer().getWorld(), bb.getKey().x, bb.getKey().y, i);
			}

		}

		try {
			if (ConfigValues.renderChunks) {
				if (MainFile.game.getServer().getWorld().worldChunks != null) {
					for (Chunk cc : new ArrayList<Chunk>(MainFile.game.getServer().getWorld().worldChunks.values())) {
						if (cc == null)
							continue;

						int xx = (int) ((cc.chunkX * Chunk.chunkSize));
						int yy = (int) ((cc.chunkY * Chunk.chunkSize));

						float blockX = (float) (((xx) - plPos.x) + ConfigValues.renderRange);
						float blockY = (float) (((yy) - plPos.y) + ConfigValues.renderRange);

						float sh = 0.3F;

						if (ConfigValues.debug) {
							g2.setColor(cc.shouldBeLoaded() ? cc.generated ? new Color(0,1.0f,0,sh) : new Color(0.5f,0.5f,0,sh) : new Color(1.0f,1.0f,0,sh));
							g2.fill(new Rectangle((int) ((blockX) * ConfigValues.size), (int) ((blockY) * ConfigValues.size), Chunk.chunkSize * ConfigValues.size, Chunk.chunkSize * ConfigValues.size));
						}


						g2.setColor(Color.black);
						g2.draw(new Rectangle((int) ((blockX) * ConfigValues.size), (int) ((blockY) * ConfigValues.size), Chunk.chunkSize * ConfigValues.size, Chunk.chunkSize * ConfigValues.size));

					}
				}
			}
		}catch (Exception e){

		}



		g2.setClip(c);
	}

	@Override
	public boolean canRender() {
		return ConfigValues.RENDER_BLOCKS && MainFile.game.getServer().getWorld() != null && !MainFile.game.getServer().getWorld().generating;
	}

	@Override
	public boolean canRenderWithWindow() {
		return false;
	}
}
