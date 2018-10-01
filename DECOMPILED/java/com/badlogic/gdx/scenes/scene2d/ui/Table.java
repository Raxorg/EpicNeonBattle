package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class Table extends WidgetGroup {
    public static Value backgroundBottom = new Value() {
        public float get(Actor context) {
            Drawable background = ((Table) context).background;
            return background == null ? 0.0f : background.getBottomHeight();
        }
    };
    public static Value backgroundLeft = new Value() {
        public float get(Actor context) {
            Drawable background = ((Table) context).background;
            return background == null ? 0.0f : background.getLeftWidth();
        }
    };
    public static Value backgroundRight = new Value() {
        public float get(Actor context) {
            Drawable background = ((Table) context).background;
            return background == null ? 0.0f : background.getRightWidth();
        }
    };
    public static Value backgroundTop = new Value() {
        public float get(Actor context) {
            Drawable background = ((Table) context).background;
            return background == null ? 0.0f : background.getTopHeight();
        }
    };
    static final Pool<Cell> cellPool = new Pool<Cell>() {
        protected Cell newObject() {
            return new Cell();
        }
    };
    private static float[] columnWeightedWidth;
    public static Color debugActorColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    public static Color debugCellColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    public static Color debugTableColor = new Color(0.0f, 0.0f, 1.0f, 1.0f);
    private static float[] rowWeightedHeight;
    int align;
    Drawable background;
    private final Cell cellDefaults;
    private final Array<Cell> cells;
    private boolean clip;
    private final Array<Cell> columnDefaults;
    private float[] columnMinWidth;
    private float[] columnPrefWidth;
    private float[] columnWidth;
    private int columns;
    Debug debug;
    Array<DebugRect> debugRects;
    private float[] expandHeight;
    private float[] expandWidth;
    private boolean implicitEndRow;
    Value padBottom;
    Value padLeft;
    Value padRight;
    Value padTop;
    boolean round;
    private Cell rowDefaults;
    private float[] rowHeight;
    private float[] rowMinHeight;
    private float[] rowPrefHeight;
    private int rows;
    private boolean sizeInvalid;
    private Skin skin;
    private float tableMinHeight;
    private float tableMinWidth;
    private float tablePrefHeight;
    private float tablePrefWidth;

    public enum Debug {
        none,
        all,
        table,
        cell,
        actor
    }

    public static class DebugRect extends Rectangle {
        static Pool<DebugRect> pool = Pools.get(DebugRect.class);
        Color color;
    }

    public Table() {
        this(null);
    }

    public Table(Skin skin) {
        this.cells = new Array(4);
        this.columnDefaults = new Array(2);
        this.sizeInvalid = true;
        this.padTop = backgroundTop;
        this.padLeft = backgroundLeft;
        this.padBottom = backgroundBottom;
        this.padRight = backgroundRight;
        this.align = 1;
        this.debug = Debug.none;
        this.round = true;
        this.skin = skin;
        this.cellDefaults = obtainCell();
        setTransform(false);
        setTouchable(Touchable.childrenOnly);
    }

    private Cell obtainCell() {
        Cell cell = (Cell) cellPool.obtain();
        cell.setLayout(this);
        return cell;
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
        if (isTransform()) {
            applyTransform(batch, computeTransform());
            drawBackground(batch, parentAlpha, 0.0f, 0.0f);
            if (this.clip) {
                batch.flush();
                float padLeft = this.padLeft.get(this);
                float padBottom = this.padBottom.get(this);
                if (clipBegin(padLeft, padBottom, (getWidth() - padLeft) - this.padRight.get(this), (getHeight() - padBottom) - this.padTop.get(this))) {
                    drawChildren(batch, parentAlpha);
                    batch.flush();
                    clipEnd();
                }
            } else {
                drawChildren(batch, parentAlpha);
            }
            resetTransform(batch);
            return;
        }
        drawBackground(batch, parentAlpha, getX(), getY());
        super.draw(batch, parentAlpha);
    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        if (this.background != null) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            this.background.draw(batch, x, y, getWidth(), getHeight());
        }
    }

    public void setBackground(String drawableName) {
        if (this.skin == null) {
            throw new IllegalStateException("Table must have a skin set to use this method.");
        }
        setBackground(this.skin.getDrawable(drawableName));
    }

    public void setBackground(Drawable background) {
        if (this.background != background) {
            float padTopOld = getPadTop();
            float padLeftOld = getPadLeft();
            float padBottomOld = getPadBottom();
            float padRightOld = getPadRight();
            this.background = background;
            float padTopNew = getPadTop();
            float padLeftNew = getPadLeft();
            float padBottomNew = getPadBottom();
            float padRightNew = getPadRight();
            if (padTopOld + padBottomOld != padTopNew + padBottomNew || padLeftOld + padRightOld != padLeftNew + padRightNew) {
                invalidateHierarchy();
            } else if (padTopOld != padTopNew || padLeftOld != padLeftNew || padBottomOld != padBottomNew || padRightOld != padRightNew) {
                invalidate();
            }
        }
    }

    public Table background(Drawable background) {
        setBackground(background);
        return this;
    }

    public Table background(String drawableName) {
        setBackground(drawableName);
        return this;
    }

    public Drawable getBackground() {
        return this.background;
    }

    public Actor hit(float x, float y, boolean touchable) {
        if (!this.clip || ((!touchable || getTouchable() != Touchable.disabled) && x >= 0.0f && x < getWidth() && y >= 0.0f && y < getHeight())) {
            return super.hit(x, y, touchable);
        }
        return null;
    }

    public void setClip(boolean enabled) {
        this.clip = enabled;
        setTransform(enabled);
        invalidate();
    }

    public boolean getClip() {
        return this.clip;
    }

    public void invalidate() {
        this.sizeInvalid = true;
        super.invalidate();
    }

    public <T extends Actor> Cell<T> add(T actor) {
        Cell<T> cell = obtainCell();
        cell.actor = actor;
        if (this.implicitEndRow) {
            this.implicitEndRow = false;
            this.rows--;
            ((Cell) this.cells.peek()).endRow = false;
        }
        Array<Cell> cells = this.cells;
        int cellCount = cells.size;
        if (cellCount > 0) {
            Cell lastCell = (Cell) cells.peek();
            if (lastCell.endRow) {
                cell.column = 0;
                cell.row = lastCell.row + 1;
            } else {
                cell.column = lastCell.column + lastCell.colspan.intValue();
                cell.row = lastCell.row;
            }
            if (cell.row > 0) {
                loop0:
                for (int i = cellCount - 1; i >= 0; i--) {
                    Cell other = (Cell) cells.get(i);
                    int column = other.column;
                    int nn = column + other.colspan.intValue();
                    while (column < nn) {
                        if (column == cell.column) {
                            cell.cellAboveIndex = i;
                            break loop0;
                        }
                        column++;
                    }
                }
            }
        } else {
            cell.column = 0;
            cell.row = 0;
        }
        cells.add(cell);
        cell.set(this.cellDefaults);
        if (cell.column < this.columnDefaults.size) {
            Cell columnCell = (Cell) this.columnDefaults.get(cell.column);
            if (columnCell != null) {
                cell.merge(columnCell);
            }
        }
        cell.merge(this.rowDefaults);
        if (actor != null) {
            addActor(actor);
        }
        return cell;
    }

    public void add(Actor... actors) {
        for (Actor add : actors) {
            add(add);
        }
    }

    public Cell<Label> add(String text) {
        if (this.skin != null) {
            return add(new Label((CharSequence) text, this.skin));
        }
        throw new IllegalStateException("Table must have a skin set to use this method.");
    }

    public Cell<Label> add(String text, String labelStyleName) {
        if (this.skin != null) {
            return add(new Label((CharSequence) text, (LabelStyle) this.skin.get(labelStyleName, LabelStyle.class)));
        }
        throw new IllegalStateException("Table must have a skin set to use this method.");
    }

    public Cell<Label> add(String text, String fontName, Color color) {
        if (this.skin != null) {
            return add(new Label((CharSequence) text, new LabelStyle(this.skin.getFont(fontName), color)));
        }
        throw new IllegalStateException("Table must have a skin set to use this method.");
    }

    public Cell<Label> add(String text, String fontName, String colorName) {
        if (this.skin != null) {
            return add(new Label((CharSequence) text, new LabelStyle(this.skin.getFont(fontName), this.skin.getColor(colorName))));
        }
        throw new IllegalStateException("Table must have a skin set to use this method.");
    }

    public Cell add() {
        return add((Actor) null);
    }

    public Cell<Stack> stack(Actor... actors) {
        Actor stack = new Stack();
        if (actors != null) {
            for (Actor addActor : actors) {
                stack.addActor(addActor);
            }
        }
        return add(stack);
    }

    public boolean removeActor(Actor actor) {
        return removeActor(actor, true);
    }

    public boolean removeActor(Actor actor, boolean unfocus) {
        if (!super.removeActor(actor, unfocus)) {
            return false;
        }
        Cell cell = getCell(actor);
        if (cell != null) {
            cell.actor = null;
        }
        return true;
    }

    public void clearChildren() {
        Array<Cell> cells = this.cells;
        for (int i = cells.size - 1; i >= 0; i--) {
            Actor actor = ((Cell) cells.get(i)).actor;
            if (actor != null) {
                actor.remove();
            }
        }
        cellPool.freeAll(cells);
        cells.clear();
        this.rows = 0;
        this.columns = 0;
        if (this.rowDefaults != null) {
            cellPool.free(this.rowDefaults);
        }
        this.rowDefaults = null;
        this.implicitEndRow = false;
        super.clearChildren();
    }

    public void reset() {
        clear();
        this.padTop = backgroundTop;
        this.padLeft = backgroundLeft;
        this.padBottom = backgroundBottom;
        this.padRight = backgroundRight;
        this.align = 1;
        debug(Debug.none);
        this.cellDefaults.reset();
        int n = this.columnDefaults.size;
        for (int i = 0; i < n; i++) {
            Cell columnCell = (Cell) this.columnDefaults.get(i);
            if (columnCell != null) {
                cellPool.free(columnCell);
            }
        }
        this.columnDefaults.clear();
    }

    public Cell row() {
        if (this.cells.size > 0) {
            endRow();
            invalidate();
        }
        if (this.rowDefaults != null) {
            cellPool.free(this.rowDefaults);
        }
        this.rowDefaults = obtainCell();
        this.rowDefaults.clear();
        return this.rowDefaults;
    }

    private void endRow() {
        Array<Cell> cells = this.cells;
        int rowColumns = 0;
        for (int i = cells.size - 1; i >= 0; i--) {
            Cell cell = (Cell) cells.get(i);
            if (cell.endRow) {
                break;
            }
            rowColumns += cell.colspan.intValue();
        }
        this.columns = Math.max(this.columns, rowColumns);
        this.rows++;
        ((Cell) cells.peek()).endRow = true;
    }

    public Cell columnDefaults(int column) {
        Cell cell;
        if (this.columnDefaults.size > column) {
            cell = (Cell) this.columnDefaults.get(column);
        } else {
            cell = null;
        }
        if (cell == null) {
            cell = obtainCell();
            cell.clear();
            if (column >= this.columnDefaults.size) {
                for (int i = this.columnDefaults.size; i < column; i++) {
                    this.columnDefaults.add(null);
                }
                this.columnDefaults.add(cell);
            } else {
                this.columnDefaults.set(column, cell);
            }
        }
        return cell;
    }

    public <T extends Actor> Cell<T> getCell(T actor) {
        Array<Cell> cells = this.cells;
        int n = cells.size;
        for (int i = 0; i < n; i++) {
            Cell c = (Cell) cells.get(i);
            if (c.actor == actor) {
                return c;
            }
        }
        return null;
    }

    public Array<Cell> getCells() {
        return this.cells;
    }

    public float getPrefWidth() {
        if (this.sizeInvalid) {
            computeSize();
        }
        float width = this.tablePrefWidth;
        if (this.background != null) {
            return Math.max(width, this.background.getMinWidth());
        }
        return width;
    }

    public float getPrefHeight() {
        if (this.sizeInvalid) {
            computeSize();
        }
        float height = this.tablePrefHeight;
        if (this.background != null) {
            return Math.max(height, this.background.getMinHeight());
        }
        return height;
    }

    public float getMinWidth() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.tableMinWidth;
    }

    public float getMinHeight() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.tableMinHeight;
    }

    public Cell defaults() {
        return this.cellDefaults;
    }

    public Table pad(Value pad) {
        if (pad == null) {
            throw new IllegalArgumentException("pad cannot be null.");
        }
        this.padTop = pad;
        this.padLeft = pad;
        this.padBottom = pad;
        this.padRight = pad;
        this.sizeInvalid = true;
        return this;
    }

    public Table pad(Value top, Value left, Value bottom, Value right) {
        if (top == null) {
            throw new IllegalArgumentException("top cannot be null.");
        } else if (left == null) {
            throw new IllegalArgumentException("left cannot be null.");
        } else if (bottom == null) {
            throw new IllegalArgumentException("bottom cannot be null.");
        } else if (right == null) {
            throw new IllegalArgumentException("right cannot be null.");
        } else {
            this.padTop = top;
            this.padLeft = left;
            this.padBottom = bottom;
            this.padRight = right;
            this.sizeInvalid = true;
            return this;
        }
    }

    public Table padTop(Value padTop) {
        if (padTop == null) {
            throw new IllegalArgumentException("padTop cannot be null.");
        }
        this.padTop = padTop;
        this.sizeInvalid = true;
        return this;
    }

    public Table padLeft(Value padLeft) {
        if (padLeft == null) {
            throw new IllegalArgumentException("padLeft cannot be null.");
        }
        this.padLeft = padLeft;
        this.sizeInvalid = true;
        return this;
    }

    public Table padBottom(Value padBottom) {
        if (padBottom == null) {
            throw new IllegalArgumentException("padBottom cannot be null.");
        }
        this.padBottom = padBottom;
        this.sizeInvalid = true;
        return this;
    }

    public Table padRight(Value padRight) {
        if (padRight == null) {
            throw new IllegalArgumentException("padRight cannot be null.");
        }
        this.padRight = padRight;
        this.sizeInvalid = true;
        return this;
    }

    public Table pad(float pad) {
        pad(new Fixed(pad));
        return this;
    }

    public Table pad(float top, float left, float bottom, float right) {
        this.padTop = new Fixed(top);
        this.padLeft = new Fixed(left);
        this.padBottom = new Fixed(bottom);
        this.padRight = new Fixed(right);
        this.sizeInvalid = true;
        return this;
    }

    public Table padTop(float padTop) {
        this.padTop = new Fixed(padTop);
        this.sizeInvalid = true;
        return this;
    }

    public Table padLeft(float padLeft) {
        this.padLeft = new Fixed(padLeft);
        this.sizeInvalid = true;
        return this;
    }

    public Table padBottom(float padBottom) {
        this.padBottom = new Fixed(padBottom);
        this.sizeInvalid = true;
        return this;
    }

    public Table padRight(float padRight) {
        this.padRight = new Fixed(padRight);
        this.sizeInvalid = true;
        return this;
    }

    public Table align(int align) {
        this.align = align;
        return this;
    }

    public Table center() {
        this.align = 1;
        return this;
    }

    public Table top() {
        this.align |= 2;
        this.align &= -5;
        return this;
    }

    public Table left() {
        this.align |= 8;
        this.align &= -17;
        return this;
    }

    public Table bottom() {
        this.align |= 4;
        this.align &= -3;
        return this;
    }

    public Table right() {
        this.align |= 16;
        this.align &= -9;
        return this;
    }

    public void setDebug(boolean enabled) {
        debug(enabled ? Debug.all : Debug.none);
    }

    public Table debug() {
        super.debug();
        return this;
    }

    public Table debugAll() {
        super.debugAll();
        return this;
    }

    public Table debugTable() {
        super.setDebug(true);
        if (this.debug != Debug.table) {
            this.debug = Debug.table;
            invalidate();
        }
        return this;
    }

    public Table debugCell() {
        super.setDebug(true);
        if (this.debug != Debug.cell) {
            this.debug = Debug.cell;
            invalidate();
        }
        return this;
    }

    public Table debugActor() {
        super.setDebug(true);
        if (this.debug != Debug.actor) {
            this.debug = Debug.actor;
            invalidate();
        }
        return this;
    }

    public Table debug(Debug debug) {
        super.setDebug(debug != Debug.none);
        if (this.debug != debug) {
            this.debug = debug;
            if (debug == Debug.none) {
                clearDebugRects();
            } else {
                invalidate();
            }
        }
        return this;
    }

    public Debug getTableDebug() {
        return this.debug;
    }

    public Value getPadTopValue() {
        return this.padTop;
    }

    public float getPadTop() {
        return this.padTop.get(this);
    }

    public Value getPadLeftValue() {
        return this.padLeft;
    }

    public float getPadLeft() {
        return this.padLeft.get(this);
    }

    public Value getPadBottomValue() {
        return this.padBottom;
    }

    public float getPadBottom() {
        return this.padBottom.get(this);
    }

    public Value getPadRightValue() {
        return this.padRight;
    }

    public float getPadRight() {
        return this.padRight.get(this);
    }

    public float getPadX() {
        return this.padLeft.get(this) + this.padRight.get(this);
    }

    public float getPadY() {
        return this.padTop.get(this) + this.padBottom.get(this);
    }

    public int getAlign() {
        return this.align;
    }

    public int getRow(float y) {
        Array<Cell> cells = this.cells;
        int row = 0;
        y += getPadTop();
        int n = cells.size;
        if (n == 0) {
            return -1;
        }
        if (n == 1) {
            return 0;
        }
        int i = 0;
        while (i < n) {
            int i2 = i + 1;
            Cell c = (Cell) cells.get(i);
            if (c.actorY + c.computedPadTop < y) {
                break;
            }
            if (c.endRow) {
                row++;
            }
            i = i2;
        }
        return row;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public void setRound(boolean round) {
        this.round = round;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public float getRowHeight(int rowIndex) {
        return this.rowHeight[rowIndex];
    }

    public float getColumnWidth(int columnIndex) {
        return this.columnWidth[columnIndex];
    }

    private float[] ensureSize(float[] array, int size) {
        if (array == null || array.length < size) {
            return new float[size];
        }
        int n = array.length;
        for (int i = 0; i < n; i++) {
            array[i] = 0.0f;
        }
        return array;
    }

    public void layout() {
        int n;
        int i;
        float width = getWidth();
        float height = getHeight();
        layout(0.0f, 0.0f, width, height);
        Array<Cell> cells = this.cells;
        Cell c;
        float actorHeight;
        float actorY;
        Actor actor;
        if (this.round) {
            n = cells.size;
            for (i = 0; i < n; i++) {
                c = (Cell) cells.get(i);
                float actorWidth = (float) Math.round(c.actorWidth);
                actorHeight = (float) Math.round(c.actorHeight);
                float actorX = (float) Math.round(c.actorX);
                actorY = (height - ((float) Math.round(c.actorY))) - actorHeight;
                c.setActorBounds(actorX, actorY, actorWidth, actorHeight);
                actor = c.actor;
                if (actor != null) {
                    actor.setBounds(actorX, actorY, actorWidth, actorHeight);
                }
            }
        } else {
            n = cells.size;
            for (i = 0; i < n; i++) {
                c = (Cell) cells.get(i);
                actorHeight = c.actorHeight;
                actorY = (height - c.actorY) - actorHeight;
                c.setActorY(actorY);
                actor = c.actor;
                if (actor != null) {
                    actor.setBounds(c.actorX, actorY, c.actorWidth, actorHeight);
                }
            }
        }
        Array<Actor> children = getChildren();
        n = children.size;
        for (i = 0; i < n; i++) {
            Actor child = (Actor) children.get(i);
            if (child instanceof Layout) {
                ((Layout) child).validate();
            }
        }
    }

    private void computeSize() {
        int i;
        float hpadding;
        float vpadding;
        int nn;
        this.sizeInvalid = false;
        Array<Cell> cells = this.cells;
        int cellCount = cells.size;
        if (cellCount <= 0 || ((Cell) cells.peek()).endRow) {
            this.implicitEndRow = false;
        } else {
            endRow();
            this.implicitEndRow = true;
        }
        int columns = this.columns;
        int rows = this.rows;
        float[] columnMinWidth = ensureSize(this.columnMinWidth, columns);
        this.columnMinWidth = columnMinWidth;
        float[] rowMinHeight = ensureSize(this.rowMinHeight, rows);
        this.rowMinHeight = rowMinHeight;
        float[] columnPrefWidth = ensureSize(this.columnPrefWidth, columns);
        this.columnPrefWidth = columnPrefWidth;
        float[] rowPrefHeight = ensureSize(this.rowPrefHeight, rows);
        this.rowPrefHeight = rowPrefHeight;
        this.columnWidth = ensureSize(this.columnWidth, columns);
        this.rowHeight = ensureSize(this.rowHeight, rows);
        float[] expandWidth = ensureSize(this.expandWidth, columns);
        this.expandWidth = expandWidth;
        float[] expandHeight = ensureSize(this.expandHeight, rows);
        this.expandHeight = expandHeight;
        float spaceRightLast = 0.0f;
        for (i = 0; i < cellCount; i++) {
            float f;
            Cell c = (Cell) cells.get(i);
            int column = c.column;
            int row = c.row;
            int colspan = c.colspan.intValue();
            Actor a = c.actor;
            if (c.expandY.intValue() != 0 && expandHeight[row] == 0.0f) {
                expandHeight[row] = (float) c.expandY.intValue();
            }
            if (colspan == 1 && c.expandX.intValue() != 0 && expandWidth[column] == 0.0f) {
                expandWidth[column] = (float) c.expandX.intValue();
            }
            c.computedPadLeft = (column == 0 ? 0.0f : Math.max(0.0f, c.spaceLeft.get(a) - spaceRightLast)) + c.padLeft.get(a);
            c.computedPadTop = c.padTop.get(a);
            if (c.cellAboveIndex != -1) {
                c.computedPadTop += Math.max(0.0f, c.spaceTop.get(a) - ((Cell) cells.get(c.cellAboveIndex)).spaceBottom.get(a));
            }
            float spaceRight = c.spaceRight.get(a);
            float f2 = c.padRight.get(a);
            if (column + colspan == columns) {
                f = 0.0f;
            } else {
                f = spaceRight;
            }
            c.computedPadRight = f + f2;
            f2 = c.padBottom.get(a);
            if (row == rows - 1) {
                f = 0.0f;
            } else {
                f = c.spaceBottom.get(a);
            }
            c.computedPadBottom = f + f2;
            spaceRightLast = spaceRight;
            float prefWidth = c.prefWidth.get(a);
            float prefHeight = c.prefHeight.get(a);
            float minWidth = c.minWidth.get(a);
            float minHeight = c.minHeight.get(a);
            float maxWidth = c.maxWidth.get(a);
            float maxHeight = c.maxHeight.get(a);
            if (prefWidth < minWidth) {
                prefWidth = minWidth;
            }
            if (prefHeight < minHeight) {
                prefHeight = minHeight;
            }
            if (maxWidth > 0.0f && prefWidth > maxWidth) {
                prefWidth = maxWidth;
            }
            if (maxHeight > 0.0f && prefHeight > maxHeight) {
                prefHeight = maxHeight;
            }
            if (colspan == 1) {
                hpadding = c.computedPadLeft + c.computedPadRight;
                columnPrefWidth[column] = Math.max(columnPrefWidth[column], prefWidth + hpadding);
                columnMinWidth[column] = Math.max(columnMinWidth[column], minWidth + hpadding);
            }
            vpadding = c.computedPadTop + c.computedPadBottom;
            rowPrefHeight[row] = Math.max(rowPrefHeight[row], prefHeight + vpadding);
            rowMinHeight[row] = Math.max(rowMinHeight[row], minHeight + vpadding);
        }
        for (i = 0; i < cellCount; i++) {
            int ii;
            c = (Cell) cells.get(i);
            int expandX = c.expandX.intValue();
            if (expandX != 0) {
                column = c.column;
                nn = column + c.colspan.intValue();
                for (ii = column; ii < nn; ii++) {
                    if (expandWidth[ii] != 0.0f) {
                        break;
                    }
                }
                for (ii = column; ii < nn; ii++) {
                    expandWidth[ii] = (float) expandX;
                }
            }
        }
        float uniformMinWidth = 0.0f;
        float uniformMinHeight = 0.0f;
        float uniformPrefWidth = 0.0f;
        float uniformPrefHeight = 0.0f;
        for (i = 0; i < cellCount; i++) {
            c = (Cell) cells.get(i);
            if (c.uniformX == Boolean.TRUE && c.colspan.intValue() == 1) {
                hpadding = c.computedPadLeft + c.computedPadRight;
                uniformMinWidth = Math.max(uniformMinWidth, columnMinWidth[c.column] - hpadding);
                uniformPrefWidth = Math.max(uniformPrefWidth, columnPrefWidth[c.column] - hpadding);
            }
            if (c.uniformY == Boolean.TRUE) {
                vpadding = c.computedPadTop + c.computedPadBottom;
                uniformMinHeight = Math.max(uniformMinHeight, rowMinHeight[c.row] - vpadding);
                uniformPrefHeight = Math.max(uniformPrefHeight, rowPrefHeight[c.row] - vpadding);
            }
        }
        if (uniformPrefWidth > 0.0f || uniformPrefHeight > 0.0f) {
            for (i = 0; i < cellCount; i++) {
                c = (Cell) cells.get(i);
                if (uniformPrefWidth > 0.0f && c.uniformX == Boolean.TRUE && c.colspan.intValue() == 1) {
                    hpadding = c.computedPadLeft + c.computedPadRight;
                    columnMinWidth[c.column] = uniformMinWidth + hpadding;
                    columnPrefWidth[c.column] = uniformPrefWidth + hpadding;
                }
                if (uniformPrefHeight > 0.0f && c.uniformY == Boolean.TRUE) {
                    vpadding = c.computedPadTop + c.computedPadBottom;
                    rowMinHeight[c.row] = uniformMinHeight + vpadding;
                    rowPrefHeight[c.row] = uniformPrefHeight + vpadding;
                }
            }
        }
        for (i = 0; i < cellCount; i++) {
            c = (Cell) cells.get(i);
            colspan = c.colspan.intValue();
            if (colspan != 1) {
                column = c.column;
                a = c.actor;
                minWidth = c.minWidth.get(a);
                prefWidth = c.prefWidth.get(a);
                maxWidth = c.maxWidth.get(a);
                if (prefWidth < minWidth) {
                    prefWidth = minWidth;
                }
                if (maxWidth > 0.0f && prefWidth > maxWidth) {
                    prefWidth = maxWidth;
                }
                float spannedMinWidth = -(c.computedPadLeft + c.computedPadRight);
                float spannedPrefWidth = spannedMinWidth;
                ii = column;
                nn = ii + colspan;
                while (ii < nn) {
                    spannedMinWidth += columnMinWidth[ii];
                    spannedPrefWidth += columnPrefWidth[ii];
                    ii++;
                }
                float totalExpandWidth = 0.0f;
                ii = column;
                nn = ii + colspan;
                while (ii < nn) {
                    totalExpandWidth += expandWidth[ii];
                    ii++;
                }
                float extraMinWidth = Math.max(0.0f, minWidth - spannedMinWidth);
                float extraPrefWidth = Math.max(0.0f, prefWidth - spannedPrefWidth);
                ii = column;
                nn = ii + colspan;
                while (ii < nn) {
                    float ratio = totalExpandWidth == 0.0f ? 1.0f / ((float) colspan) : expandWidth[ii] / totalExpandWidth;
                    columnMinWidth[ii] = columnMinWidth[ii] + (extraMinWidth * ratio);
                    columnPrefWidth[ii] = columnPrefWidth[ii] + (extraPrefWidth * ratio);
                    ii++;
                }
            }
        }
        this.tableMinWidth = 0.0f;
        this.tableMinHeight = 0.0f;
        this.tablePrefWidth = 0.0f;
        this.tablePrefHeight = 0.0f;
        for (i = 0; i < columns; i++) {
            this.tableMinWidth += columnMinWidth[i];
            this.tablePrefWidth += columnPrefWidth[i];
        }
        for (i = 0; i < rows; i++) {
            this.tableMinHeight += rowMinHeight[i];
            this.tablePrefHeight += Math.max(rowMinHeight[i], rowPrefHeight[i]);
        }
        hpadding = this.padLeft.get(this) + this.padRight.get(this);
        vpadding = this.padTop.get(this) + this.padBottom.get(this);
        this.tableMinWidth += hpadding;
        this.tableMinHeight += vpadding;
        this.tablePrefWidth = Math.max(this.tablePrefWidth + hpadding, this.tableMinWidth);
        this.tablePrefHeight = Math.max(this.tablePrefHeight + vpadding, this.tableMinHeight);
    }

    private void layout(float layoutX, float layoutY, float layoutWidth, float layoutHeight) {
        int i;
        float[] columnWeightedWidth;
        float extraWidth;
        float[] rowWeightedHeight;
        float extra;
        float used;
        int lastIndex;
        Array<Cell> cells = this.cells;
        int cellCount = cells.size;
        if (this.sizeInvalid) {
            computeSize();
        }
        float padLeft = this.padLeft.get(this);
        float hpadding = padLeft + this.padRight.get(this);
        float padTop = this.padTop.get(this);
        float vpadding = padTop + this.padBottom.get(this);
        int columns = this.columns;
        int rows = this.rows;
        float[] expandWidth = this.expandWidth;
        float[] expandHeight = this.expandHeight;
        float[] columnWidth = this.columnWidth;
        float[] rowHeight = this.rowHeight;
        float totalExpandWidth = 0.0f;
        float totalExpandHeight = 0.0f;
        for (i = 0; i < columns; i++) {
            totalExpandWidth += expandWidth[i];
        }
        for (i = 0; i < rows; i++) {
            totalExpandHeight += expandHeight[i];
        }
        float totalGrowWidth = this.tablePrefWidth - this.tableMinWidth;
        if (totalGrowWidth == 0.0f) {
            columnWeightedWidth = this.columnMinWidth;
        } else {
            extraWidth = Math.min(totalGrowWidth, Math.max(0.0f, layoutWidth - this.tableMinWidth));
            columnWeightedWidth = ensureSize(columnWeightedWidth, columns);
            columnWeightedWidth = columnWeightedWidth;
            float[] columnMinWidth = this.columnMinWidth;
            float[] columnPrefWidth = this.columnPrefWidth;
            for (i = 0; i < columns; i++) {
                columnWeightedWidth[i] = columnMinWidth[i] + (extraWidth * ((columnPrefWidth[i] - columnMinWidth[i]) / totalGrowWidth));
            }
        }
        float totalGrowHeight = this.tablePrefHeight - this.tableMinHeight;
        if (totalGrowHeight == 0.0f) {
            rowWeightedHeight = this.rowMinHeight;
        } else {
            rowWeightedHeight = ensureSize(rowWeightedHeight, rows);
            rowWeightedHeight = rowWeightedHeight;
            float extraHeight = Math.min(totalGrowHeight, Math.max(0.0f, layoutHeight - this.tableMinHeight));
            float[] rowMinHeight = this.rowMinHeight;
            float[] rowPrefHeight = this.rowPrefHeight;
            for (i = 0; i < rows; i++) {
                rowWeightedHeight[i] = rowMinHeight[i] + (extraHeight * ((rowPrefHeight[i] - rowMinHeight[i]) / totalGrowHeight));
            }
        }
        for (i = 0; i < cellCount; i++) {
            Cell c = (Cell) cells.get(i);
            int column = c.column;
            int row = c.row;
            Actor a = c.actor;
            float spannedWeightedWidth = 0.0f;
            int colspan = c.colspan.intValue();
            int ii = column;
            int nn = ii + colspan;
            while (ii < nn) {
                spannedWeightedWidth += columnWeightedWidth[ii];
                ii++;
            }
            float weightedHeight = rowWeightedHeight[row];
            float prefWidth = c.prefWidth.get(a);
            float prefHeight = c.prefHeight.get(a);
            float minWidth = c.minWidth.get(a);
            float minHeight = c.minHeight.get(a);
            float maxWidth = c.maxWidth.get(a);
            float maxHeight = c.maxHeight.get(a);
            if (prefWidth < minWidth) {
                prefWidth = minWidth;
            }
            if (prefHeight < minHeight) {
                prefHeight = minHeight;
            }
            if (maxWidth > 0.0f && prefWidth > maxWidth) {
                prefWidth = maxWidth;
            }
            if (maxHeight > 0.0f && prefHeight > maxHeight) {
                prefHeight = maxHeight;
            }
            c.actorWidth = Math.min((spannedWeightedWidth - c.computedPadLeft) - c.computedPadRight, prefWidth);
            c.actorHeight = Math.min((weightedHeight - c.computedPadTop) - c.computedPadBottom, prefHeight);
            if (colspan == 1) {
                columnWidth[column] = Math.max(columnWidth[column], spannedWeightedWidth);
            }
            rowHeight[row] = Math.max(rowHeight[row], weightedHeight);
        }
        if (totalExpandWidth > 0.0f) {
            extra = layoutWidth - hpadding;
            for (i = 0; i < columns; i++) {
                extra -= columnWidth[i];
            }
            used = 0.0f;
            lastIndex = 0;
            for (i = 0; i < columns; i++) {
                if (expandWidth[i] != 0.0f) {
                    float amount = (expandWidth[i] * extra) / totalExpandWidth;
                    columnWidth[i] = columnWidth[i] + amount;
                    used += amount;
                    lastIndex = i;
                }
            }
            columnWidth[lastIndex] = columnWidth[lastIndex] + (extra - used);
        }
        if (totalExpandHeight > 0.0f) {
            extra = layoutHeight - vpadding;
            for (i = 0; i < rows; i++) {
                extra -= rowHeight[i];
            }
            used = 0.0f;
            lastIndex = 0;
            for (i = 0; i < rows; i++) {
                if (expandHeight[i] != 0.0f) {
                    amount = (expandHeight[i] * extra) / totalExpandHeight;
                    rowHeight[i] = rowHeight[i] + amount;
                    used += amount;
                    lastIndex = i;
                }
            }
            rowHeight[lastIndex] = rowHeight[lastIndex] + (extra - used);
        }
        for (i = 0; i < cellCount; i++) {
            c = (Cell) cells.get(i);
            colspan = c.colspan.intValue();
            if (colspan != 1) {
                extraWidth = 0.0f;
                column = c.column;
                nn = column + colspan;
                while (column < nn) {
                    extraWidth += columnWeightedWidth[column] - columnWidth[column];
                    column++;
                }
                extraWidth = (extraWidth - Math.max(0.0f, c.computedPadLeft + c.computedPadRight)) / ((float) colspan);
                if (extraWidth > 0.0f) {
                    column = c.column;
                    nn = column + colspan;
                    while (column < nn) {
                        columnWidth[column] = columnWidth[column] + extraWidth;
                        column++;
                    }
                }
            }
        }
        float tableWidth = hpadding;
        float tableHeight = vpadding;
        for (i = 0; i < columns; i++) {
            tableWidth += columnWidth[i];
        }
        for (i = 0; i < rows; i++) {
            tableHeight += rowHeight[i];
        }
        int align = this.align;
        float x = layoutX + padLeft;
        if ((align & 16) != 0) {
            x += layoutWidth - tableWidth;
        } else if ((align & 8) == 0) {
            x += (layoutWidth - tableWidth) / 2.0f;
        }
        float y = layoutY + padTop;
        if ((align & 4) != 0) {
            y += layoutHeight - tableHeight;
        } else if ((align & 2) == 0) {
            y += (layoutHeight - tableHeight) / 2.0f;
        }
        float currentX = x;
        float currentY = y;
        for (i = 0; i < cellCount; i++) {
            c = (Cell) cells.get(i);
            float spannedCellWidth = 0.0f;
            column = c.column;
            nn = column + c.colspan.intValue();
            while (column < nn) {
                spannedCellWidth += columnWidth[column];
                column++;
            }
            spannedCellWidth -= c.computedPadLeft + c.computedPadRight;
            currentX += c.computedPadLeft;
            float fillX = c.fillX.floatValue();
            float fillY = c.fillY.floatValue();
            if (fillX > 0.0f) {
                c.actorWidth = Math.max(spannedCellWidth * fillX, c.minWidth.get(c.actor));
                maxWidth = c.maxWidth.get(c.actor);
                if (maxWidth > 0.0f) {
                    c.actorWidth = Math.min(c.actorWidth, maxWidth);
                }
            }
            if (fillY > 0.0f) {
                c.actorHeight = Math.max(((rowHeight[c.row] * fillY) - c.computedPadTop) - c.computedPadBottom, c.minHeight.get(c.actor));
                maxHeight = c.maxHeight.get(c.actor);
                if (maxHeight > 0.0f) {
                    c.actorHeight = Math.min(c.actorHeight, maxHeight);
                }
            }
            align = c.align.intValue();
            if ((align & 8) != 0) {
                c.actorX = currentX;
            } else if ((align & 16) != 0) {
                c.actorX = (currentX + spannedCellWidth) - c.actorWidth;
            } else {
                c.actorX = ((spannedCellWidth - c.actorWidth) / 2.0f) + currentX;
            }
            if ((align & 2) != 0) {
                c.actorY = c.computedPadTop + currentY;
            } else if ((align & 4) != 0) {
                c.actorY = ((rowHeight[c.row] + currentY) - c.actorHeight) - c.computedPadBottom;
            } else {
                c.actorY = ((((rowHeight[c.row] - c.actorHeight) + c.computedPadTop) - c.computedPadBottom) / 2.0f) + currentY;
            }
            if (c.endRow) {
                currentX = x;
                currentY += rowHeight[c.row];
            } else {
                currentX += c.computedPadRight + spannedCellWidth;
            }
        }
        if (this.debug != Debug.none) {
            clearDebugRects();
            float currentX2 = x;
            currentY = y;
            if (this.debug == Debug.table || this.debug == Debug.all) {
                addDebugRect(layoutX, layoutY, layoutWidth, layoutHeight, debugTableColor);
                addDebugRect(x, y, tableWidth - hpadding, tableHeight - vpadding, debugTableColor);
            }
            i = 0;
            while (i < cellCount) {
                c = (Cell) cells.get(i);
                if (this.debug == Debug.actor || this.debug == Debug.all) {
                    addDebugRect(c.actorX, c.actorY, c.actorWidth, c.actorHeight, debugActorColor);
                }
                spannedCellWidth = 0.0f;
                column = c.column;
                nn = column + c.colspan.intValue();
                while (column < nn) {
                    spannedCellWidth += columnWidth[column];
                    column++;
                }
                spannedCellWidth -= c.computedPadLeft + c.computedPadRight;
                currentX = currentX2 + c.computedPadLeft;
                if (this.debug == Debug.cell || this.debug == Debug.all) {
                    addDebugRect(currentX, currentY + c.computedPadTop, spannedCellWidth, (rowHeight[c.row] - c.computedPadTop) - c.computedPadBottom, debugCellColor);
                }
                if (c.endRow) {
                    currentX = x;
                    currentY += rowHeight[c.row];
                } else {
                    currentX += c.computedPadRight + spannedCellWidth;
                }
                i++;
                currentX2 = currentX;
            }
        }
    }

    private void clearDebugRects() {
        if (this.debugRects != null) {
            DebugRect.pool.freeAll(this.debugRects);
            this.debugRects.clear();
        }
    }

    private void addDebugRect(float x, float y, float w, float h, Color color) {
        if (this.debugRects == null) {
            this.debugRects = new Array();
        }
        DebugRect rect = (DebugRect) DebugRect.pool.obtain();
        rect.color = color;
        rect.set(x, (getHeight() - y) - h, w, h);
        this.debugRects.add(rect);
    }

    public void drawDebug(ShapeRenderer shapes) {
        if (isTransform()) {
            applyTransform(shapes, computeTransform());
            drawDebugRects(shapes);
            if (this.clip) {
                shapes.flush();
                float x = 0.0f;
                float y = 0.0f;
                float width = getWidth();
                float height = getHeight();
                if (this.background != null) {
                    x = this.padLeft.get(this);
                    y = this.padBottom.get(this);
                    width -= this.padRight.get(this) + x;
                    height -= this.padTop.get(this) + y;
                }
                if (clipBegin(x, y, width, height)) {
                    drawDebugChildren(shapes);
                    clipEnd();
                }
            } else {
                drawDebugChildren(shapes);
            }
            resetTransform(shapes);
            return;
        }
        drawDebugRects(shapes);
        super.drawDebug(shapes);
    }

    protected void drawDebugBounds(ShapeRenderer shapes) {
    }

    private void drawDebugRects(ShapeRenderer shapes) {
        if (this.debugRects != null && getDebug()) {
            shapes.set(ShapeType.Line);
            shapes.setColor(getStage().getDebugColor());
            float x = 0.0f;
            float y = 0.0f;
            if (!isTransform()) {
                x = getX();
                y = getY();
            }
            int n = this.debugRects.size;
            for (int i = 0; i < n; i++) {
                DebugRect debugRect = (DebugRect) this.debugRects.get(i);
                shapes.setColor(debugRect.color);
                shapes.rect(debugRect.x + x, debugRect.y + y, debugRect.width, debugRect.height);
            }
        }
    }

    public Skin getSkin() {
        return this.skin;
    }
}
