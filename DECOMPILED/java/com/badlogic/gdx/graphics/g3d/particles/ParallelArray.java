package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import java.util.Iterator;

public class ParallelArray {
    Array<Channel> arrays = new Array(false, 2, Channel.class);
    public int capacity;
    public int size;

    public abstract class Channel {
        public Object data;
        public int id;
        public int strideSize;

        public abstract void add(int i, Object... objArr);

        protected abstract void setCapacity(int i);

        public abstract void swap(int i, int i2);

        public Channel(int id, Object data, int strideSize) {
            this.id = id;
            this.strideSize = strideSize;
            this.data = data;
        }
    }

    public static class ChannelDescriptor {
        public int count;
        public int id;
        public Class<?> type;

        public ChannelDescriptor(int id, Class<?> type, int count) {
            this.id = id;
            this.type = type;
            this.count = count;
        }
    }

    public interface ChannelInitializer<T extends Channel> {
        void init(T t);
    }

    public class FloatChannel extends Channel {
        public float[] data = ((float[]) this.data);

        public FloatChannel(int id, int strideSize, int size) {
            super(id, new float[(size * strideSize)], strideSize);
        }

        public void add(int index, Object... objects) {
            int i = this.strideSize * ParallelArray.this.size;
            int c = i + this.strideSize;
            int k = 0;
            while (i < c) {
                this.data[i] = ((Float) objects[k]).floatValue();
                i++;
                k++;
            }
        }

        public void swap(int i, int k) {
            i *= this.strideSize;
            k *= this.strideSize;
            int c = i + this.strideSize;
            while (i < c) {
                float t = this.data[i];
                this.data[i] = this.data[k];
                this.data[k] = t;
                i++;
                k++;
            }
        }

        public void setCapacity(int requiredCapacity) {
            float[] newData = new float[(this.strideSize * requiredCapacity)];
            System.arraycopy(this.data, 0, newData, 0, Math.min(this.data.length, newData.length));
            this.data = newData;
            this.data = newData;
        }
    }

    public class IntChannel extends Channel {
        public int[] data = ((int[]) this.data);

        public IntChannel(int id, int strideSize, int size) {
            super(id, new int[(size * strideSize)], strideSize);
        }

        public void add(int index, Object... objects) {
            int i = this.strideSize * ParallelArray.this.size;
            int c = i + this.strideSize;
            int k = 0;
            while (i < c) {
                this.data[i] = ((Integer) objects[k]).intValue();
                i++;
                k++;
            }
        }

        public void swap(int i, int k) {
            i *= this.strideSize;
            k *= this.strideSize;
            int c = i + this.strideSize;
            while (i < c) {
                int t = this.data[i];
                this.data[i] = this.data[k];
                this.data[k] = t;
                i++;
                k++;
            }
        }

        public void setCapacity(int requiredCapacity) {
            int[] newData = new int[(this.strideSize * requiredCapacity)];
            System.arraycopy(this.data, 0, newData, 0, Math.min(this.data.length, newData.length));
            this.data = newData;
            this.data = newData;
        }
    }

    public class ObjectChannel<T> extends Channel {
        Class<T> componentType;
        public T[] data = ((Object[]) this.data);

        public ObjectChannel(int id, int strideSize, int size, Class<T> type) {
            super(id, ArrayReflection.newInstance(type, size * strideSize), strideSize);
            this.componentType = type;
        }

        public void add(int index, Object... objects) {
            int i = this.strideSize * ParallelArray.this.size;
            int c = i + this.strideSize;
            int k = 0;
            while (i < c) {
                this.data[i] = objects[k];
                i++;
                k++;
            }
        }

        public void swap(int i, int k) {
            i *= this.strideSize;
            k *= this.strideSize;
            int c = i + this.strideSize;
            while (i < c) {
                T t = this.data[i];
                this.data[i] = this.data[k];
                this.data[k] = t;
                i++;
                k++;
            }
        }

        public void setCapacity(int requiredCapacity) {
            Object[] newData = (Object[]) ((Object[]) ArrayReflection.newInstance(this.componentType, this.strideSize * requiredCapacity));
            System.arraycopy(this.data, 0, newData, 0, Math.min(this.data.length, newData.length));
            this.data = newData;
            this.data = newData;
        }
    }

    public ParallelArray(int capacity) {
        this.capacity = capacity;
        this.size = 0;
    }

    public <T extends Channel> T addChannel(ChannelDescriptor channelDescriptor) {
        return addChannel(channelDescriptor, null);
    }

    public <T extends Channel> T addChannel(ChannelDescriptor channelDescriptor, ChannelInitializer<T> initializer) {
        T channel = getChannel(channelDescriptor);
        if (channel == null) {
            channel = allocateChannel(channelDescriptor);
            if (initializer != null) {
                initializer.init(channel);
            }
            this.arrays.add(channel);
        }
        return channel;
    }

    private <T extends Channel> T allocateChannel(ChannelDescriptor channelDescriptor) {
        if (channelDescriptor.type == Float.TYPE) {
            return new FloatChannel(channelDescriptor.id, channelDescriptor.count, this.capacity);
        }
        if (channelDescriptor.type == Integer.TYPE) {
            return new IntChannel(channelDescriptor.id, channelDescriptor.count, this.capacity);
        }
        return new ObjectChannel(channelDescriptor.id, channelDescriptor.count, this.capacity, channelDescriptor.type);
    }

    public <T> void removeArray(int id) {
        this.arrays.removeIndex(findIndex(id));
    }

    private int findIndex(int id) {
        for (int i = 0; i < this.arrays.size; i++) {
            if (((Channel[]) this.arrays.items)[i].id == id) {
                return i;
            }
        }
        return -1;
    }

    public void addElement(Object... values) {
        if (this.size == this.capacity) {
            throw new GdxRuntimeException("Capacity reached, cannot add other elements");
        }
        int k = 0;
        Iterator it = this.arrays.iterator();
        while (it.hasNext()) {
            Channel strideArray = (Channel) it.next();
            strideArray.add(k, values);
            k += strideArray.strideSize;
        }
        this.size++;
    }

    public void removeElement(int index) {
        int last = this.size - 1;
        Iterator it = this.arrays.iterator();
        while (it.hasNext()) {
            ((Channel) it.next()).swap(index, last);
        }
        this.size = last;
    }

    public <T extends Channel> T getChannel(ChannelDescriptor descriptor) {
        Iterator it = this.arrays.iterator();
        while (it.hasNext()) {
            Channel array = (Channel) it.next();
            if (array.id == descriptor.id) {
                return array;
            }
        }
        return null;
    }

    public void clear() {
        this.arrays.clear();
        this.size = 0;
    }

    public void setCapacity(int requiredCapacity) {
        if (this.capacity != requiredCapacity) {
            Iterator it = this.arrays.iterator();
            while (it.hasNext()) {
                ((Channel) it.next()).setCapacity(requiredCapacity);
            }
            this.capacity = requiredCapacity;
        }
    }
}
