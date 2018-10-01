package com.badlogic.gdx.utils;

public class BinaryHeap<T extends Node> {
    private final boolean isMaxHeap;
    private Node[] nodes;
    public int size;

    public static class Node {
        int index;
        float value;

        public Node(float value) {
            this.value = value;
        }

        public float getValue() {
            return this.value;
        }

        public String toString() {
            return Float.toString(this.value);
        }
    }

    public BinaryHeap() {
        this(16, false);
    }

    public BinaryHeap(int capacity, boolean isMaxHeap) {
        this.isMaxHeap = isMaxHeap;
        this.nodes = new Node[capacity];
    }

    public T add(T node) {
        if (this.size == this.nodes.length) {
            Node[] newNodes = new Node[(this.size << 1)];
            System.arraycopy(this.nodes, 0, newNodes, 0, this.size);
            this.nodes = newNodes;
        }
        node.index = this.size;
        this.nodes[this.size] = node;
        int i = this.size;
        this.size = i + 1;
        up(i);
        return node;
    }

    public T add(T node, float value) {
        node.value = value;
        return add(node);
    }

    public T peek() {
        if (this.size != 0) {
            return this.nodes[0];
        }
        throw new IllegalStateException("The heap is empty.");
    }

    public T pop() {
        return remove(0);
    }

    public T remove(T node) {
        return remove(node.index);
    }

    private T remove(int index) {
        Node[] nodes = this.nodes;
        Node removed = nodes[index];
        int i = this.size - 1;
        this.size = i;
        nodes[index] = nodes[i];
        nodes[this.size] = null;
        if (this.size > 0 && index < this.size) {
            down(index);
        }
        return removed;
    }

    public void clear() {
        Node[] nodes = this.nodes;
        int n = this.size;
        for (int i = 0; i < n; i++) {
            nodes[i] = null;
        }
        this.size = 0;
    }

    public void setValue(T node, float value) {
        float oldValue = node.value;
        node.value = value;
        if (((value < oldValue ? 1 : 0) ^ this.isMaxHeap) != 0) {
            up(node.index);
        } else {
            down(node.index);
        }
    }

    private void up(int index) {
        Node[] nodes = this.nodes;
        Node node = nodes[index];
        float value = node.value;
        while (index > 0) {
            int parentIndex = (index - 1) >> 1;
            Node parent = nodes[parentIndex];
            if (((value < parent.value ? 1 : 0) ^ this.isMaxHeap) == 0) {
                break;
            }
            nodes[index] = parent;
            parent.index = index;
            index = parentIndex;
        }
        nodes[index] = node;
        node.index = index;
    }

    private void down(int index) {
        Node[] nodes = this.nodes;
        int size = this.size;
        Node node = nodes[index];
        float value = node.value;
        while (true) {
            int leftIndex = (index << 1) + 1;
            if (leftIndex < size) {
                Node rightNode;
                float rightValue;
                int i;
                int rightIndex = leftIndex + 1;
                Node leftNode = nodes[leftIndex];
                float leftValue = leftNode.value;
                if (rightIndex >= size) {
                    rightNode = null;
                    rightValue = this.isMaxHeap ? Float.MIN_VALUE : Float.MAX_VALUE;
                } else {
                    rightNode = nodes[rightIndex];
                    rightValue = rightNode.value;
                }
                if (leftValue < rightValue) {
                    i = 1;
                } else {
                    i = 0;
                }
                if ((i ^ this.isMaxHeap) == 0) {
                    if (rightValue != value) {
                        if (rightValue > value) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        if ((i ^ this.isMaxHeap) != 0) {
                            break;
                        }
                        nodes[index] = rightNode;
                        rightNode.index = index;
                        index = rightIndex;
                    } else {
                        break;
                    }
                } else if (leftValue != value) {
                    if (leftValue > value) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    if ((i ^ this.isMaxHeap) != 0) {
                        break;
                    }
                    nodes[index] = leftNode;
                    leftNode.index = index;
                    index = leftIndex;
                } else {
                    break;
                }
            }
            break;
        }
        nodes[index] = node;
        node.index = index;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryHeap)) {
            return false;
        }
        BinaryHeap other = (BinaryHeap) obj;
        if (other.size != this.size) {
            return false;
        }
        int n = this.size;
        for (int i = 0; i < n; i++) {
            if (other.nodes[i].value != this.nodes[i].value) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int h = 1;
        for (int i = 0; i < this.size; i++) {
            h = (h * 31) + Float.floatToIntBits(this.nodes[i].value);
        }
        return h;
    }

    public String toString() {
        if (this.size == 0) {
            return "[]";
        }
        Node[] nodes = this.nodes;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(nodes[0].value);
        for (int i = 1; i < this.size; i++) {
            buffer.append(", ");
            buffer.append(nodes[i].value);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
