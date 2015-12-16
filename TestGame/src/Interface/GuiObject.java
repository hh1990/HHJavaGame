package Interface;

import Main.MainFile;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.gui.MouseOverArea;

public abstract class GuiObject extends MouseOverArea {

	public int x, y;
	public int width, height;
	public boolean enabled = true;
	public Gui gui;

	public GuiObject( int x, int y, int width, int height, Gui gui ) {
		super(MainFile.gameContainer, null, x, y, width, height);

		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;

		this.gui = gui;
	}

	public abstract void onClicked( int button, int x, int y, Gui gui );

	public abstract void renderObject( Graphics g2, Gui gui );
}
