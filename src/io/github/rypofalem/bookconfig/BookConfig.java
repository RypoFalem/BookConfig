package io.github.rypofalem.bookconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/*
 * Reads key:value sets from books in Minecraft.
 */
public class BookConfig {
	HashMap<String, String> map;
	ArrayList<String> debugInfo= new ArrayList<String>();
	boolean destructive = true; //whether or not to destroy signs, chests and books that it uses
	
	/*
	 * searchCenter: center point of search cuboid
	 * radius: radius of search cuboid
	 * signLabel: Looks only for signs with this text written on any line
	 * destructive: whether or not to destroy any signs, chests and books after they are used
	*/
	public BookConfig(Location searchCenter, int radius, String signLabel, boolean destructive){
		map = new HashMap<String, String>();
		this.destructive = destructive;
		load(searchCenter, radius, signLabel);
	}
	
	public BookConfig(ArrayList<BookMeta> books){
		loadFromBooks(books);
	}
	
	public boolean hasString(String key){
		String value = map.get(key);
		if(value == null || value == ""){
			return false;
		}else{
			return true;
		}
	}
	
	//Returns null if there is no string
	//Recommend calling hasString(key) first
	public String getString(String key){
		if(hasString(key)){
			return (String) map.get(key);
		}else{
			return null;
		}
	}
	
	public boolean hasInt(String key){
		String value = map.get(key);
		try{
			Integer.parseInt(value);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	//Returns Integer.MIN_VALUE if the string attached to the value isn't an int.
	//Recommend calling hasInt(key) first
	public int getInt(String key){
		if(!hasInt(key)) return Integer.MIN_VALUE;
		return Integer.parseInt(map.get(key));
	}
	
	public boolean hasFloat(String key){
		String value = map.get(key);
		try{
			Float.parseFloat(value);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	//Returns Float.MIN_VALUE if the string attached to the value isn't an int.
	//Recommend calling hasFloat(key) first
	public float getFloat(String key){
		if(!hasInt(key)) return Float.MIN_VALUE;
		return Float.parseFloat(map.get(key));
	}
	
	public boolean hasBoolean(String key){
		String value = map.get(key);
		try{
			Boolean.parseBoolean(value);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	//return false if the string attached to the value isn't a boolean
	//Recommend calling hasBoolean(key) first
	public boolean getBoolean(String key){
		if(!hasBoolean(key)) return false;
		return Boolean.parseBoolean(map.get(key));
	}
	
	//searches at midpoint "searchCenter" in a radius for signs containing a line with the right signLabel
	//Checks for chests attached to those signs, checks for books in those chests.
	public ArrayList<BookMeta> findBooks(Location searchCenter, int radius, String signLabel){
		ArrayList<Sign> signs = Util.getSignsInRadius(searchCenter, radius, signLabel);
		ArrayList<BookMeta> books = new ArrayList<BookMeta>();
		
		for(Sign sign : signs){
			org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
			BlockFace direction = signData.getAttachedFace();
			BlockState chestCandidate = sign.getBlock().getRelative(direction).getState();
			if(!(chestCandidate instanceof Chest)) continue;
			Chest chest = (Chest)chestCandidate;
			Inventory chestInv = chest.getInventory();
			
			for(ItemStack item : chestInv.getStorageContents()){
				if(item == null) continue;
				if(item.getType() == Material.BOOK_AND_QUILL || item.getType() == Material.WRITTEN_BOOK){
					books.add((BookMeta)item.getItemMeta());
				}
			}
			if(destructive){
				sign.getBlock().setType(Material.AIR);
				chest.getInventory().clear();
				chest.getBlock().setType(Material.AIR);
				
				//destroy potential double-chests
				Location twinChest = chest.getLocation().clone().add(-1, 0, 0);
				if(twinChest.getBlock().getType() == Material.CHEST) twinChest.getBlock().setType(Material.AIR);
				twinChest = chest.getLocation().clone().add(1, 0, 0);
				if(twinChest.getBlock().getType() == Material.CHEST) twinChest.getBlock().setType(Material.AIR);
				twinChest = chest.getLocation().clone().add(0, 0, -1);
				if(twinChest.getBlock().getType() == Material.CHEST) twinChest.getBlock().setType(Material.AIR);
				twinChest = chest.getLocation().clone().add(0, 0, 1);
				if(twinChest.getBlock().getType() == Material.CHEST) twinChest.getBlock().setType(Material.AIR);
			}
		}
		
		return books;
	}
	
	void load(Location searchCenter, int radius, String signLabel){
		loadFromBooks(findBooks(searchCenter, radius, signLabel));
	}
	
	void loadFromBooks(ArrayList<BookMeta> books){
		map = new HashMap<String, String>();
		for(BookMeta book : books){
			String bookName = book.getDisplayName();
			if(bookName == null) bookName = book.getTitle();
			if(bookName == null) bookName ="BookNotNamed";
			
			int pageNum = 1;
			for(String page : book.getPages()){
				Scanner scan = new Scanner(page);
				scan.useDelimiter("\n");
				
				int lineNum = 1;
				while(scan.hasNext()){
					String line = scan.next();
					line.trim();
					int colonLoc = line.indexOf(":");
					
					if(colonLoc == -1){
						String message = String.format("Book Configuration encountered an element with no colon on line %d page %d of \"%s\". This line will be ignored.", 
								lineNum, pageNum, bookName);
						debugInfo.add(message);
						continue;
					}
					if(colonLoc == 0){
						String message = String.format("Book Configuration encountered an element with no name on line %d page %d of \"%s\". This line will be ignored.", 
								lineNum, pageNum, bookName);
						debugInfo.add(message);
						continue;
					}
					if(colonLoc == line.length()-1){
						String message = String.format("Book Configuration encountered an empty element: \"%s\" on line %d page %d of \"%s\". This line will be ignored.", 
								ChatColor.stripColor(line.substring(0, colonLoc)), lineNum, pageNum, bookName);
						debugInfo.add(message);
						continue;
					}
					
					String key = line.substring(0, colonLoc);
					key = ChatColor.stripColor(key).trim();
					String value = line.substring(colonLoc+1);
					value = ChatColor.stripColor(value).trim();
					map.put(key, value);
					
					lineNum++;
				}
				scan.close();
			}
		}
	}
	
	public Set<String> getKeys(){
		if(map != null) return map.keySet();
		return null;
	}
	
	public ArrayList<String> getDebugMessages(){
		return debugInfo;
	}
}
