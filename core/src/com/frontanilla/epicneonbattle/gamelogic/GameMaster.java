package com.frontanilla.epicneonbattle.gamelogic;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.map.Map;
import com.frontanilla.epicneonbattle.map.MapViewer;
import com.frontanilla.epicneonbattle.menus.EndOfBattle;
import com.frontanilla.epicneonbattle.menus.MapSelection;
import com.frontanilla.epicneonbattle.menus.Menu;
import com.frontanilla.epicneonbattle.menus.PlayerSelection;
import com.frontanilla.epicneonbattle.neonbattle.android.BuildConfig;
import com.frontanilla.epicneonbattle.placeables.Laboratory;
import com.frontanilla.epicneonbattle.placeables.Refinery;
import com.frontanilla.epicneonbattle.placeables.Structure;

import java.util.Iterator;

public class GameMaster {
    private static GameMaster ourInstance = new GameMaster();
    private ActionObserver actionObserver;
    private Task activateInput = new Task() {
        public void run() {
            Gdx.input.setInputProcessor(GameMaster.this.gameInputListener);
        }
    };
    private Task endOfBattle = new Task() {
        public void run() {
            MapSelection.resetMap();
            PlayerSelection.defaultPlayers();
            GameMaster.this.game.setScreen(GameMaster.this.endOfBattleScreen);
        }
    };
    private EndOfBattle endOfBattleScreen;
    private Task fadeIn = new Task() {
        public void run() {
            if (((double) GameMaster.this.gameAlpha) + 0.01d <= 1.0d) {
                GameMaster.this.gameAlpha = (float) (((double) GameMaster.this.gameAlpha) + 0.01d);
            }
        }
    };
    private Task fadeOut = new Task() {
        public void run() {
            if (((double) GameMaster.this.gameAlpha) - 0.01d >= 0.0d) {
                GameMaster.this.gameAlpha = (float) (((double) GameMaster.this.gameAlpha) - 0.01d);
            }
        }
    };
    private Game game;
    private float gameAlpha;
    private GameHUDBar gameHUDBar;
    private GameInputListener gameInputListener;
    private GameScreen gameScreen;
    private boolean gameStuffLoaded;
    private Map map;
    private MapViewer mapViewer;
    private Player[] players;
    private Menu previousMenu;
    private Task toPreviousMenu = new Task() {
        public void run() {
            MapSelection.resetMap();
            PlayerSelection.defaultPlayers();
            GameMaster.this.game.setScreen(GameMaster.this.previousMenu);
        }
    };
    private TurnObserver turnObserver;
    private WeaponManager weaponManager;

    public void fadeIn() {
        Timer.schedule(this.fadeIn, 0.0f, 0.01f, 100);
    }

    public void fadeOut() {
        Timer.schedule(this.fadeOut, 0.0f, 0.01f, 100);
    }

    public void activateInput(float delay) {
        Timer.schedule(this.activateInput, delay);
    }

    private GameMaster() {
    }

    public static GameMaster getInstance() {
        return ourInstance;
    }

    public void init(Map map, Player[] players, Game game, Menu previous) {
        if (!this.gameStuffLoaded) {
            AssetManager.getInstance().loadGame();
            this.gameStuffLoaded = true;
        }
        this.map = map;
        this.mapViewer = new MapViewer(map);
        this.players = players;
        this.game = game;
        this.gameHUDBar = new GameHUDBar();
        this.gameHUDBar.makeButtons();
        this.actionObserver = new ActionObserver();
        this.actionObserver.setAction(ActionObserver.Action.NOTHING);
        this.actionObserver.setBuyable(ActionObserver.Buyable.NOTHING);
        this.weaponManager = new WeaponManager();
        this.turnObserver = new TurnObserver();
        this.gameScreen = new GameScreen();
        this.gameScreen.load();
        assignMapTextures();
        this.gameInputListener = new GameInputListener();
        this.gameInputListener.init();
        this.endOfBattleScreen = new EndOfBattle(previous);
        this.previousMenu = previous;
    }

    private void assignMapTextures() {
        for (int r = 0; r < this.map.getMapData().getNumRows(); r++) {
            for (int c = 0; c < this.map.getMapData().getNumColumns(); c++) {
                switch (this.map.getMapData().getData()[r][c]) {
                    case Encoder.EMatchFinderTypeBT2 /*0*/:
                        this.map.getCell(r, c).setRegionNormal(AssetManager.game.defaultCellNormal);
                        this.map.getCell(r, c).setRegionGlow(AssetManager.game.defaultCellGlow);
                        break;
                    case BuildConfig.VERSION_CODE /*1*/:
                        this.map.getCell(r, c).setRegionNormal(AssetManager.game.disabledCellNormal);
                        this.map.getCell(r, c).setRegionGlow(AssetManager.game.disabledCellGlow);
                        break;
                    case Base.kNumLenToPosStatesBits /*2*/:
                        this.map.getCell(r, c).setRegionNormal(AssetManager.game.blockedCellNormal);
                        this.map.getCell(r, c).setRegionGlow(AssetManager.game.blockedCellGlow);
                        this.map.addToBlocked(this.map.getCell(r, c));
                        break;
                    case Base.kNumMidLenBits /*3*/:
                        this.map.getCell(r, c).setRegionNormal(AssetManager.game.baseCellNormal);
                        this.map.getCell(r, c).setRegionGlow(AssetManager.game.baseCellGlow);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void showGameScreen() {
        this.game.setScreen(this.gameScreen);
    }

    private void endOfGame() {
        Player winner = null;
        for (Player p : this.players) {
            if (p.isAlive()) {
                winner = p;
                break;
            }
        }
        this.endOfBattleScreen.setWinner(winner);
        fadeOut();
        Timer.schedule(this.endOfBattle, 1.0f);
    }

    public void goToPreviousMenu() {
        fadeOut();
        Timer.schedule(this.toPreviousMenu, 1.0f);
    }

    public Map getMap() {
        return this.map;
    }

    public MapViewer getMapViewer() {
        return this.mapViewer;
    }

    public Player[] getPlayers() {
        return this.players;
    }

    public GameHUDBar getGameHUDBar() {
        return this.gameHUDBar;
    }

    public ActionObserver getActionObserver() {
        return this.actionObserver;
    }

    public WeaponManager getWeaponManager() {
        return this.weaponManager;
    }

    public TurnObserver getTurnObserver() {
        return this.turnObserver;
    }

    public float getAlpha() {
        return this.gameAlpha;
    }

    public Color getColorWithAlpha(Color c) {
        return new Color(c.r, c.g, c.b, this.gameAlpha);
    }

    public void passRound() {
        for (Player player : this.players) {
            player.setCubes(player.getCubes() + 5);
            player.setTech(player.getTech() + 3);
            if (player.getCubes() > 99) {
                player.setCubes(99);
            }
            if (player.getTech() > 99) {
                player.setTech(99);
            }
            Iterator it = player.getStructures().iterator();
            while (it.hasNext()) {
                Structure s = (Structure) it.next();
                if (s instanceof Refinery) {
                    player.setCubes(player.getCubes() + 1);
                }
                if (s instanceof Laboratory) {
                    player.setTech(player.getTech() + 1);
                }
            }
        }
    }

    public Player getCurrentPlayer() {
        return this.players[this.turnObserver.getTurn()];
    }

    public void playerDied() {
        int playersLeft = 0;
        for (Player player : this.players) {
            if (player.isAlive()) {
                playersLeft++;
            }
        }
        if (playersLeft == 1) {
            endOfGame();
        }
    }
}
