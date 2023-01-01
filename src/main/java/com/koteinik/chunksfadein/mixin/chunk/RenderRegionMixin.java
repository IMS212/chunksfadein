package com.koteinik.chunksfadein.mixin.chunk;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkFadeInController;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionExt;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import org.spongepowered.asm.mixin.injection.At;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
    @Shadow
    private final Set<RenderSection> chunks = new ObjectOpenHashSet<>();

    private ChunkFadeInController fadeController;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void modifyConstructor(RenderRegionManager manager, int x, int y, int z, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeController = new ChunkFadeInController();
    }

    @Inject(method = "addChunk", at = @At(value = "TAIL"))
    @SuppressWarnings("resource")
    private void modifyAddChunk(RenderSection chunk, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        boolean completeAnimation = false;
        if (!Config.animateNearPlayer) {
            final int chunkX = chunk.getChunkX();
            final int chunkY = chunk.getChunkY();
            final int chunkZ = chunk.getChunkZ();

            Entity camera = MinecraftClient.getInstance().cameraEntity;
            if (camera != null) {
                final int camChunkX = MathUtils.floor((float) (camera.lastRenderX / 16));
                final int camChunkY = MathUtils.floor((float) (camera.lastRenderY / 16));
                final int camChunkZ = MathUtils.floor((float) (camera.lastRenderZ / 16));

                if (MathUtils.chunkInRange(chunkX, chunkY, chunkZ, camChunkX, camChunkY, camChunkZ, 1))
                    completeAnimation = true;
            }
        }

        if (!completeAnimation)
            fadeController.resetFadeForChunk(chunk.getChunkId());
        else
            fadeController.completeChunkFade(chunk.getChunkId(), false);
    }

    @Inject(method = "deleteResources", at = @At(value = "TAIL"))
    private void modifyDeleteResources(CommandList commandList, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeController.delete(commandList);
    }

    @Override
    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader, CommandList commandList) {
        fadeController.updateChunksFade(chunks, shader, commandList);
    }

    @Override
    public float[] getChunkData(int x, int y, int z) {
        return fadeController.getChunkData(x, y, z);
    }

    @Override
    public void completeChunkFade(int x, int y, int z, boolean completeFade) {
        fadeController.completeChunkFade(x, y, z, completeFade);
    }
}