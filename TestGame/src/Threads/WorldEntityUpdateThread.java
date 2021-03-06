package Threads;

import EntityFiles.Entity;
import Main.MainFile;
import Utils.LoggerUtil;

public class WorldEntityUpdateThread extends Thread {

	public WorldEntityUpdateThread() {
		setName("WorldEntityUpdateThread");
	}

	public void run() {
		while (true) {
			try {

				if (!MainFile.game.gameContainer.isPaused()) {
					if(MainFile.game.getServer().getWorld().Entities != null && MainFile.game.getServer().getWorld().Entities.size() > 0) {
						for (Entity ent : MainFile.game.getServer().getWorld().Entities) {
							ent.updateEntity();
						}

					}
				}

				try {
					sleep(130);
				} catch (Exception e) {

				}

			} catch (Exception e) {
				LoggerUtil.exception(e);
			}
		}
	}
}

