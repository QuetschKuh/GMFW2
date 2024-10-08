package de.lolsu.gmfw.events.ingame;

import de.lolsu.gmfw.interfaces.IGamestateManager;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.managers.GamestateManager;
import de.lolsu.gmfw.statistics.StatManager;
import de.lolsu.gmfw.statistics.Stats;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Damage implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();

        // Update last damager and last damage type if was hit by a player, exploded by a tnt lit by a player or shot by a player
        if(e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;

            Player damager = null;
            if (ee.getDamager() instanceof Player) {
                damager = (Player) ee.getDamager();
            } else if (ee.getDamager() instanceof TNTPrimed && ((TNTPrimed) ee.getDamager()).getSource() instanceof Player) {
                damager = (Player) ((TNTPrimed) ee.getDamager()).getSource();
            } else if (ee.getDamager() instanceof Projectile && ((Projectile) ee.getDamager()).getShooter() instanceof Player) {
                damager = (Player) ((Projectile) ee.getDamager()).getShooter();
            }

            if(damager != null && damager != damaged) {
                Gamemode.getPlayerMan().lastDamager.put(damaged, damager);
                Gamemode.getPlayerMan().lastTimeDamaged.put(damaged, System.currentTimeMillis());
                StatManager.increase(damager, Stats.DMG_DEALT, (int) e.getFinalDamage());
            }
        }

        StatManager.increase(damaged, Stats.DMG_TAKEN, (int) e.getFinalDamage());

        // Only continue if player 'dies' from the damage
        if(damaged.getHealth() - e.getFinalDamage() <= 0) {
            e.setCancelled(true);
            if(GamestateManager.state == IGamestateManager.State.INGAME)
                Gamemode.getPlayerMan().kill(damaged.getPlayer());
        }
    }

}
