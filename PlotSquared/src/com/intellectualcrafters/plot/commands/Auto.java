/*
 * Copyright (c) IntellectualCrafters - 2014.
 * You are not allowed to distribute and/or monetize any of our intellectual property.
 * IntellectualCrafters is not affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 *
 * >> File = Auto.java
 * >> Generated by: Citymonstret at 2014-08-09 01:40
 */
package com.intellectualcrafters.plot.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;

@SuppressWarnings("deprecation")
public class Auto extends SubCommand {
    public Auto() {
        super("auto", "plots.auto", "Claim the nearest plot", "auto", "a", CommandCategory.CLAIMING);
    }

    // TODO auto claim a mega plot!!!!!!!!!!!!
    @Override
    public boolean execute(Player plr, String... args) {
        World world;
        int size_x = 1;
        int size_z = 1;
        if (PlotMain.getPlotWorlds().length == 1) {
            world = Bukkit.getWorld(PlotMain.getPlotWorlds()[0]);
        } else {
            if (PlotMain.isPlotWorld(plr.getWorld())) {
                world = plr.getWorld();
            } else {
                PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
                return false;
            }
        }
        if (args.length == 1) {
            if (PlotMain.hasPermission(plr, "plots.auto.mega")) {
                try {
                    String[] split = args[0].split(",");
                    size_x = Integer.parseInt(split[0]);
                    size_z = Integer.parseInt(split[1]);
                    if ((size_x < 1) || (size_z < 1)) {
                        PlayerFunctions.sendMessage(plr, "&cError: size<=0");
                    }
                    if ((size_x > 4) || (size_z > 4)) {
                        PlayerFunctions.sendMessage(plr, "&cError: size>4");
                    }
                } catch (Exception e) {
                    PlayerFunctions.sendMessage(plr, "&cError: Invalid size (X,Y)");
                    return false;
                }
            } else {
                PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
                return false;
            }
        }
        if (PlayerFunctions.getPlayerPlotCount(world, plr) >= PlayerFunctions.getAllowedPlots(plr)) {
            PlayerFunctions.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
            return false;
        }
        boolean br = false;
        int x = 0, z = 0, q = 100;
        PlotId id;
        if ((size_x == 1) && (size_z == 1)) {
            while (!br) {
                id = new PlotId(x, z);
                if (PlotHelper.getPlot(world, id).owner == null) {
                    Plot plot = PlotHelper.getPlot(world, id);
                    boolean result = Claim.claimPlot(plr, plot, true);
                    br = !result;
                }
                if ((z < q) && ((z - x) < q)) {
                    z++;
                } else if (x < q) {
                    x++;
                    z = q - 100;
                } else {
                    q += 100;
                    x = q;
                    z = q;
                }
            }
        } else {
            while (!br) {
                PlotId start = new PlotId(x, z);
                PlotId end = new PlotId((x + size_x) - 1, (z + size_z) - 1);
                if (isUnowned(world, start, end)) {
                    // TODO claim event
                    // Claim.claimPlot calls that event...
                    for (int i = start.x; i <= end.x; i++) {
                        for (int j = start.y; j <= end.y; j++) {
                            Plot plot = PlotHelper.getPlot(world, new PlotId(i, j));
                            boolean teleport = ((i == end.x) && (j == end.y)) ? true : false;
                            Claim.claimPlot(plr, plot, teleport);
                        }
                    }
                    PlotHelper.mergePlots(world, PlayerFunctions.getPlotSelectionIds(world, start, end));
                    br = true;
                }
                if ((z < q) && ((z - x) < q)) {
                    z += size_z;
                } else if (x < q) {
                    x += size_x;
                    z = q - 100;
                } else {
                    q += 100;
                    x = q;
                    z = q;
                }
            }
        }
        return true;
    }

    public boolean isUnowned(World world, PlotId pos1, PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                PlotId id = new PlotId(x, y);
                if (PlotMain.getPlots(world).get(id) != null) {
                    if (PlotMain.getPlots(world).get(id).owner != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
