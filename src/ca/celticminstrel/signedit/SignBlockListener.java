package ca.celticminstrel.signedit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

final class SignBlockListener extends BlockListener {
	private final SignEdit signEdit;
	
	SignBlockListener(SignEdit instance) {
		signEdit = instance;
	}

	@Override
	public void onSignChange(SignChangeEvent evt) {
		Location loc = evt.getBlock().getLocation();
		Player setter = evt.getPlayer();
		for(int i = 0; i < 4; i++)
			evt.setLine(i, signEdit.parseColour(evt.getLine(i), setter));
		if(signEdit.updates.containsKey(loc)) {
			//logger.info("Editing sign at " + loc);
			signEdit.updates.get(loc).setLines(evt.getLines()).run();
			signEdit.updates.remove(loc);
		} else if(!signEdit.ownership.containsKey(loc)) {
			//logger.info("Placing sign at " + loc);
			signEdit.ownership.put(loc, setter.getName());
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent evt) {
		Block block = evt.getBlock();
		if(signEdit.updates.containsKey(block.getLocation())) {
			//logger.info("Cancelled breaking of an updater sign.");
			evt.setCancelled(true);
		} else if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
			if(Option.BREAK_PROTECT.get()) {
				Player player = evt.getPlayer();
				if(!signEdit.isOwnerOf(player, evt.getBlock().getLocation())) {
					evt.setCancelled(true);
					player.sendMessage("Sorry, you are not the owner of that sign.");
					return;
				}
			}
			signEdit.ownership.remove(block.getLocation());
		}
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent evt) {
		Block block = evt.getBlockPlaced();
		if(block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) return;
		if(signEdit.updates.containsKey(block.getLocation())) {
			Sign updater = (Sign) block.getState();
			Sign editing = (Sign) evt.getBlockAgainst().getState();
			int i = 0;
			for(String line : editing.getLines())
				updater.setLine(i++, line.replace("&","&&").replace('\u00A7', '&'));
			updater.update();
		}
	}
}