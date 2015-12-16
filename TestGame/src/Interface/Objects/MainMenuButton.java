package Interface.Objects;

import Interface.Gui;
import Utils.RenderUtil;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.FontUtils;

import java.awt.*;


public abstract class MainMenuButton extends GuiButton {

	//TODO Remove the hardcoded 325-x position when adding window resizing
	public MainMenuButton( int x, int y, int width, int height, String name, Gui gui ) {
		super(x, y, width, height, name, gui);
	}

	@Override
	public void renderObject( Graphics g2, Gui gui ) {
		Color temp = g2.getColor();

		boolean hover = isMouseOver();

		if (hover) {
			g2.setColor(new Color(95, 95, 95, 174));
		} else {
			g2.setColor(new Color(95, 95, 95, 86));
		}

		if (!enabled) {
			g2.setColor(new Color(41, 41, 41, 174));
		}

		g2.fill(new Rectangle(x, y, width, height));

		g2.setColor(hover ? Color.white : Color.lightGray);

		if (!enabled) g2.setColor(Color.gray);

		RenderUtil.resizeFont(g2, 22);
		RenderUtil.changeFontStyle(g2, Font.BOLD);
		FontUtils.drawCenter(g2.getFont(), text, x, y, 185);

		RenderUtil.resetFont(g2);


		g2.setColor(temp);
	}

}
