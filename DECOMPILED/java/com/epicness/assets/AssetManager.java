package com.epicness.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.compression.lzma.Base;

public class AssetManager {
    public static Fonts fonts;
    public static Game game;
    private static AssetManager instance = new AssetManager();
    public static Menus menus;
    public static Sounds sounds;

    public class Fonts {
        public BitmapFont glow;
        public BitmapFont normal;

        private void load() {
            this.normal = new BitmapFont(Gdx.files.internal("Fonts/normal.fnt"));
            this.glow = new BitmapFont(Gdx.files.internal("Fonts/glow.fnt"));
        }
    }

    public class Game {
        public TextureRegion baseCellGlow;
        public TextureRegion baseCellNormal;
        public TextureRegion baseGlow1;
        public TextureRegion baseGlow2;
        public TextureRegion baseGlow3;
        public TextureRegion baseGlow4;
        public TextureRegion baseNormal1;
        public TextureRegion baseNormal2;
        public TextureRegion baseNormal3;
        public TextureRegion baseNormal4;
        public Texture black;
        public TextureRegion blockedCellGlow;
        public TextureRegion blockedCellNormal;
        public TextureRegion bulletGlow;
        public TextureRegion bulletNormal;
        public TextureRegion buyGlow;
        public TextureRegion buyNormal;
        public Texture cells;
        public Texture cube;
        public TextureRegion defaultCellGlow;
        public TextureRegion defaultCellNormal;
        public TextureRegion disabledCellGlow;
        public TextureRegion disabledCellNormal;
        public TextureRegion endGlow;
        public TextureRegion endNormal;
        public Texture gameBarButtons;
        public TextureRegion laboratoryGlow1;
        public TextureRegion laboratoryGlow2;
        public TextureRegion laboratoryNormal1;
        public TextureRegion laboratoryNormal2;
        public TextureRegion observeGlow;
        public TextureRegion observeNormal;
        public TextureRegion pauseGlow;
        public TextureRegion pauseNormal;
        public Texture potion;
        public TextureRegion refineryGlow1;
        public TextureRegion refineryGlow2;
        public TextureRegion refineryNormal1;
        public TextureRegion refineryNormal2;
        public Texture structures;
        public TextureRegion tankGlow;
        public TextureRegion tankNormal;
        public Texture transparent;
        public Texture units;
        public TextureRegion wallGlow1;
        public TextureRegion wallGlow2;
        public TextureRegion wallGlow3;
        public TextureRegion wallNormal1;
        public TextureRegion wallNormal2;
        public TextureRegion wallNormal3;

