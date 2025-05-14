package GhostyPlugin.Modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ArmorReseter implements Listener {

    private static final Random Random = new Random();

    private static final double ArmorBreakChance = 0.025;
    private static final double AnvilDamageChance = 0.12;;
    private static final int MinDurabilityLossPercent = 30;
    private static final int MaxDurabilityLossPercent = 70;

    private final Map<UUID, Block> PlayerAnvilInteractions = new HashMap<>();

    @EventHandler
    public void OnAnvilPrepare(PrepareAnvilEvent Event) {
        ItemStack[] Items = {Event.getInventory().getFirstItem(), Event.getInventory().getSecondItem()};

        if (!IsValidArmorReset(Items[0], Items[1])) {
            return;
        }

        ItemStack ResultItem = CreateResetArmorItem(Items[0]);
        Event.setResult(ResultItem);
    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent Event) {
        if (Event.getInventory().getType() != InventoryType.ANVIL || Event.getRawSlot() != 2 ||
            Event.getCurrentItem() == null || Event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player Player = (Player) Event.getWhoClicked();
        AnvilInventory AnvilInventory = (AnvilInventory) Event.getInventory();
        ItemStack[] Items = {AnvilInventory.getFirstItem(), AnvilInventory.getSecondItem()};

        if (!IsValidArmorReset(Items[0], Items[1])) {
            return;
        }

        if (!HasInventorySpace(Player, CreateResetArmorItem(Items[0]))) {
            Event.setCancelled(true);
            return;
        }

        Items[1].setAmount(Items[1].getAmount() - 1);

        if (Random.nextDouble() <= ArmorBreakChance) {
            ItemStack DamagedItem = CreateResetArmorItem(Items[0]);
            DamageArmorItem(DamagedItem);
            Player.getInventory().addItem(DamagedItem);
            Player.playSound(Player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
        } else {
            ItemStack ResultItem = CreateResetArmorItem(Items[0]);
            Player.getInventory().addItem(ResultItem);

            Player.playSound(Player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        }

        Block AnvilBlock = PlayerAnvilInteractions.get(Player.getUniqueId());
        if (Random.nextDouble() <= AnvilDamageChance && AnvilBlock != null) {
            DamageAnvil(AnvilBlock);
            Player.playSound(Player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.7f, 1.0f);
        }

        AnvilInventory.setItem(0, null);
        if (Items[1].getAmount() <= 0) {
            AnvilInventory.setItem(1, null);
        }
    }

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent Event) {
        Player Player = Event.getPlayer();
        if (Event.getClickedBlock() != null && (
                Event.getClickedBlock().getType() == Material.ANVIL || 
                Event.getClickedBlock().getType() == Material.CHIPPED_ANVIL || 
                Event.getClickedBlock().getType() == Material.DAMAGED_ANVIL)) {
            PlayerAnvilInteractions.put(Player.getUniqueId(), Event.getClickedBlock());
        }
    }

    private boolean HasInventorySpace(Player Player, ItemStack Item) {
        for (ItemStack InvItem : Player.getInventory().getStorageContents()) {
            if (InvItem == null || InvItem.getType() == Material.AIR) {
                return true;
            }

            if (InvItem.isSimilar(Item) && InvItem.getAmount() < InvItem.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void DamageAnvil(Block Block) {
        Material BlockType = Block.getType();

        if (BlockType == Material.ANVIL) {

            org.bukkit.block.data.Directional Directional = (org.bukkit.block.data.Directional) Block.getBlockData();
            org.bukkit.block.data.Directional NewDirectional = (org.bukkit.block.data.Directional) Material.CHIPPED_ANVIL.createBlockData();
            NewDirectional.setFacing(Directional.getFacing());

            Block.setType(Material.CHIPPED_ANVIL);
            Block.setBlockData(NewDirectional);
        } else if (BlockType == Material.CHIPPED_ANVIL) {

            org.bukkit.block.data.Directional Directional = (org.bukkit.block.data.Directional) Block.getBlockData();
            org.bukkit.block.data.Directional NewDirectional = (org.bukkit.block.data.Directional) Material.DAMAGED_ANVIL.createBlockData();
            NewDirectional.setFacing(Directional.getFacing());

            Block.setType(Material.DAMAGED_ANVIL);
            Block.setBlockData(NewDirectional);

        } else if (BlockType == Material.DAMAGED_ANVIL) {
            Block.setType(Material.AIR);
        }
    }

    private boolean IsValidArmorReset(ItemStack ArmorItem, ItemStack GlassItem) {
        if (ArmorItem == null || GlassItem == null) {
            return false;
        }

        if (!(ArmorItem.getItemMeta() instanceof ArmorMeta ArmorMeta)) {
            return false;
        }

        return ArmorMeta.getTrim() != null && GlassItem.getType() == Material.GLASS_PANE;
    }

    private ItemStack CreateResetArmorItem(ItemStack ArmorItem) {
        ItemStack ResultItem = ArmorItem.clone();
        ItemMeta ResultMeta = ResultItem.getItemMeta();

        if (ResultMeta instanceof ArmorMeta ArmorMeta) {
            ArmorMeta.setTrim(null);
            ResultItem.setItemMeta(ArmorMeta);
        }

        return ResultItem;
    }
    
    private void DamageArmorItem(ItemStack ArmorItem) {
        if (ArmorItem != null && ArmorItem.getItemMeta() instanceof Damageable Damageable) {
            int MaxDurability = ArmorItem.getType().getMaxDurability();
            int CurrentDamage = Damageable.getDamage();
            int RemainingDurability = MaxDurability - CurrentDamage;
            
            if (RemainingDurability > 0) {
                int DurabilityLossPercent = MinDurabilityLossPercent + Random.nextInt(MaxDurabilityLossPercent - MinDurabilityLossPercent + 1);
                int DurabilityLoss = (RemainingDurability * DurabilityLossPercent) / 100;

                Damageable.setDamage(CurrentDamage + DurabilityLoss);
                ArmorItem.setItemMeta((ItemMeta) Damageable);
            }
        }
    }
}