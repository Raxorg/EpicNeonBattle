package com.frontanilla.epicneonbattle.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.components.Button;
import com.frontanilla.epicneonbattle.gamelogic.listeners.BuyMenuListener;
import com.frontanilla.epicneonbattle.gamelogic.listeners.GameMenuListener;
import com.frontanilla.epicneonbattle.neonbattle.android.BuildConfig;
import com.frontanilla.epicneonbattle.placeables.Tank;
import com.frontanilla.epicneonbattle.placeables.Unit;


import java.util.Iterator;

public class GameHUDBar {
    private boolean buyMenuActive;
    private Button[] buyMenuButtons;
    private BuyMenuListener buyMenuListener;
    private Button[] gameHUDButtons;
    private Button[] gameMenuButtons;
    private GameMenuListener gameMenuListener;
    private boolean menuActive;

    public void makeButtons() {
        this.gameMenuListener = new GameMenuListener();
        this.buyMenuListener = new BuyMenuListener();
        this.gameHUDButtons = new Button[4];
        this.gameHUDButtons[0] = new Button(AssetManager.game.pauseNormal, AssetManager.game.pauseGlow, (float) (Gdx.graphics.getWidth() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8), Color.WHITE) {
            public void onTouchUp() {
                GameHUDBar.this.menuActive = true;
                Gdx.input.setInputProcessor(GameHUDBar.this.gameMenuListener);
            }
        };
        this.gameHUDButtons[1] = new Button(AssetManager.game.endNormal, AssetManager.game.endGlow, (float) (Gdx.graphics.getWidth() - (Gdx.graphics.getHeight() / 4)), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8), Color.WHITE) {
            public void onTouchUp() {
                boolean fired = false;
                if (GameMaster.getInstance().getCurrentPlayer().hasBase()) {
                    Iterator it = GameMaster.getInstance().getCurrentPlayer().getUnits().iterator();
                    while (it.hasNext()) {
                        Unit unit = (Unit) it.next();
                        if (unit instanceof Tank) {
                            ((Tank) unit).fireBullet();
                            fired = true;
                        }
                    }
                    if (!fired) {
                        GameMaster.getInstance().getTurnObserver().passTurn();
                    }
                    GameMaster.getInstance().getActionObserver().setAction(ActionObserver.Action.OBSERVING);
                }
            }
        };
        this.gameHUDButtons[2] = new Button(AssetManager.game.observeNormal, AssetManager.game.observeGlow, (float) (Gdx.graphics.getWidth() - ((Gdx.graphics.getHeight() / 8) * 3)), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8), Color.WHITE) {
            public void onTouchUp() {
                if (GameMaster.getInstance().getActionObserver().getAction().equals(ActionObserver.Action.OBSERVING)) {
                    GameMaster.getInstance().getActionObserver().setAction(ActionObserver.Action.NOTHING);
                } else {
                    GameMaster.getInstance().getActionObserver().setAction(ActionObserver.Action.OBSERVING);
                }
            }
        };
        this.gameHUDButtons[3] = new Button(AssetManager.game.buyNormal, AssetManager.game.buyGlow, (float) (Gdx.graphics.getWidth() - (Gdx.graphics.getHeight() / 2)), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8), Color.WHITE) {
            public void onTouchUp() {
                GameMaster.getInstance().getActionObserver().setAction(ActionObserver.Action.BUILDING);
                if (GameMaster.getInstance().getCurrentPlayer().hasBase()) {
                    GameHUDBar.this.buyMenuActive = true;
                    Gdx.input.setInputProcessor(GameHUDBar.this.buyMenuListener);
                    return;
                }
                GameMaster.getInstance().getActionObserver().setBuyable(ActionObserver.Buyable.BASE);
            }
        };
        makeMenuButtons();
    }

    private void makeMenuButtons() {
        this.gameMenuButtons = new Button[2];
        this.gameMenuButtons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getHeight() / 12)), (float) ((Gdx.graphics.getHeight() / 2) - (Gdx.graphics.getHeight() / 6)), (float) (Gdx.graphics.getHeight() / 6), (float) (Gdx.graphics.getHeight() / 6), Color.RED) {
            public void onTouchUp() {
                Gdx.input.setInputProcessor(null);
                GameMaster.getInstance().goToPreviousMenu();
                GameHUDBar.this.menuActive = false;
            }
        };
        this.gameMenuButtons[1] = new Button(AssetManager.menus.nextNormal, AssetManager.menus.nextGlow, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getHeight() / 12)), (float) (Gdx.graphics.getHeight() / 2), (float) (Gdx.graphics.getHeight() / 6), (float) (Gdx.graphics.getHeight() / 6), Color.BLUE) {
            public void onTouchUp() {
                GameMaster.getInstance().activateInput(0.0f);
                GameHUDBar.this.menuActive = false;
            }
        };
        makeBuyMenuButtons();
    }

    private void makeBuyMenuButtons() {
        this.buyMenuButtons = new Button[4];
        this.buyMenuButtons[0] = new Button(AssetManager.game.tankNormal, AssetManager.game.tankGlow, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getHeight() / 6)), (float) ((Gdx.graphics.getHeight() / 2) - (Gdx.graphics.getHeight() / 6)), (float) (Gdx.graphics.getHeight() / 6), (float) (Gdx.graphics.getHeight() / 6), Color.WHITE) {
            public void onTouchUp() {
                GameMaster.getInstance().getActionObserver().setBuyable(ActionObserver.Buyable.TANK);
                GameMaster.getInstance().activateInput(0.0f);
                GameHUDBar.this.buyMenuActive = false;
            }
        };
        this.buyMenuButtons[1] = new Button(AssetManager.game.wallNormal3, AssetManager.game.wallGlow3, (float) (Gdx.graphics.getWidth() / 2), (float) ((Gdx.graphics.getHeight() / 2) - (Gdx.graphics.getHeight() / 6)), (float) (Gdx.graphics.getHeight() / 6), (float) (Gdx.graphics.getHeight() / 6), Color.WHITE) {
            public void onTouchUp() {
                GameMaster.getInstance().getActionObserver().setBuyable(ActionObserver.Buyable.WALL);
                GameMaster.getInstance().activateInput(0.0f);
                GameHUDBar.this.buyMenuActive = false;
            }
        };
        this.buyMenuButtons[2] = new Button(AssetManager.game.refineryNormal2, AssetManager.game.refineryGlow2, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getHeight() / 6)), (float) (Gdx.graphics.getHeight() / 2), (float) (Gdx.graphics.getHeight() / 6), (float) (Gdx.graphics.getHeight() / 6), Color.WHITE) {
            public void onTouchUp() {
                GameMaster.getInstance().getActionObserver().setBuyable(ActionObserver.Buyable.REFINERY);
                GameMaster.getInstance().activateInput(0.0f);
                GameHUDBar.this.buyMenuActive = false;
            }
        };
        this.buyMenuButtons[3] = new Button(AssetManager.game.laboratoryNormal2, AssetManager.game.laboratoryGlow2, (float) (Gdx.graphics.getWidth() / 2), (float) (Gdx.graphics.getHeight() / 2), (float) (Gdx.graphics.getHeight() / 6), (float) (Gdx.graphics.getHeight() / 6), Color.WHITE) {
            public void onTouchUp() {
                GameMaster.getInstance().getActionObserver().setBuyable(ActionObserver.Buyable.LABORATORY);
                GameMaster.getInstance().activateInput(0.0f);
                GameHUDBar.this.buyMenuActive = false;
            }
        };
    }

    public Button[] getGameHUDButtons() {
        return this.gameHUDButtons;
    }

    public Button[] getGameMenuButtons() {
        return this.gameMenuButtons;
    }

    public Button[] getBuyMenuButtons() {
        return this.buyMenuButtons;
    }

    public void render(SpriteBatch batch, float alpha) {
        batch.begin();
        batch.setColor(1.0f, 1.0f, 1.0f, 0.75f * alpha);
        batch.draw(AssetManager.game.black, 0.0f, (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) Gdx.graphics.getWidth(), (float) (Gdx.graphics.getHeight() / 8));
        for (Button b : this.gameHUDButtons) {
            batch.setColor(1.0f, 1.0f, 1.0f, alpha);
            batch.draw(b.getNormalRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
            batch.setColor(GameMaster.getInstance().getColorWithAlpha(GameMaster.getInstance().getCurrentPlayer().getColor()));
            batch.draw(b.getGlowingRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
        }
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(AssetManager.game.cube, 0.0f, (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8));
        batch.draw(AssetManager.game.potion, (float) ((Gdx.graphics.getHeight() / 8) * 3), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8));
        AssetManager.fonts.normal.setColor(1.0f, 1.0f, 1.0f, alpha);
        AssetManager.fonts.normal.draw((Batch) batch, BuildConfig.VERSION_NAME + GameMaster.getInstance().getCurrentPlayer().getCubes(), (float) (Gdx.graphics.getHeight() / 8), (float) Gdx.graphics.getHeight());
        AssetManager.fonts.normal.draw((Batch) batch, BuildConfig.VERSION_NAME + GameMaster.getInstance().getCurrentPlayer().getTech(), (float) (Gdx.graphics.getHeight() / 2), (float) Gdx.graphics.getHeight());
        AssetManager.fonts.glow.setColor(Color.GREEN.r, Color.GREEN.g, Color.GREEN.b, alpha);
        AssetManager.fonts.glow.draw((Batch) batch, BuildConfig.VERSION_NAME + GameMaster.getInstance().getCurrentPlayer().getCubes(), (float) (Gdx.graphics.getHeight() / 8), (float) Gdx.graphics.getHeight());
        AssetManager.fonts.glow.setColor(Color.PURPLE.r, Color.PURPLE.g, Color.PURPLE.b, alpha);
        AssetManager.fonts.glow.draw((Batch) batch, BuildConfig.VERSION_NAME + GameMaster.getInstance().getCurrentPlayer().getTech(), (float) (Gdx.graphics.getHeight() / 2), (float) Gdx.graphics.getHeight());
        for (int i = 0; i < 3; i++) {
            if (GameMaster.getInstance().getActionObserver().getAction().equals(ActionObserver.Action.BUILDING)) {
                batch.draw(AssetManager.game.transparent, (float) (Gdx.graphics.getWidth() - (Gdx.graphics.getHeight() / 2)), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8));
            } else if (GameMaster.getInstance().getActionObserver().getAction().equals(ActionObserver.Action.OBSERVING)) {
                batch.draw(AssetManager.game.transparent, (float) (Gdx.graphics.getWidth() - ((Gdx.graphics.getHeight() / 8) * 3)), (float) (Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)), (float) (Gdx.graphics.getHeight() / 8), (float) (Gdx.graphics.getHeight() / 8));
            }
        }
        batch.end();
        renderMenu(batch, alpha);
        renderBuyMenu(batch, alpha);
    }

    private void renderMenu(SpriteBatch batch, float alpha) {
        if (this.menuActive) {
            batch.begin();
            batch.setColor(1.0f, 1.0f, 1.0f, 0.75f * alpha);
            batch.draw(AssetManager.game.black, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getWidth() / 4)), (float) ((Gdx.graphics.getHeight() / 2) - (Gdx.graphics.getHeight() / 4)), (float) (Gdx.graphics.getWidth() / 2), (float) (Gdx.graphics.getHeight() / 2));
            for (Button b : this.gameMenuButtons) {
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(b.getNormalRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
                batch.setColor(GameMaster.getInstance().getColorWithAlpha(b.getColor()));
                batch.draw(b.getGlowingRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
            }
            batch.end();
        }
    }

    private void renderBuyMenu(SpriteBatch batch, float alpha) {
        if (this.buyMenuActive) {
            batch.begin();
            batch.setColor(1.0f, 1.0f, 1.0f, 0.75f * alpha);
            batch.draw(AssetManager.game.black, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getWidth() / 4)), (float) ((Gdx.graphics.getHeight() / 2) - (Gdx.graphics.getHeight() / 4)), (float) (Gdx.graphics.getWidth() / 2), (float) (Gdx.graphics.getHeight() / 2));
            for (Button b : this.buyMenuButtons) {
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(b.getNormalRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
                batch.setColor(GameMaster.getInstance().getColorWithAlpha(GameMaster.getInstance().getCurrentPlayer().getColor()));
                batch.draw(b.getGlowingRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
            }
            batch.end();
        }
    }
}