        private void load() {
            this.black = new Texture("Colors/black.png");
            this.transparent = new Texture("Colors/transparent.png");
            this.cube = new Texture("Images/Game/Specials/cube.png");
            this.potion = new Texture("Images/Game/Specials/potion.png");
            this.cells = new Texture("Images/Game/cells.png");
            this.units = new Texture("Images/Game/Placeables/units.png");
            this.structures = new Texture("Images/Game/Placeables/structures.png");
            this.gameBarButtons = new Texture("Images/Game/HUDButtons/gameBarButtons.png");
            this.defaultCellNormal = new TextureRegion(this.cells, 0, 0, 64, 64);
            this.defaultCellGlow = new TextureRegion(this.cells, 0, 64, 64, 64);
            this.blockedCellNormal = new TextureRegion(this.cells, 64, 0, 64, 64);
            this.blockedCellGlow = new TextureRegion(this.cells, 64, 64, 64, 64);
            this.disabledCellNormal = new TextureRegion(this.cells, Base.kNumFullDistances, 0, 64, 64);
            this.disabledCellGlow = new TextureRegion(this.cells, Base.kNumFullDistances, 64, 64, 64);
            this.baseCellNormal = new TextureRegion(this.cells, 192, 0, 64, 64);
            this.baseCellGlow = new TextureRegion(this.cells, 192, 64, 64, 64);
            this.tankNormal = new TextureRegion(this.units, 0, 0, 64, 64);
            this.tankGlow = new TextureRegion(this.units, 0, 64, 64, 64);
            this.wallNormal3 = new TextureRegion(this.structures, 0, 0, 64, 64);
            this.wallGlow3 = new TextureRegion(this.structures, 0, 64, 64, 64);
            this.wallNormal2 = new TextureRegion(this.structures, 64, 0, 64, 64);
            this.wallGlow2 = new TextureRegion(this.structures, 64, 64, 64, 64);
            this.wallNormal1 = new TextureRegion(this.structures, Base.kNumFullDistances, 0, 64, 64);
            this.wallGlow1 = new TextureRegion(this.structures, Base.kNumFullDistances, 64, 64, 64);
            this.baseNormal4 = new TextureRegion(this.structures, 192, 0, 64, 64);
            this.baseGlow4 = new TextureRegion(this.structures, 192, 64, 64, 64);
            this.baseNormal3 = new TextureRegion(this.structures, (int) Usage.BiNormal, 0, 64, 64);
            this.baseGlow3 = new TextureRegion(this.structures, (int) Usage.BiNormal, 64, 64, 64);
            this.baseNormal2 = new TextureRegion(this.structures, 320, 0, 64, 64);
            this.baseGlow2 = new TextureRegion(this.structures, 320, 64, 64, 64);
            this.baseNormal1 = new TextureRegion(this.structures, 384, 0, 64, 64);
            this.baseGlow1 = new TextureRegion(this.structures, 384, 64, 64, 64);
            this.refineryNormal2 = new TextureRegion(this.structures, 0, Base.kNumFullDistances, 64, 64);
            this.refineryGlow2 = new TextureRegion(this.structures, 0, 192, 64, 64);
            this.refineryNormal1 = new TextureRegion(this.structures, 64, Base.kNumFullDistances, 64, 64);
            this.refineryGlow1 = new TextureRegion(this.structures, 64, 192, 64, 64);
            this.laboratoryNormal2 = new TextureRegion(this.structures, Base.kNumFullDistances, Base.kNumFullDistances, 64, 64);
            this.laboratoryGlow2 = new TextureRegion(this.structures, Base.kNumFullDistances, 192, 64, 64);
            this.laboratoryNormal1 = new TextureRegion(this.structures, 192, Base.kNumFullDistances, 64, 64);
            this.laboratoryGlow1 = new TextureRegion(this.structures, 192, 192, 64, 64);
            this.bulletNormal = new TextureRegion(this.units, 64, 0, 64, 64);
            this.bulletGlow = new TextureRegion(this.units, 64, 64, 64, 64);
            this.pauseNormal = new TextureRegion(this.gameBarButtons, 0, 0, GL20.GL_NEVER, GL20.GL_NEVER);
            this.pauseGlow = new TextureRegion(this.gameBarButtons, 0, GL20.GL_NEVER, GL20.GL_NEVER, GL20.GL_NEVER);
            this.endNormal = new TextureRegion(this.gameBarButtons, GL20.GL_NEVER, 0, GL20.GL_NEVER, GL20.GL_NEVER);
            this.endGlow = new TextureRegion(this.gameBarButtons, GL20.GL_NEVER, GL20.GL_NEVER, GL20.GL_NEVER, GL20.GL_NEVER);
            this.buyNormal = new TextureRegion(this.gameBarButtons, (int) GL20.GL_STENCIL_BUFFER_BIT, 0, GL20.GL_NEVER, GL20.GL_NEVER);
            this.buyGlow = new TextureRegion(this.gameBarButtons, (int) GL20.GL_STENCIL_BUFFER_BIT, GL20.GL_NEVER, GL20.GL_NEVER, GL20.GL_NEVER);
            this.observeNormal = new TextureRegion(this.gameBarButtons, 1536, 0, GL20.GL_NEVER, GL20.GL_NEVER);
            this.observeGlow = new TextureRegion(this.gameBarButtons, 1536, GL20.GL_NEVER, GL20.GL_NEVER, GL20.GL_NEVER);
        }
    }

