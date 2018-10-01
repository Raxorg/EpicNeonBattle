package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class NinePatch {
    public static final int BOTTOM_CENTER = 7;
    public static final int BOTTOM_LEFT = 6;
    public static final int BOTTOM_RIGHT = 8;
    public static final int MIDDLE_CENTER = 4;
    public static final int MIDDLE_LEFT = 3;
    public static final int MIDDLE_RIGHT = 5;
    public static final int TOP_CENTER = 1;
    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 2;
    private static final Color tmpDrawColor = new Color();
    private int bottomCenter;
    private float bottomHeight;
    private int bottomLeft;
    private int bottomRight;
    private final Color color;
    private int idx;
    private float leftWidth;
    private int middleCenter;
    private float middleHeight;
    private int middleLeft;
    private int middleRight;
    private float middleWidth;
    private float padBottom;
    private float padLeft;
    private float padRight;
    private float padTop;
    private float rightWidth;
    private Texture texture;
    private int topCenter;
    private float topHeight;
    private int topLeft;
    private int topRight;
    private float[] vertices;

    public NinePatch(Texture texture, int left, int right, int top, int bottom) {
        this(new TextureRegion(texture), left, right, top, bottom);
    }

    public NinePatch(TextureRegion region, int left, int right, int top, int bottom) {
        this.bottomLeft = -1;
        this.bottomCenter = -1;
        this.bottomRight = -1;
        this.middleLeft = -1;
        this.middleCenter = -1;
        this.middleRight = -1;
        this.topLeft = -1;
        this.topCenter = -1;
        this.topRight = -1;
        this.vertices = new float[180];
        this.color = new Color(Color.WHITE);
        this.padLeft = -1.0f;
        this.padRight = -1.0f;
        this.padTop = -1.0f;
        this.padBottom = -1.0f;
        if (region == null) {
            throw new IllegalArgumentException("region cannot be null.");
        }
        int middleWidth = (region.getRegionWidth() - left) - right;
        int middleHeight = (region.getRegionHeight() - top) - bottom;
        TextureRegion[] patches = new TextureRegion[9];
        if (top > 0) {
            if (left > 0) {
                patches[TOP_LEFT] = new TextureRegion(region, (int) TOP_LEFT, (int) TOP_LEFT, left, top);
            }
            if (middleWidth > 0) {
                patches[TOP_CENTER] = new TextureRegion(region, left, (int) TOP_LEFT, middleWidth, top);
            }
            if (right > 0) {
                patches[TOP_RIGHT] = new TextureRegion(region, left + middleWidth, (int) TOP_LEFT, right, top);
            }
        }
        if (middleHeight > 0) {
            if (left > 0) {
                patches[MIDDLE_LEFT] = new TextureRegion(region, (int) TOP_LEFT, top, left, middleHeight);
            }
            if (middleWidth > 0) {
                patches[MIDDLE_CENTER] = new TextureRegion(region, left, top, middleWidth, middleHeight);
            }
            if (right > 0) {
                patches[MIDDLE_RIGHT] = new TextureRegion(region, left + middleWidth, top, right, middleHeight);
            }
        }
        if (bottom > 0) {
            if (left > 0) {
                patches[BOTTOM_LEFT] = new TextureRegion(region, (int) TOP_LEFT, top + middleHeight, left, bottom);
            }
            if (middleWidth > 0) {
                patches[BOTTOM_CENTER] = new TextureRegion(region, left, top + middleHeight, middleWidth, bottom);
            }
            if (right > 0) {
                patches[BOTTOM_RIGHT] = new TextureRegion(region, left + middleWidth, top + middleHeight, right, bottom);
            }
        }
        if (left == 0 && middleWidth == 0) {
            patches[TOP_CENTER] = patches[TOP_RIGHT];
            patches[MIDDLE_CENTER] = patches[MIDDLE_RIGHT];
            patches[BOTTOM_CENTER] = patches[BOTTOM_RIGHT];
            patches[TOP_RIGHT] = null;
            patches[MIDDLE_RIGHT] = null;
            patches[BOTTOM_RIGHT] = null;
        }
        if (top == 0 && middleHeight == 0) {
            patches[MIDDLE_LEFT] = patches[BOTTOM_LEFT];
            patches[MIDDLE_CENTER] = patches[BOTTOM_CENTER];
            patches[MIDDLE_RIGHT] = patches[BOTTOM_RIGHT];
            patches[BOTTOM_LEFT] = null;
            patches[BOTTOM_CENTER] = null;
            patches[BOTTOM_RIGHT] = null;
        }
        load(patches);
    }

    public NinePatch(Texture texture, Color color) {
        this(texture);
        setColor(color);
    }

    public NinePatch(Texture texture) {
        this(new TextureRegion(texture));
    }

    public NinePatch(TextureRegion region, Color color) {
        this(region);
        setColor(color);
    }

    public NinePatch(TextureRegion region) {
        this.bottomLeft = -1;
        this.bottomCenter = -1;
        this.bottomRight = -1;
        this.middleLeft = -1;
        this.middleCenter = -1;
        this.middleRight = -1;
        this.topLeft = -1;
        this.topCenter = -1;
        this.topRight = -1;
        this.vertices = new float[180];
        this.color = new Color(Color.WHITE);
        this.padLeft = -1.0f;
        this.padRight = -1.0f;
        this.padTop = -1.0f;
        this.padBottom = -1.0f;
        load(new TextureRegion[]{null, null, null, null, region, null, null, null, null});
    }

    public NinePatch(TextureRegion... patches) {
        this.bottomLeft = -1;
        this.bottomCenter = -1;
        this.bottomRight = -1;
        this.middleLeft = -1;
        this.middleCenter = -1;
        this.middleRight = -1;
        this.topLeft = -1;
        this.topCenter = -1;
        this.topRight = -1;
        this.vertices = new float[180];
        this.color = new Color(Color.WHITE);
        this.padLeft = -1.0f;
        this.padRight = -1.0f;
        this.padTop = -1.0f;
        this.padBottom = -1.0f;
        if (patches == null || patches.length != 9) {
            throw new IllegalArgumentException("NinePatch needs nine TextureRegions");
        }
        load(patches);
        float leftWidth = getLeftWidth();
        if ((patches[TOP_LEFT] == null || ((float) patches[TOP_LEFT].getRegionWidth()) == leftWidth) && ((patches[MIDDLE_LEFT] == null || ((float) patches[MIDDLE_LEFT].getRegionWidth()) == leftWidth) && (patches[BOTTOM_LEFT] == null || ((float) patches[BOTTOM_LEFT].getRegionWidth()) == leftWidth))) {
            float rightWidth = getRightWidth();
            if ((patches[TOP_RIGHT] == null || ((float) patches[TOP_RIGHT].getRegionWidth()) == rightWidth) && ((patches[MIDDLE_RIGHT] == null || ((float) patches[MIDDLE_RIGHT].getRegionWidth()) == rightWidth) && (patches[BOTTOM_RIGHT] == null || ((float) patches[BOTTOM_RIGHT].getRegionWidth()) == rightWidth))) {
                float bottomHeight = getBottomHeight();
                if ((patches[BOTTOM_LEFT] == null || ((float) patches[BOTTOM_LEFT].getRegionHeight()) == bottomHeight) && ((patches[BOTTOM_CENTER] == null || ((float) patches[BOTTOM_CENTER].getRegionHeight()) == bottomHeight) && (patches[BOTTOM_RIGHT] == null || ((float) patches[BOTTOM_RIGHT].getRegionHeight()) == bottomHeight))) {
                    float topHeight = getTopHeight();
                    if ((patches[TOP_LEFT] != null && ((float) patches[TOP_LEFT].getRegionHeight()) != topHeight) || ((patches[TOP_CENTER] != null && ((float) patches[TOP_CENTER].getRegionHeight()) != topHeight) || (patches[TOP_RIGHT] != null && ((float) patches[TOP_RIGHT].getRegionHeight()) != topHeight))) {
                        throw new GdxRuntimeException("Top side patches must have the same height");
                    }
                    return;
                }
                throw new GdxRuntimeException("Bottom side patches must have the same height");
            }
            throw new GdxRuntimeException("Right side patches must have the same width");
        }
        throw new GdxRuntimeException("Left side patches must have the same width");
    }

    public NinePatch(NinePatch ninePatch) {
        this(ninePatch, ninePatch.color);
    }

    public NinePatch(NinePatch ninePatch, Color color) {
        this.bottomLeft = -1;
        this.bottomCenter = -1;
        this.bottomRight = -1;
        this.middleLeft = -1;
        this.middleCenter = -1;
        this.middleRight = -1;
        this.topLeft = -1;
        this.topCenter = -1;
        this.topRight = -1;
        this.vertices = new float[180];
        this.color = new Color(Color.WHITE);
        this.padLeft = -1.0f;
        this.padRight = -1.0f;
        this.padTop = -1.0f;
        this.padBottom = -1.0f;
        this.texture = ninePatch.texture;
        this.bottomLeft = ninePatch.bottomLeft;
        this.bottomCenter = ninePatch.bottomCenter;
        this.bottomRight = ninePatch.bottomRight;
        this.middleLeft = ninePatch.middleLeft;
        this.middleCenter = ninePatch.middleCenter;
        this.middleRight = ninePatch.middleRight;
        this.topLeft = ninePatch.topLeft;
        this.topCenter = ninePatch.topCenter;
        this.topRight = ninePatch.topRight;
        this.leftWidth = ninePatch.leftWidth;
        this.rightWidth = ninePatch.rightWidth;
        this.middleWidth = ninePatch.middleWidth;
        this.middleHeight = ninePatch.middleHeight;
        this.topHeight = ninePatch.topHeight;
        this.bottomHeight = ninePatch.bottomHeight;
        this.padLeft = ninePatch.padLeft;
        this.padTop = ninePatch.padTop;
        this.padBottom = ninePatch.padBottom;
        this.padRight = ninePatch.padRight;
        this.vertices = new float[ninePatch.vertices.length];
        System.arraycopy(ninePatch.vertices, TOP_LEFT, this.vertices, TOP_LEFT, ninePatch.vertices.length);
        this.idx = ninePatch.idx;
        this.color.set(color);
    }

    private void load(TextureRegion[] patches) {
        float color = Color.WHITE.toFloatBits();
        if (patches[BOTTOM_LEFT] != null) {
            this.bottomLeft = add(patches[BOTTOM_LEFT], color, false, false);
            this.leftWidth = (float) patches[BOTTOM_LEFT].getRegionWidth();
            this.bottomHeight = (float) patches[BOTTOM_LEFT].getRegionHeight();
        }
        if (patches[BOTTOM_CENTER] != null) {
            this.bottomCenter = add(patches[BOTTOM_CENTER], color, true, false);
            this.middleWidth = Math.max(this.middleWidth, (float) patches[BOTTOM_CENTER].getRegionWidth());
            this.bottomHeight = Math.max(this.bottomHeight, (float) patches[BOTTOM_CENTER].getRegionHeight());
        }
        if (patches[BOTTOM_RIGHT] != null) {
            this.bottomRight = add(patches[BOTTOM_RIGHT], color, false, false);
            this.rightWidth = Math.max(this.rightWidth, (float) patches[BOTTOM_RIGHT].getRegionWidth());
            this.bottomHeight = Math.max(this.bottomHeight, (float) patches[BOTTOM_RIGHT].getRegionHeight());
        }
        if (patches[MIDDLE_LEFT] != null) {
            this.middleLeft = add(patches[MIDDLE_LEFT], color, false, true);
            this.leftWidth = Math.max(this.leftWidth, (float) patches[MIDDLE_LEFT].getRegionWidth());
            this.middleHeight = Math.max(this.middleHeight, (float) patches[MIDDLE_LEFT].getRegionHeight());
        }
        if (patches[MIDDLE_CENTER] != null) {
            this.middleCenter = add(patches[MIDDLE_CENTER], color, true, true);
            this.middleWidth = Math.max(this.middleWidth, (float) patches[MIDDLE_CENTER].getRegionWidth());
            this.middleHeight = Math.max(this.middleHeight, (float) patches[MIDDLE_CENTER].getRegionHeight());
        }
        if (patches[MIDDLE_RIGHT] != null) {
            this.middleRight = add(patches[MIDDLE_RIGHT], color, false, true);
            this.rightWidth = Math.max(this.rightWidth, (float) patches[MIDDLE_RIGHT].getRegionWidth());
            this.middleHeight = Math.max(this.middleHeight, (float) patches[MIDDLE_RIGHT].getRegionHeight());
        }
        if (patches[TOP_LEFT] != null) {
            this.topLeft = add(patches[TOP_LEFT], color, false, false);
            this.leftWidth = Math.max(this.leftWidth, (float) patches[TOP_LEFT].getRegionWidth());
            this.topHeight = Math.max(this.topHeight, (float) patches[TOP_LEFT].getRegionHeight());
        }
        if (patches[TOP_CENTER] != null) {
            this.topCenter = add(patches[TOP_CENTER], color, true, false);
            this.middleWidth = Math.max(this.middleWidth, (float) patches[TOP_CENTER].getRegionWidth());
            this.topHeight = Math.max(this.topHeight, (float) patches[TOP_CENTER].getRegionHeight());
        }
        if (patches[TOP_RIGHT] != null) {
            this.topRight = add(patches[TOP_RIGHT], color, false, false);
            this.rightWidth = Math.max(this.rightWidth, (float) patches[TOP_RIGHT].getRegionWidth());
            this.topHeight = Math.max(this.topHeight, (float) patches[TOP_RIGHT].getRegionHeight());
        }
        if (this.idx < this.vertices.length) {
            float[] newVertices = new float[this.idx];
            System.arraycopy(this.vertices, TOP_LEFT, newVertices, TOP_LEFT, this.idx);
            this.vertices = newVertices;
        }
    }

    private int add(TextureRegion region, float color, boolean isStretchW, boolean isStretchH) {
        if (this.texture == null) {
            this.texture = region.getTexture();
        } else if (this.texture != region.getTexture()) {
            throw new IllegalArgumentException("All regions must be from the same texture.");
        }
        float u = region.u;
        float v = region.v2;
        float u2 = region.u2;
        float v2 = region.v;
        if (isStretchW) {
            float halfTexelWidth = 0.5f / ((float) this.texture.getWidth());
            u += halfTexelWidth;
            u2 -= halfTexelWidth;
        }
        if (isStretchH) {
            float halfTexelHeight = 0.5f / ((float) this.texture.getHeight());
            v -= halfTexelHeight;
            v2 += halfTexelHeight;
        }
        float[] vertices = this.vertices;
        this.idx += TOP_RIGHT;
        int i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = color;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = u;
        vertices[this.idx] = v;
        this.idx += MIDDLE_LEFT;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = color;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = u;
        vertices[this.idx] = v2;
        this.idx += MIDDLE_LEFT;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = color;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = u2;
        vertices[this.idx] = v2;
        this.idx += MIDDLE_LEFT;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = color;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = u2;
        i = this.idx;
        this.idx = i + TOP_CENTER;
        vertices[i] = v;
        return this.idx - 20;
    }

    private void set(int idx, float x, float y, float width, float height, float color) {
        float fx2 = x + width;
        float fy2 = y + height;
        float[] vertices = this.vertices;
        int idx2 = idx + TOP_CENTER;
        vertices[idx] = x;
        idx = idx2 + TOP_CENTER;
        vertices[idx2] = y;
        vertices[idx] = color;
        idx += MIDDLE_LEFT;
        idx2 = idx + TOP_CENTER;
        vertices[idx] = x;
        idx = idx2 + TOP_CENTER;
        vertices[idx2] = fy2;
        vertices[idx] = color;
        idx += MIDDLE_LEFT;
        idx2 = idx + TOP_CENTER;
        vertices[idx] = fx2;
        idx = idx2 + TOP_CENTER;
        vertices[idx2] = fy2;
        vertices[idx] = color;
        idx += MIDDLE_LEFT;
        idx2 = idx + TOP_CENTER;
        vertices[idx] = fx2;
        idx = idx2 + TOP_CENTER;
        vertices[idx2] = y;
        vertices[idx] = color;
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        float centerColumnX = x + this.leftWidth;
        float rightColumnX = (x + width) - this.rightWidth;
        float middleRowY = y + this.bottomHeight;
        float topRowY = (y + height) - this.topHeight;
        float c = tmpDrawColor.set(this.color).mul(batch.getColor()).toFloatBits();
        if (this.bottomLeft != -1) {
            set(this.bottomLeft, x, y, centerColumnX - x, middleRowY - y, c);
        }
        if (this.bottomCenter != -1) {
            set(this.bottomCenter, centerColumnX, y, rightColumnX - centerColumnX, middleRowY - y, c);
        }
        if (this.bottomRight != -1) {
            set(this.bottomRight, rightColumnX, y, (x + width) - rightColumnX, middleRowY - y, c);
        }
        if (this.middleLeft != -1) {
            set(this.middleLeft, x, middleRowY, centerColumnX - x, topRowY - middleRowY, c);
        }
        if (this.middleCenter != -1) {
            set(this.middleCenter, centerColumnX, middleRowY, rightColumnX - centerColumnX, topRowY - middleRowY, c);
        }
        if (this.middleRight != -1) {
            set(this.middleRight, rightColumnX, middleRowY, (x + width) - rightColumnX, topRowY - middleRowY, c);
        }
        if (this.topLeft != -1) {
            float f = x;
            float f2 = topRowY;
            set(this.topLeft, f, f2, centerColumnX - x, (y + height) - topRowY, c);
        }
        if (this.topCenter != -1) {
            f = centerColumnX;
            f2 = topRowY;
            set(this.topCenter, f, f2, rightColumnX - centerColumnX, (y + height) - topRowY, c);
        }
        if (this.topRight != -1) {
            set(this.topRight, rightColumnX, topRowY, (x + width) - rightColumnX, (y + height) - topRowY, c);
        }
        batch.draw(this.texture, this.vertices, (int) TOP_LEFT, this.idx);
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public Color getColor() {
        return this.color;
    }

    public float getLeftWidth() {
        return this.leftWidth;
    }

    public void setLeftWidth(float leftWidth) {
        this.leftWidth = leftWidth;
    }

    public float getRightWidth() {
        return this.rightWidth;
    }

    public void setRightWidth(float rightWidth) {
        this.rightWidth = rightWidth;
    }

    public float getTopHeight() {
        return this.topHeight;
    }

    public void setTopHeight(float topHeight) {
        this.topHeight = topHeight;
    }

    public float getBottomHeight() {
        return this.bottomHeight;
    }

    public void setBottomHeight(float bottomHeight) {
        this.bottomHeight = bottomHeight;
    }

    public float getMiddleWidth() {
        return this.middleWidth;
    }

    public void setMiddleWidth(float middleWidth) {
        this.middleWidth = middleWidth;
    }

    public float getMiddleHeight() {
        return this.middleHeight;
    }

    public void setMiddleHeight(float middleHeight) {
        this.middleHeight = middleHeight;
    }

    public float getTotalWidth() {
        return (this.leftWidth + this.middleWidth) + this.rightWidth;
    }

    public float getTotalHeight() {
        return (this.topHeight + this.middleHeight) + this.bottomHeight;
    }

    public void setPadding(float left, float right, float top, float bottom) {
        this.padLeft = left;
        this.padRight = right;
        this.padTop = top;
        this.padBottom = bottom;
    }

    public float getPadLeft() {
        if (this.padLeft == -1.0f) {
            return getLeftWidth();
        }
        return this.padLeft;
    }

    public void setPadLeft(float left) {
        this.padLeft = left;
    }

    public float getPadRight() {
        if (this.padRight == -1.0f) {
            return getRightWidth();
        }
        return this.padRight;
    }

    public void setPadRight(float right) {
        this.padRight = right;
    }

    public float getPadTop() {
        if (this.padTop == -1.0f) {
            return getTopHeight();
        }
        return this.padTop;
    }

    public void setPadTop(float top) {
        this.padTop = top;
    }

    public float getPadBottom() {
        if (this.padBottom == -1.0f) {
            return getBottomHeight();
        }
        return this.padBottom;
    }

    public void setPadBottom(float bottom) {
        this.padBottom = bottom;
    }

    public void scale(float scaleX, float scaleY) {
        this.leftWidth *= scaleX;
        this.rightWidth *= scaleX;
        this.topHeight *= scaleY;
        this.bottomHeight *= scaleY;
        this.middleWidth *= scaleX;
        this.middleHeight *= scaleY;
        if (this.padLeft != -1.0f) {
            this.padLeft *= scaleX;
        }
        if (this.padRight != -1.0f) {
            this.padRight *= scaleX;
        }
        if (this.padTop != -1.0f) {
            this.padTop *= scaleY;
        }
        if (this.padBottom != -1.0f) {
            this.padBottom *= scaleY;
        }
    }

    public Texture getTexture() {
        return this.texture;
    }
}
