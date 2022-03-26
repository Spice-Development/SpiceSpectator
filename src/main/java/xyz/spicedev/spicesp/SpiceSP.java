package xyz.spicedev.spicesp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

// A listener for the spectator and spectator's stuff.

public class SpiceSP extends JavaPlugin implements Listener {
   private HashMap<String, String> Spectators;
   private HashMap<String, ItemStack[]> ArmorContents;
   private HashMap<String, ItemStack[]> InventoryContents;
   private HashMap<String, Integer> level;
   private HashMap<String, Integer> foodLevel;
   private HashMap<String, Float> expLevel;
   private HashMap<String, Boolean> isFlying;
   private HashMap<String, Collection<PotionEffect>> potions;
   private HashMap<String, GameMode> currentGameMode = new HashMap();
   private HashMap<String, Location> location;

   // A thing so that people don't have to type "§" and only have to use normal colour codes like: &c, &6, &4, &
   // Although, I didn't add a function for myself to do that because I'm too lazy, and I wanted to see if "§" actually worked without translation.

   private String replace(String path) {
      return this.getConfig().getString(path).replaceAll("&", "§");
   }

   // Registers everything onEnable omg so cool!11!!1!

   public void onEnable() {
      this.ArmorContents = new HashMap();
      this.InventoryContents = new HashMap();
      this.level = new HashMap();
      this.foodLevel = new HashMap();
      this.expLevel = new HashMap();
      this.isFlying = new HashMap();
      this.potions = new HashMap();
      this.currentGameMode = new HashMap();
      this.location = new HashMap();
      this.Spectators = new HashMap();
      this.saveDefaultConfig();
      Bukkit.getServer().getPluginManager().registerEvents(this, this);
      System.out.println("SpiceSP / Plugin enabled");
   }

   // No way, is that actually an onDisable method?

   public void onDisable() {
      System.out.println("SpiceSP / Plugin disabled");
      super.onDisable();
   }

   // Sets the spectator player and culprit's (player being spectated) stuff.

   private void setSpectate(Player player, Player culprit) {
      this.save(player);
      player.teleport(culprit.getLocation());
      this.Spectators.put(player.getName(), culprit.getName());
      player.setGameMode(GameMode.CREATIVE);
      Iterator var4 = Bukkit.getOnlinePlayers().iterator();

      while(var4.hasNext()) {
         Player online = (Player)var4.next();
         online.hidePlayer(player);
      }

   }

   // After the player is no longer being spectated.

