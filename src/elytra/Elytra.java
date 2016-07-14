package elytra;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Elytra extends JavaPlugin implements Listener {

    private final int version = 3;

    private final HashMap<String, Long> delay = new HashMap<>();
    private final HashMap<String, Long> delayMessage = new HashMap<>();

    private final LinkedList<String> denyWorlds = new LinkedList<>();
    
    private ItemStack fuel;

    int setDelay = 4;
    float velocity = 1.5F;

    boolean enablePowerParticle = true;
    boolean enablePowerSound = true;
    boolean enablePowerSoundReload = true;
    boolean enableFlyParticle = true;

    boolean enablePowerFuel = true;
    boolean enableMessagesFuel = true;

    String messageFuel = "You need have fuel - Fire Charge, to use power!";

    Particle particleElytra = Particle.valueOf("CLOUD");
    Particle particlePower = Particle.valueOf("FLAME");
    Sound soundPower = Sound.valueOf("ITEM_FIRECHARGE_USE");
    Sound soundReload = Sound.valueOf("BLOCK_FIRE_EXTINGUISH");

    float soundVolumePower = 1.0F;
    float soundVolumePowerReload = 1.0F;
    float soundVolumeElytra = 1.0F;

    float soundPichPower = 1.0F;
    float soundPichPowerReload = 1.0F;
    float soundPichElytra = 1.0F;

    int particleCountPower = 16;
    float particlePowerDx = 0.1F;
    float particlePowerDy = 0.1F;
    float particlePowerDz = 0.1F;
    float particlePowerSpeed = 0.15F;

    int particleCountElytra = 3;
    float particleFlyDx = 0.1F;
    float particleFlyDy = 0.1F;
    float particleFlyDz = 0.1F;
    float particleFlySpeed = 0.1F;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.reloadConfigElytra();
        this.getLogger().info("Enabled");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("powerelytra.admin.reload")) {
                sender.sendMessage("You have no permission for use this command");
                return true;
            }

            reloadConfigElytra();
            sender.sendMessage("[PowerElitra] Config reloaded.");

            return true;
        }

        // /<command> givefuel amount playerName
        if (args.length >= 1 && args[0].equalsIgnoreCase("givefuel")) {
            if (!sender.hasPermission("powerelytra.admin.givefuel")) {
                sender.sendMessage("You have no permission for use this command");
                return true;
            }

            Player receiver = null;
            if (args.length >= 3) {
                receiver = this.getServer().getPlayer(args[2]);
            } else if (sender instanceof Player) {
                receiver = (Player) sender;
            }

            if (receiver == null) {
                sender.sendMessage("[PowerElitra] Player not found or not provided.");
                return true;
            }

            int amount = this.fuel.getAmount();

            if (args.length > 1) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception ex) {
                    sender.sendMessage("Wrong value: " + args[1]);
                }
            }

            if (amount < 1) {
                amount = 1;
            } else if (amount > 128) {
                amount = 128;
            }
            
            ItemStack toDrop = this.fuel.clone();
            toDrop.setAmount(amount);

            receiver.getWorld().dropItemNaturally(receiver.getLocation(), fuel);
            return true;
        }

        //TODO: help
        return true;
    }

    private void reloadConfigElytra() {
        File cfgFile = new File(this.getDataFolder(), "config.yml");
        if (!cfgFile.exists()) {
            this.saveDefaultConfig();
        }

        int configVersion = this.getConfig().getInt("version");

        if (this.version != configVersion) {
            cfgFile.renameTo(new File(this.getDataFolder(), "configOld-v" + configVersion + ".yml"));
            cfgFile.delete();
            this.saveDefaultConfig();
            this.getLogger().log(Level.INFO, "Loading default config file. Your config version != {0}", this.version);
        }

        this.reloadConfig();
        FileConfiguration c = this.getConfig();
        this.setDelay = c.getInt("delay");
        this.velocity = (float) c.getDouble("velocity");
        this.enablePowerParticle = c.getBoolean("power.particle.enable");
        this.particlePower = Particle.valueOf(c.getString("power.particle.type"));
        this.particleCountPower = c.getInt("power.particle.count");
        this.particlePowerDx = (float) c.getDouble("power.particle.dx");
        this.particlePowerDy = (float) c.getDouble("power.particle.dy");
        this.particlePowerDz = (float) c.getDouble("power.particle.dz");
        this.particlePowerSpeed = (float) c.getDouble("power.particle.speed");
        this.enablePowerSound = c.getBoolean("power.sound.enable");
        this.soundPower = Sound.valueOf(c.getString("power.sound.type"));
        this.soundVolumePower = (float) c.getDouble("power.sound.volume");
        this.soundPichPower = (float) c.getDouble("power.sound.pich");
        this.enablePowerSoundReload = c.getBoolean("power.soundReload.enable");
        this.soundReload = Sound.valueOf(c.getString("power.soundReload.type"));
        this.soundVolumePowerReload = (float) c.getDouble("power.soundReload.volume");
        this.soundPichPowerReload = (float) c.getDouble("power.soundReload.pich");

        this.enableFlyParticle = c.getBoolean("fly.particle.enable");
        this.particleElytra = Particle.valueOf(c.getString("fly.particle.type"));
        this.particleCountElytra = c.getInt("fly.particle.count");
        this.particleFlyDx = (float) c.getDouble("fly.particle.dx");
        this.particleFlyDy = (float) c.getDouble("fly.particle.dy");
        this.particleFlyDz = (float) c.getDouble("fly.particle.dz");
        this.particleFlySpeed = (float) c.getDouble("fly.particle.speed");

        this.enablePowerFuel = c.getBoolean("fuel.enable");
        this.enableMessagesFuel = c.getBoolean("fuel.messages.enable");
        this.messageFuel = ChatColor.translateAlternateColorCodes('&', c.getString("fuel.messages.message"));
        
        this.fuel = new ItemStack(
                c.getInt("fuel.item.id"), 
                c.getInt("fuel.item.count"), 
                (short) c.getInt("fuel.item.data")
        );
        
        if (c.getBoolean("fuel.item.loreDetect.enable")) {
            ItemMeta im = this.fuel.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            c.getStringList("fuel.item.loreDetect.lore").forEach((String loreEntry) -> {
                lore.add(ChatColor.translateAlternateColorCodes('&', loreEntry));
            });
            im.setLore(lore);
        }

        this.denyWorlds.clear();
        this.denyWorlds.addAll(c.getStringList("denyWorlds"));

        this.getLogger().info("Config reloaded.");

    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent quit) {
        Player player = quit.getPlayer();
        this.delay.remove(player.getName());
        this.delayMessage.remove(player.getName());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent move) {
        Player player = move.getPlayer();
        ItemStack chestItem = player.getInventory().getChestplate();

        if (chestItem == null || !chestItem.getType().equals(Material.ELYTRA) && player.isOnGround() && player.isFlying()) {
            return;
        }

        if (this.denyWorlds.contains(player.getWorld().getName())) {
            return;
        }

        if (!player.hasPermission("powerelytra.player.use")) {
            return;
        }

        if (this.delay.containsKey(player.getName()) && this.delay.get(player.getName()) > System.currentTimeMillis()) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        if (!this.enablePowerFuel) {
            return;
        }

        if (player.hasPermission("powerelytra.admin.nofuel")) {
            power(player);
            return;
        }

        ItemStack fuelItem = this.fuel.clone();

        int totalCount = 0;
        for (Map.Entry<Integer, ? extends ItemStack> e : player.getInventory().all(fuelItem.getType()).entrySet()) {
            totalCount += e.getValue().getAmount();
        }

        if (totalCount < fuelItem.getAmount()) {
            if (!this.enableMessagesFuel) {
                return; 
            }
            
            if (!this.delayMessage.containsKey(player.getName())) {
                return;
            }
            
            if(this.delayMessage.get(player.getName()) <= System.currentTimeMillis()) {
                this.delayMessage.put(player.getName(), System.currentTimeMillis() + (this.setDelay * 1000L));
                player.sendMessage(this.messageFuel);
            }
            
            return;
        }
        
        player.getInventory().removeItem(fuelItem);
        power(player);

        if (this.delay.get(player.getName()) != null && this.setDelay != 0) {
            if (this.delay.get(player.getName()) / 1000 == System.currentTimeMillis() / 1000) {
                if (enablePowerSoundReload) {
                    player.getWorld().playSound(player.getLocation(), this.soundReload, this.soundVolumeElytra, this.soundPichElytra);
                }
                this.delay.remove(player.getName());
            }
        }

        if (enableFlyParticle) {
            player.getWorld().spawnParticle(this.particleElytra, player.getLocation(), this.particleCountElytra, this.particleFlyDx, this.particleFlyDy, this.particleFlyDz, this.particleFlySpeed);
        }
    }

    private void power(Player player) {
        this.delay.put(player.getName(), System.currentTimeMillis() + (this.setDelay * 1000L));
        Vector pv = player.getLocation().getDirection();
        Vector v = pv.multiply(velocity);
        player.setVelocity(v);
        if (enablePowerSound) {
            player.getWorld().playSound(player.getLocation(), this.soundPower, this.soundVolumePower, this.soundPichPower);
        }
        if (enablePowerParticle) {
            player.getWorld().spawnParticle(this.particlePower, player.getLocation(), this.particleCountPower, this.particlePowerDx, this.particlePowerDy, this.particlePowerDz, this.particlePowerSpeed);
        }
    }
}
