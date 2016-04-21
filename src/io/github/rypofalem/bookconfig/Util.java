package io.github.rypofalem.bookconfig;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;

public abstract class Util {
	
    //gets any sign in a radius
    public static ArrayList<Sign> getSignsInRadius(Location center, int radius){
    	return  getSignsInRadius(center, radius, null);
    }

    //if type is null, gets any sign in a radius
    // otherwise, gets any sign with a line that matches (ignoring case) type
    public static ArrayList<Sign> getSignsInRadius(Location center, int radius, String type){
    	ArrayList<Sign> signs = new ArrayList<Sign>();
    	for(int x =0; x <= radius*2; x++){
			for(int y =0; y <= radius*2; y++){
				for(int z =0; z <= radius*2; z++){
					Location testLoc = new Location(center.getWorld(), center.getBlockX() - radius + x, center.getBlockY() - radius + y, center.getBlockZ() - radius + z);
					if(testLoc.getBlock().getType() == Material.WALL_SIGN  || testLoc.getBlock().getType() == Material.SIGN_POST){
						Sign sign = (Sign)testLoc.getBlock().getState();
						if(type == null){
							signs.add(sign);
						} else{
							for(String line : sign.getLines()){
								if(line.equalsIgnoreCase(type)){
									signs.add(sign);
									break;
								}
							}
						}
					}
				}
			}
		}
    	return signs;
    }
}