    public class Menus {
        public TextureRegion backGlow;
        public TextureRegion backNormal;
        public TextureRegion beginGlow;
        public TextureRegion beginNormal;
        public Texture commonMenuButtons;
        public TextureRegion creditsGlow;
        public TextureRegion creditsNormal;
        public TextureRegion defaultGlow;
        public TextureRegion defaultNormal;
        public TextureRegion editGlow;
        public TextureRegion editNormal;
        public Texture editing;
        public TextureRegion emptyFrameGlow;
        public TextureRegion emptyFrameNormal;
        public Texture loadingCircle;
        public TextureRegion loadingCircleRegion;
        public Texture mainMenuButtons;
        public Texture mapAtlasA;
        public TextureRegion mapGlow;
        public TextureRegion mapNormal;
        public TextureRegion missionsGlow;
        public TextureRegion missionsNormal;
        public Texture modeSelectionButtons;
        public Texture multiplayerButtons;
        public TextureRegion multiplayerGlow;
        public TextureRegion multiplayerNormal;
        public TextureRegion nextGlow;
        public TextureRegion nextNormal;
        public TextureRegion optionsGlow;
        public TextureRegion optionsNormal;
        public TextureRegion playGlow;
        public TextureRegion playNormal;
        public TextureRegion playersGlow;
        public TextureRegion playersNormal;
        public TextureRegion plusGlow;
        public TextureRegion plusNormal;
        public TextureRegion scoresGlow;
        public TextureRegion scoresNormal;
        public TextureRegion squareGlow;
        public TextureRegion squareNormal;
        public TextureRegion tutorialGlow;
        public TextureRegion tutorialNormal;
        public Texture victory;

