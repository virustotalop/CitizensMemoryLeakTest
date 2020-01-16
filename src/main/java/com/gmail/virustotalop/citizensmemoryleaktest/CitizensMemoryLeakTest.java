package com.gmail.virustotalop.citizensmemoryleaktest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

public class CitizensMemoryLeakTest extends JavaPlugin {

	private int mode;
	private List<Location> npcLocations;
	private NPCRegistry registry;
	private Long npcUpdateInterval;
	
	@Override
	public void onEnable()
	{
		this.saveResource("config.yml", false);
		this.mode = this.getConfig().getInt("mode");
		this.npcLocations = new ArrayList<>();
		for(String npcStr : this.getConfig().getStringList("npc-locations"))
		{
			String[] split = npcStr.split(",");
			World world = Bukkit.getServer().getWorld(split[0]);
			double x = Double.parseDouble(split[1]);
			double y = Double.parseDouble(split[2]);
			double z = Double.parseDouble(split[3]);
			Location loc = new Location(world, x, y, z);
			this.npcLocations.add(loc);
		}
		
		this.npcUpdateInterval = this.getConfig().getLong("npc-update-interval");
		
		MemoryNPCDataStore dataStore = new MemoryNPCDataStore();
		this.registry = CitizensAPI.createAnonymousNPCRegistry(dataStore);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
		{
			if(this.mode == 0)
			{
				this.registry.deregisterAll();
			}
			else if(this.mode == 1)
			{
				List<NPC> npcs = this.getNPCS();
				
				for(NPC npc : npcs)
				{
					npc.destroy();
				}
			}
			else if(this.mode == 2)
			{
				List<NPC> npcs = this.getNPCS();
				for(NPC npc : npcs)
				{
					npc.destroy();
					npc.despawn();
				}
			}
			
			for(int i = 0; i < this.npcLocations.size(); i++)
			{
				NPC npc = this.registry.createNPC(EntityType.PLAYER, "memory-test-" + i);
				npc.spawn(this.npcLocations.get(i));
			}
		}, this.npcUpdateInterval, this.npcUpdateInterval);
	}
	
	private List<NPC> getNPCS()
	{
		List<NPC> npcs = new ArrayList<>();
		Iterator<NPC> it = this.registry.iterator();
		while(it.hasNext())
		{
			npcs.add(it.next());
		}
		
		return npcs;
	}
	
	@Override
	public void onDisable()
	{
		this.registry.deregisterAll();
	}
}