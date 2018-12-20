package com.github.intellectualsites.plotsquared.plot.util.block;

import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class BasicLocalBlockQueue<T> extends LocalBlockQueue {

    private final String world;
    private final ConcurrentHashMap<Long, LocalChunk> blocks = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<LocalChunk> chunks = new ConcurrentLinkedDeque<>();
    private long modified;
    private LocalChunk lastWrappedChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;

    public BasicLocalBlockQueue(String world) {
        super(world);
        this.world = world;
        this.modified = System.currentTimeMillis();
    }

    public abstract LocalChunk getLocalChunk(int x, int z);

    @Override public abstract PlotBlock getBlock(int x, int y, int z);

    public abstract void setComponents(LocalChunk<T> lc);

    @Override public final String getWorld() {
        return world;
    }

    @Override public final boolean next() {
        lastX = Integer.MIN_VALUE;
        lastZ = Integer.MIN_VALUE;
        try {
            if (this.blocks.size() == 0) {
                return false;
            }
            synchronized (blocks) {
                LocalChunk chunk = chunks.poll();
                if (chunk != null) {
                    blocks.remove(chunk.longHash());
                    this.execute(chunk);
                    return true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public final boolean execute(final LocalChunk<T> lc) {
        if (lc == null) {
            return false;
        }
        this.setComponents(lc);
        return true;
    }

    @Override public void startSet(boolean parallel) {
        // Do nothing
    }

    @Override public void endSet(boolean parallel) {
        // Do nothing
    }

    @Override public final int size() {
        return chunks.size();
    }

    @Override public final long getModified() {
        return modified;
    }

    @Override public final void setModified(long modified) {
        this.modified = modified;
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        int cx = x >> 4;
        int cz = z >> 4;
        if (cx != lastX || cz != lastZ) {
            lastX = cx;
            lastZ = cz;
            long pair = (long) (cx) << 32 | (cz) & 0xFFFFFFFFL;
            lastWrappedChunk = this.blocks.get(pair);
            if (lastWrappedChunk == null) {
                lastWrappedChunk = this.getLocalChunk(x >> 4, z >> 4);
                lastWrappedChunk.setBlock(x & 15, y, z & 15, id);
                LocalChunk previous = this.blocks.put(pair, lastWrappedChunk);
                if (previous == null) {
                    chunks.add(lastWrappedChunk);
                    return true;
                }
                this.blocks.put(pair, previous);
                lastWrappedChunk = previous;
            }
        }
        lastWrappedChunk.setBlock(x & 15, y, z & 15, id);
        return true;
    }

    @Override public boolean setBlock(int x, int y, int z, PlotBlock id) {
        if ((y > 255) || (y < 0)) {
            return false;
        }
        int cx = x >> 4;
        int cz = z >> 4;
        if (cx != lastX || cz != lastZ) {
            lastX = cx;
            lastZ = cz;
            long pair = (long) (cx) << 32 | (cz) & 0xFFFFFFFFL;
            lastWrappedChunk = this.blocks.get(pair);
            if (lastWrappedChunk == null) {
                lastWrappedChunk = this.getLocalChunk(x >> 4, z >> 4);
                lastWrappedChunk.setBlock(x & 15, y, z & 15, id);
                LocalChunk previous = this.blocks.put(pair, lastWrappedChunk);
                if (previous == null) {
                    chunks.add(lastWrappedChunk);
                    return true;
                }
                this.blocks.put(pair, previous);
                lastWrappedChunk = previous;
            }
        }
        lastWrappedChunk.setBlock(x & 15, y, z & 15, id);
        return true;
    }

    @Override public final boolean setBiome(int x, int z, String biome) {
        long pair = (long) (x >> 4) << 32 | (z >> 4) & 0xFFFFFFFFL;
        LocalChunk result = this.blocks.get(pair);
        if (result == null) {
            result = this.getLocalChunk(x >> 4, z >> 4);
            LocalChunk previous = this.blocks.put(pair, result);
            if (previous != null) {
                this.blocks.put(pair, previous);
                result = previous;
            } else {
                chunks.add(result);
            }
        }
        result.setBiome(x & 15, z & 15, biome);
        return true;
    }

    public final void setChunk(LocalChunk<T> chunk) {
        LocalChunk previous = this.blocks.put(chunk.longHash(), chunk);
        if (previous != null) {
            chunks.remove(previous);
        }
        chunks.add(chunk);
    }

    @Override public void flush() {
        GlobalBlockQueue.IMP.dequeue(this);
        TaskManager.IMP.sync(new RunnableVal<Object>() {
            @Override public void run(Object value) {
                while (next())
                    ;
            }
        });
    }


    public abstract class LocalChunk<T> {
        public final BasicLocalBlockQueue parent;
        public final int z;
        public final int x;

        public T[] blocks;
        public String[][] biomes;

        public LocalChunk(BasicLocalBlockQueue<T> parent, int x, int z) {
            this.parent = parent;
            this.x = x;
            this.z = z;
        }

        /**
         * Get the parent queue this chunk belongs to
         *
         * @return
         */
        public BasicLocalBlockQueue getParent() {
            return parent;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public abstract void setBlock(final int x, final int y, final int z, final PlotBlock block);

        public abstract void setBlock(final int x, final int y, final int z, final BaseBlock id);

        public void setBiome(int x, int z, String biome) {
            if (this.biomes == null) {
                this.biomes = new String[16][];
            }
            String[] index = this.biomes[x];
            if (index == null) {
                index = this.biomes[x] = new String[16];
            }
            index[z] = biome;
        }

        public long longHash() {
            return MathMan.pairInt(x, z);
        }

        @Override public int hashCode() {
            return MathMan.pair((short) x, (short) z);
        }
    }


    public class BasicLocalChunk extends LocalChunk<PlotBlock[]> {
        public BasicLocalChunk(BasicLocalBlockQueue parent, int x, int z) {
            super(parent, x, z);
            blocks = new PlotBlock[16][];
        }

        @Override public void setBlock(int x, int y, int z, PlotBlock block) {
            this.setInternal(x, y, z, block);
        }

        @Override
        public void setBlock(final int x, final int y, final int z, @NonNull final BaseBlock id) {
            this.setInternal(x, y, z, id);
        }

        private void setInternal(final int x, final int y, final int z, final BaseBlock bsh) {
            final int i = MainUtil.CACHE_I[y][x][z];
            final int j = MainUtil.CACHE_J[y][x][z];
            PlotBlock[] array = blocks[i];
            if (array == null) {
                array = (blocks[i] = new PlotBlock[4096]);
            }
            array[j] = PlotBlock.get(bsh);
        }

        private void setInternal(final int x, final int y, final int z, final PlotBlock plotBlock) {
            final int i = MainUtil.CACHE_I[y][x][z];
            final int j = MainUtil.CACHE_J[y][x][z];
            PlotBlock[] array = blocks[i];
            if (array == null) {
                array = (blocks[i] = new PlotBlock[4096]);
            }
            array[j] = plotBlock;
        }

        public void setBlock(final int x, final int y, final int z, final int id, final int data) {
            final PlotBlock block = PlotBlock.get(id, data);
            this.setInternal(x, y, z, block);
        }
    }
}
