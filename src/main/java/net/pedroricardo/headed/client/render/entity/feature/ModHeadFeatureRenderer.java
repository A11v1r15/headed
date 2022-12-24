package net.pedroricardo.headed.client.render.entity.feature;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.pedroricardo.headed.block.AbstractModSkullBlock;
import net.pedroricardo.headed.block.ModSkullBlock;
import net.pedroricardo.headed.client.render.block.entity.ModSkullBlockEntityModel;
import net.pedroricardo.headed.client.render.block.entity.ModSkullBlockEntityRenderer;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModHeadFeatureRenderer<T extends LivingEntity, M extends EntityModel<T> & ModelWithHead> extends FeatureRenderer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<ModSkullBlock.SkullType, ModSkullBlockEntityModel> headModels;

    public ModHeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        this(context, loader, 1.0F, 1.0F, 1.0F);
    }

    public ModHeadFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, float scaleX, float scaleY, float scaleZ) {
        super(context);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.headModels = ModSkullBlockEntityRenderer.getModels(loader);
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.HEAD);
        if (!itemStack.isEmpty()) {
            Item item = itemStack.getItem();
            matrixStack.push();
            matrixStack.scale(this.scaleX, this.scaleY, this.scaleZ);
            boolean bl = livingEntity instanceof VillagerEntity || livingEntity instanceof ZombieVillagerEntity;
            float m;
            if (livingEntity.isBaby() && !(livingEntity instanceof VillagerEntity)) {
                m = 2.0F;
                float n = 1.4F;
                matrixStack.translate(0.0F, 0.03125F, 0.0F);
                matrixStack.scale(0.7F, 0.7F, 0.7F);
                matrixStack.translate(0.0F, 1.0F, 0.0F);
            }

            ((ModelWithHead)this.getContextModel()).getHead().rotate(matrixStack);
            if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractModSkullBlock) {
                m = 1.1875F;
                matrixStack.scale(1.1875F, -1.1875F, -1.1875F);
                if (bl) {
                    matrixStack.translate(0.0F, 0.0625F, 0.0F);
                }

                GameProfile gameProfile = null;
                if (itemStack.hasNbt()) {
                    NbtCompound nbtCompound = itemStack.getNbt();
                    if (nbtCompound.contains("SkullOwner", 10)) {
                        gameProfile = NbtHelper.toGameProfile(nbtCompound.getCompound("SkullOwner"));
                    }
                }

                matrixStack.translate(-0.5, 0.0, -0.5);
                ModSkullBlock.SkullType skullType = ((AbstractModSkullBlock)((BlockItem)item).getBlock()).getSkullType();
                ModSkullBlockEntityModel skullBlockEntityModel = (ModSkullBlockEntityModel)this.headModels.get(skullType);
                RenderLayer renderLayer = ModSkullBlockEntityRenderer.getRenderLayer(skullType, gameProfile);
                ModSkullBlockEntityRenderer.renderSkull(skullType, null, 180.0F, f, matrixStack, vertexConsumerProvider, i, skullBlockEntityModel, renderLayer, 1.0F, 1.0F, 1.0F);
                ModSkullBlockEntityRenderer.testForSpecialSkull(skullType, null, 180.0F, f, matrixStack, vertexConsumerProvider, i);
            }

            matrixStack.pop();
        }
    }

    public static void translate(MatrixStack matrices, boolean villager) {
        float f = 0.625F;
        matrices.translate(0.0F, -0.25F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.scale(0.625F, -0.625F, -0.625F);
        if (villager) {
            matrices.translate(0.0F, 0.1875F, 0.0F);
        }

    }
}