package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class TextArea extends TextField {
    int cursorLine;
    int firstLineShowing;
    private String lastText;
    IntArray linesBreak;
    private int linesShowing;
    float moveOffset;
    private float prefRows;

    public class TextAreaListener extends TextFieldClickListener {
        public TextAreaListener() {
            super();
        }

        protected void setCursorPosition(float x, float y) {
            TextArea.this.moveOffset = -1.0f;
            Drawable background = TextArea.this.style.background;
            BitmapFont font = TextArea.this.style.font;
            float height = TextArea.this.getHeight();
            if (background != null) {
                height -= background.getTopHeight();
                x -= background.getLeftWidth();
            }
            x = Math.max(0.0f, x);
            if (background != null) {
                y -= background.getTopHeight();
            }
            TextArea.this.cursorLine = ((int) Math.floor((double) ((height - y) / font.getLineHeight()))) + TextArea.this.firstLineShowing;
            TextArea.this.cursorLine = Math.max(0, Math.min(TextArea.this.cursorLine, TextArea.this.getLines() - 1));
            super.setCursorPosition(x, y);
            TextArea.this.updateCurrentLine();
        }

        public boolean keyDown(InputEvent event, int keycode) {
            super.keyDown(event, keycode);
            Stage stage = TextArea.this.getStage();
            if (stage == null || stage.getKeyboardFocus() != TextArea.this) {
                return false;
            }
            boolean shift;
            boolean repeat = false;
            if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                shift = true;
            } else {
                shift = false;
            }
            if (keycode == 20) {
                if (!shift) {
                    TextArea.this.clearSelection();
                } else if (!TextArea.this.hasSelection) {
                    TextArea.this.selectionStart = TextArea.this.cursor;
                    TextArea.this.hasSelection = true;
                }
                TextArea.this.moveCursorLine(TextArea.this.cursorLine + 1);
                repeat = true;
            } else if (keycode == 19) {
                if (!shift) {
                    TextArea.this.clearSelection();
                } else if (!TextArea.this.hasSelection) {
                    TextArea.this.selectionStart = TextArea.this.cursor;
                    TextArea.this.hasSelection = true;
                }
                TextArea.this.moveCursorLine(TextArea.this.cursorLine - 1);
                repeat = true;
            } else {
                TextArea.this.moveOffset = -1.0f;
            }
            if (repeat) {
                scheduleKeyRepeatTask(keycode);
            }
            TextArea.this.showCursor();
            return true;
        }

        public boolean keyTyped(InputEvent event, char character) {
            boolean result = super.keyTyped(event, character);
            TextArea.this.showCursor();
            return result;
        }

        protected void goHome(boolean jump) {
            if (jump) {
                TextArea.this.cursor = 0;
            } else if (TextArea.this.cursorLine * 2 < TextArea.this.linesBreak.size) {
                TextArea.this.cursor = TextArea.this.linesBreak.get(TextArea.this.cursorLine * 2);
            }
        }

        protected void goEnd(boolean jump) {
            if (jump || TextArea.this.cursorLine >= TextArea.this.getLines()) {
                TextArea.this.cursor = TextArea.this.text.length();
            } else if ((TextArea.this.cursorLine * 2) + 1 < TextArea.this.linesBreak.size) {
                TextArea.this.cursor = TextArea.this.linesBreak.get((TextArea.this.cursorLine * 2) + 1);
            }
        }
    }

    public TextArea(String text, Skin skin) {
        super(text, skin);
    }

    public TextArea(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public TextArea(String text, TextFieldStyle style) {
        super(text, style);
    }

    protected void initialize() {
        super.initialize();
        this.writeEnters = true;
        this.linesBreak = new IntArray();
        this.cursorLine = 0;
        this.firstLineShowing = 0;
        this.moveOffset = -1.0f;
        this.linesShowing = 0;
    }

    protected int letterUnderCursor(float x) {
        if (this.linesBreak.size <= 0) {
            return 0;
        }
        if (this.cursorLine * 2 >= this.linesBreak.size) {
            return this.text.length();
        }
        int start = this.linesBreak.items[this.cursorLine * 2];
        int end = this.linesBreak.items[(this.cursorLine * 2) + 1];
        int i = start;
        boolean found = false;
        while (i <= end && !found) {
            if (this.glyphPositions.items[i] - this.glyphPositions.items[start] > x) {
                found = true;
            } else {
                i++;
            }
        }
        return Math.max(0, i - 1);
    }

    public void setPrefRows(float prefRows) {
        this.prefRows = prefRows;
    }

    public float getPrefHeight() {
        if (this.prefRows <= 0.0f) {
            return super.getPrefHeight();
        }
        float prefHeight = this.textHeight * this.prefRows;
        if (this.style.background != null) {
            return Math.max((this.style.background.getBottomHeight() + prefHeight) + this.style.background.getTopHeight(), this.style.background.getMinHeight());
        }
        return prefHeight;
    }

    public int getLines() {
        return (newLineAtEnd() ? 1 : 0) + (this.linesBreak.size / 2);
    }

    public boolean newLineAtEnd() {
        return this.text.length() != 0 && (this.text.charAt(this.text.length() - 1) == '\n' || this.text.charAt(this.text.length() - 1) == '\r');
    }

    public void moveCursorLine(int line) {
        float f = 0.0f;
        if (line < 0) {
            this.cursorLine = 0;
            this.cursor = 0;
            this.moveOffset = -1.0f;
        } else if (line >= getLines()) {
            int newLine = getLines() - 1;
            this.cursor = this.text.length();
            if (line > getLines() || newLine == this.cursorLine) {
                this.moveOffset = -1.0f;
            }
            this.cursorLine = newLine;
        } else if (line != this.cursorLine) {
            if (this.moveOffset < 0.0f) {
                if (this.linesBreak.size > this.cursorLine * 2) {
                    f = this.glyphPositions.get(this.cursor) - this.glyphPositions.get(this.linesBreak.get(this.cursorLine * 2));
                }
                this.moveOffset = f;
            }
            this.cursorLine = line;
            this.cursor = this.cursorLine * 2 >= this.linesBreak.size ? this.text.length() : this.linesBreak.get(this.cursorLine * 2);
            while (this.cursor < this.text.length() && this.cursor <= this.linesBreak.get((this.cursorLine * 2) + 1) - 1 && this.glyphPositions.get(this.cursor) - this.glyphPositions.get(this.linesBreak.get(this.cursorLine * 2)) < this.moveOffset) {
                this.cursor++;
            }
            showCursor();
        }
    }

    void updateCurrentLine() {
        int index = calculateCurrentLineIndex(this.cursor);
        int line = index / 2;
        if (index % 2 != 0 && index + 1 < this.linesBreak.size && this.cursor == this.linesBreak.items[index] && this.linesBreak.items[index + 1] == this.linesBreak.items[index]) {
            return;
        }
        if (line < this.linesBreak.size / 2 || this.text.length() == 0 || this.text.charAt(this.text.length() - 1) == '\n' || this.text.charAt(this.text.length() - 1) == '\r') {
            this.cursorLine = line;
        }
    }

    void showCursor() {
        updateCurrentLine();
        if (this.cursorLine != this.firstLineShowing) {
            int step = this.cursorLine >= this.firstLineShowing ? 1 : -1;
            while (true) {
                if (this.firstLineShowing > this.cursorLine || (this.firstLineShowing + this.linesShowing) - 1 < this.cursorLine) {
                    this.firstLineShowing += step;
                } else {
                    return;
                }
            }
        }
    }

    private int calculateCurrentLineIndex(int cursor) {
        int index = 0;
        while (index < this.linesBreak.size && cursor > this.linesBreak.items[index]) {
            index++;
        }
        return index;
    }

    protected void sizeChanged() {
        this.lastText = null;
        BitmapFont font = this.style.font;
        Drawable background = this.style.background;
        this.linesShowing = (int) Math.floor((double) ((getHeight() - (background == null ? 0.0f : background.getBottomHeight() + background.getTopHeight())) / font.getLineHeight()));
    }

    protected float getTextY(BitmapFont font, Drawable background) {
        float textY = getHeight();
        if (background != null) {
            return (float) ((int) (textY - background.getTopHeight()));
        }
        return textY;
    }

    protected void drawSelection(Drawable selection, Batch batch, BitmapFont font, float x, float y) {
        int i = this.firstLineShowing * 2;
        float offsetY = 0.0f;
        int minIndex = Math.min(this.cursor, this.selectionStart);
        int maxIndex = Math.max(this.cursor, this.selectionStart);
        while (i + 1 < this.linesBreak.size && i < (this.firstLineShowing + this.linesShowing) * 2) {
            int lineStart = this.linesBreak.get(i);
            int lineEnd = this.linesBreak.get(i + 1);
            if ((minIndex >= lineStart || minIndex >= lineEnd || maxIndex >= lineStart || maxIndex >= lineEnd) && (minIndex <= lineStart || minIndex <= lineEnd || maxIndex <= lineStart || maxIndex <= lineEnd)) {
                int start = Math.max(this.linesBreak.get(i), minIndex);
                float selectionWidth = this.glyphPositions.get(Math.min(this.linesBreak.get(i + 1), maxIndex)) - this.glyphPositions.get(start);
                Drawable drawable = selection;
                Batch batch2 = batch;
                drawable.draw(batch2, (x + (this.glyphPositions.get(start) - this.glyphPositions.get(this.linesBreak.get(i)))) + this.fontOffset, ((y - this.textHeight) - font.getDescent()) - offsetY, selectionWidth, font.getLineHeight());
            }
            offsetY += font.getLineHeight();
            i += 2;
        }
    }

    protected void drawText(Batch batch, BitmapFont font, float x, float y) {
        float offsetY = 0.0f;
        int i = this.firstLineShowing * 2;
        while (i < (this.firstLineShowing + this.linesShowing) * 2 && i < this.linesBreak.size) {
            font.draw(batch, this.displayText, x, y + offsetY, this.linesBreak.items[i], this.linesBreak.items[i + 1], 0.0f, 8, false);
            offsetY -= font.getLineHeight();
            i += 2;
        }
    }

    protected void drawCursor(Drawable cursorPatch, Batch batch, BitmapFont font, float x, float y) {
        float textOffset;
        if (this.cursor >= this.glyphPositions.size || this.cursorLine * 2 >= this.linesBreak.size) {
            textOffset = 0.0f;
        } else {
            textOffset = this.glyphPositions.get(this.cursor) - this.glyphPositions.get(this.linesBreak.items[this.cursorLine * 2]);
        }
        cursorPatch.draw(batch, ((x + textOffset) + this.fontOffset) + font.getData().cursorX, (y - (font.getDescent() / 2.0f)) - (((float) ((this.cursorLine - this.firstLineShowing) + 1)) * font.getLineHeight()), cursorPatch.getMinWidth(), font.getLineHeight());
    }

    protected void calculateOffsets() {
        super.calculateOffsets();
        if (!this.text.equals(this.lastText)) {
            this.lastText = this.text;
            BitmapFont font = this.style.font;
            float maxWidthLine = getWidth() - (this.style.background != null ? this.style.background.getLeftWidth() + this.style.background.getRightWidth() : 0.0f);
            this.linesBreak.clear();
            int lineStart = 0;
            int lastSpace = 0;
            Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
            GlyphLayout layout = (GlyphLayout) layoutPool.obtain();
            for (int i = 0; i < this.text.length(); i++) {
                char lastCharacter = this.text.charAt(i);
                if (lastCharacter == '\r' || lastCharacter == '\n') {
                    this.linesBreak.add(lineStart);
                    this.linesBreak.add(i);
                    lineStart = i + 1;
                } else {
                    if (!continueCursor(i, 0)) {
                        lastSpace = i;
                    }
                    layout.setText(font, this.text.subSequence(lineStart, i + 1));
                    if (layout.width > maxWidthLine) {
                        if (lineStart >= lastSpace) {
                            lastSpace = i - 1;
                        }
                        this.linesBreak.add(lineStart);
                        this.linesBreak.add(lastSpace + 1);
                        lineStart = lastSpace + 1;
                        lastSpace = lineStart;
                    }
                }
            }
            layoutPool.free(layout);
            if (lineStart < this.text.length()) {
                this.linesBreak.add(lineStart);
                this.linesBreak.add(this.text.length());
            }
            showCursor();
        }
    }

    protected InputListener createInputListener() {
        return new TextAreaListener();
    }

    public void setSelection(int selectionStart, int selectionEnd) {
        super.setSelection(selectionStart, selectionEnd);
        updateCurrentLine();
    }

    protected void moveCursor(boolean forward, boolean jump) {
        int count = forward ? 1 : -1;
        int index = (this.cursorLine * 2) + count;
        if (index < 0 || index + 1 >= this.linesBreak.size || this.linesBreak.items[index] != this.cursor || this.linesBreak.items[index + 1] != this.cursor) {
            super.moveCursor(forward, jump);
        } else {
            this.cursorLine += count;
            if (jump) {
                super.moveCursor(forward, jump);
            }
            showCursor();
        }
        updateCurrentLine();
    }

    protected boolean continueCursor(int index, int offset) {
        int pos = calculateCurrentLineIndex(index + offset);
        return super.continueCursor(index, offset) && (pos < 0 || pos >= this.linesBreak.size - 2 || this.linesBreak.items[pos + 1] != index || this.linesBreak.items[pos + 1] == this.linesBreak.items[pos + 2]);
    }

    public int getCursorLine() {
        return this.cursorLine;
    }

    public int getFirstLineShowing() {
        return this.firstLineShowing;
    }

    public int getLinesShowing() {
        return this.linesShowing;
    }
}
