package com.frontanilla.epicneonbattle.gamelogic;

import com.badlogic.gdx.graphics.Color;
import com.frontanilla.epicneonbattle.map.Map;
import com.frontanilla.epicneonbattle.placeables.Base;
import com.frontanilla.epicneonbattle.placeables.Placeable;
import com.frontanilla.epicneonbattle.placeables.Structure;
import com.frontanilla.epicneonbattle.placeables.Unit;
import com.frontanilla.epicneonbattle.placeables.Wall;

import java.util.ArrayList;

public class Player {
    private boolean alive = true;
    private Color color;
    private int cubes = 5;
    private boolean hasBase = false;
    private String name = "BOB";
    private ArrayList<Structure> structures = new ArrayList();
    private Team team;
    private int tech = 3;
    private ArrayList<Map.Cell> territory = new ArrayList();
    private ArrayList<Unit> units = new ArrayList();

    public boolean isAlive() {
        return this.alive;
    }

    public void buy(Placeable placeable, Map.Cell cell) {
        if (!cell.isBlocked()) {
            int r;
            int c;
            Map.Cell currentCell;
            if (this.hasBase && this.territory.contains(cell) && !(placeable instanceof Base)) {
                if (placeable.place(cell)) {
                    this.cubes -= placeable.getCubeCost();
                    this.tech -= placeable.getTechCost();
                    if (placeable instanceof Unit) {
                        this.units.add((Unit) placeable);
                    } else if (placeable instanceof Structure) {
                        this.structures.add((Structure) placeable);
                        if (placeable instanceof Wall) {
                            for (r = -1; r <= 1; r++) {
                                for (c = -1; c <= 1; c++) {
                                    currentCell = GameMaster.getInstance().getMap().getCell(cell.getRow() + r, cell.getColumn() + c);
                                    if (!(currentCell == null || this.territory.contains(currentCell) || currentCell.isBlocked())) {
                                        this.territory.add(currentCell);
                                    }
                                }
                            }
                        }
                    }
                    placeable.setOwner(this);
                }
            } else if (!this.hasBase && cell.isBaseCell() && placeable.place(cell)) {
                this.structures.add((Structure) placeable);
                for (r = -1; r <= 1; r++) {
                    for (c = -1; c <= 1; c++) {
                        currentCell = GameMaster.getInstance().getMap().getCell(cell.getRow() + r, cell.getColumn() + c);
                        if (!(currentCell == null || this.territory.contains(currentCell) || currentCell.isBlocked())) {
                            this.territory.add(currentCell);
                        }
                    }
                }
                placeable.setOwner(this);
                this.hasBase = true;
            }
        }
    }

    public boolean hasBase() {
        return this.hasBase;
    }

    public void removePlaceable(Placeable p) {
        if (p instanceof Unit) {
            this.units.remove(p);
        }
        if (p instanceof Structure) {
            if (p instanceof Wall) {
                wallDestroyed(p.getCell().getRow(), p.getCell().getColumn());
            }
            this.structures.remove(p);
        }
        p.getCell().clearContent();
        if (p instanceof Base) {
            this.alive = false;
            GameMaster.getInstance().playerDied();
        }
    }

    private void wallDestroyed(int row, int column) {
        int r;
        int c;
        Map.Cell currentCell;
        for (r = -1; r <= 1; r++) {
            for (c = -1; c <= 1; c++) {
                currentCell = GameMaster.getInstance().getMap().getCell(row + r, column + c);
                if (currentCell != null && this.territory.contains(currentCell)) {
                    this.territory.remove(currentCell);
                }
            }
        }
        for (r = -2; r <= 2; r++) {
            c = -2;
            while (c <= 2) {
                if (r != 0 || c != 0) {
                    currentCell = GameMaster.getInstance().getMap().getCell(row + r, column + c);
                    if (currentCell != null && (((currentCell.getContent() instanceof Wall) || (currentCell.getContent() instanceof Base)) && currentCell.getContent().getOwner().getTeam() == this.team)) {
                        for (int r2 = -1; r2 <= 1; r2++) {
                            for (int c2 = -1; c2 <= 1; c2++) {
                                Map.Cell currentCell2 = GameMaster.getInstance().getMap().getCell(currentCell.getRow() + r2, currentCell.getColumn() + c2);
                                if (!(currentCell2 == null || this.territory.contains(currentCell2) || currentCell2.isBlocked())) {
                                    this.territory.add(currentCell2);
                                }
                            }
                        }
                    }
                }
                c++;
            }
        }
    }

    public ArrayList<Map.Cell> getTerritory() {
        return this.territory;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setCubes(int cubes) {
        this.cubes = cubes;
    }

    public int getCubes() {
        return this.cubes;
    }

    public void setTech(int tech) {
        this.tech = tech;
    }

    public int getTech() {
        return this.tech;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public ArrayList<Unit> getUnits() {
        return this.units;
    }

    public ArrayList<Structure> getStructures() {
        return this.structures;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
