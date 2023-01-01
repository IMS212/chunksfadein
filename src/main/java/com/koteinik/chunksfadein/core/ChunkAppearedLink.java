package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.extenstions.RenderRegionExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionManagerExt;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public class ChunkAppearedLink {
    public static RenderRegionManagerExt regionManager = null;

    public static float[] getChunkData(int x, int y, int z) {
        if (regionManager == null)
            return null;

        RenderRegion region = regionManager.getRenderRegion(x, y, z);

        if (region == null)
            return ChunkData.FULLY_FADED;

        RenderRegionExt ext = (RenderRegionExt) region;

        return ext.getChunkData(x, y, z);
    }

    public static void completeChunkFade(int x, int y, int z, boolean completeFade) {
        if (regionManager == null)
            return;

        RenderRegion region = regionManager.getRenderRegion(x, y, z);

        if (region == null)
            return;

        RenderRegionExt ext = (RenderRegionExt) region;

        ext.completeChunkFade(x, y, z, completeFade);
    }
}