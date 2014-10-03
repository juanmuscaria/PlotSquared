/*
 * Copyright (c) IntellectualCrafters - 2014.
 * You are not allowed to distribute and/or monetize any of our intellectual property.
 * IntellectualCrafters is not affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 *
 * >> File = PlayerEvents.java
 * >> Generated by: Citymonstret at 2014-08-09 01:43
 */

package com.intellectualcrafters.plot.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.Settings;
import com.intellectualcrafters.plot.Title;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;

/**
 * Player Events involving plots
 * 
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public class PlayerEvents implements Listener {

    private String getName(UUID uuid) {
        String player = Bukkit.getOfflinePlayer(uuid).getName();
        if (player != null) {
            return player;
        }
        return "unknown";
    }

    public boolean enteredPlot(Location l1, Location l2) {
        return !isInPlot(l1) && isInPlot(l2);
    }

    public boolean leftPlot(Location l1, Location l2) {
        return isInPlot(l1) && !isInPlot(l2);
    }

    private boolean isPlotWorld(Location l) {
        return PlotMain.isPlotWorld(l.getWorld());
    }

    private boolean isPlotWorld(World w) {
        return PlotMain.isPlotWorld(w);
    }

    public static boolean isInPlot(Location loc) {
        return getCurrentPlot(loc) != null;
    }

    public static Plot getCurrentPlot(Location loc) {
        PlotId id = PlayerFunctions.getPlot(loc);
        if (id == null) {
            return null;
        }
        World world = loc.getWorld();
        if (PlotMain.getPlots(world).containsKey(id)) {
            return PlotMain.getPlots(world).get(id);
        }
        return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), loc.getWorld().getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().saveData();
        }
        textures(event.getPlayer());
    }

    private void textures(Player p) {
        if ((Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1) && isPlotWorld(p.getWorld())) {
            p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
        }
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        if (isPlotWorld(event.getFrom()) && (Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1)) {
            event.getPlayer().setResourcePack("");
        } else {
            textures(event.getPlayer());
        }
    }

    @EventHandler
    public void PlayerMove(PlayerMoveEvent event) {
        try {
            Player player = event.getPlayer();
            Location from = event.getFrom();
            Location to = event.getTo();
            if ((from.getBlockX() != to.getBlockX()) || (from.getBlockZ() != to.getBlockZ())) {
                if (!isPlotWorld(player.getWorld())) {
                    return;
                }
                if (enteredPlot(event.getFrom(), event.getTo())) {
                    Plot plot = getCurrentPlot(event.getTo());
                    if (plot.hasOwner()) {
                        if (C.TITLE_ENTERED_PLOT.s().length() > 2) {
                            String sTitleMain = C.TITLE_ENTERED_PLOT.s().replaceFirst("%s", plot.getDisplayName());
                            String sTitleSub = C.TITLE_ENTERED_PLOT_SUB.s().replaceFirst("%s", getName(plot.owner));
                            ChatColor sTitleMainColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s());
                            ChatColor sTitleSubColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s());
                            Title title = new Title(sTitleMain, sTitleSub, 10, 20, 10);
                            title.setTitleColor(sTitleMainColor);
                            title.setSubtitleColor(sTitleSubColor);
                            title.setTimingsToTicks();
                            title.send(player);
                        }
                        {
                            PlayerEnterPlotEvent callEvent = new PlayerEnterPlotEvent(player, plot);
                            Bukkit.getPluginManager().callEvent(callEvent);
                        }
                        boolean admin = player.hasPermission("plots.admin");

                        PlayerFunctions.sendMessage(player, plot.settings.getJoinMessage());
                        if (plot.deny_entry(player) && !admin) {
                            event.setCancelled(true);
                            return;
                        }
                        if (plot.settings.getRain()) {
                            PlayerFunctions.togglePlotWeather(player, plot);
                        }
                        if (plot.settings.getChangeTime()) {
                            PlayerFunctions.togglePlotTime(player, plot);
                        }
                    }
                } else if (leftPlot(event.getFrom(), event.getTo())) {
                    Plot plot = getCurrentPlot(event.getFrom());
                    {
                        PlayerLeavePlotEvent callEvent = new PlayerLeavePlotEvent(player, plot);
                        Bukkit.getPluginManager().callEvent(callEvent);
                    }
                    event.getPlayer().resetPlayerTime();
                    event.getPlayer().resetPlayerWeather();
                    PlayerFunctions.sendMessage(player, plot.settings.getLeaveMessage());
                }
            }
        } catch (Exception e) {
            // Gotta catch 'em all.
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        PlotWorld plotworld = PlotMain.getWorldSettings(world);
        if (!plotworld.PLOT_CHAT) {
            return;
        }
        if (getCurrentPlot(event.getPlayer().getLocation()) == null) {
            return;
        }
        String message = event.getMessage();
        String format = C.PLOT_CHAT_FORMAT.s();
        String sender = event.getPlayer().getDisplayName();
        Plot plot = getCurrentPlot(event.getPlayer().getLocation());
        PlotId id = plot.id;
        Set<Player> recipients = event.getRecipients();
        recipients.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distanceSquared(event.getPlayer().getLocation()) <= Math.pow(plotworld.PLOT_WIDTH, 2d)) {
                if (getCurrentPlot(p.getLocation()) == plot) {
                    recipients.add(p);
                }
            }
        }
        format = format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", sender).replaceAll("%msg%", message);
        format = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void BlockDestroy(BlockBreakEvent event) {
        World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if (event.getPlayer().hasPermission("plots.admin")) {
            return;
        }
        if (isInPlot(event.getBlock().getLocation())) {
            Plot plot = getCurrentPlot(event.getBlock().getLocation());
            if (!plot.hasRights(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
        if (PlayerFunctions.getPlot(event.getBlock().getLocation()) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void BlockCreate(BlockPlaceEvent event) {
        World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if (event.getPlayer().hasPermission("plots.admin")) {
            return;
        }
        if (isInPlot(event.getBlock().getLocation())) {
            Plot plot = getCurrentPlot(event.getBlockPlaced().getLocation());
            if (!plot.hasRights(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
        if (PlayerFunctions.getPlot(event.getBlockPlaced().getLocation()) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBigBoom(EntityExplodeEvent event) {
        World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPeskyMobsChangeTheWorldLikeWTFEvent( // LOL!
            EntityChangeBlockEvent event) {
        World world = event.getBlock().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        Entity e = event.getEntity();
        if (!(e instanceof Player)) {
            if (!(e instanceof org.bukkit.entity.FallingBlock)) {
                event.setCancelled(true);
            }
        } else {
            Block b = event.getBlock();
            Player p = (Player) e;
            if (!isInPlot(b.getLocation())) {
                if (!p.hasPermission("plots.admin")) {
                    event.setCancelled(true);
                }
            } else {
                Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null) {
                    if (!p.hasPermission("plots.admin")) {
                        event.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!p.hasPermission("plots.admin")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event) {
        World world = event.getBlock().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if ((!(event.getEntity() instanceof Player))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBS(final BlockSpreadEvent e) {
        Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBF(final BlockFormEvent e) {
        Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBD(final BlockDamageEvent e) {
        Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFade(final BlockFadeEvent e) {
        Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChange(final BlockFromToEvent e) {
        Block b = e.getToBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGrow(final BlockGrowEvent e) {
        Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent e) {
        if (isInPlot(e.getBlock().getLocation())) {

            e.getDirection();
            int modifier = e.getBlocks().size();
            Location l = e.getBlock().getLocation();
            {
                if (e.getDirection() == BlockFace.EAST) {
                    l = e.getBlock().getLocation().subtract(modifier, 0, 0);
                } else if (e.getDirection() == BlockFace.NORTH) {
                    l = e.getBlock().getLocation().subtract(0, 0, modifier);
                } else if (e.getDirection() == BlockFace.SOUTH) {
                    l = e.getBlock().getLocation().add(0, 0, modifier);
                } else if (e.getDirection() == BlockFace.WEST) {
                    l = e.getBlock().getLocation().add(modifier, 0, 0);
                }

                if (!isInPlot(l)) {
                    e.setCancelled(true);
                    return;
                }
            }
            for (Block b : e.getBlocks()) {
                if (!isInPlot(b.getLocation())) {
                    return;
                }
                {
                    if (e.getDirection() == BlockFace.EAST) {
                        if (!isInPlot(b.getLocation().subtract(1, 0, 0))) {
                            e.setCancelled(true);
                        }
                    } else if (e.getDirection() == BlockFace.NORTH) {
                        if (!isInPlot(b.getLocation().subtract(0, 0, 1))) {
                            e.setCancelled(true);
                        }
                    } else if (e.getDirection() == BlockFace.SOUTH) {
                        if (!isInPlot(b.getLocation().add(0, 0, 1))) {
                            e.setCancelled(true);
                        }
                    } else if (e.getDirection() == BlockFace.WEST) {
                        if (!isInPlot(b.getLocation().add(1, 0, 0))) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonRetract(final BlockPistonRetractEvent e) {
        Block b = e.getRetractLocation().getBlock();
        if (isPlotWorld(b.getLocation()) && (e.getBlock().getType() == Material.PISTON_STICKY_BASE)) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent e) {
        List<BlockState> blocks = e.getBlocks();
        boolean f = false;
        for (int i = 0; i < blocks.size(); i++) {
            if (f || isPlotWorld(blocks.get(i).getLocation())) {
                f = true;
                if (!isInPlot(blocks.get(i).getLocation())) {
                    e.getBlocks().remove(i);
                    i--;
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if (event.getPlayer().hasPermission("plots.admin")) {
            return;
        }
        if (isInPlot(event.getClickedBlock().getLocation())) {
            Plot plot = getCurrentPlot(event.getClickedBlock().getLocation());
            if (new ArrayList<>(Arrays.asList(new Material[] { Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.LEVER, Material.STONE_PLATE, Material.WOOD_PLATE, Material.CHEST, Material.TRAPPED_CHEST, Material.TRAP_DOOR, Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.DISPENSER, Material.DROPPER

            })).contains(event.getClickedBlock().getType())) {
                return;
            }
            if (!plot.hasRights(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
        if (PlayerFunctions.getPlot(event.getClickedBlock().getLocation()) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void MobSpawn(CreatureSpawnEvent event) {
        World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if ((event.getSpawnReason() != SpawnReason.SPAWNER_EGG) || !isInPlot(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            e.setCancelled(true);
            return;
        }
        Block b = e.getBlock();
        if (b != null) {
            if (e.getPlayer() != null) {
                Player p = e.getPlayer();
                if (!isInPlot(b.getLocation())) {
                    if (!p.hasPermission("plots.admin")) {
                        e.setCancelled(true);
                    }
                } else {
                    Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null) {
                        if (!p.hasPermission("plots.admin")) {
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!p.hasPermission("plots.admin")) {
                            e.setCancelled(true);
                        }
                    }
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (isPlotWorld(event.getTo())) {
            if (isInPlot(event.getTo())) {
                Plot plot = getCurrentPlot(event.getTo());
                if (plot.deny_entry(event.getPlayer())) {
                    PlayerFunctions.sendMessage(event.getPlayer(), C.YOU_BE_DENIED);
                    event.setCancelled(true);
                }
            }
            if ((event.getTo().getBlockX() >= 29999999) || (event.getTo().getBlockX() <= -29999999) || (event.getTo().getBlockZ() >= 29999999) || (event.getTo().getBlockZ() <= -29999999)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!e.getPlayer().hasPermission("plots.admin")) {
            BlockFace bf = e.getBlockFace();
            Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
            if (isPlotWorld(b.getLocation())) {
                if (!isInPlot(b.getLocation())) {
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PERMISSION);
                    e.setCancelled(true);
                } else {
                    Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PERMISSION);
                        e.setCancelled(true);
                    } else if (!plot.hasRights(e.getPlayer())) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("PlotSquared Commands")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!e.getPlayer().hasPermission("plots.admin")) {
            Block b = e.getBlockClicked();
            if (isPlotWorld(b.getLocation())) {
                if (!isInPlot(b.getLocation())) {
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PERMISSION);
                    e.setCancelled(true);
                } else {
                    Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PERMISSION);
                        e.setCancelled(true);
                    } else if (!plot.hasRights(e.getPlayer())) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(final HangingPlaceEvent e) {
        Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            Player p = e.getPlayer();
            if (isInPlot(b.getLocation())) {
                if (!p.hasPermission("plots.admin")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                    e.setCancelled(true);
                }
            } else {
                Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        Entity r = e.getRemover();
        if (r instanceof Player) {
            Player p = (Player) r;
            Location l = e.getEntity().getLocation();
            if (isPlotWorld(l)) {
                if (!isInPlot(l)) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                } else {
                    Plot plot = getCurrentPlot(l);
                    if (plot == null) {
                        if (!p.hasPermission("plots.admin")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!p.hasPermission("plots.admin")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        Location l = e.getRightClicked().getLocation();
        if (isPlotWorld(l)) {
            Player p = e.getPlayer();
            if (!isInPlot(l)) {
                if (!p.hasPermission("plots.admin")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                    e.setCancelled(true);
                }
            } else {
                Plot plot = getCurrentPlot(l);
                if (plot == null) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
        Location l = e.getEntity().getLocation();
        Entity d = e.getDamager();
        if (isPlotWorld(l)) {
            if (d instanceof Player) {
                Player p = (Player) d;
                if (!isInPlot(l)) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setCancelled(true);
                    }
                } else {
                    Plot plot = getCurrentPlot(l);
                    if (plot == null) {
                        if (!p.hasPermission("plots.admin")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!p.hasPermission("plots.admin")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        Location l = e.getEgg().getLocation();
        if (isPlotWorld(l)) {
            Player p = e.getPlayer();
            if (!isInPlot(l)) {
                if (!p.hasPermission("plots.admin")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                    e.setHatching(false);
                }
            } else {
                Plot plot = getCurrentPlot(l);
                if (plot == null) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setHatching(false);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!p.hasPermission("plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION);
                        e.setHatching(false);
                    }
                }
            }
        }
    }
}
