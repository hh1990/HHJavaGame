package Items.Rendering;

import Items.Utils.Item;
import Items.Utils.ItemStack;
import Utils.ConfigValues;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

public class ItemRendrerer implements IItemRenderer {

	public static ItemRendrerer staticReferense = new ItemRendrerer();

	@Override
	public void renderItem( Graphics g, int rX, int rY, ItemStack m ) {
		Item item = (Item)m.getItem();

		if(item.getTexture(m) != null){
			item.getTexture(m).draw(rX - (ConfigValues.size / 4) , rY - (ConfigValues.size / 1.3F), ConfigValues.size * 2, ConfigValues.size * 2);

			if(m.getStackDamage() > 0){
				Rectangle rectangle = new Rectangle(rX - 16, rY + 32, 48, 8);

				g.setColor(Color.darkGray.darker());
				g.fill(rectangle);

				float f = (float)m.getStackDamage() / (float)item.getMaxItemDamage();

				rectangle.setWidth(48 - (f*48));

				if(rectangle.getWidth() > 0) {
					g.setColor(new Color(0,1.0f,0,1.0f));
					g.fill(rectangle);
				}

			}
		}
	}
}
