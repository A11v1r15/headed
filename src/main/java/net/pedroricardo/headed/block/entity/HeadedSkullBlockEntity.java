package net.pedroricardo.headed.block.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class HeadedSkullBlockEntity extends BlockEntity {
    public static final String SKULL_OWNER_KEY = "SkullOwner";
    public static final String NOTE_BLOCK_SOUND_KEY = "note_block_sound";
    @Nullable
    private static UserCache userCache;
    @Nullable
    private static MinecraftSessionService sessionService;
    @Nullable
    private static Executor executor;
    @Nullable
    private GameProfile owner;
    @Nullable
    private Identifier noteBlockSound;
    private int poweredTicks;
    private boolean powered;

    public HeadedSkullBlockEntity(BlockPos pos, BlockState state) {
        super(HeadedBlockEntities.SKULL, pos, state);
    }

    public static void setServices(ApiServices apiServices, Executor executor) {
        userCache = apiServices.userCache();
        sessionService = apiServices.sessionService();
        HeadedSkullBlockEntity.executor = executor;
    }

    public static void clearServices() {
        userCache = null;
        sessionService = null;
        executor = null;
    }

    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.owner != null) {
            NbtCompound nbtCompound = new NbtCompound();
            NbtHelper.writeGameProfile(nbtCompound, this.owner);
            nbt.put("SkullOwner", nbtCompound);
        }

        if (this.noteBlockSound != null) {
            nbt.putString("note_block_sound", this.noteBlockSound.toString());
        }

    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("SkullOwner", 10)) {
            this.setOwner(NbtHelper.toGameProfile(nbt.getCompound("SkullOwner")));
        } else if (nbt.contains("ExtraType", 8)) {
            String string = nbt.getString("ExtraType");
            if (!StringHelper.isEmpty(string)) {
                this.setOwner(new GameProfile((UUID)null, string));
            }
        }

        if (nbt.contains("note_block_sound", 8)) {
            this.noteBlockSound = Identifier.tryParse(nbt.getString("note_block_sound"));
        }

    }

    public static void tick(World world, BlockPos pos, BlockState state, HeadedSkullBlockEntity blockEntity) {
        if (world.isReceivingRedstonePower(pos)) {
            blockEntity.powered = true;
            ++blockEntity.poweredTicks;
        } else {
            blockEntity.powered = false;
        }

    }

    public float getPoweredTicks(float tickDelta) {
        return this.powered ? (float)this.poweredTicks + tickDelta : (float)this.poweredTicks;
    }

    @Nullable
    public GameProfile getOwner() {
        return this.owner;
    }

    @Nullable
    public Identifier getNoteBlockSound() {
        return this.noteBlockSound;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    public void setOwner(@Nullable GameProfile owner) {
        synchronized(this) {
            this.owner = owner;
        }

        this.loadOwnerProperties();
    }

    private void loadOwnerProperties() {
        loadProperties(this.owner, (owner) -> {
            this.owner = owner;
            this.markDirty();
        });
    }

    public static void loadProperties(@Nullable GameProfile owner, Consumer<GameProfile> callback) {
        callback.accept(owner);
    }
}