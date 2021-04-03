package dev.benergy10.pressf;

import dev.benergy10.commandexecutorapi.CommandGroup;
import dev.benergy10.commandexecutorapi.CommandProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PressF extends JavaPlugin implements Listener {

    private Map<OfflinePlayer, Date> coolDownTracker;
    private CommandProvider commandProvider;
    private CommandGroup onPressCommands;

    @Override
    public void onEnable() {
        this.coolDownTracker = new HashMap<>();
        this.commandProvider = new CommandProvider();

        this.saveDefaultConfig();
        this.reloadConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        List<String> commands = this.getConfig().getStringList("trigger-actions");
        this.onPressCommands = this.commandProvider.toCommandGroup(commands);
    }

    @EventHandler
    public void onPressF(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (this.getDisabledWorlds().contains(player.getWorld().getName())) {
            return;
        }
        if (!this.canTriggerF(player)) {
            return;
        }
        event.setCancelled(true);
        this.onPressCommands.executeAll(player);
    }

    public boolean canTriggerF(OfflinePlayer player) {
        return this.getMillisecondsSinceTrigger(player) > this.getCoolDown();
    }

    public long getMillisecondsSinceTrigger(OfflinePlayer player) {
        return new Date().getTime() - this.getLastTriggerTime(player).getTime();
    }

    public Date getLastTriggerTime(OfflinePlayer player) {
        return this.coolDownTracker.computeIfAbsent(player, p -> new Date(0));
    }

    public long getCoolDown() {
        return this.getConfig().getLong("cooldown", 1000);
    }

    public List<String> getDisabledWorlds() {
        return this.getConfig().getStringList("disabled-worlds");
    }
}
