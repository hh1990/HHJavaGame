package Render.Renders;

import EntityFiles.Entity;
import Main.MainFile;
import Rendering.AbstractWindowRender;
import Utils.ConfigValues;
import com.sun.javafx.geom.Vec2d;

import java.util.ArrayList;


public class EntityRendering extends AbstractWindowRender {


	@Override
	public void render( org.newdawn.slick.Graphics g2 ) {
		Vec2d plPos = new Vec2d(MainFile.game.getClient().getPlayer().getEntityPostion().x, MainFile.game.getClient().getPlayer().getEntityPostion().y);
		for (Entity ent : new ArrayList<>(MainFile.game.getServer().getWorld().Entities)) {

			if (ent.getEntityPostion().distance(MainFile.game.getClient().getPlayer().getEntityPostion()) <= ConfigValues.renderDistance) {

				float entX = ((float) (ent.getEntityPostion().x - plPos.x) + ConfigValues.renderRange);
				float entY = ((float) (ent.getEntityPostion().y - plPos.y) + ConfigValues.renderRange) + 1;

				ent.renderEntity(g2, (int) ((entX) * ConfigValues.size), (int) ((entY) * ConfigValues.size));
			}
		}
	}

	@Override
	public boolean canRender() {
		return ConfigValues.RENDER_ENTITIES && MainFile.game.getServer().getWorld() != null && !MainFile.game.getServer().getWorld().generating;
	}

	@Override
	public boolean canRenderWithWindow() {
		return false;
	}
}
