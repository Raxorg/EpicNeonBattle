package com.frontanilla.epicneonbattle.gamelogic;

import java.util.ArrayList;

public class Team {
    private boolean defeated = false;
    private ArrayList<Player> players = new ArrayList();

    public void addPlayer(Player p) {
        p.setTeam(this);
        this.players.add(p);
    }

    public void defeat() {
        this.defeated = true;
    }
}
