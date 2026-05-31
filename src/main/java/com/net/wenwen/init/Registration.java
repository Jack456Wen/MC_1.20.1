package com.net.wenwen.init;

import com.net.wenwen.Const;
import com.net.wenwen.Structure.ModFeatures;
import com.net.wenwen.Structure.RubiksCubePiece;
import com.net.wenwen.Structure.RubiksCubeStructure;
import com.net.wenwen.block.BlockCeramicJar;
import com.net.wenwen.block.LockChestBlock;
import com.net.wenwen.block.MysteriousStoneBlock;
import com.net.wenwen.block.TileEntityChest;
import com.net.wenwen.client.LockChestRender;
import com.net.wenwen.common.CageBoxBlock;
import com.net.wenwen.common.CageBoxBlockEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;


public class Registration {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Const.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Const.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Const.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Const.MOD_ID);
    public static RegistryObject<LockChestBlock> CHEST;
    public static RegistryObject<BlockEntityType<TileEntityChest>> CHEST_TILE_TYPE;
    public static RegistryObject<Item> Chest_ITEM;

    public static void register() {

        CHEST = BLOCKS.register("chest_stone", LockChestBlock::new);

        Chest_ITEM=ITEMS.register("chest_stone", () -> new BlockItem(CHEST.get(), new Item.Properties()){
            @Override
            public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                super.initializeClient(consumer);

                consumer.accept(new IClientItemExtensions() {
                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return LockChestRender.INSTANCE;
                    }
                });
            }
        });

        CHEST_TILE_TYPE = BLOCK_ENTITIES.register("chest_tile", () -> BlockEntityType.Builder.of(TileEntityChest::new, CHEST.get()).build(null));
    }


    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Const.MOD_ID);

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Const.MOD_ID);

    public static final RegistryObject<StructurePieceType> RUBIKS_CUBE_PIECE =
            STRUCTURE_PIECE_TYPES.register("rubiks_cube_piece", () -> new StructurePieceType() {
                @Override
                public StructurePiece load(StructurePieceSerializationContext context, CompoundTag tag) {
                    return new RubiksCubePiece(this, tag);
                }
            });

    public static final RegistryObject<StructureType<RubiksCubeStructure>> RUBIKS_CUBE_STRUCTURE =
            STRUCTURE_TYPES.register("rubiks_cube_structure", () -> () -> RubiksCubeStructure.CODEC);

    public static RegistryObject<Block> TRAP_CAGE  = BLOCKS.register("trap_cage", () -> new CageBoxBlock(Block.Properties.of()
            .destroyTime(5.0F)
            .sound(SoundType.WOOD)
            .mapColor(MapColor.WOOD)
    ));
    public static RegistryObject<Item> TRAP_CAGE_ITEM  = ITEMS.register("trap_cage", () -> new BlockItem(TRAP_CAGE.get(), new Item.Properties()));

    public static final RegistryObject<Block> CERAMIC_JAR = BLOCKS.register("ceramic_jar",
            () -> new BlockCeramicJar(Block.Properties.of()
                    .sound(new SoundType(
                            0.8F,
                            1.0F,
                            WenwenModSounds.JAR_BREAK.get(), // 破坏音效
                            WenwenModSounds.JAR_BREAK.get(), // 踩踏音效
                            WenwenModSounds.JAR_BREAK.get(), // 放置音效
                            WenwenModSounds.JAR_BREAK.get(), // 击打音效
                            WenwenModSounds.JAR_BREAK.get()  // 掉落音效
                    ))
            ));


    public static final RegistryObject<Item> CERAMIC_JAR_ITEM = ITEMS.register("ceramic_jar",
            () -> new BlockItem(CERAMIC_JAR.get(), new Item.Properties())
    );


    public static RegistryObject<Block> MYSTERIOUS_STONE  = BLOCKS.register("mysterious_stone", () -> new MysteriousStoneBlock(Block.Properties.of()
            .destroyTime(-1.0F)
            .sound(SoundType.STONE)
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
    ));
    public static RegistryObject<Item> MYSTERIOUS_STONE_ITEM  = ITEMS.register("mysterious_stone", () -> new BlockItem(MYSTERIOUS_STONE.get(), new Item.Properties()));

    public static RegistryObject<BlockEntityType<?>> TRAP_CAGE_TILE = BLOCK_ENTITIES.register("trap_cage", () ->
            BlockEntityType.Builder.of(CageBoxBlockEntity::new, TRAP_CAGE.get()).build(null));

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TABS.register("wenwen", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.wenwen"))
            .icon(() -> WenwenModItems.GOLD_EN_POTATO.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                    output.accept(TRAP_CAGE_ITEM.get());
                    output.accept(MYSTERIOUS_STONE_ITEM.get());
                    output.accept(WenwenModItems.GOLD_ENARADISH.get());
                    output.accept(WenwenModItems.GOLD_POTATO.get());
                    output.accept(WenwenModItems.GOLD_EN_POTATO.get());
                    output.accept(WenwenModItems.ENMELON.get());
                    output.accept(WenwenModItems.HEARTCONTAINER.get());
                    output.accept(WenwenModItems.HEART_PIECE.get());
                    output.accept(WenwenModItems.Break.get());
                    output.accept(WenwenModItems.MIRROR.get());
                    output.accept(WenwenModItems.DIAMOND_POTATO.get());
                    output.accept(WenwenModItems.DIAMOND_CARROT.get());
                    output.accept(WenwenModItems.HEART_AMULET.get());
                    output.accept(WenwenModItems.DARKNESS.get());
                    output.accept(WenwenModItems.Tears.get());
                    output.accept(WenwenModItems.Key.get());
                    output.accept(Chest_ITEM.get());
                    output.accept(WenwenModItems.OCTOROK_SPAWN_EGG.get());
                    output.accept(WenwenModItems.BAT_SPAWN_EGG.get());
                    output.accept(CERAMIC_JAR_ITEM.get());
            })
            .build());

    public static class EntityTags {
        public static final TagKey<EntityType<?>> CAGE_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, Const.rl("cage_trap_blacklist"));
    }
}
