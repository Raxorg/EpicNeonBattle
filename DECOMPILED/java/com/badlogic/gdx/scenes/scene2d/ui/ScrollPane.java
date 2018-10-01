package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

public class ScrollPane extends WidgetGroup {
    float amountX;
    float amountY;
    float areaHeight;
    float areaWidth;
    boolean cancelTouchFocus;
    private boolean clamp;
    private boolean disableX;
    private boolean disableY;
    int draggingPointer;
    float fadeAlpha;
    float fadeAlphaSeconds;
    float fadeDelay;
    float fadeDelaySeconds;
    private boolean fadeScrollBars;
    boolean flickScroll;
    private ActorGestureListener flickScrollListener;
    float flingTime;
    float flingTimer;
    private boolean forceScrollX;
    private boolean forceScrollY;
    final Rectangle hKnobBounds;
    final Rectangle hScrollBounds;
    boolean hScrollOnBottom;
    final Vector2 lastPoint;
    float maxX;
    float maxY;
    private float overscrollDistance;
    private float overscrollSpeedMax;
    private float overscrollSpeedMin;
    private boolean overscrollX;
    private boolean overscrollY;
    private final Rectangle scissorBounds;
    boolean scrollX;
    boolean scrollY;
    private boolean scrollbarsOnTop;
    private boolean smoothScrolling;
    private ScrollPaneStyle style;
    boolean touchScrollH;
    boolean touchScrollV;
    final Rectangle vKnobBounds;
    final Rectangle vScrollBounds;
    boolean vScrollOnRight;
    private boolean variableSizeKnobs;
    float velocityX;
    float velocityY;
    float visualAmountX;
    float visualAmountY;
    private Actor widget;
    private final Rectangle widgetAreaBounds;
    private final Rectangle widgetCullingArea;

    public static class ScrollPaneStyle {
        public Drawable background;
        public Drawable corner;
        public Drawable hScroll;
        public Drawable hScrollKnob;
        public Drawable vScroll;
        public Drawable vScrollKnob;

        public ScrollPaneStyle(Drawable background, Drawable hScroll, Drawable hScrollKnob, Drawable vScroll, Drawable vScrollKnob) {
            this.background = background;
            this.hScroll = hScroll;
            this.hScrollKnob = hScrollKnob;
            this.vScroll = vScroll;
            this.vScrollKnob = vScrollKnob;
        }

        public ScrollPaneStyle(ScrollPaneStyle style) {
            this.background = style.background;
            this.hScroll = style.hScroll;
            this.hScrollKnob = style.hScrollKnob;
            this.vScroll = style.vScroll;
            this.vScrollKnob = style.vScrollKnob;
        }
    }

    public ScrollPane(Actor widget) {
        this(widget, new ScrollPaneStyle());
    }

    public ScrollPane(Actor widget, Skin skin) {
        this(widget, (ScrollPaneStyle) skin.get(ScrollPaneStyle.class));
    }

    public ScrollPane(Actor widget, Skin skin, String styleName) {
        this(widget, (ScrollPaneStyle) skin.get(styleName, ScrollPaneStyle.class));
    }

