package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.OrderedMap;
import java.util.Iterator;

public class PixmapPacker implements Disposable {
    private static final String ANONYMOUS = "ANONYMOUS";
    private static final boolean debug = false;
    Page current;
    boolean disposed;
    final boolean duplicateBorder;
    boolean packToTexture;
    final int padding;
    final Format pageFormat;
    final int pageHeight;
    final int pageWidth;
    final Array<Page> pages = new Array();

    static final class Node {
        public String leafName;
        public Node leftChild;
        public Rectangle rect;
        public Node rightChild;

        public Node(int x, int y, int width, int height, Node leftChild, Node rightChild, String leafName) {
            this.rect = new Rectangle((float) x, (float) y, (float) width, (float) height);
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.leafName = leafName;
        }

        public Node() {
            this.rect = new Rectangle();
        }
    }

    public static class Page {
        final Array<String> addedRects = new Array();
        boolean dirty;
        Pixmap image;
        OrderedMap<String, Rectangle> rects;
        Node root;
        Texture texture;

        public Pixmap getPixmap() {
            return this.image;
        }

        public OrderedMap<String, Rectangle> getRects() {
            return this.rects;
        }

        public Texture getTexture() {
            return this.texture;
        }

        public boolean updateTexture(TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
            if (this.texture == null) {
                this.texture = new Texture(new PixmapTextureData(this.image, this.image.getFormat(), useMipMaps, false, true)) {
                    public void dispose() {
                        super.dispose();
                        Page.this.image.dispose();
                    }
                };
                this.texture.setFilter(minFilter, magFilter);
            } else if (!this.dirty) {
                return false;
            } else {
                this.texture.load(this.texture.getTextureData());
            }
            this.dirty = false;
            return true;
        }
    }

