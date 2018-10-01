package com.frontanilla.epicneonbattle.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.assets.Colors;
import com.frontanilla.epicneonbattle.components.Button;
import com.frontanilla.epicneonbattle.gamelogic.Player;
import com.frontanilla.epicneonbattle.gamelogic.Team;
import com.frontanilla.epicneonbattle.neonbattle.android.BuildConfig;

import java.util.ArrayList;

public class PlayerSelection extends Menu implements TextInputListener {
    private static Player[] players;
    private static float[] xPositions;
    private static float[] yPositions;
    private Menu multiplayer;
    private int playerToEditIndex;

    private class ColorButton extends Button {
        private EditNameButton brother;
        private int playerIndex;

        public ColorButton(float x, float y, float width, float height, int playerIndex) {
            super(AssetManager.menus.emptyFrameNormal, AssetManager.menus.emptyFrameGlow, x, y, width, height, PlayerSelection.players[playerIndex].getColor());
            this.playerIndex = playerIndex;
            Button[] newButtons = new Button[(PlayerSelection.this.buttons.length + 1)];
            System.arraycopy(PlayerSelection.this.buttons, 0, newButtons, 0, PlayerSelection.this.buttons.length);
            newButtons[newButtons.length - 1] = this;
            PlayerSelection.this.buttons = newButtons;
        }

        public void onTouchUp() {
            ArrayList<Color> usedColors = new ArrayList();
            for (int i = 0; i < PlayerSelection.players.length; i++) {
                if (i != this.playerIndex) {
                    usedColors.add(PlayerSelection.players[i].getColor());
                }
            }
            PlayerSelection.players[this.playerIndex].setColor(Colors.getInstance().nextUnusedColor(PlayerSelection.players[this.playerIndex].getColor(), usedColors));
            this.color = PlayerSelection.players[this.playerIndex].getColor();
            this.brother.setColor(this.color);
        }

        public void setBrother(EditNameButton editNameButton) {
            this.brother = editNameButton;
        }
    }

    private class EditNameButton extends Button {
        private int playerIndex;

        public EditNameButton(float x, float y, float width, float height, int playerIndex) {
            super(AssetManager.menus.editNormal, AssetManager.menus.editGlow, x, y, width, height, PlayerSelection.players[playerIndex].getColor());
            this.playerIndex = playerIndex;
            Button[] newButtons = new Button[(PlayerSelection.this.buttons.length + 1)];
            System.arraycopy(PlayerSelection.this.buttons, 0, newButtons, 0, PlayerSelection.this.buttons.length);
            newButtons[newButtons.length - 1] = this;
            PlayerSelection.this.buttons = newButtons;
        }

        public void onTouchUp() {
            PlayerSelection.this.playerToEditIndex = this.playerIndex;
            Gdx.input.getTextInput(PlayerSelection.this, "Enter a player name", BuildConfig.VERSION_NAME, "New name");
        }
    }

    public PlayerSelection(Menu previous) {
        this.multiplayer = previous;
        initPositions();
        defaultPlayers();
    }

    private void initPositions() {
        xPositions = new float[6];
        yPositions = new float[6];
        xPositions[0] = 0.0f;
        yPositions[0] = ((float) Gdx.graphics.getHeight()) * 0.6f;
        xPositions[1] = 0.0f;
        yPositions[1] = ((float) Gdx.graphics.getHeight()) * 0.4f;
        xPositions[2] = 0.0f;
        yPositions[2] = ((float) Gdx.graphics.getHeight()) * 0.2f;
        xPositions[3] = (float) (Gdx.graphics.getWidth() / 2);
        yPositions[3] = ((float) Gdx.graphics.getHeight()) * 0.6f;
        xPositions[4] = (float) (Gdx.graphics.getWidth() / 2);
        yPositions[4] = ((float) Gdx.graphics.getHeight()) * 0.4f;
        xPositions[5] = (float) (Gdx.graphics.getWidth() / 2);
        yPositions[5] = ((float) Gdx.graphics.getHeight()) * 0.2f;
    }

    public static Player[] defaultPlayers() {
        players = new Player[2];
        players[0] = new Player();
        players[0].setColor(Color.RED);
        players[1] = new Player();
        players[1].setColor(Color.BLUE);
        Team[] teams = new Team[2];
        teams[0] = new Team();
        teams[0].addPlayer(players[0]);
        teams[1] = new Team();
        teams[1].addPlayer(players[1]);
        return players;
    }