        private void load() {
            this.commonMenuButtons = new Texture("Images/Menus/commonButtons.png");
            this.loadingCircle = new Texture("Images/loading.png");
            this.victory = new Texture("Images/victory.png");
            this.loadingCircleRegion = new TextureRegion(this.loadingCircle);
            this.backNormal = new TextureRegion(this.commonMenuButtons, 0, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.backGlow = new TextureRegion(this.commonMenuButtons, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.mainMenuButtons = new Texture("Images/Menus/mainMenuButtons.png");
            this.playNormal = new TextureRegion(this.mainMenuButtons, 0, (int) GL20.GL_NEVER, 1536, (int) GL20.GL_NEVER);
            this.playGlow = new TextureRegion(this.mainMenuButtons, 0, 1536, 1536, (int) GL20.GL_NEVER);
            this.optionsNormal = new TextureRegion(this.mainMenuButtons, 1536, 0, 1536, (int) GL20.GL_NEVER);
            this.optionsGlow = new TextureRegion(this.mainMenuButtons, 1536, (int) GL20.GL_STENCIL_BUFFER_BIT, 1536, (int) GL20.GL_NEVER);
            this.scoresNormal = new TextureRegion(this.mainMenuButtons, 1536, (int) GL20.GL_NEVER, 1536, (int) GL20.GL_NEVER);
            this.scoresGlow = new TextureRegion(this.mainMenuButtons, 1536, 1536, 1536, (int) GL20.GL_NEVER);
            this.creditsNormal = new TextureRegion(this.mainMenuButtons, 0, 0, 1536, (int) GL20.GL_NEVER);
            this.creditsGlow = new TextureRegion(this.mainMenuButtons, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, 1536, (int) GL20.GL_NEVER);
            this.modeSelectionButtons = new Texture("Images/Menus/modeSelectionButtons.png");
            this.missionsNormal = new TextureRegion(this.modeSelectionButtons, 0, 0, 1536, (int) GL20.GL_NEVER);
            this.missionsGlow = new TextureRegion(this.modeSelectionButtons, 0, (int) GL20.GL_NEVER, 1536, (int) GL20.GL_NEVER);
            this.tutorialNormal = new TextureRegion(this.modeSelectionButtons, 1536, 0, 1536, (int) GL20.GL_NEVER);
            this.tutorialGlow = new TextureRegion(this.modeSelectionButtons, 1536, (int) GL20.GL_NEVER, 1536, (int) GL20.GL_NEVER);
            this.multiplayerNormal = new TextureRegion(this.modeSelectionButtons, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, 1536, (int) GL20.GL_NEVER);
            this.multiplayerGlow = new TextureRegion(this.modeSelectionButtons, 0, 1536, 1536, (int) GL20.GL_NEVER);
            this.multiplayerButtons = new Texture("Images/Menus/multiplayerButtons.png");
            this.mapNormal = new TextureRegion(this.multiplayerButtons, 0, 0, 1536, (int) GL20.GL_NEVER);
            this.mapGlow = new TextureRegion(this.multiplayerButtons, 0, (int) GL20.GL_NEVER, 1536, (int) GL20.GL_NEVER);
            this.playersNormal = new TextureRegion(this.multiplayerButtons, 1536, 0, 1536, (int) GL20.GL_NEVER);
            this.playersGlow = new TextureRegion(this.multiplayerButtons, 1536, (int) GL20.GL_NEVER, 1536, (int) GL20.GL_NEVER);
            this.beginNormal = new TextureRegion(this.multiplayerButtons, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, 1536, (int) GL20.GL_NEVER);
            this.beginGlow = new TextureRegion(this.multiplayerButtons, 0, 1536, 1536, (int) GL20.GL_NEVER);
            this.mapAtlasA = new Texture("Images/Menus/mapAtlasA.png");
            this.defaultNormal = new TextureRegion(this.mapAtlasA, 0, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT);
            this.defaultGlow = new TextureRegion(this.mapAtlasA, (int) GL20.GL_STENCIL_BUFFER_BIT, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT);
            this.squareNormal = new TextureRegion(this.mapAtlasA, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT);
            this.squareGlow = new TextureRegion(this.mapAtlasA, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT, (int) GL20.GL_STENCIL_BUFFER_BIT);
            this.nextNormal = new TextureRegion(this.commonMenuButtons, (int) GL20.GL_NEVER, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.nextGlow = new TextureRegion(this.commonMenuButtons, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.editing = new Texture("Images/Menus/editing.png");
            this.editNormal = new TextureRegion(this.editing, 0, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.editGlow = new TextureRegion(this.editing, (int) GL20.GL_NEVER, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.plusNormal = new TextureRegion(this.editing, (int) GL20.GL_STENCIL_BUFFER_BIT, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.plusGlow = new TextureRegion(this.editing, 1536, 0, (int) GL20.GL_NEVER, (int) GL20.GL_NEVER);
            this.emptyFrameNormal = new TextureRegion(this.editing, 0, (int) GL20.GL_NEVER, 2048, (int) GL20.GL_NEVER);
            this.emptyFrameGlow = new TextureRegion(this.editing, 0, (int) GL20.GL_STENCIL_BUFFER_BIT, 2048, (int) GL20.GL_NEVER);
        }
    }

    public class Sounds {
        public Music background;
        public Sound buttonTouchUp;
        public Sound explosion;
        public Sound fireBullet;

        private void load() {
            this.background = Gdx.audio.newMusic(Gdx.files.internal("Sounds/Music/background.wav"));
            this.buttonTouchUp = Gdx.audio.newSound(Gdx.files.internal("Sounds/Menus/buttonTouchUp.ogg"));
            this.explosion = Gdx.audio.newSound(Gdx.files.internal("Sounds/Game/explosion.wav"));
            this.fireBullet = Gdx.audio.newSound(Gdx.files.internal("Sounds/Game/firebullet.wav"));
        }
    }

    private AssetManager() {
        fonts = new Fonts();
        menus = new Menus();
        sounds = new Sounds();
        game = new Game();
    }

    public static AssetManager getInstance() {
        return instance;
    }

    public void loadFonts() {
        fonts.load();
    }

    public void loadMenus() {
        menus.load();
    }

    public void loadSounds() {
        sounds.load();
    }

    public void loadGame() {
        game.load();
    }
}
