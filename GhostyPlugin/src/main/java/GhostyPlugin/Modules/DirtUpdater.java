package GhostyPlugin.Modules;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DirtUpdater implements Listener {
    
    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent Event) {
        if (Event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!Event.getPlayer().isSneaking()) return;
        
        ItemStack ItemInHand = Event.getItem();
        if (ItemInHand == null || ItemInHand.getType() != Material.BONE_MEAL) return;
        
        Block ClickedBlock = Event.getClickedBlock();
        if (ClickedBlock == null || ClickedBlock.getType() != Material.DIRT) return;
        
        Material ClosestBlockType = FindNearestBlockType(ClickedBlock);
        
        boolean TransformSuccess = false;
        
        if (ClosestBlockType != null) {
            ClickedBlock.setType(ClosestBlockType);
            TransformSuccess = true;
        }
        
        if (TransformSuccess) {
            Event.setCancelled(true);
            
            Player Player = Event.getPlayer();

            ClickedBlock.getWorld().playSound(ClickedBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 0.8f);

            ClickedBlock.getWorld().spawnParticle(Particle.WAX_OFF, ClickedBlock.getLocation().add(0.5, 1.0, 0.5), 15, 0.3, 0.1, 0.3, 0.025);

            if (Player.getGameMode().name().equals("SURVIVAL")) {
                ItemStack NewStack = ItemInHand.clone();
                if (NewStack.getAmount() > 1) {
                    NewStack.setAmount(ItemInHand.getAmount() - 1);
                    Player.getInventory().setItemInMainHand(NewStack);
                } else {
                    Player.getInventory().setItemInMainHand(null);
                }
            }
        }
    }
    
    private Material FindNearestBlockType(Block ClickedBlock) {
        Material ClosestBlockType = null;
        double MinDistance = Double.MAX_VALUE;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                double Distance = Math.sqrt(x * x + z * z);
                
                if (Distance >= MinDistance) continue;
                
                Block NearbyBlock = ClickedBlock.getRelative(x, 0, z);
                if (NearbyBlock.getType() == Material.GRASS_BLOCK ||
                    NearbyBlock.getType() == Material.MYCELIUM ||
                    NearbyBlock.getType() == Material.PODZOL) {
                    
                    MinDistance = Distance;
                    ClosestBlockType = NearbyBlock.getType();
                }
            }
        }

        return ClosestBlockType;
    }
}