    public static Player[] getPlayers() {
        if (players == null) {
            return defaultPlayers();
        }
        return players;
    }

    private void updateButtons() {
        makeButtons();
        for (int i = 0; i < players.length; i++) {
            new ColorButton(xPositions[i], yPositions[i], (float) ((Gdx.graphics.getHeight() / 8) * 4), (float) (Gdx.graphics.getHeight() / 8), i).setBrother(new EditNameButton(((float) ((Gdx.graphics.getHeight() / 8) * 4)) + xPositions[i], yPositions[i], (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8), i));
        }
        getMenuButtonListener().setButtons(this.buttons);
    }

    protected void makeButtons() {
        this.buttons = new Button[2];
        this.buttons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.ORANGE) {
            public void onTouchUp() {
                PlayerSelection.this.scheduleTransition(PlayerSelection.this.multiplayer);
            }
        };
        this.buttons[1] = new Button(AssetManager.menus.plusNormal, AssetManager.menus.plusGlow, (screenWidth / 2.0f) - (buttonHeight / 2.0f), screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.WHITE) {
            public void onTouchUp() {
                if (PlayerSelection.players.length == MapSelection.getCurrentMap().getMaxPlayers()) {
                    PlayerSelection.defaultPlayers();
                    PlayerSelection.this.updateButtons();
                } else {
                    Player[] newPlayers = new Player[(PlayerSelection.players.length + 1)];
                    System.arraycopy(PlayerSelection.players, 0, newPlayers, 0, PlayerSelection.players.length);
                    newPlayers[newPlayers.length - 1] = new Player();
                    ArrayList<Color> usedColors = new ArrayList();
                    for (int i = 0; i < newPlayers.length - 1; i++) {
                        usedColors.add(PlayerSelection.players[i].getColor());
                    }
                    newPlayers[newPlayers.length - 1].setColor(Colors.getInstance().nextUnusedColor(usedColors));
                    PlayerSelection.players = newPlayers;
                    PlayerSelection.this.updateButtons();
                }
                PlayerSelection.this.assignTeams();
            }
        };
    }

    private void assignTeams() {
        for (Player player : players) {
            new Team().addPlayer(player);
        }
    }

    protected void appear() {
        AssetManager.fonts.normal.getData().setScale(((float) (Gdx.graphics.getHeight() / 8)) / 512.0f, ((float) (Gdx.graphics.getHeight() / 8)) / 512.0f);
        AssetManager.fonts.glow.getData().setScale(((float) (Gdx.graphics.getHeight() / 8)) / 512.0f, ((float) (Gdx.graphics.getHeight() / 8)) / 512.0f);
        updateButtons();
    }

    protected void step() {
        staticBatch.begin();
        int i = 0;
        while (i < players.length) {
            AssetManager.fonts.normal.setColor(1.0f, 1.0f, 1.0f, getMenusAlpha());
            AssetManager.fonts.glow.setColor(players[i].getColor().r, players[i].getColor().g, players[i].getColor().b, getMenusAlpha());
            float offset = (float) ((-(Gdx.graphics.getHeight() / 8)) / 4);
            if (!(players[i].getName().charAt(0) == 'M' || players[i].getName().charAt(0) == 'W')) {
                offset = (float) ((Gdx.graphics.getHeight() / 8) / 16);
            }
            AssetManager.fonts.normal.draw(staticBatch, players[i].getName(), xPositions[i] + offset, yPositions[i] + ((float) (Gdx.graphics.getHeight() / 8)));
            AssetManager.fonts.glow.draw(staticBatch, players[i].getName(), xPositions[i] + offset, yPositions[i] + ((float) (Gdx.graphics.getHeight() / 8)));
            i++;
        }
        staticBatch.end();
    }

    public void input(String text) {
        if (text.length() == 0) {
            canceled();
            return;
        }
        double space = 4.0d;
        char[] chars = new char[4];
        char[] temp = text.toCharArray();
        int i = 0;
        while (i < 4 && i < text.length()) {
            if (temp[i] == 'm' || temp[i] == 'w') {
                if (space >= 1.5d) {
                    chars[i] = temp[i];
                    space -= 1.5d;
                }
            } else if (space >= 1.0d) {
                chars[i] = temp[i];
                space -= 1.0d;
            }
            i++;
        }
        players[this.playerToEditIndex].setName(new String(chars).toUpperCase());
    }

    public void canceled() {
        this.playerToEditIndex = 0;
    }
}
