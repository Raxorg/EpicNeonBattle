package com.epicness.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.epicness.placeables.Placeable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Map {
    private ArrayList<Cell> blockedCells;
    private float cellSize;
    private Cell[][] cells = ((Cell[][]) Array.newInstance(Cell.class, new int[]{this.rows, this.columns}));
    private int columns;
    private MapData mapData;
    private int maxPlayers;
    private int rows;

    public class Cell {
        private int column;
        private Placeable content;
        private TextureRegion regionGlow;
        private TextureRegion regionNormal;
        private int row;

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.column;
        }

        public TextureRegion getRegionNormal() {
            return this.regionNormal;
        }

        public void setRegionNormal(TextureRegion region) {
            this.regionNormal = region;
        }

        public TextureRegion getRegionGlow() {
            return this.regionGlow;
        }

        public void setRegionGlow(TextureRegion regionGlow) {
            this.regionGlow = regionGlow;
        }

        public Placeable getContent() {
            return this.content;
        }

        public boolean setContent(Placeable p) {
            if (this.content != null) {
                return false;
            }
            this.content = p;
            p.setCell(this);
            return true;
        }

        public void clearContent() {
            this.content = null;
        }

        public boolean isEmpty() {
            return this.content == null;
        }

        public boolean isBlocked() {
            return Map.this.mapData.getData()[this.row][this.column] == 1 || Map.this.mapData.getData()[this.row][this.column] == 2;
        }

        public boolean isBaseCell() {
            return Map.this.mapData.getData()[this.row][this.column] == 3;
        }
    }

    public Map(float cellSize, MapData mapData) {
        this.cellSize = cellSize;
        this.mapData = mapData;
        this.rows = mapData.getNumRows();
        this.columns = mapData.getNumColumns();
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.columns; c++) {
                this.cells[r][c] = new Cell(r, c);
            }
        }
        this.maxPlayers = mapData.getMaxPlayers();
        this.blockedCells = new ArrayList();
    }

    public void addToBlocked(Cell c) {
        this.blockedCells.add(c);
    }

    public ArrayList<Cell> getBlockedCells() {
        return this.blockedCells;
    }

    public String toString() {
        return this.mapData.getName().toUpperCase();
    }

    public float getCellSize() {
        return this.cellSize;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public TextureRegion getRegionGlow() {
        return this.mapData.getRegionGlow();
    }

    public TextureRegion getRegionNormal() {
        return this.mapData.getRegionNormal();
    }

    public MapData getMapData() {
        return this.mapData;
    }

    public float getWidth() {
        return ((float) this.columns) * this.cellSize;
    }

    public float getHeight() {
        return ((float) this.rows) * this.cellSize;
    }

    public Cell getCell(int row, int column) {
        if (row < 0 || column < 0 || row >= this.rows || column >= this.columns) {
            return null;
        }
        return this.cells[row][column];
    }
}
