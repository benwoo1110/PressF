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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PressF extends JavaPlugin implements Listener {

    private Map<OfflinePlayer, Long> coolDownTracker;
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
        if (!this.getDoItemSwap()) {
            event.setCancelled(true);
        }
        setLastTriggerTime(player);
        this.onPressCommands.executeAll(player);
    }

    public boolean canTriggerF(OfflinePlayer player) {
        return this.getMillisecondsSinceTrigger(player) > this.getCoolDown();
    }

    public long getMillisecondsSinceTrigger(OfflinePlayer player) {
        return System.currentTimeMillis() - this.getLastTriggerTime(player);
    }

    public long getLastTriggerTime(OfflinePlayer player) {
        return this.coolDownTracker.computeIfAbsent(player, p -> 0L);
    }

    public void setLastTriggerTime(OfflinePlayer player) {
        this.coolDownTracker.put(player, System.currentTimeMillis());
    }

    public long getCoolDown() {
        return this.getConfig().getLong("cooldown", 1000);
    }

    public boolean getDoItemSwap() {
        return this.getConfig().getBoolean("do-item-swap", true);
    }

    public List<String> getDisabledWorlds() {
        return this.getConfig().getStringList("disabled-worlds");
    }
}
