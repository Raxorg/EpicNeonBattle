package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Window extends Table {
    private static final int MOVE = 32;
    private static final Vector2 tmpPosition = new Vector2();
    private static final Vector2 tmpSize = new Vector2();
    boolean dragging;
    boolean drawTitleTable;
    boolean isModal;
    boolean isMovable;
    boolean isResizable;
    boolean keepWithinStage;
    int resizeBorder;
    private WindowStyle style;
    Label titleLabel;
    Table titleTable;

    public static class WindowStyle {
        public Drawable background;
        public Drawable stageBackground;
        public BitmapFont titleFont;
        public Color titleFontColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);

        public WindowStyle(BitmapFont titleFont, Color titleFontColor, Drawable background) {
            this.background = background;
            this.titleFont = titleFont;
            this.titleFontColor.set(titleFontColor);
        }

        public WindowStyle(WindowStyle style) {
            this.background = style.background;
            this.titleFont = style.titleFont;
            this.titleFontColor = new Color(style.titleFontColor);
        }
    }

    public Window(String title, Skin skin) {
        this(title, (WindowStyle) skin.get(WindowStyle.class));
        setSkin(skin);
    }

    public Window(String title, Skin skin, String styleName) {
        this(title, (WindowStyle) skin.get(styleName, WindowStyle.class));
        setSkin(skin);
    }

    public Window(String title, WindowStyle style) {
        this.isMovable = true;
        this.resizeBorder = 8;
        this.keepWithinStage = true;
        if (title == null) {
            throw new IllegalArgumentException("title cannot be null.");
        }
        setTouchable(Touchable.enabled);
        setClip(true);
        this.titleLabel = new Label((CharSequence) title, new LabelStyle(style.titleFont, style.titleFontColor));
        this.titleLabel.setEllipsis(true);
        this.titleTable = new Table() {
            public void draw(Batch batch, float parentAlpha) {
                if (Window.this.drawTitleTable) {
                    super.draw(batch, parentAlpha);
                }
            }
        };
        this.titleTable.add(this.titleLabel).expandX().fillX().minWidth(0.0f);
        addActor(this.titleTable);
        setStyle(style);
        setWidth(150.0f);
        setHeight(150.0f);
        addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Window.this.toFront();
                return false;
            }
        });
        addListener(new InputListener() {
            int edge;
            float lastX;
            float lastY;
            float startX;
            float startY;

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 0) {
                    boolean z;
                    int border = Window.this.resizeBorder;
                    float width = Window.this.getWidth();
                    float height = Window.this.getHeight();
                    this.edge = 0;
                    if (Window.this.isResizable && x >= 0.0f && x < width && y >= 0.0f && y < height) {
                        if (x < ((float) border)) {
                            this.edge |= 8;
                        }
                        if (x > width - ((float) border)) {
                            this.edge |= 16;
                        }
                        if (y < ((float) border)) {
                            this.edge |= 4;
                        }
                        if (y > height - ((float) border)) {
                            this.edge |= 2;
                        }
                        if (this.edge != 0) {
                            border += 25;
                        }
                        if (x < ((float) border)) {
                            this.edge |= 8;
                        }
                        if (x > width - ((float) border)) {
                            this.edge |= 16;
                        }
                        if (y < ((float) border)) {
                            this.edge |= 4;
                        }
                        if (y > height - ((float) border)) {
                            this.edge |= 2;
                        }
                    }
                    if (Window.this.isMovable && this.edge == 0 && y <= height && y >= height - Window.this.getPadTop() && x >= 0.0f && x <= width) {
                        this.edge = Window.MOVE;
                    }
                    Window window = Window.this;
                    if (this.edge != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    window.dragging = z;
                    this.startX = x;
                    this.startY = y;
                    this.lastX = x;
                    this.lastY = y;
                }
                if (this.edge != 0 || Window.this.isModal) {
                    return true;
                }
                return false;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Window.this.dragging = false;
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (Window.this.dragging) {
                    float amountX;
                    float amountY;
                    float width = Window.this.getWidth();
                    float height = Window.this.getHeight();
                    float windowX = Window.this.getX();
                    float windowY = Window.this.getY();
                    float minWidth = Window.this.getMinWidth();
                    float maxWidth = Window.this.getMaxWidth();
                    float minHeight = Window.this.getMinHeight();
                    float maxHeight = Window.this.getMaxHeight();
                    Stage stage = Window.this.getStage();
                    boolean clampPosition = Window.this.keepWithinStage && Window.this.getParent() == stage.getRoot();
                    if ((this.edge & Window.MOVE) != 0) {
                        windowX += x - this.startX;
                        windowY += y - this.startY;
                    }
                    if ((this.edge & 8) != 0) {
                        amountX = x - this.startX;
                        if (width - amountX < minWidth) {
                            amountX = -(minWidth - width);
                        }
                        if (clampPosition && windowX + amountX < 0.0f) {
                            amountX = -windowX;
                        }
                        width -= amountX;
                        windowX += amountX;
                    }
                    if ((this.edge & 4) != 0) {
                        amountY = y - this.startY;
                        if (height - amountY < minHeight) {
                            amountY = -(minHeight - height);
                        }
                        if (clampPosition && windowY + amountY < 0.0f) {
                            amountY = -windowY;
                        }
                        height -= amountY;
                        windowY += amountY;
                    }
                    if ((this.edge & 16) != 0) {
                        amountX = x - this.lastX;
                        if (width + amountX < minWidth) {
                            amountX = minWidth - width;
                        }
                        if (clampPosition && (windowX + width) + amountX > stage.getWidth()) {
                            amountX = (stage.getWidth() - windowX) - width;
                        }
                        width += amountX;
                    }
                    if ((this.edge & 2) != 0) {
                        amountY = y - this.lastY;
                        if (height + amountY < minHeight) {
                            amountY = minHeight - height;
                        }
                        if (clampPosition && (windowY + height) + amountY > stage.getHeight()) {
                            amountY = (stage.getHeight() - windowY) - height;
                        }
                        height += amountY;
                    }
                    this.lastX = x;
                    this.lastY = y;
                    Window.this.setBounds((float) Math.round(windowX), (float) Math.round(windowY), (float) Math.round(width), (float) Math.round(height));
                }
            }

            public boolean mouseMoved(InputEvent event, float x, float y) {
                return Window.this.isModal;
            }

            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                return Window.this.isModal;
            }

            public boolean keyDown(InputEvent event, int keycode) {
                return Window.this.isModal;
            }

            public boolean keyUp(InputEvent event, int keycode) {
                return Window.this.isModal;
            }

            public boolean keyTyped(InputEvent event, char character) {
                return Window.this.isModal;
            }
        });
    }

    public void setStyle(WindowStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null.");
        }
        this.style = style;
        setBackground(style.background);
        this.titleLabel.setStyle(new LabelStyle(style.titleFont, style.titleFontColor));
        invalidateHierarchy();
    }

    public WindowStyle getStyle() {
        return this.style;
    }

    void keepWithinStage() {
        if (this.keepWithinStage) {
            Stage stage = getStage();
            Camera camera = stage.getCamera();
            float parentWidth;
            float parentHeight;
            if (camera instanceof OrthographicCamera) {
                OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
                parentWidth = stage.getWidth();
                parentHeight = stage.getHeight();
                if (getX(16) - camera.position.x > (parentWidth / 2.0f) / orthographicCamera.zoom) {
                    setPosition(camera.position.x + ((parentWidth / 2.0f) / orthographicCamera.zoom), getY(16), 16);
                }
                if (getX(8) - camera.position.x < ((-parentWidth) / 2.0f) / orthographicCamera.zoom) {
                    setPosition(camera.position.x - ((parentWidth / 2.0f) / orthographicCamera.zoom), getY(8), 8);
                }
                if (getY(2) - camera.position.y > (parentHeight / 2.0f) / orthographicCamera.zoom) {
                    setPosition(getX(2), camera.position.y + ((parentHeight / 2.0f) / orthographicCamera.zoom), 2);
                }
                if (getY(4) - camera.position.y < ((-parentHeight) / 2.0f) / orthographicCamera.zoom) {
                    setPosition(getX(4), camera.position.y - ((parentHeight / 2.0f) / orthographicCamera.zoom), 4);
                }
            } else if (getParent() == stage.getRoot()) {
                parentWidth = stage.getWidth();
                parentHeight = stage.getHeight();
                if (getX() < 0.0f) {
                    setX(0.0f);
                }
                if (getRight() > parentWidth) {
                    setX(parentWidth - getWidth());
                }
                if (getY() < 0.0f) {
                    setY(0.0f);
                }
                if (getTop() > parentHeight) {
                    setY(parentHeight - getHeight());
                }
            }
        }
    }

    public void draw(Batch batch, float parentAlpha) {
        Stage stage = getStage();
        if (stage.getKeyboardFocus() == null) {
            stage.setKeyboardFocus(this);
        }
        keepWithinStage();
        if (this.style.stageBackground != null) {
            stageToLocalCoordinates(tmpPosition.set(0.0f, 0.0f));
            stageToLocalCoordinates(tmpSize.set(stage.getWidth(), stage.getHeight()));
            drawStageBackground(batch, parentAlpha, getX() + tmpPosition.x, getY() + tmpPosition.y, getX() + tmpSize.x, getY() + tmpSize.y);
        }
        super.draw(batch, parentAlpha);
    }

    protected void drawStageBackground(Batch batch, float parentAlpha, float x, float y, float width, float height) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        this.style.stageBackground.draw(batch, x, y, width, height);
    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        super.drawBackground(batch, parentAlpha, x, y);
        this.titleTable.getColor().a = getColor().a;
        float padTop = getPadTop();
        float padLeft = getPadLeft();
        this.titleTable.setSize((getWidth() - padLeft) - getPadRight(), padTop);
        this.titleTable.setPosition(padLeft, getHeight() - padTop);
        this.drawTitleTable = true;
        this.titleTable.draw(batch, parentAlpha);
        this.drawTitleTable = false;
    }

    public Actor hit(float x, float y, boolean touchable) {
        Actor hit = super.hit(x, y, touchable);
        if (hit == null && this.isModal && (!touchable || getTouchable() == Touchable.enabled)) {
            return this;
        }
        float height = getHeight();
        if (hit == null || hit == this) {
            return hit;
        }
        if (y <= height && y >= height - getPadTop() && x >= 0.0f && x <= getWidth()) {
            Actor current = hit;
            while (current.getParent() != this) {
                current = current.getParent();
            }
            if (getCell(current) != null) {
                return this;
            }
        }
        return hit;
    }

    public boolean isMovable() {
        return this.isMovable;
    }

    public void setMovable(boolean isMovable) {
        this.isMovable = isMovable;
    }

    public boolean isModal() {
        return this.isModal;
    }

    public void setModal(boolean isModal) {
        this.isModal = isModal;
    }

    public void setKeepWithinStage(boolean keepWithinStage) {
        this.keepWithinStage = keepWithinStage;
    }

    public boolean isResizable() {
        return this.isResizable;
    }

    public void setResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }

    public void setResizeBorder(int resizeBorder) {
        this.resizeBorder = resizeBorder;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), (this.titleLabel.getPrefWidth() + getPadLeft()) + getPadRight());
    }

    public Table getTitleTable() {
        return this.titleTable;
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }
}
