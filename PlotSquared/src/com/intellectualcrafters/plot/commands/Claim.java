/*
 * Copyright (c) IntellectualCrafters - 2014.
 * You are not allowed to distribute and/or monetize any of our intellectual property.
 * IntellectualCrafters is not affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 *
 * >> File = Claim.java
 * >> Generated by: Citymonstret at 2014-08-09 01:41
 */

package com.intellectualcrafters.plot.commands;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.SchematicHandler;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;

/**
 * 
 * @author Citymonstret
 * 
 */
public class Claim extends SubCommand {

    public Claim() {
        super(Command.CLAIM, "Claim the current plot you're standing on.", "claim", CommandCategory.CLAIMING);
    }

    @Override
    public boolean execute(Player plr, String... args) {
        String schematic = "";
        if (args.length >= 1) {
            schematic = args[0];
        }
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return true;
        }
        if (PlayerFunctions.getPlayerPlotCount(plr.getWorld(), plr) >= PlayerFunctions.getAllowedPlots(plr)) {
            PlayerFunctions.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
            return true;
        }
        Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (plot.hasOwner()) {
            PlayerFunctions.sendMessage(plr, C.PLOT_IS_CLAIMED);
            return false;
        }
        PlotWorld world = PlotMain.getWorldSettings(plot.getWorld());
        if (PlotMain.useEconomy && world.USE_ECONOMY) {
            double cost = world.PLOT_PRICE;
            if (cost > 0d) {
                Economy economy = PlotMain.economy;
                if (economy.getBalance(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                    return true;
                }
                economy.withdrawPlayer(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (!schematic.equals("")) {
            if (world.SCHEMATIC_CLAIM_SPECIFY) {
                if (!world.SCHEMATICS.contains(schematic.toLowerCase())) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
                    return true;
                }
                if (!plr.hasPermission("plots.claim." + schematic) && !plr.hasPermission("plots.admin")) {
                    PlayerFunctions.sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                    return true;
                }
            }
        }
        boolean result = claimPlot(plr, plot, false, schematic);
        if (result) {
            PlayerFunctions.sendMessage(plr, C.PLOT_NOT_CLAIMED);
            return false;
        }
        return true;

    }

    public static boolean claimPlot(Player player, Plot plot, boolean teleport) {
        return claimPlot(player, plot, teleport, "");
    }

    public static boolean claimPlot(Player player, Plot plot, boolean teleport, String schematic) {
        PlayerClaimPlotEvent event = new PlayerClaimPlotEvent(player, plot);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            PlotHelper.createPlot(player, plot);
            PlotHelper.setSign(player, plot);
            PlayerFunctions.sendMessage(player, C.CLAIMED);
            if (teleport) {
                PlotMain.teleportPlayer(player, player.getLocation(), plot);
            }
            PlotWorld world = PlotMain.getWorldSettings(plot.getWorld());
            if (world.SCHEMATIC_ON_CLAIM) {
                SchematicHandler handler = new SchematicHandler();
                SchematicHandler.Schematic sch;
                if (schematic.equals("")) {
                    sch = handler.getSchematic(world.SCHEMATIC_FILE);
                } else {
                    sch = handler.getSchematic(schematic);
                    if (sch == null) {
                        sch = handler.getSchematic(world.SCHEMATIC_FILE);
                    }
                }
                handler.paste(player.getLocation(), sch, plot);
            }
            plot.settings.setFlags(PlotMain.getWorldSettings(player.getWorld()).DEFAULT_FLAGS);
        }
        return event.isCancelled();
    }
}
