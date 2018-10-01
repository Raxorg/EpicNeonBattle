package com.epicness.gamelogic;

public class TurnObserver {
    private int turn = 0;

    public void passTurn() {
        this.turn++;
        if (this.turn >= GameMaster.getInstance().getPlayers().length) {
            GameMaster.getInstance().passRound();
            this.turn = 0;
        }
        if (!GameMaster.getInstance().getPlayers()[this.turn].isAlive()) {
            this.turn++;
        }
        if (this.turn >= GameMaster.getInstance().getPlayers().length) {
            GameMaster.getInstance().passRound();
            this.turn = 0;
        }
    }

    public int getTurn() {
        return this.turn;
    }
}