   private void removeSpectate(Player player) {
      this.Spectators.remove(player.getName());
      this.restore(player);
      player.sendMessage("§cSpiceSP §7/ §fYou exited spectator mode");
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player online = (Player)var3.next();
         online.showPlayer(player);
      }

   }

   // Saving the player's inventory before spectating and replacing items.

   private void save(Player player) {
      this.ArmorContents.put(player.getName(), player.getInventory().getArmorContents());
      this.InventoryContents.put(player.getName(), player.getInventory().getContents());
      this.level.put(player.getName(), player.getLevel());
      this.foodLevel.put(player.getName(), player.getFoodLevel());
      this.expLevel.put(player.getName(), player.getExp());
      this.isFlying.put(player.getName(), player.isFlying());
      this.potions.put(player.getName(), player.getActivePotionEffects());
      this.currentGameMode.put(player.getName(), player.getGameMode());
      this.location.put(player.getName(), player.getLocation());
      Iterator var3 = player.getActivePotionEffects().iterator();

      while(var3.hasNext()) {
         PotionEffect effect = (PotionEffect)var3.next();
         player.removePotionEffect(effect.getType());
      }

      player.getInventory().clear();
      player.getInventory().setArmorContents(null);
      this.loadItems(player);
      player.updateInventory();
   }

   // Restoring the player's inventory after spectating.

   private void restore(Player player) {
      player.getInventory().clear();
      player.getInventory().setArmorContents(null);
      player.getInventory().clear();
      player.getInventory().setArmorContents(this.ArmorContents.get(player.getName()));
      player.getInventory().setContents(this.InventoryContents.get(player.getName()));
      player.setLevel(Integer.valueOf(this.level.get(player.getName())));
      player.setFoodLevel(Integer.valueOf(this.foodLevel.get(player.getName())));
      player.setExp(Float.valueOf(this.expLevel.get(player.getName())));
      player.setFlying(Boolean.valueOf(this.isFlying.get(player.getName())));
      player.setGameMode(this.currentGameMode.get(player.getName()));
      player.addPotionEffects(this.potions.get(player.getName()));
      player.teleport(this.location.get(player.getName()));
      player.setHealth(20.0D);
      player.updateInventory();
      String name = player.getName();
      this.ArmorContents.remove(name);
      this.InventoryContents.remove(name);
      this.level.remove(name);
      this.foodLevel.remove(name);
      this.expLevel.remove(name);
      this.isFlying.remove(name);
      this.currentGameMode.remove(name);
      this.potions.remove(name);
      this.location.remove(name);
   }

   // Commands and anti-retardation (Like people trying to spectate themselves or a player that isn't online).

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("spectate")) {
         if (sender instanceof Player) {
            Player player = (Player)sender;
            if (player.hasPermission("spicesp.spectate")) {
               if (args.length == 0) {
                  player.sendMessage("§cSpiceSP §7/ §f");
               } else {
                  Player target = Bukkit.getPlayer(args[0]);
                  if (target != null && !target.getName().equalsIgnoreCase(player.getName())) {
                     this.setSpectate(player, target);
                     player.sendMessage("§cSpiceSP §7/ §fYou are now spectating §e" + target.getName() + "§f.");
                  } else {
                     player.sendMessage("§cSpiceSP §7/ §fIt appears that §e" + target.getName() + " §fis offline. Check that you typed their name correctly.");
                  }
               }
            } else {
               player.sendMessage("§cSpiceSP §7/ §fYou need the permission §espicesp.spectate §fto execute that command.");
            }
         } else {
            sender.sendMessage("§cSpiceSP §7/ §fIt appears that the player you tried to spectate is... not a player... somehow.");
         }
      }

      return true;
   }

   // No way... Is that actually something to check when someone joins (and then removes them from the spectating thing, say that they rage-quit)?

   @EventHandler
   private void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (this.Spectators.containsKey(player.getName())) {
         this.removeSpectate(player);
         Iterator var4 = Bukkit.getOnlinePlayers().iterator();

         while(var4.hasNext()) {
            Player online = (Player)var4.next();
            online.showPlayer(player);
         }
      }

   }

   // Whenever a player right clicks with the exit item, it exits spectator mode.

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      Action action = event.getAction();
      ItemStack hand = player.getItemInHand();
      if (this.Spectators.containsKey(player.getName()) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && hand.getType() == Material.getMaterial(this.getConfig().getString("quit-item-name"))) {
         this.removeSpectate(player);
      }

   }

   // After the inventory is cleared earlier, it then puts the item in the slot you specified in the config and gives it a cool custom name.

   private void loadItems(Player player) {
      ItemStack item = new ItemStack(Material.getMaterial(this.getConfig().getString("quit-item-name")));
      ItemMeta itemmeta = item.getItemMeta();
      itemmeta.setDisplayName(this.replace("quit-item-custom-name"));
      item.setItemMeta(itemmeta);
      player.getInventory().setItem(this.getConfig().getInt("quit-item-slot"), item);
   }

   // Another anti-retardation tool, this ensures that the spectating player can't hit the player that is being spectated.

   @EventHandler
   public void onEntityDamage(EntityDamageByEntityEvent event) {
      Entity entity = event.getEntity();
      Entity attacker = event.getDamager();
      if (attacker instanceof Player) {
         Player player = (Player)attacker;
         if (this.Spectators.containsKey(player.getName())) {
            event.setCancelled(true);
         }

         if (this.Spectators.containsKey(entity.getName())) {
            event.setCancelled(true);
         }

      }
   }

   // No clicking your inventory while spectating!11!!!1 (This is to prevent them just throwing the spectator exit item away.)

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (this.Spectators.containsKey(player.getName())) {
         event.setCancelled(true);
      }

   }

   // Prevents the spectator from breaking blocks while spectating.

   @EventHandler
   public void onBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (this.Spectators.containsKey(player.getName())) {
         event.setCancelled(true);
      }

   }

   // Prevents the spectator from placing blocks while spectating.

   @EventHandler
   public void onBreak(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      if (this.Spectators.containsKey(player.getName())) {
         event.setCancelled(true);
      }

   }

   // Again, prevents the player from dropping the spectator exit item.

   @EventHandler
   public void onDrop(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      if (this.Spectators.containsKey(player.getName())) {
         event.setCancelled(true);
      }

   }
}