    public ScrollPane(Actor widget, ScrollPaneStyle style) {
        this.hScrollBounds = new Rectangle();
        this.vScrollBounds = new Rectangle();
        this.hKnobBounds = new Rectangle();
        this.vKnobBounds = new Rectangle();
        this.widgetAreaBounds = new Rectangle();
        this.widgetCullingArea = new Rectangle();
        this.scissorBounds = new Rectangle();
        this.vScrollOnRight = true;
        this.hScrollOnBottom = true;
        this.lastPoint = new Vector2();
        this.fadeScrollBars = true;
        this.smoothScrolling = true;
        this.fadeAlphaSeconds = 1.0f;
        this.fadeDelaySeconds = 1.0f;
        this.cancelTouchFocus = true;
        this.flickScroll = true;
        this.overscrollX = true;
        this.overscrollY = true;
        this.flingTime = 1.0f;
        this.overscrollDistance = 50.0f;
        this.overscrollSpeedMin = 30.0f;
        this.overscrollSpeedMax = 200.0f;
        this.clamp = true;
        this.variableSizeKnobs = true;
        this.draggingPointer = -1;
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null.");
        }
        this.style = style;
        setWidget(widget);
        setSize(150.0f, 150.0f);
        addCaptureListener(new InputListener() {
            private float handlePosition;

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                int i = -1;
                if (ScrollPane.this.draggingPointer != -1) {
                    return false;
                }
                if (pointer == 0 && button != 0) {
                    return false;
                }
                ScrollPane.this.getStage().setScrollFocus(ScrollPane.this);
                if (!ScrollPane.this.flickScroll) {
                    ScrollPane.this.resetFade();
                }
                if (ScrollPane.this.fadeAlpha == 0.0f) {
                    return false;
                }
                ScrollPane scrollPane;
                float f;
                float f2;
                if (ScrollPane.this.scrollX && ScrollPane.this.hScrollBounds.contains(x, y)) {
                    event.stop();
                    ScrollPane.this.resetFade();
                    if (ScrollPane.this.hKnobBounds.contains(x, y)) {
                        ScrollPane.this.lastPoint.set(x, y);
                        this.handlePosition = ScrollPane.this.hKnobBounds.x;
                        ScrollPane.this.touchScrollH = true;
                        ScrollPane.this.draggingPointer = pointer;
                        return true;
                    }
                    scrollPane = ScrollPane.this;
                    f = ScrollPane.this.amountX;
                    f2 = ScrollPane.this.areaWidth;
                    if (x >= ScrollPane.this.hKnobBounds.x) {
                        i = 1;
                    }
                    scrollPane.setScrollX((((float) i) * f2) + f);
                    return true;
                } else if (!ScrollPane.this.scrollY || !ScrollPane.this.vScrollBounds.contains(x, y)) {
                    return false;
                } else {
                    event.stop();
                    ScrollPane.this.resetFade();
                    if (ScrollPane.this.vKnobBounds.contains(x, y)) {
                        ScrollPane.this.lastPoint.set(x, y);
                        this.handlePosition = ScrollPane.this.vKnobBounds.y;
                        ScrollPane.this.touchScrollV = true;
                        ScrollPane.this.draggingPointer = pointer;
                        return true;
                    }
                    scrollPane = ScrollPane.this;
                    f = ScrollPane.this.amountY;
                    f2 = ScrollPane.this.areaHeight;
                    if (y < ScrollPane.this.vKnobBounds.y) {
                        i = 1;
                    }
                    scrollPane.setScrollY((((float) i) * f2) + f);
                    return true;
                }
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == ScrollPane.this.draggingPointer) {
                    ScrollPane.this.cancel();
                }
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (pointer == ScrollPane.this.draggingPointer) {
                    float total;
                    if (ScrollPane.this.touchScrollH) {
                        float scrollH = this.handlePosition + (x - ScrollPane.this.lastPoint.x);
                        this.handlePosition = scrollH;
                        scrollH = Math.min((ScrollPane.this.hScrollBounds.x + ScrollPane.this.hScrollBounds.width) - ScrollPane.this.hKnobBounds.width, Math.max(ScrollPane.this.hScrollBounds.x, scrollH));
                        total = ScrollPane.this.hScrollBounds.width - ScrollPane.this.hKnobBounds.width;
                        if (total != 0.0f) {
                            ScrollPane.this.setScrollPercentX((scrollH - ScrollPane.this.hScrollBounds.x) / total);
                        }
                        ScrollPane.this.lastPoint.set(x, y);
                    } else if (ScrollPane.this.touchScrollV) {
                        float scrollV = this.handlePosition + (y - ScrollPane.this.lastPoint.y);
                        this.handlePosition = scrollV;
                        scrollV = Math.min((ScrollPane.this.vScrollBounds.y + ScrollPane.this.vScrollBounds.height) - ScrollPane.this.vKnobBounds.height, Math.max(ScrollPane.this.vScrollBounds.y, scrollV));
                        total = ScrollPane.this.vScrollBounds.height - ScrollPane.this.vKnobBounds.height;
                        if (total != 0.0f) {
                            ScrollPane.this.setScrollPercentY(1.0f - ((scrollV - ScrollPane.this.vScrollBounds.y) / total));
                        }
                        ScrollPane.this.lastPoint.set(x, y);
                    }
                }
            }

            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (!ScrollPane.this.flickScroll) {
                    ScrollPane.this.resetFade();
                }
                return false;
            }
        });
        this.flickScrollListener = new ActorGestureListener() {
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                ScrollPane.this.resetFade();
                ScrollPane scrollPane = ScrollPane.this;
                scrollPane.amountX -= deltaX;
                scrollPane = ScrollPane.this;
                scrollPane.amountY += deltaY;
                ScrollPane.this.clamp();
                if (ScrollPane.this.cancelTouchFocus) {
                    ScrollPane.this.cancelTouchFocus();
                }
            }

            public void fling(InputEvent event, float x, float y, int button) {
                if (Math.abs(x) > 150.0f) {
                    ScrollPane.this.flingTimer = ScrollPane.this.flingTime;
                    ScrollPane.this.velocityX = x;
                    if (ScrollPane.this.cancelTouchFocus) {
                        ScrollPane.this.cancelTouchFocus();
                    }
                }
                if (Math.abs(y) > 150.0f) {
                    ScrollPane.this.flingTimer = ScrollPane.this.flingTime;
                    ScrollPane.this.velocityY = -y;
                    if (ScrollPane.this.cancelTouchFocus) {
                        ScrollPane.this.cancelTouchFocus();
                    }
                }
            }

            public boolean handle(Event event) {
                if (!super.handle(event)) {
                    return false;
                }
                if (((InputEvent) event).getType() == Type.touchDown) {
                    ScrollPane.this.flingTimer = 0.0f;
                }
                return true;
            }
        };
        addListener(this.flickScrollListener);
        addListener(new InputListener() {
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                ScrollPane.this.resetFade();
                if (ScrollPane.this.scrollY) {
                    ScrollPane.this.setScrollY(ScrollPane.this.amountY + (ScrollPane.this.getMouseWheelY() * ((float) amount)));
                } else if (!ScrollPane.this.scrollX) {
                    return false;
                } else {
                    ScrollPane.this.setScrollX(ScrollPane.this.amountX + (ScrollPane.this.getMouseWheelX() * ((float) amount)));
                }
                return true;
            }
        });
    }

    void resetFade() {
        this.fadeAlpha = this.fadeAlphaSeconds;
        this.fadeDelay = this.fadeDelaySeconds;
    }

    public void cancelTouchFocus() {
        Stage stage = getStage();
        if (stage != null) {
            stage.cancelTouchFocusExcept(this.flickScrollListener, this);
        }
    }

    public void cancel() {
        this.draggingPointer = -1;
        this.touchScrollH = false;
        this.touchScrollV = false;
        this.flickScrollListener.getGestureDetector().cancel();
    }

    void clamp() {
        if (this.clamp) {
            scrollX(this.overscrollX ? MathUtils.clamp(this.amountX, -this.overscrollDistance, this.maxX + this.overscrollDistance) : MathUtils.clamp(this.amountX, 0.0f, this.maxX));
            scrollY(this.overscrollY ? MathUtils.clamp(this.amountY, -this.overscrollDistance, this.maxY + this.overscrollDistance) : MathUtils.clamp(this.amountY, 0.0f, this.maxY));
        }
    }

    public void setStyle(ScrollPaneStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null.");
        }
        this.style = style;
        invalidateHierarchy();
    }

    public ScrollPaneStyle getStyle() {
        return this.style;
    }

    public void act(float delta) {
        super.act(delta);
        boolean panning = this.flickScrollListener.getGestureDetector().isPanning();
        boolean animating = false;
        if (!(this.fadeAlpha <= 0.0f || !this.fadeScrollBars || panning || this.touchScrollH || this.touchScrollV)) {
            this.fadeDelay -= delta;
            if (this.fadeDelay <= 0.0f) {
                this.fadeAlpha = Math.max(0.0f, this.fadeAlpha - delta);
            }
            animating = true;
        }
        if (this.flingTimer > 0.0f) {
            resetFade();
            float alpha = this.flingTimer / this.flingTime;
            this.amountX -= (this.velocityX * alpha) * delta;
            this.amountY -= (this.velocityY * alpha) * delta;
            clamp();
            if (this.amountX == (-this.overscrollDistance)) {
                this.velocityX = 0.0f;
            }
            if (this.amountX >= this.maxX + this.overscrollDistance) {
                this.velocityX = 0.0f;
            }
            if (this.amountY == (-this.overscrollDistance)) {
                this.velocityY = 0.0f;
            }
            if (this.amountY >= this.maxY + this.overscrollDistance) {
                this.velocityY = 0.0f;
            }
            this.flingTimer -= delta;
            if (this.flingTimer <= 0.0f) {
                this.velocityX = 0.0f;
                this.velocityY = 0.0f;
            }
            animating = true;
        }
        if (!this.smoothScrolling || this.flingTimer > 0.0f || panning || ((this.touchScrollH && (!this.scrollX || this.maxX / (this.hScrollBounds.width - this.hKnobBounds.width) <= this.areaWidth * 0.1f)) || (this.touchScrollV && (!this.scrollY || this.maxY / (this.vScrollBounds.height - this.vKnobBounds.height) <= this.areaHeight * 0.1f)))) {
            if (this.visualAmountX != this.amountX) {
                visualScrollX(this.amountX);
            }
            if (this.visualAmountY != this.amountY) {
                visualScrollY(this.amountY);
            }
        } else {
            if (this.visualAmountX != this.amountX) {
                if (this.visualAmountX < this.amountX) {
                    visualScrollX(Math.min(this.amountX, this.visualAmountX + Math.max(200.0f * delta, ((this.amountX - this.visualAmountX) * 7.0f) * delta)));
                } else {
                    visualScrollX(Math.max(this.amountX, this.visualAmountX - Math.max(200.0f * delta, ((this.visualAmountX - this.amountX) * 7.0f) * delta)));
                }
                animating = true;
            }
            if (this.visualAmountY != this.amountY) {
                if (this.visualAmountY < this.amountY) {
                    visualScrollY(Math.min(this.amountY, this.visualAmountY + Math.max(200.0f * delta, ((this.amountY - this.visualAmountY) * 7.0f) * delta)));
                } else {
                    visualScrollY(Math.max(this.amountY, this.visualAmountY - Math.max(200.0f * delta, ((this.visualAmountY - this.amountY) * 7.0f) * delta)));
                }
                animating = true;
            }
        }
        if (!panning) {
            if (this.overscrollX && this.scrollX) {
                if (this.amountX < 0.0f) {
                    resetFade();
                    this.amountX += (this.overscrollSpeedMin + (((this.overscrollSpeedMax - this.overscrollSpeedMin) * (-this.amountX)) / this.overscrollDistance)) * delta;
                    if (this.amountX > 0.0f) {
                        scrollX(0.0f);
                    }
                    animating = true;
                } else if (this.amountX > this.maxX) {
                    resetFade();
                    this.amountX -= (this.overscrollSpeedMin + (((this.overscrollSpeedMax - this.overscrollSpeedMin) * (-(this.maxX - this.amountX))) / this.overscrollDistance)) * delta;
                    if (this.amountX < this.maxX) {
                        scrollX(this.maxX);
                    }
                    animating = true;
                }
            }
            if (this.overscrollY && this.scrollY) {
                if (this.amountY < 0.0f) {
                    resetFade();
                    this.amountY += (this.overscrollSpeedMin + (((this.overscrollSpeedMax - this.overscrollSpeedMin) * (-this.amountY)) / this.overscrollDistance)) * delta;
                    if (this.amountY > 0.0f) {
                        scrollY(0.0f);
                    }
                    animating = true;
                } else if (this.amountY > this.maxY) {
                    resetFade();
                    this.amountY -= (this.overscrollSpeedMin + (((this.overscrollSpeedMax - this.overscrollSpeedMin) * (-(this.maxY - this.amountY))) / this.overscrollDistance)) * delta;
                    if (this.amountY < this.maxY) {
                        scrollY(this.maxY);
                    }
                    animating = true;
                }
            }
        }
        if (animating) {
            Stage stage = getStage();
            if (stage != null && stage.getActionsRequestRendering()) {
                Gdx.graphics.requestRendering();
            }
        }
    }

    public void layout() {
        Drawable bg = this.style.background;
        Drawable hScrollKnob = this.style.hScrollKnob;
        Drawable vScrollKnob = this.style.vScrollKnob;
        float bgLeftWidth = 0.0f;
        float bgRightWidth = 0.0f;
        float bgTopHeight = 0.0f;
        float bgBottomHeight = 0.0f;
        if (bg != null) {
            bgLeftWidth = bg.getLeftWidth();
            bgRightWidth = bg.getRightWidth();
            bgTopHeight = bg.getTopHeight();
            bgBottomHeight = bg.getBottomHeight();
        }
        float width = getWidth();
        float height = getHeight();
        float scrollbarHeight = 0.0f;
        if (hScrollKnob != null) {
            scrollbarHeight = hScrollKnob.getMinHeight();
        }
        if (this.style.hScroll != null) {
            scrollbarHeight = Math.max(scrollbarHeight, this.style.hScroll.getMinHeight());
        }
        float scrollbarWidth = 0.0f;
        if (vScrollKnob != null) {
            scrollbarWidth = vScrollKnob.getMinWidth();
        }
        if (this.style.vScroll != null) {
            scrollbarWidth = Math.max(scrollbarWidth, this.style.vScroll.getMinWidth());
        }
        this.areaWidth = (width - bgLeftWidth) - bgRightWidth;
        this.areaHeight = (height - bgTopHeight) - bgBottomHeight;
        if (this.widget != null) {
            float widgetWidth;
            float widgetHeight;
            if (this.widget instanceof Layout) {
                Layout layout = this.widget;
                widgetWidth = layout.getPrefWidth();
                widgetHeight = layout.getPrefHeight();
            } else {
                widgetWidth = this.widget.getWidth();
                widgetHeight = this.widget.getHeight();
            }
            boolean z = this.forceScrollX || (widgetWidth > this.areaWidth && !this.disableX);
            this.scrollX = z;
            z = this.forceScrollY || (widgetHeight > this.areaHeight && !this.disableY);
            this.scrollY = z;
            boolean fade = this.fadeScrollBars;
            if (!fade) {
                if (this.scrollY) {
                    this.areaWidth -= scrollbarWidth;
                    if (!(this.scrollX || widgetWidth <= this.areaWidth || this.disableX)) {
                        this.scrollX = true;
                    }
                }
                if (this.scrollX) {
                    this.areaHeight -= scrollbarHeight;
                    if (!(this.scrollY || widgetHeight <= this.areaHeight || this.disableY)) {
                        this.scrollY = true;
                        this.areaWidth -= scrollbarWidth;
                    }
                }
            }
            this.widgetAreaBounds.set(bgLeftWidth, bgBottomHeight, this.areaWidth, this.areaHeight);
            if (fade) {
                if (this.scrollX && this.scrollY) {
                    this.areaHeight -= scrollbarHeight;
                    this.areaWidth -= scrollbarWidth;
                }
            } else if (this.scrollbarsOnTop) {
                if (this.scrollX) {
                    r22 = this.widgetAreaBounds;
                    r22.height += scrollbarHeight;
                }
                if (this.scrollY) {
                    r22 = this.widgetAreaBounds;
                    r22.width += scrollbarWidth;
                }
            } else {
                if (this.scrollX && this.hScrollOnBottom) {
                    r22 = this.widgetAreaBounds;
                    r22.y += scrollbarHeight;
                }
                if (this.scrollY && !this.vScrollOnRight) {
                    r22 = this.widgetAreaBounds;
                    r22.x += scrollbarWidth;
                }
            }
            widgetWidth = this.disableX ? this.areaWidth : Math.max(this.areaWidth, widgetWidth);
            if (this.disableY) {
                widgetHeight = this.areaHeight;
            } else {
                widgetHeight = Math.max(this.areaHeight, widgetHeight);
            }
            this.maxX = widgetWidth - this.areaWidth;
            this.maxY = widgetHeight - this.areaHeight;
            if (fade && this.scrollX && this.scrollY) {
                this.maxY -= scrollbarHeight;
                this.maxX -= scrollbarWidth;
            }
            scrollX(MathUtils.clamp(this.amountX, 0.0f, this.maxX));
            scrollY(MathUtils.clamp(this.amountY, 0.0f, this.maxY));
            if (this.scrollX) {
                if (hScrollKnob != null) {
                    float hScrollHeight = this.style.hScroll != null ? this.style.hScroll.getMinHeight() : hScrollKnob.getMinHeight();
                    this.hScrollBounds.set(this.vScrollOnRight ? bgLeftWidth : bgLeftWidth + scrollbarWidth, this.hScrollOnBottom ? bgBottomHeight : (height - bgTopHeight) - hScrollHeight, this.areaWidth, hScrollHeight);
                    if (this.variableSizeKnobs) {
                        this.hKnobBounds.width = Math.max(hScrollKnob.getMinWidth(), (float) ((int) ((this.hScrollBounds.width * this.areaWidth) / widgetWidth)));
                    } else {
                        this.hKnobBounds.width = hScrollKnob.getMinWidth();
                    }
                    this.hKnobBounds.height = hScrollKnob.getMinHeight();
                    this.hKnobBounds.x = this.hScrollBounds.x + ((float) ((int) ((this.hScrollBounds.width - this.hKnobBounds.width) * getScrollPercentX())));
                    this.hKnobBounds.y = this.hScrollBounds.y;
                } else {
                    this.hScrollBounds.set(0.0f, 0.0f, 0.0f, 0.0f);
                    this.hKnobBounds.set(0.0f, 0.0f, 0.0f, 0.0f);
                }
            }
            if (this.scrollY) {
                if (vScrollKnob != null) {
                    float boundsY;
                    float boundsX;
                    float vScrollWidth = this.style.vScroll != null ? this.style.vScroll.getMinWidth() : vScrollKnob.getMinWidth();
                    if (this.hScrollOnBottom) {
                        boundsY = (height - bgTopHeight) - this.areaHeight;
                    } else {
                        boundsY = bgBottomHeight;
                    }
                    if (this.vScrollOnRight) {
                        boundsX = (width - bgRightWidth) - vScrollWidth;
                    } else {
                        boundsX = bgLeftWidth;
                    }
                    this.vScrollBounds.set(boundsX, boundsY, vScrollWidth, this.areaHeight);
                    this.vKnobBounds.width = vScrollKnob.getMinWidth();
                    if (this.variableSizeKnobs) {
                        this.vKnobBounds.height = Math.max(vScrollKnob.getMinHeight(), (float) ((int) ((this.vScrollBounds.height * this.areaHeight) / widgetHeight)));
                    } else {
                        this.vKnobBounds.height = vScrollKnob.getMinHeight();
                    }
                    if (this.vScrollOnRight) {
                        this.vKnobBounds.x = (width - bgRightWidth) - vScrollKnob.getMinWidth();
                    } else {
                        this.vKnobBounds.x = bgLeftWidth;
                    }
                    this.vKnobBounds.y = this.vScrollBounds.y + ((float) ((int) ((this.vScrollBounds.height - this.vKnobBounds.height) * (1.0f - getScrollPercentY()))));
                } else {
                    this.vScrollBounds.set(0.0f, 0.0f, 0.0f, 0.0f);
                    this.vKnobBounds.set(0.0f, 0.0f, 0.0f, 0.0f);
                }
            }
            this.widget.setSize(widgetWidth, widgetHeight);
            if (this.widget instanceof Layout) {
                ((Layout) this.widget).validate();
            }
        }
    }

    public void draw(Batch batch, float parentAlpha) {
        if (this.widget != null) {
            validate();
            applyTransform(batch, computeTransform());
            if (this.scrollX) {
                this.hKnobBounds.x = this.hScrollBounds.x + ((float) ((int) ((this.hScrollBounds.width - this.hKnobBounds.width) * getVisualScrollPercentX())));
            }
            if (this.scrollY) {
                this.vKnobBounds.y = this.vScrollBounds.y + ((float) ((int) ((this.vScrollBounds.height - this.vKnobBounds.height) * (1.0f - getVisualScrollPercentY()))));
            }
            float y = this.widgetAreaBounds.y;
            if (this.scrollY) {
                y -= (float) ((int) (this.maxY - this.visualAmountY));
            } else {
                y -= (float) ((int) this.maxY);
            }
            float x = this.widgetAreaBounds.x;
            if (this.scrollX) {
                x -= (float) ((int) this.visualAmountX);
            }
            if (!this.fadeScrollBars && this.scrollbarsOnTop) {
                if (this.scrollX && this.hScrollOnBottom) {
                    float scrollbarHeight = 0.0f;
                    if (this.style.hScrollKnob != null) {
                        scrollbarHeight = this.style.hScrollKnob.getMinHeight();
                    }
                    if (this.style.hScroll != null) {
                        scrollbarHeight = Math.max(scrollbarHeight, this.style.hScroll.getMinHeight());
                    }
                    y += scrollbarHeight;
                }
                if (this.scrollY && !this.vScrollOnRight) {
                    float scrollbarWidth = 0.0f;
                    if (this.style.hScrollKnob != null) {
                        scrollbarWidth = this.style.hScrollKnob.getMinWidth();
                    }
                    if (this.style.hScroll != null) {
                        scrollbarWidth = Math.max(scrollbarWidth, this.style.hScroll.getMinWidth());
                    }
                    x += scrollbarWidth;
                }
            }
            this.widget.setPosition(x, y);
            if (this.widget instanceof Cullable) {
                this.widgetCullingArea.x = (-this.widget.getX()) + this.widgetAreaBounds.x;
                this.widgetCullingArea.y = (-this.widget.getY()) + this.widgetAreaBounds.y;
                this.widgetCullingArea.width = this.widgetAreaBounds.width;
                this.widgetCullingArea.height = this.widgetAreaBounds.height;
                ((Cullable) this.widget).setCullingArea(this.widgetCullingArea);
            }
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            if (this.style.background != null) {
                this.style.background.draw(batch, 0.0f, 0.0f, getWidth(), getHeight());
                batch.flush();
            }
            getStage().calculateScissors(this.widgetAreaBounds, this.scissorBounds);
            if (ScissorStack.pushScissors(this.scissorBounds)) {
                drawChildren(batch, parentAlpha);
                batch.flush();
                ScissorStack.popScissors();
            }
            batch.setColor(color.r, color.g, color.b, (color.a * parentAlpha) * Interpolation.fade.apply(this.fadeAlpha / this.fadeAlphaSeconds));
            if (this.scrollX && this.scrollY && this.style.corner != null) {
                this.style.corner.draw(batch, this.hScrollBounds.width + this.hScrollBounds.x, this.hScrollBounds.y, this.vScrollBounds.width, this.vScrollBounds.y);
            }
            if (this.scrollX) {
                if (this.style.hScroll != null) {
                    this.style.hScroll.draw(batch, this.hScrollBounds.x, this.hScrollBounds.y, this.hScrollBounds.width, this.hScrollBounds.height);
                }
                if (this.style.hScrollKnob != null) {
                    this.style.hScrollKnob.draw(batch, this.hKnobBounds.x, this.hKnobBounds.y, this.hKnobBounds.width, this.hKnobBounds.height);
                }
            }
            if (this.scrollY) {
                if (this.style.vScroll != null) {
                    this.style.vScroll.draw(batch, this.vScrollBounds.x, this.vScrollBounds.y, this.vScrollBounds.width, this.vScrollBounds.height);
                }
                if (this.style.vScrollKnob != null) {
                    this.style.vScrollKnob.draw(batch, this.vKnobBounds.x, this.vKnobBounds.y, this.vKnobBounds.width, this.vKnobBounds.height);
                }
            }
            resetTransform(batch);
        }
    }

    public void fling(float flingTime, float velocityX, float velocityY) {
        this.flingTimer = flingTime;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public float getPrefWidth() {
        if (!(this.widget instanceof Layout)) {
            return 150.0f;
        }
        float width = ((Layout) this.widget).getPrefWidth();
        if (this.style.background != null) {
            width += this.style.background.getLeftWidth() + this.style.background.getRightWidth();
        }
        if (!this.forceScrollY) {
            return width;
        }
        float scrollbarWidth = 0.0f;
        if (this.style.vScrollKnob != null) {
            scrollbarWidth = this.style.vScrollKnob.getMinWidth();
        }
        if (this.style.vScroll != null) {
            scrollbarWidth = Math.max(scrollbarWidth, this.style.vScroll.getMinWidth());
        }
        return width + scrollbarWidth;
    }

    public float getPrefHeight() {
        if (!(this.widget instanceof Layout)) {
            return 150.0f;
        }
        float height = ((Layout) this.widget).getPrefHeight();
        if (this.style.background != null) {
            height += this.style.background.getTopHeight() + this.style.background.getBottomHeight();
        }
        if (!this.forceScrollX) {
            return height;
        }
        float scrollbarHeight = 0.0f;
        if (this.style.hScrollKnob != null) {
            scrollbarHeight = this.style.hScrollKnob.getMinHeight();
        }
        if (this.style.hScroll != null) {
            scrollbarHeight = Math.max(scrollbarHeight, this.style.hScroll.getMinHeight());
        }
        return height + scrollbarHeight;
    }

    public float getMinWidth() {
        return 0.0f;
    }

    public float getMinHeight() {
        return 0.0f;
    }

    public void setWidget(Actor widget) {
        if (widget == this) {
            throw new IllegalArgumentException("widget cannot be the ScrollPane.");
        }
        if (this.widget != null) {
            super.removeActor(this.widget);
        }
        this.widget = widget;
        if (widget != null) {
            super.addActor(widget);
        }
    }

    public Actor getWidget() {
        return this.widget;
    }

    public void addActor(Actor actor) {
        throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
    }

    public void addActorAt(int index, Actor actor) {
        throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
    }

    public void addActorBefore(Actor actorBefore, Actor actor) {
        throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
    }

    public void addActorAfter(Actor actorAfter, Actor actor) {
        throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
    }

    public boolean removeActor(Actor actor) {
        if (actor != this.widget) {
            return false;
        }
        setWidget(null);
        return true;
    }

    public Actor hit(float x, float y, boolean touchable) {
        if (x < 0.0f || x >= getWidth() || y < 0.0f || y >= getHeight()) {
            return null;
        }
        if (this.scrollX && this.hScrollBounds.contains(x, y)) {
            return this;
        }
        return (this.scrollY && this.vScrollBounds.contains(x, y)) ? this : super.hit(x, y, touchable);
    }

    protected void scrollX(float pixelsX) {
        this.amountX = pixelsX;
    }

    protected void scrollY(float pixelsY) {
        this.amountY = pixelsY;
    }

    protected void visualScrollX(float pixelsX) {
        this.visualAmountX = pixelsX;
    }

    protected void visualScrollY(float pixelsY) {
        this.visualAmountY = pixelsY;
    }

    protected float getMouseWheelX() {
        return Math.max(this.areaWidth * 0.9f, this.maxX * 0.1f) / 4.0f;
    }

    protected float getMouseWheelY() {
        return Math.max(this.areaHeight * 0.9f, this.maxY * 0.1f) / 4.0f;
    }

    public void setScrollX(float pixels) {
        scrollX(MathUtils.clamp(pixels, 0.0f, this.maxX));
    }

    public float getScrollX() {
        return this.amountX;
    }

    public void setScrollY(float pixels) {
        scrollY(MathUtils.clamp(pixels, 0.0f, this.maxY));
    }

    public float getScrollY() {
        return this.amountY;
    }

    public void updateVisualScroll() {
        this.visualAmountX = this.amountX;
        this.visualAmountY = this.amountY;
    }

    public float getVisualScrollX() {
        return !this.scrollX ? 0.0f : this.visualAmountX;
    }

    public float getVisualScrollY() {
        return !this.scrollY ? 0.0f : this.visualAmountY;
    }

    public float getVisualScrollPercentX() {
        return MathUtils.clamp(this.visualAmountX / this.maxX, 0.0f, 1.0f);
    }

    public float getVisualScrollPercentY() {
        return MathUtils.clamp(this.visualAmountY / this.maxY, 0.0f, 1.0f);
    }

    public float getScrollPercentX() {
        return MathUtils.clamp(this.amountX / this.maxX, 0.0f, 1.0f);
    }

    public void setScrollPercentX(float percentX) {
        scrollX(this.maxX * MathUtils.clamp(percentX, 0.0f, 1.0f));
    }

    public float getScrollPercentY() {
        return MathUtils.clamp(this.amountY / this.maxY, 0.0f, 1.0f);
    }

    public void setScrollPercentY(float percentY) {
        scrollY(this.maxY * MathUtils.clamp(percentY, 0.0f, 1.0f));
    }

    public void setFlickScroll(boolean flickScroll) {
        if (this.flickScroll != flickScroll) {
            this.flickScroll = flickScroll;
            if (flickScroll) {
                addListener(this.flickScrollListener);
            } else {
                removeListener(this.flickScrollListener);
            }
            invalidate();
        }
    }

    public void setFlickScrollTapSquareSize(float halfTapSquareSize) {
        this.flickScrollListener.getGestureDetector().setTapSquareSize(halfTapSquareSize);
    }

    public void scrollTo(float x, float y, float width, float height) {
        scrollTo(x, y, width, height, false, false);
    }

    public void scrollTo(float x, float y, float width, float height, boolean centerHorizontal, boolean centerVertical) {
        float amountX = this.amountX;
        if (centerHorizontal) {
            amountX = (x - (this.areaWidth / 2.0f)) + (width / 2.0f);
        } else {
            if (x + width > this.areaWidth + amountX) {
                amountX = (x + width) - this.areaWidth;
            }
            if (x < amountX) {
                amountX = x;
            }
        }
        scrollX(MathUtils.clamp(amountX, 0.0f, this.maxX));
        float amountY = this.amountY;
        if (centerVertical) {
            amountY = ((this.maxY - y) + (this.areaHeight / 2.0f)) - (height / 2.0f);
        } else {
            if (amountY > ((this.maxY - y) - height) + this.areaHeight) {
                amountY = ((this.maxY - y) - height) + this.areaHeight;
            }
            if (amountY < this.maxY - y) {
                amountY = this.maxY - y;
            }
        }
        scrollY(MathUtils.clamp(amountY, 0.0f, this.maxY));
    }

    public float getMaxX() {
        return this.maxX;
    }

    public float getMaxY() {
        return this.maxY;
    }

    public float getScrollBarHeight() {
        if (!this.scrollX) {
            return 0.0f;
        }
        float height = 0.0f;
        if (this.style.hScrollKnob != null) {
            height = this.style.hScrollKnob.getMinHeight();
        }
        if (this.style.hScroll != null) {
            return Math.max(height, this.style.hScroll.getMinHeight());
        }
        return height;
    }

    public float getScrollBarWidth() {
        if (!this.scrollY) {
            return 0.0f;
        }
        float width = 0.0f;
        if (this.style.vScrollKnob != null) {
            width = this.style.vScrollKnob.getMinWidth();
        }
        if (this.style.vScroll != null) {
            return Math.max(width, this.style.vScroll.getMinWidth());
        }
        return width;
    }

    public float getScrollWidth() {
        return this.areaWidth;
    }

    public float getScrollHeight() {
        return this.areaHeight;
    }

    public boolean isScrollX() {
        return this.scrollX;
    }

    public boolean isScrollY() {
        return this.scrollY;
    }

    public void setScrollingDisabled(boolean x, boolean y) {
        this.disableX = x;
        this.disableY = y;
    }

    public boolean isLeftEdge() {
        return !this.scrollX || this.amountX <= 0.0f;
    }

    public boolean isRightEdge() {
        return !this.scrollX || this.amountX >= this.maxX;
    }

    public boolean isTopEdge() {
        return !this.scrollY || this.amountY <= 0.0f;
    }

    public boolean isBottomEdge() {
        return !this.scrollY || this.amountY >= this.maxY;
    }

    public boolean isDragging() {
        return this.draggingPointer != -1;
    }

    public boolean isPanning() {
        return this.flickScrollListener.getGestureDetector().isPanning();
    }

    public boolean isFlinging() {
        return this.flingTimer > 0.0f;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float getVelocityX() {
        return this.velocityX;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public float getVelocityY() {
        return this.velocityY;
    }

    public void setOverscroll(boolean overscrollX, boolean overscrollY) {
        this.overscrollX = overscrollX;
        this.overscrollY = overscrollY;
    }

    public void setupOverscroll(float distance, float speedMin, float speedMax) {
        this.overscrollDistance = distance;
        this.overscrollSpeedMin = speedMin;
        this.overscrollSpeedMax = speedMax;
    }

    public void setForceScroll(boolean x, boolean y) {
        this.forceScrollX = x;
        this.forceScrollY = y;
    }

    public boolean isForceScrollX() {
        return this.forceScrollX;
    }

    public boolean isForceScrollY() {
        return this.forceScrollY;
    }

    public void setFlingTime(float flingTime) {
        this.flingTime = flingTime;
    }

    public void setClamp(boolean clamp) {
        this.clamp = clamp;
    }

    public void setScrollBarPositions(boolean bottom, boolean right) {
        this.hScrollOnBottom = bottom;
        this.vScrollOnRight = right;
    }

    public void setFadeScrollBars(boolean fadeScrollBars) {
        if (this.fadeScrollBars != fadeScrollBars) {
            this.fadeScrollBars = fadeScrollBars;
            if (!fadeScrollBars) {
                this.fadeAlpha = this.fadeAlphaSeconds;
            }
            invalidate();
        }
    }

    public void setupFadeScrollBars(float fadeAlphaSeconds, float fadeDelaySeconds) {
        this.fadeAlphaSeconds = fadeAlphaSeconds;
        this.fadeDelaySeconds = fadeDelaySeconds;
    }

    public void setSmoothScrolling(boolean smoothScrolling) {
        this.smoothScrolling = smoothScrolling;
    }

    public void setScrollbarsOnTop(boolean scrollbarsOnTop) {
        this.scrollbarsOnTop = scrollbarsOnTop;
        invalidate();
    }

    public boolean getVariableSizeKnobs() {
        return this.variableSizeKnobs;
    }

    public void setVariableSizeKnobs(boolean variableSizeKnobs) {
        this.variableSizeKnobs = variableSizeKnobs;
    }

    public void setCancelTouchFocus(boolean cancelTouchFocus) {
        this.cancelTouchFocus = cancelTouchFocus;
    }

    public void drawDebug(ShapeRenderer shapes) {
        shapes.flush();
        applyTransform(shapes, computeTransform());
        if (ScissorStack.pushScissors(this.scissorBounds)) {
            drawDebugChildren(shapes);
            ScissorStack.popScissors();
        }
        resetTransform(shapes);
    }
}
