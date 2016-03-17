package Settings;

import GameFiles.BaseGame;
import Main.MainFile;
import Render.EnumRenderMode;
import Settings.Values.ConfigOption;
import Settings.Values.Keybinding;
import Utils.ConfigValues;
import Utils.DataHandler;
import Utils.FileUtil;
import Utils.LoggerUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.newdawn.slick.Input;

import java.util.logging.Level;

public class ConfigFile extends Config{

	private ConfigOption[] configOptions = new ConfigOption[]{
	   new ConfigOption("Simple Rendering", new Boolean[]{ false, true }, ConfigValues.simpleBlockRender) {
		@Override
		public void setValue( Object ob ) {
			ConfigValues.simpleBlockRender = (Boolean)ob;
		}

	}, new ConfigOption("Render mode", new EnumRenderMode[]{ EnumRenderMode.render2_5D, EnumRenderMode.render2D }, ConfigValues.renderMod) {
		@Override
		public void setValue( Object ob ) {
			ConfigValues.renderMod = (EnumRenderMode)ob;
		}

	}, new ConfigOption("Debug mode", new Boolean[]{ false, true }, ConfigValues.debug) {
		@Override
		public void setValue( Object ob ) {
			ConfigValues.debug = (Boolean)ob;
		}

		public boolean showOption(){return MainFile.inDebugMode;}

	}, new ConfigOption("Vsync", new Boolean[]{true, false}, MainFile.game.gameContainer.isVSyncRequested()) {
		@Override
		public void setValue(Object ob) {
			MainFile.game.gameContainer.setVSync((Boolean)ob);
		}
	}, new ConfigOption("Brightness", new Float[]{1F, 0.25F, 0.5F, 0.75F}, 1F) {
		@Override
		public void setValue(Object ob) {
			if((float)ob < 1F){
				ConfigValues.brightness = (float)ob + 1F;
			}else{
				ConfigValues.brightness = (float)ob;
			}
		}
	},
	};

	private Keybinding[] keybindings = new Keybinding[]{
			new Keybinding("Exit/menu", "exit", Input.KEY_ESCAPE, "Menus"),
			new Keybinding("Open map", "openMap", Input.KEY_M, "Menus"),

			new Keybinding("Jump", "jump.walk", Input.KEY_W, "Movement"),
			new Keybinding("Walk right", "right.walk", Input.KEY_D, "Movement"),
			new Keybinding("Walk left", "left.walk", Input.KEY_A, "Movement"),

			new Keybinding("Open inventory", "inventory", Input.KEY_E, "Inventory"),
			new Keybinding("Crafting Inventory", "crafting", Input.KEY_C, "Inventory"),

			new Keybinding("Drop item", "drop", Input.KEY_Q, "Action"),

			new Keybinding("Toggle chunks", "chunkRender", Input.KEY_L, "Debug"){
				@Override
				public boolean isEnabled() {
					return MainFile.inDebugMode;
				}
			},
	};


	@Override
	public Keybinding[] getKeybindings() {
		return keybindings;
	}

	@Override
	public ConfigOption[] getConfigOptions() {
		return configOptions;
	}


	public void saveConfig( BaseGame game, String fileLoc){
		super.saveConfig(game, fileLoc);

		DataHandler handlerOptions = game.saveUtil.getDataHandler(fileLoc + "data.cfg");
		handlerOptions.setObject("texturePackName", MainFile.game.texturePack.name);
	}

	public void loadConfig( BaseGame game, String fileLoc){
		super.loadConfig(game,fileLoc);

		DataHandler handlerOptions = game.saveUtil.getDataHandler(fileLoc + "data.cfg");
		String t = (String)handlerOptions.getString("texturePackName");

		if(FileUtil.getTexturePackByName(t) != null){
			MainFile.game.texturePack = FileUtil.getTexturePackByName(t);
		}
	}

}