    public PixmapPacker(int pageWidth, int pageHeight, Format pageFormat, int padding, boolean duplicateBorder) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.pageFormat = pageFormat;
        this.padding = padding;
        this.duplicateBorder = duplicateBorder;
        newPage();
    }

    public synchronized Rectangle pack(Pixmap image) {
        return pack(null, image);
    }

    public synchronized Rectangle pack(String name, Pixmap image) {
        Rectangle rectangle;
        if (this.disposed) {
            rectangle = null;
        } else {
            if (name != null) {
                if (getRect(name) != null) {
                    throw new GdxRuntimeException("Pixmap has already been packed with name: " + name);
                }
            }
            int borderPixels = ((this.duplicateBorder ? 1 : 0) + this.padding) << 1;
            Rectangle rectangle2 = new Rectangle(0.0f, 0.0f, (float) (image.getWidth() + borderPixels), (float) (image.getHeight() + borderPixels));
            if (rectangle2.getWidth() <= ((float) this.pageWidth) && rectangle2.getHeight() <= ((float) this.pageHeight)) {
                Node node = insert(this.current.root, rectangle2);
                if (node == null) {
                    newPage();
                    rectangle = pack(name, image);
                } else {
                    String str;
                    if (name == null) {
                        str = ANONYMOUS;
                    } else {
                        str = name;
                    }
                    node.leafName = str;
                    rectangle2 = new Rectangle(node.rect);
                    rectangle2.width -= (float) borderPixels;
                    rectangle2.height -= (float) borderPixels;
                    borderPixels >>= 1;
                    rectangle2.x += (float) borderPixels;
                    rectangle2.y += (float) borderPixels;
                    if (name != null) {
                        this.current.rects.put(name, rectangle2);
                        this.current.addedRects.add(name);
                    }
                    int rectX = (int) rectangle2.x;
                    int rectY = (int) rectangle2.y;
                    int rectWidth = (int) rectangle2.width;
                    int rectHeight = (int) rectangle2.height;
                    if (!this.packToTexture || this.duplicateBorder || this.current.texture == null || this.current.dirty) {
                        this.current.dirty = true;
                    } else {
                        this.current.texture.bind();
                        Gdx.gl.glTexSubImage2D(this.current.texture.glTarget, 0, rectX, rectY, rectWidth, rectHeight, image.getGLFormat(), image.getGLType(), image.getPixels());
                    }
                    Blending blending = Pixmap.getBlending();
                    Pixmap.setBlending(Blending.None);
                    this.current.image.drawPixmap(image, rectX, rectY);
                    if (this.duplicateBorder) {
                        int imageWidth = image.getWidth();
                        int imageHeight = image.getHeight();
                        this.current.image.drawPixmap(image, 0, 0, 1, 1, rectX - 1, rectY - 1, 1, 1);
                        this.current.image.drawPixmap(image, imageWidth - 1, 0, 1, 1, rectX + rectWidth, rectY - 1, 1, 1);
                        this.current.image.drawPixmap(image, 0, imageHeight - 1, 1, 1, rectX - 1, rectY + rectHeight, 1, 1);
                        this.current.image.drawPixmap(image, imageWidth - 1, imageHeight - 1, 1, 1, rectX + rectWidth, rectY + rectHeight, 1, 1);
                        this.current.image.drawPixmap(image, 0, 0, imageWidth, 1, rectX, rectY - 1, rectWidth, 1);
                        this.current.image.drawPixmap(image, 0, imageHeight - 1, imageWidth, 1, rectX, rectY + rectHeight, rectWidth, 1);
                        this.current.image.drawPixmap(image, 0, 0, 1, imageHeight, rectX - 1, rectY, 1, rectHeight);
                        this.current.image.drawPixmap(image, imageWidth - 1, 0, 1, imageHeight, rectX + rectWidth, rectY, 1, rectHeight);
                    }
                    Pixmap.setBlending(blending);
                }
            } else if (name == null) {
                throw new GdxRuntimeException("Page size too small for anonymous pixmap.");
            } else {
                throw new GdxRuntimeException("Page size too small for pixmap: " + name);
            }
        }
        return rectangle;
    }

    private void newPage() {
        Page page = new Page();
        page.image = new Pixmap(this.pageWidth, this.pageHeight, this.pageFormat);
        page.root = new Node(0, 0, this.pageWidth, this.pageHeight, null, null, null);
        page.rects = new OrderedMap();
        this.pages.add(page);
        this.current = page;
    }

    private Node insert(Node node, Rectangle rect) {
        if (node.leafName == null && node.leftChild != null && node.rightChild != null) {
            Node newNode = insert(node.leftChild, rect);
            return newNode == null ? insert(node.rightChild, rect) : newNode;
        } else if (node.leafName != null) {
            return null;
        } else {
            if (node.rect.width == rect.width && node.rect.height == rect.height) {
                return node;
            }
            if (node.rect.width < rect.width || node.rect.height < rect.height) {
                return null;
            }
            node.leftChild = new Node();
            node.rightChild = new Node();
            if (((int) node.rect.width) - ((int) rect.width) > ((int) node.rect.height) - ((int) rect.height)) {
                node.leftChild.rect.x = node.rect.x;
                node.leftChild.rect.y = node.rect.y;
                node.leftChild.rect.width = rect.width;
                node.leftChild.rect.height = node.rect.height;
                node.rightChild.rect.x = node.rect.x + rect.width;
                node.rightChild.rect.y = node.rect.y;
                node.rightChild.rect.width = node.rect.width - rect.width;
                node.rightChild.rect.height = node.rect.height;
            } else {
                node.leftChild.rect.x = node.rect.x;
                node.leftChild.rect.y = node.rect.y;
                node.leftChild.rect.width = node.rect.width;
                node.leftChild.rect.height = rect.height;
                node.rightChild.rect.x = node.rect.x;
                node.rightChild.rect.y = node.rect.y + rect.height;
                node.rightChild.rect.width = node.rect.width;
                node.rightChild.rect.height = node.rect.height - rect.height;
            }
            return insert(node.leftChild, rect);
        }
    }

    public Array<Page> getPages() {
        return this.pages;
    }

    public synchronized Rectangle getRect(String name) {
        Rectangle rect;
        Iterator it = this.pages.iterator();
        while (it.hasNext()) {
            rect = (Rectangle) ((Page) it.next()).rects.get(name);
            if (rect != null) {
                break;
            }
        }
        rect = null;
        return rect;
    }

    public synchronized Page getPage(String name) {
        Page page;
        Iterator it = this.pages.iterator();
        while (it.hasNext()) {
            page = (Page) it.next();
            if (((Rectangle) page.rects.get(name)) != null) {
                break;
            }
        }
        page = null;
        return page;
    }

    public synchronized int getPageIndex(String name) {
        int i;
        i = 0;
        while (i < this.pages.size) {
            if (((Rectangle) ((Page) this.pages.get(i)).rects.get(name)) != null) {
                break;
            }
            i++;
        }
        i = -1;
        return i;
    }

    public synchronized void dispose() {
        Iterator it = this.pages.iterator();
        while (it.hasNext()) {
            Page page = (Page) it.next();
            if (page.texture == null) {
                page.image.dispose();
            }
        }
        this.disposed = true;
    }

    public synchronized TextureAtlas generateTextureAtlas(TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        TextureAtlas atlas;
        atlas = new TextureAtlas();
        updateTextureAtlas(atlas, minFilter, magFilter, useMipMaps);
        return atlas;
    }

    public synchronized void updateTextureAtlas(TextureAtlas atlas, TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        updatePageTextures(minFilter, magFilter, useMipMaps);
        Iterator it = this.pages.iterator();
        while (it.hasNext()) {
            Page page = (Page) it.next();
            if (page.addedRects.size > 0) {
                Iterator it2 = page.addedRects.iterator();
                while (it2.hasNext()) {
                    String name = (String) it2.next();
                    Rectangle rect = (Rectangle) page.rects.get(name);
                    atlas.addRegion(name, new TextureRegion(page.texture, (int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height));
                }
                page.addedRects.clear();
                atlas.getTextures().add(page.texture);
            }
        }
    }

    public synchronized void updateTextureRegions(Array<TextureRegion> regions, TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        updatePageTextures(minFilter, magFilter, useMipMaps);
        while (regions.size < this.pages.size) {
            regions.add(new TextureRegion(((Page) this.pages.get(regions.size)).texture));
        }
    }

    public synchronized void updatePageTextures(TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        Iterator it = this.pages.iterator();
        while (it.hasNext()) {
            ((Page) it.next()).updateTexture(minFilter, magFilter, useMipMaps);
        }
    }

    public int getPageWidth() {
        return this.pageWidth;
    }

    public int getPageHeight() {
        return this.pageHeight;
    }

    public int getPadding() {
        return this.padding;
    }

    public boolean getDuplicateBorder() {
        return this.duplicateBorder;
    }

    public boolean getPackToTexture() {
        return this.packToTexture;
    }

    public void setPackToTexture(boolean packToTexture) {
        this.packToTexture = packToTexture;
    }
}
