package org.bukkit.craftbukkit.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Statistic.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.CraftRaid;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftRaider;
import org.bukkit.craftbukkit.entity.CraftSpellcaster;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.util.CraftDamageSource;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Animals;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Strider;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.ArrowBodyCountChangeEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.BatToggleSleepEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntitySpellCastEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.StriderTemperatureChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;

public class CraftEventFactory {
    public static final DamageSource MELTING = CraftDamageSource.copyOf(DamageSource.ON_FIRE);
    public static final DamageSource POISON = CraftDamageSource.copyOf(DamageSource.MAGIC);
    public static org.bukkit.block.Block blockDamage; // For use in EntityDamageByBlockEvent
    public static Entity entityDamage; // For use in EntityDamageByEntityEvent

    // helper methods
    private static boolean canBuild(ServerWorld world, Player player, int x, int z) {
        int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.getRegistryKey() != World.OVERWORLD) return true;
        if (spawnSize <= 0) return true;
        if (((CraftServer) Bukkit.getServer()).getHandle().getOpList().isEmpty()) return true;
        if (player.isOp()) return true;

        BlockPos chunkcoordinates = world.getSpawnPos();

        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getZ()));
        return distanceFromSpawn > spawnSize;
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * PlayerBedEnterEvent
     */
    public static Either<PlayerEntity.SleepFailureReason, Unit> callPlayerBedEnterEvent(PlayerEntity player, BlockPos bed, Either<PlayerEntity.SleepFailureReason, Unit> nmsBedResult) {
        BedEnterResult bedEnterResult = nmsBedResult.mapBoth(new Function<PlayerEntity.SleepFailureReason, BedEnterResult>() {
            @Override
            public BedEnterResult apply(PlayerEntity.SleepFailureReason t) {
                switch (t) {
                    case NOT_POSSIBLE_HERE:
                        return BedEnterResult.NOT_POSSIBLE_HERE;
                    case NOT_POSSIBLE_NOW:
                        return BedEnterResult.NOT_POSSIBLE_NOW;
                    case TOO_FAR_AWAY:
                        return BedEnterResult.TOO_FAR_AWAY;
                    case NOT_SAFE:
                        return BedEnterResult.NOT_SAFE;
                    default:
                        return BedEnterResult.OTHER_PROBLEM;
                }
            }
        }, t -> BedEnterResult.OK).map(java.util.function.Function.identity(), java.util.function.Function.identity());

        PlayerBedEnterEvent event = new PlayerBedEnterEvent((Player) player.getBukkitEntity(), CraftBlock.at(player.world, bed), bedEnterResult);
        Bukkit.getServer().getPluginManager().callEvent(event);

        Result result = event.useBed();
        if (result == Result.ALLOW) {
            return Either.right(Unit.INSTANCE);
        } else if (result == Result.DENY) {
            return Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM);
        }

        return nmsBedResult;
    }

    /**
     * Entity Enter Love Mode Event
     */
    public static EntityEnterLoveModeEvent callEntityEnterLoveModeEvent(PlayerEntity entityHuman, AnimalEntity entityAnimal, int loveTicks) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = new EntityEnterLoveModeEvent((Animals) entityAnimal.getBukkitEntity(), entityHuman != null ? (HumanEntity) entityHuman.getBukkitEntity() : null, loveTicks);
        Bukkit.getPluginManager().callEvent(entityEnterLoveModeEvent);
        return entityEnterLoveModeEvent;
    }

    /**
     * Player Harvest Block Event
     */
    public static PlayerHarvestBlockEvent callPlayerHarvestBlockEvent(World world, BlockPos blockposition, PlayerEntity who, List<ItemStack> itemsToHarvest) {
        List<org.bukkit.inventory.ItemStack> bukkitItemsToHarvest = new ArrayList<>(itemsToHarvest.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));
        Player player = (Player) who.getBukkitEntity();
        PlayerHarvestBlockEvent playerHarvestBlockEvent = new PlayerHarvestBlockEvent(player, CraftBlock.at(world, blockposition), bukkitItemsToHarvest);
        Bukkit.getPluginManager().callEvent(playerHarvestBlockEvent);
        return playerHarvestBlockEvent;
    }

    /**
     * Trade Index Change Event
     */
    public static TradeSelectEvent callTradeSelectEvent(ServerPlayerEntity player, int newIndex, MerchantScreenHandler merchant) {
        TradeSelectEvent tradeSelectEvent = new TradeSelectEvent(merchant.getBukkitView(), newIndex);
        Bukkit.getPluginManager().callEvent(tradeSelectEvent);
        return tradeSelectEvent;
    }

    /**
     * Block place methods
     */
    public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(ServerWorld world, PlayerEntity who, Hand hand, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getCraftWorld();
        CraftServer craftServer = world.getCraftServer();
        Player player = (Player) who.getBukkitEntity();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);

        boolean canBuild = true;
        for (int i = 0; i < blockStates.size(); i++) {
            if (!canBuild(world, player, blockStates.get(i).getX(), blockStates.get(i).getZ())) {
                canBuild = false;
                break;
            }
        }

        org.bukkit.inventory.ItemStack item;
        if (hand == Hand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
        } else {
            item = player.getInventory().getItemInOffHand();
        }

        BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, item, player, canBuild);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(ServerWorld world, PlayerEntity who, Hand hand, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getCraftWorld();
        CraftServer craftServer = world.getCraftServer();

        Player player = (Player) who.getBukkitEntity();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        Block placedBlock = replacedBlockState.getBlock();

        boolean canBuild = canBuild(world, player, placedBlock.getX(), placedBlock.getZ());

        org.bukkit.inventory.ItemStack item;
        EquipmentSlot equipmentSlot;
        if (hand == Hand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
            equipmentSlot = EquipmentSlot.HAND;
        } else {
            item = player.getInventory().getItemInOffHand();
            equipmentSlot = EquipmentSlot.OFF_HAND;
        }

        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, item, player, canBuild, equipmentSlot);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static void handleBlockDropItemEvent(Block block, BlockState state, ServerPlayerEntity player, List<ItemEntity> items) {
        BlockDropItemEvent event = new BlockDropItemEvent(block, state, player.getBukkitEntity(), Lists.transform(items, (item) -> (org.bukkit.entity.Item) item.getBukkitEntity()));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            for (ItemEntity item : items) {
                item.world.spawnEntity(item);
            }
        }
    }

    public static EntityPlaceEvent callEntityPlaceEvent(ItemUsageContext itemactioncontext, Entity entity) {
        return callEntityPlaceEvent(itemactioncontext.getWorld(), itemactioncontext.getBlockPos(), itemactioncontext.getSide(), itemactioncontext.getPlayer(), entity);
    }

    public static EntityPlaceEvent callEntityPlaceEvent(World world, BlockPos clickPosition, Direction clickedFace, PlayerEntity human, Entity entity) {
        Player who = (human == null) ? null : (Player) human.getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at(world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        EntityPlaceEvent event = new EntityPlaceEvent(entity.getBukkitEntity(), who, blockClicked, blockFace);
        entity.world.getCraftServer().getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Bucket methods
     */
    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(ServerWorld world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemInHand, Items.BUCKET);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(ServerWorld world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.item.Item bucket) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, ServerWorld world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.item.Item item) {
        Player player = (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());

        CraftServer craftServer = (CraftServer) player.getServer();

        Block block = CraftBlock.at(world, changed);
        Block blockClicked = CraftBlock.at(world, clicked);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

        PlayerEvent event;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, block, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketFillEvent) event).setCancelled(!canBuild(world, player, changed.getX(), changed.getZ()));
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!canBuild(world, player, changed.getX(), changed.getZ()));
        }

        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Player Interact event
     */
    public static PlayerInteractEvent callPlayerInteractEvent(PlayerEntity who, Action action, ItemStack itemstack, Hand hand) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            throw new AssertionError(String.format("%s performing %s with %s", who, action, itemstack));
        }
        return callPlayerInteractEvent(who, action, null, Direction.SOUTH, itemstack, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(PlayerEntity who, Action action, BlockPos position, Direction direction, ItemStack itemstack, Hand hand) {
        return callPlayerInteractEvent(who, action, position, direction, itemstack, false, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(PlayerEntity who, Action action, BlockPos position, Direction direction, ItemStack itemstack, boolean cancelledBlock, Hand hand) {
        Player player = (who == null) ? null : (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = null;
        if (position != null) {
            blockClicked = craftWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
        } else {
            switch (action) {
                case LEFT_CLICK_BLOCK:
                    action = Action.LEFT_CLICK_AIR;
                    break;
                case RIGHT_CLICK_BLOCK:
                    action = Action.RIGHT_CLICK_AIR;
                    break;
            }
        }
        BlockFace blockFace = CraftBlock.notchToBlockFace(direction);

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace, (hand == null) ? null : ((hand == Hand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        if (cancelledBlock) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityTransformEvent
     */
    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.entity.LivingEntity original, net.minecraft.entity.LivingEntity coverted, EntityTransformEvent.TransformReason transformReason) {
        return callEntityTransformEvent(original, Collections.singletonList(coverted), transformReason);
    }

    /**
     * EntityTransformEvent
     */
    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.entity.LivingEntity original, List<net.minecraft.entity.LivingEntity> convertedList, EntityTransformEvent.TransformReason convertType) {
        List<org.bukkit.entity.Entity> list = new ArrayList<>();
        for (net.minecraft.entity.LivingEntity entityLiving : convertedList) {
            list.add(entityLiving.getBukkitEntity());
        }

        EntityTransformEvent event = new EntityTransformEvent(original.getBukkitEntity(), list, convertType);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityShootBowEvent
     */
    public static EntityShootBowEvent callEntityShootBowEvent(net.minecraft.entity.LivingEntity who, ItemStack bow, ItemStack consumableItem, Entity entityArrow, Hand hand, float force, boolean consumeItem) {
        LivingEntity shooter = (LivingEntity) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(bow);
        CraftItemStack itemConsumable = CraftItemStack.asCraftMirror(consumableItem);
        org.bukkit.entity.Entity arrow = entityArrow.getBukkitEntity();
        EquipmentSlot handSlot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)) {
            itemInHand = null;
        }

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, itemConsumable, arrow, handSlot, force, consumeItem);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * VillagerCareerChangeEvent
     */
    public static VillagerCareerChangeEvent callVillagerCareerChangeEvent(VillagerEntity vilager, Profession future, VillagerCareerChangeEvent.ChangeReason reason) {
        VillagerCareerChangeEvent event = new VillagerCareerChangeEvent((Villager) vilager.getBukkitEntity(), future, reason);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * BlockDamageEvent
     */
    public static BlockDamageEvent callBlockDamageEvent(PlayerEntity who, int x, int y, int z, ItemStack itemstack, boolean instaBreak) {
        Player player = (who == null) ? null : (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = craftWorld.getBlockAt(x, y, z);

        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static boolean doEntityAddEventCalling(World world, Entity entity, SpawnReason spawnReason) {
        if (entity == null) return false;

        org.bukkit.event.Cancellable event = null;
        if (entity instanceof net.minecraft.entity.LivingEntity && !(entity instanceof ServerPlayerEntity)) {
            boolean isAnimal = entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity || entity instanceof GolemEntity;
            boolean isMonster = entity instanceof HostileEntity || entity instanceof GhastEntity || entity instanceof SlimeEntity;
            boolean isNpc = entity instanceof Npc;

            if (spawnReason != SpawnReason.CUSTOM) {
                if (isAnimal && !world.getCraftWorld().getAllowAnimals() || isMonster && !world.getCraftWorld().getAllowMonsters() || isNpc && !world.getCraftServer().getServer().shouldSpawnNpcs()) {
                    entity.removed = true;
                    return false;
                }
            }

            event = CraftEventFactory.callCreatureSpawnEvent((net.minecraft.entity.LivingEntity) entity, spawnReason);
        } else if (entity instanceof ItemEntity) {
            event = CraftEventFactory.callItemSpawnEvent((ItemEntity) entity);
        } else if (entity.getBukkitEntity() instanceof org.bukkit.entity.Projectile) {
            // Not all projectiles extend EntityProjectile, so check for Bukkit interface instead
            event = CraftEventFactory.callProjectileLaunchEvent(entity);
        } else if (entity.getBukkitEntity() instanceof org.bukkit.entity.Vehicle) {
            event = CraftEventFactory.callVehicleCreateEvent(entity);
        } else if (!(entity instanceof ServerPlayerEntity)) {
            event = CraftEventFactory.callEntitySpawnEvent(entity);
        }

        if (event != null && (event.isCancelled() || entity.removed)) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                vehicle.removed = true;
            }
            for (Entity passenger : entity.getPassengersDeep()) {
                passenger.removed = true;
            }
            entity.removed = true;
            return false;
        }

        return true;
    }

    /**
     * EntitySpawnEvent
     */
    public static EntitySpawnEvent callEntitySpawnEvent(Entity entity) {
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();

        EntitySpawnEvent event = new EntitySpawnEvent(bukkitEntity);
        bukkitEntity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * CreatureSpawnEvent
     */
    public static CreatureSpawnEvent callCreatureSpawnEvent(net.minecraft.entity.LivingEntity entityliving, SpawnReason spawnReason) {
        LivingEntity entity = (LivingEntity) entityliving.getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();

        CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * EntityTameEvent
     */
    public static EntityTameEvent callEntityTameEvent(MobEntity entity, PlayerEntity tamer) {
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        org.bukkit.entity.AnimalTamer bukkitTamer = (tamer != null ? tamer.getBukkitEntity() : null);
        CraftServer craftServer = (CraftServer) bukkitEntity.getServer();

        entity.persistent = true;

        EntityTameEvent event = new EntityTameEvent((LivingEntity) bukkitEntity, bukkitTamer);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemSpawnEvent
     */
    public static ItemSpawnEvent callItemSpawnEvent(ItemEntity entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item) entityitem.getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();

        ItemSpawnEvent event = new ItemSpawnEvent(entity);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemDespawnEvent
     */
    public static ItemDespawnEvent callItemDespawnEvent(ItemEntity entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item) entityitem.getBukkitEntity();

        ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());

        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemMergeEvent
     */
    public static ItemMergeEvent callItemMergeEvent(ItemEntity merging, ItemEntity mergingWith) {
        org.bukkit.entity.Item entityMerging = (org.bukkit.entity.Item) merging.getBukkitEntity();
        org.bukkit.entity.Item entityMergingWith = (org.bukkit.entity.Item) mergingWith.getBukkitEntity();

        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * PotionSplashEvent
     */
    public static PotionSplashEvent callPotionSplashEvent(PotionEntity potion, Map<LivingEntity, Double> affectedEntities) {
        ThrownPotion thrownPotion = (ThrownPotion) potion.getBukkitEntity();

        PotionSplashEvent event = new PotionSplashEvent(thrownPotion, affectedEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LingeringPotionSplashEvent callLingeringPotionSplashEvent(PotionEntity potion, AreaEffectCloudEntity cloud) {
        ThrownPotion thrownPotion = (ThrownPotion) potion.getBukkitEntity();
        AreaEffectCloud effectCloud = (AreaEffectCloud) cloud.getBukkitEntity();

        LingeringPotionSplashEvent event = new LingeringPotionSplashEvent(thrownPotion, effectCloud);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * BlockFadeEvent
     */
    public static BlockFadeEvent callBlockFadeEvent(WorldAccess world, BlockPos pos, net.minecraft.block.BlockState newBlock) {
        CraftBlockState state = CraftBlockState.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleMoistureChangeEvent(World world, BlockPos pos, net.minecraft.block.BlockState newBlock, int flag) {
        CraftBlockState state = CraftBlockState.getBlockState(world, pos, flag);
        state.setData(newBlock);

        MoistureChangeEvent event = new MoistureChangeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static boolean handleBlockSpreadEvent(World world, BlockPos source, BlockPos target, net.minecraft.block.BlockState block) {
        return handleBlockSpreadEvent(world, source, target, block, 2);
    }

    public static boolean handleBlockSpreadEvent(World world, BlockPos source, BlockPos target, net.minecraft.block.BlockState block, int flag) {
        CraftBlockState state = CraftBlockState.getBlockState(world, target, flag);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(world.getCraftWorld().getBlockAt(target.getX(), target.getY(), target.getZ()), world.getCraftWorld().getBlockAt(source.getX(), source.getY(), source.getZ()), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.entity.LivingEntity victim) {
        return callEntityDeathEvent(victim, new ArrayList<org.bukkit.inventory.ItemStack>(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.entity.LivingEntity victim, List<org.bukkit.inventory.ItemStack> drops) {
        CraftLivingEntity entity = (CraftLivingEntity) victim.getBukkitEntity();
        EntityDeathEvent event = new EntityDeathEvent(entity, drops, victim.getExpReward());
        CraftWorld world = (CraftWorld) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        victim.expToDrop = event.getDroppedExp();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) continue;

            world.dropItem(entity.getLocation(), stack);
        }

        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(ServerPlayerEntity victim, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory) {
        CraftPlayer entity = victim.getBukkitEntity();
        PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, victim.getExpReward(), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        org.bukkit.World world = entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            world.dropItem(entity.getLocation(), stack);
        }

        return event;
    }

    /**
     * Server methods
     */
    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent(address, motd, numPlayers, maxPlayers);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        return handleEntityDamageEvent(entity, source, modifiers, modifierFunctions, false);
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        if (source.isExplosive()) {
            DamageCause damageCause;
            Entity damager = entityDamage;
            entityDamage = null;
            EntityDamageEvent event;
            if (damager == null) {
                event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions);
            } else if (entity instanceof EnderDragonEntity && /*PAIL FIXME ((EntityEnderDragon) entity).target == damager*/ false) {
                event = new EntityDamageEvent(entity.getBukkitEntity(), DamageCause.ENTITY_EXPLOSION, modifiers, modifierFunctions);
            } else {
                if (damager instanceof org.bukkit.entity.TNTPrimed) {
                    damageCause = DamageCause.BLOCK_EXPLOSION;
                } else {
                    damageCause = DamageCause.ENTITY_EXPLOSION;
                }
                event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), entity.getBukkitEntity(), damageCause, modifiers, modifierFunctions);
            }
            event.setCancelled(cancelled);

            callEvent(event);

            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            }
            return event;
        } else if (source instanceof EntityDamageSource) {
            Entity damager = source.getAttacker();
            DamageCause cause = (source.isSweep()) ? DamageCause.ENTITY_SWEEP_ATTACK : DamageCause.ENTITY_ATTACK;

            if (source instanceof ProjectileDamageSource) {
                damager = ((ProjectileDamageSource) source).getProximateDamageSource();
                if (damager.getBukkitEntity() instanceof ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (damager.getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if ("thorns".equals(source.name)) {
                cause = DamageCause.THORNS;
            }

            return callEntityDamageEvent(damager, entity, cause, modifiers, modifierFunctions, cancelled);
        } else if (source == DamageSource.OUT_OF_WORLD) {
            EntityDamageEvent event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            }
            return event;
        } else if (source == DamageSource.LAVA) {
            EntityDamageEvent event = (new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions));
            event.setCancelled(cancelled);
            callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            }
            return event;
        } else if (blockDamage != null) {
            DamageCause cause = null;
            Block damager = blockDamage;
            blockDamage = null;
            if (source == DamageSource.CACTUS || source == DamageSource.SWEET_BERRY_BUSH) {
                cause = DamageCause.CONTACT;
            } else if (source == DamageSource.HOT_FLOOR) {
                cause = DamageCause.HOT_FLOOR;
            } else if (source == DamageSource.MAGIC) {
                cause = DamageCause.MAGIC;
            } else {
                throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s", entity, damager, source.name));
            }
            EntityDamageEvent event = new EntityDamageByBlockEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            }
            return event;
        } else if (entityDamage != null) {
            DamageCause cause = null;
            CraftEntity damager = entityDamage.getBukkitEntity();
            entityDamage = null;
            if (source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) {
                cause = DamageCause.FALLING_BLOCK;
            } else if (damager instanceof LightningStrike) {
                cause = DamageCause.LIGHTNING;
            } else if (source == DamageSource.FALL) {
                cause = DamageCause.FALL;
            } else if (source == DamageSource.DRAGON_BREATH) {
                cause = DamageCause.DRAGON_BREATH;
            } else if (source == DamageSource.MAGIC) {
                cause = DamageCause.MAGIC;
            } else {
                throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s", entity, damager.getHandle(), source.name));
            }
            EntityDamageEvent event = new EntityDamageByEntityEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            }
            return event;
        }

        DamageCause cause = null;
        if (source == DamageSource.IN_FIRE) {
            cause = DamageCause.FIRE;
        } else if (source == DamageSource.STARVE) {
            cause = DamageCause.STARVATION;
        } else if (source == DamageSource.WITHER) {
            cause = DamageCause.WITHER;
        } else if (source == DamageSource.IN_WALL) {
            cause = DamageCause.SUFFOCATION;
        } else if (source == DamageSource.DROWN) {
            cause = DamageCause.DROWNING;
        } else if (source == DamageSource.ON_FIRE) {
            cause = DamageCause.FIRE_TICK;
        } else if (source == MELTING) {
            cause = DamageCause.MELTING;
        } else if (source == POISON) {
            cause = DamageCause.POISON;
        } else if (source == DamageSource.MAGIC) {
            cause = DamageCause.MAGIC;
        } else if (source == DamageSource.FALL) {
            cause = DamageCause.FALL;
        } else if (source == DamageSource.FLY_INTO_WALL) {
            cause = DamageCause.FLY_INTO_WALL;
        } else if (source == DamageSource.CRAMMING) {
            cause = DamageCause.CRAMMING;
        } else if (source == DamageSource.DRYOUT) {
            cause = DamageCause.DRYOUT;
        } else if (source == DamageSource.GENERIC) {
            cause = DamageCause.CUSTOM;
        }

        if (cause != null) {
            return callEntityDamageEvent(null, entity, cause, modifiers, modifierFunctions, cancelled);
        }

        throw new IllegalStateException(String.format("Unhandled damage of %s from %s", entity, source.name));
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        return callEntityDamageEvent(damager, damagee, cause, modifiers, modifierFunctions, false);
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        EntityDamageEvent event;
        if (damager != null) {
            event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        } else {
            event = new EntityDamageEvent(damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        }
        event.setCancelled(cancelled);
        callEvent(event);

        if (!event.isCancelled()) {
            event.getEntity().setLastDamageCause(event);
        }

        return event;
    }

    private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);

    public static EntityDamageEvent handleLivingEntityDamageEvent(Entity damagee, DamageSource source, double rawDamage, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption) {
        Map<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, rawDamage);
        modifierFunctions.put(DamageModifier.BASE, ZERO);
        if (source == DamageSource.FALLING_BLOCK || source == DamageSource.ANVIL) {
            modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
            modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
        }
        if (damagee instanceof PlayerEntity) {
            modifiers.put(DamageModifier.BLOCKING, blockingModifier);
            modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        }
        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        return handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
    }

    // Non-Living Entities such as EntityEnderCrystal and EntityFireball need to call this
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, true);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, cancelOnZeroDamage, false);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage, boolean cancelled) {
        final EnumMap<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        final EnumMap<DamageModifier, Function<? super Double, Double>> functions = new EnumMap(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, damage);
        functions.put(DamageModifier.BASE, ZERO);

        final EntityDamageEvent event = handleEntityDamageEvent(entity, source, modifiers, functions, cancelled);

        if (event == null) {
            return false;
        }
        return event.isCancelled() || (cancelOnZeroDamage && event.getDamage() == 0);
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(PlayerEntity entity, int expAmount) {
        Player player = (Player) entity.getBukkitEntity();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerItemMendEvent callPlayerItemMendEvent(PlayerEntity entity, ExperienceOrbEntity orb, net.minecraft.item.ItemStack nmsMendedItem, int repairAmount) {
        Player player = (Player) entity.getBukkitEntity();
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, (ExperienceOrb) orb.getBukkitEntity(), repairAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockGrowEvent(World world, BlockPos pos, net.minecraft.block.BlockState block) {
        return handleBlockGrowEvent(world, pos, block, 3);
    }

    public static boolean handleBlockGrowEvent(World world, BlockPos pos, net.minecraft.block.BlockState newData, int flag) {
        Block block = world.getCraftWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setData(newData);

        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    public static FluidLevelChangeEvent callFluidLevelChangeEvent(World world, BlockPos block, net.minecraft.block.BlockState newData) {
        FluidLevelChangeEvent event = new FluidLevelChangeEvent(CraftBlock.at(world, block), CraftBlockData.fromData(newData));
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(PlayerEntity entity, int level) {
        return callFoodLevelChangeEvent(entity, level, null);
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(PlayerEntity entity, int level, ItemStack item) {
        FoodLevelChangeEvent event = new FoodLevelChangeEvent(entity.getBukkitEntity(), level, (item == null) ? null : CraftItemStack.asBukkitCopy(item));
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PigZapEvent callPigZapEvent(Entity pig, Entity lightning, Entity pigzombie) {
        PigZapEvent event = new PigZapEvent((Pig) pig.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), (PigZombie) pigzombie.getBukkitEntity());
        pig.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static HorseJumpEvent callHorseJumpEvent(Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((AbstractHorse) horse.getBukkitEntity(), power);
        horse.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.block.BlockState newBlock) {
        return callEntityChangeBlockEvent(entity, position, newBlock, false);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.block.BlockState newBlock, boolean cancelled) {
        Block block = entity.world.getCraftWorld().getBlockAt(position.getX(), position.getY(), position.getZ());

        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.getBukkitEntity(), block, CraftBlockData.fromData(newBlock));
        event.setCancelled(cancelled);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, CreeperPowerEvent.PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper) creeper.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), cause);
        creeper.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetEvent callEntityTargetEvent(Entity entity, Entity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetEvent event = new EntityTargetEvent(entity.getBukkitEntity(), (target == null) ? null : target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, net.minecraft.entity.LivingEntity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(entity.getBukkitEntity(), (target == null) ? null : (LivingEntity) target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, BlockPos pos) {
        org.bukkit.entity.Entity entity1 = entity.getBukkitEntity();
        Block block = CraftBlock.at(entity.world, pos);

        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block);
        entity1.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container, boolean cancelled) {
        if (player.currentScreenHandler != player.playerScreenHandler) { // fire INVENTORY_CLOSE if one already open
            player.networkHandler.a(new CloseHandledScreenC2SPacket(player.currentScreenHandler.syncId));
        }

        CraftServer server = player.world.getCraftServer();
        CraftPlayer craftPlayer = player.getBukkitEntity();
        player.currentScreenHandler.transferTo(container, craftPlayer);

        InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            container.transferTo(player.currentScreenHandler, craftPlayer);
            return null;
        }

        return container;
    }

    public static ItemStack callPreCraftEvent(Inventory matrix, Inventory resultInventory, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);

        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    public static ProjectileLaunchEvent callProjectileLaunchEvent(Entity entity) {
        Projectile bukkitEntity = (Projectile) entity.getBukkitEntity();
        ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void callProjectileHitEvent(Entity entity, HitResult position) {
        if (position.getType() == HitResult.Type.MISS) {
            return;
        }

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == HitResult.Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;
            hitBlock = CraftBlock.at(entity.world, positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getSide());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == HitResult.Type.ENTITY) {
            hitEntity = ((EntityHitResult) position).getEntity().getBukkitEntity();
        }

        ProjectileHitEvent event = new ProjectileHitEvent((Projectile) entity.getBukkitEntity(), hitEntity, hitBlock, hitFace);
        entity.world.getCraftServer().getPluginManager().callEvent(event);
    }

    public static ExpBottleEvent callExpBottleEvent(Entity entity, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle) entity.getBukkitEntity();
        ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(World world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(world.getCraftWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), oldCurrent, newCurrent);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static NotePlayEvent callNotePlayEvent(World world, BlockPos pos, Instrument instrument, int note) {
        NotePlayEvent event = new NotePlayEvent(world.getCraftWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), org.bukkit.Instrument.getByType((byte) instrument.ordinal()), new org.bukkit.Note(note));
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(PlayerEntity human, ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) human.getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, BlockPos block, BlockPos source) {
        org.bukkit.World bukkitWorld = world.getCraftWorld();
        Block igniter = bukkitWorld.getBlockAt(source.getX(), source.getY(), source.getZ());
        IgniteCause cause;
        switch (igniter.getType()) {
            case LAVA:
                cause = IgniteCause.LAVA;
                break;
            case DISPENSER:
                cause = IgniteCause.FLINT_AND_STEEL;
                break;
            case FIRE: // Fire or any other unknown block counts as SPREAD.
            default:
                cause = IgniteCause.SPREAD;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(block.getX(), block.getY(), block.getZ()), cause, igniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, BlockPos pos, Entity igniter) {
        org.bukkit.World bukkitWorld = world.getCraftWorld();
        org.bukkit.entity.Entity bukkitIgniter = igniter.getBukkitEntity();
        IgniteCause cause;
        switch (bukkitIgniter.getType()) {
            case ENDER_CRYSTAL:
                cause = IgniteCause.ENDER_CRYSTAL;
                break;
            case LIGHTNING:
                cause = IgniteCause.LIGHTNING;
                break;
            case SMALL_FIREBALL:
            case FIREBALL:
                cause = IgniteCause.FIREBALL;
                break;
            case ARROW:
                cause = IgniteCause.ARROW;
                break;
            default:
                cause = IgniteCause.FLINT_AND_STEEL;
        }

        if (igniter instanceof ProjectileEntity) {
            Entity shooter = ((ProjectileEntity) igniter).getOwner();
            if (shooter != null) {
                bukkitIgniter = shooter.getBukkitEntity();
            }
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, bukkitIgniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Explosion explosion) {
        org.bukkit.World bukkitWorld = world.getCraftWorld();
        org.bukkit.entity.Entity igniter = explosion.entity == null ? null : explosion.entity.getBukkitEntity();

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, BlockPos pos, IgniteCause cause, Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(world.getCraftWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, igniter.getBukkitEntity());
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void handleInventoryCloseEvent(PlayerEntity human) {
        InventoryCloseEvent event = new InventoryCloseEvent(human.currentScreenHandler.getBukkitView());
        human.world.getCraftServer().getPluginManager().callEvent(event);
        human.currentScreenHandler.transferTo(human.playerScreenHandler, human.getBukkitEntity());
    }

    public static ItemStack handleEditBookEvent(ServerPlayerEntity player, int itemInHandIndex, ItemStack itemInHand, ItemStack newBookItem) {
        PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent(player.getBukkitEntity(), itemInHandIndex, (BookMeta) CraftItemStack.getItemMeta(itemInHand), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == Items.WRITTEN_BOOK);
        player.world.getCraftServer().getPluginManager().callEvent(editBookEvent);

        // If they've got the same item in their hand, it'll need to be updated.
        if (itemInHand != null && itemInHand.getItem() == Items.WRITABLE_BOOK) {
            if (!editBookEvent.isCancelled()) {
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(Items.WRITTEN_BOOK);
                }
                CraftMetaBook meta = (CraftMetaBook) editBookEvent.getNewBookMeta();
                List<Text> pages = meta.pages;
                for (int i = 0; i < pages.size(); i++) {
                    pages.set(i, stripEvents(pages.get(i)));
                }
                CraftItemStack.setItemMeta(itemInHand, meta);
            }
        }

        return itemInHand;
    }

    private static Text stripEvents(Text c) {
        Style modi = c.getStyle();
        if (modi != null) {
            modi = modi.withClickEvent(null);
            modi = modi.withHoverEvent(null);
        }
        if (c instanceof TranslatableText) {
            TranslatableText cm = (TranslatableText) c;
            Object[] oo = cm.getArgs();
            for (int i = 0; i < oo.length; i++) {
                Object o = oo[i];
                if (o instanceof Text) {
                    oo[i] = stripEvents((Text) o);
                }
            }
        }
        List<Text> ls = c.getSiblings();
        if (ls != null) {
            for (int i = 0; i < ls.size(); i++) {
                ls.set(i, stripEvents(ls.get(i)));
            }
        }
        return c.shallowCopy().setStyle(modi);
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(MobEntity entity, PlayerEntity player) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (Player) player.getBukkitEntity());
        entity.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(MobEntity entity, Entity leashHolder, PlayerEntity player) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(entity.getBukkitEntity(), leashHolder.getBukkitEntity(), (Player) player.getBukkitEntity());
        entity.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockShearEntityEvent callBlockShearEntityEvent(Entity animal, org.bukkit.block.Block dispenser, CraftItemStack is) {
        BlockShearEntityEvent bse = new BlockShearEntityEvent(dispenser, animal.getBukkitEntity(), is);
        Bukkit.getPluginManager().callEvent(bse);
        return bse;
    }

    public static boolean handlePlayerShearEntityEvent(PlayerEntity player, Entity sheared, ItemStack shears, Hand hand) {
        if (!(player instanceof ServerPlayerEntity)) {
            return true;
        }

        PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player) player.getBukkitEntity(), sheared.getBukkitEntity(), CraftItemStack.asCraftMirror(shears), (hand == Hand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Cancellable handleStatisticsIncrease(PlayerEntity entityHuman, net.minecraft.stat.Stat<?> statistic, int current, int newValue) {
        Player player = ((ServerPlayerEntity) entityHuman).getBukkitEntity();
        Event event;
        if (true) {
            org.bukkit.Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
            if (stat == null) {
                System.err.println("Unhandled statistic: " + statistic);
                return null;
            }
            switch (stat) {
                case FALL_ONE_CM:
                case BOAT_ONE_CM:
                case CLIMB_ONE_CM:
                case WALK_ON_WATER_ONE_CM:
                case WALK_UNDER_WATER_ONE_CM:
                case FLY_ONE_CM:
                case HORSE_ONE_CM:
                case MINECART_ONE_CM:
                case PIG_ONE_CM:
                case PLAY_ONE_MINUTE:
                case SWIM_ONE_CM:
                case WALK_ONE_CM:
                case SPRINT_ONE_CM:
                case CROUCH_ONE_CM:
                case TIME_SINCE_DEATH:
                case SNEAK_TIME:
                    // Do not process event for these - too spammy
                    return null;
                default:
            }
            if (stat.getType() == Type.UNTYPED) {
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue);
            } else if (stat.getType() == Type.ENTITY) {
                EntityType entityType = CraftStatistic.getEntityTypeFromStatistic((net.minecraft.stat.Stat<net.minecraft.entity.EntityType<?>>) statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, entityType);
            } else {
                Material material = CraftStatistic.getMaterialFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, material);
            }
        }
        entityHuman.world.getCraftServer().getPluginManager().callEvent(event);
        return (Cancellable) event;
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(FireworkRocketEntity firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework) firework.getBukkitEntity());
        firework.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PrepareAnvilEvent callPrepareAnvilEvent(InventoryView view, ItemStack item) {
        PrepareAnvilEvent event = new PrepareAnvilEvent(view, CraftItemStack.asCraftMirror(item).clone());
        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setItem(2, event.getResult());
        return event;
    }

    public static PrepareSmithingEvent callPrepareSmithingEvent(InventoryView view, ItemStack item) {
        PrepareSmithingEvent event = new PrepareSmithingEvent(view, CraftItemStack.asCraftMirror(item).clone());
        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setItem(2, event.getResult());
        return event;
    }

    public static EntityToggleGlideEvent callToggleGlideEvent(net.minecraft.entity.LivingEntity entity, boolean gliding) {
        EntityToggleGlideEvent event = new EntityToggleGlideEvent((LivingEntity) entity.getBukkitEntity(), gliding);
        entity.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleSwimEvent callToggleSwimEvent(net.minecraft.entity.LivingEntity entity, boolean swimming) {
        EntityToggleSwimEvent event = new EntityToggleSwimEvent((LivingEntity) entity.getBukkitEntity(), swimming);
        entity.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static AreaEffectCloudApplyEvent callAreaEffectCloudApplyEvent(AreaEffectCloudEntity cloud, List<LivingEntity> entities) {
        AreaEffectCloudApplyEvent event = new AreaEffectCloudApplyEvent((AreaEffectCloud) cloud.getBukkitEntity(), entities);
        cloud.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static VehicleCreateEvent callVehicleCreateEvent(Entity entity) {
        Vehicle bukkitEntity = (Vehicle) entity.getBukkitEntity();
        VehicleCreateEvent event = new VehicleCreateEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreedEvent callEntityBreedEvent(net.minecraft.entity.LivingEntity child, net.minecraft.entity.LivingEntity mother, net.minecraft.entity.LivingEntity father, net.minecraft.entity.LivingEntity breeder, ItemStack bredWith, int experience) {
        org.bukkit.entity.LivingEntity breederEntity = (LivingEntity) (breeder == null ? null : breeder.getBukkitEntity());
        CraftItemStack bredWithStack = bredWith == null ? null : CraftItemStack.asCraftMirror(bredWith).clone();

        EntityBreedEvent event = new EntityBreedEvent((LivingEntity) child.getBukkitEntity(), (LivingEntity) mother.getBukkitEntity(), (LivingEntity) father.getBukkitEntity(), breederEntity, bredWithStack, experience);
        child.world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPhysicsEvent callBlockPhysicsEvent(WorldAccess world, BlockPos blockposition) {
        org.bukkit.block.Block block = CraftBlock.at(world, blockposition);
        BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getBlockData());
        // Suppress during worldgen
        if (world instanceof World) {
            ((World) world).getServer().server.getPluginManager().callEvent(event);
        }
        return event;
    }

    public static boolean handleBlockFormEvent(World world, BlockPos pos, net.minecraft.block.BlockState block) {
        return handleBlockFormEvent(world, pos, block, 3);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.entity.LivingEntity entity, @Nullable StatusEffectInstance oldEffect, @Nullable StatusEffectInstance newEffect, EntityPotionEffectEvent.Cause cause) {
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.entity.LivingEntity entity, @Nullable StatusEffectInstance oldEffect, @Nullable StatusEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action) {
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.entity.LivingEntity entity, @Nullable StatusEffectInstance oldEffect, @Nullable StatusEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, boolean willOverride) {
        EntityPotionEffectEvent.Action action = EntityPotionEffectEvent.Action.CHANGED;
        if (oldEffect == null) {
            action = EntityPotionEffectEvent.Action.ADDED;
        } else if (newEffect == null) {
            action = EntityPotionEffectEvent.Action.REMOVED;
        }

        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, willOverride);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.entity.LivingEntity entity, @Nullable StatusEffectInstance oldEffect, @Nullable StatusEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action, boolean willOverride) {
        PotionEffect bukkitOldEffect = (oldEffect == null) ? null : CraftPotionUtil.toBukkit(oldEffect);
        PotionEffect bukkitNewEffect = (newEffect == null) ? null : CraftPotionUtil.toBukkit(newEffect);

        if (bukkitOldEffect == null && bukkitNewEffect == null) {
            throw new IllegalStateException("Old and new potion effect are both null");
        }

        EntityPotionEffectEvent event = new EntityPotionEffectEvent((LivingEntity) entity.getBukkitEntity(), bukkitOldEffect, bukkitNewEffect, cause, action, willOverride);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static boolean handleBlockFormEvent(World world, BlockPos pos, net.minecraft.block.BlockState block, @Nullable Entity entity) {
        return handleBlockFormEvent(world, pos, block, 3, entity);
    }

    public static boolean handleBlockFormEvent(World world, BlockPos pos, net.minecraft.block.BlockState block, int flag) {
        return handleBlockFormEvent(world, pos, block, flag, null);
    }

    public static boolean handleBlockFormEvent(World world, BlockPos pos, net.minecraft.block.BlockState block, int flag, @Nullable Entity entity) {
        CraftBlockState blockState = CraftBlockState.getBlockState(world, pos, flag);
        blockState.setData(block);

        BlockFormEvent event = (entity == null) ? new BlockFormEvent(blockState.getBlock(), blockState) : new EntityBlockFormEvent(entity.getBukkitEntity(), blockState.getBlock(), blockState);
        world.getCraftServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            blockState.update(true);
        }

        return !event.isCancelled();
    }

    public static boolean handleBatToggleSleepEvent(Entity bat, boolean awake) {
        BatToggleSleepEvent event = new BatToggleSleepEvent((Bat) bat.getBukkitEntity(), awake);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handlePlayerRecipeListUpdateEvent(PlayerEntity who, Identifier recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((Player) who.getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static EntityPickupItemEvent callEntityPickupItemEvent(Entity who, ItemEntity item, int remaining, boolean cancelled) {
        EntityPickupItemEvent event = new EntityPickupItemEvent((LivingEntity) who.getBukkitEntity(), (Item) item.getBukkitEntity(), remaining);
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Raid events
     */
    public static boolean callRaidTriggerEvent(Raid raid, ServerPlayerEntity player) {
        RaidTriggerEvent event = new RaidTriggerEvent(new CraftRaid(raid), raid.getWorld().getCraftWorld(), player.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void callRaidFinishEvent(Raid raid, List<Player> players) {
        RaidFinishEvent event = new RaidFinishEvent(new CraftRaid(raid), raid.getWorld().getCraftWorld(), players);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidStopEvent(Raid raid, RaidStopEvent.Reason reason) {
        RaidStopEvent event = new RaidStopEvent(new CraftRaid(raid), raid.getWorld().getCraftWorld(), reason);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidSpawnWaveEvent(Raid raid, RaiderEntity leader, List<RaiderEntity> raiders) {
        Raider craftLeader = (CraftRaider) leader.getBukkitEntity();
        List<Raider> craftRaiders = new ArrayList<>();
        for (RaiderEntity entityRaider : raiders) {
            craftRaiders.add((Raider) entityRaider.getBukkitEntity());
        }
        RaidSpawnWaveEvent event = new RaidSpawnWaveEvent(new CraftRaid(raid), raid.getWorld().getCraftWorld(), craftLeader, craftRaiders);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static LootGenerateEvent callLootGenerateEvent(Inventory inventory, LootTable lootTable, LootContext lootInfo, List<ItemStack> loot, boolean plugin) {
        CraftWorld world = lootInfo.getWorld().getCraftWorld();
        Entity entity = lootInfo.get(LootContextParameters.THIS_ENTITY);
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(world.getHandle().getServer().getLootManager().lootTableToKey.get(lootTable));
        CraftLootTable craftLootTable = new CraftLootTable(key, lootTable);
        List<org.bukkit.inventory.ItemStack> bukkitLoot = loot.stream().map(CraftItemStack::asCraftMirror).collect(Collectors.toCollection(ArrayList::new));

        LootGenerateEvent event = new LootGenerateEvent(world, (entity != null ? entity.getBukkitEntity() : null), inventory.getOwner(), craftLootTable, CraftLootTable.convertContext(lootInfo), bukkitLoot, plugin);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void callStriderTemperatureChangeEvent(StriderEntity strider, boolean shivering) {
        StriderTemperatureChangeEvent event = new StriderTemperatureChangeEvent((Strider) strider.getBukkitEntity(), shivering);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static boolean handleEntitySpellCastEvent(SpellcastingIllagerEntity caster, SpellcastingIllagerEntity.Spell spell) {
        EntitySpellCastEvent event = new EntitySpellCastEvent((Spellcaster) caster.getBukkitEntity(), CraftSpellcaster.toBukkitSpell(spell));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * ArrowBodyCountChangeEvent
     */
    public static ArrowBodyCountChangeEvent callArrowBodyCountChangeEvent(net.minecraft.entity.LivingEntity entity, int oldAmount, int newAmount, boolean isReset) {
        org.bukkit.entity.LivingEntity bukkitEntity = (LivingEntity) entity.getBukkitEntity();

        ArrowBodyCountChangeEvent event = new ArrowBodyCountChangeEvent(bukkitEntity, oldAmount, newAmount, isReset);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }
}
