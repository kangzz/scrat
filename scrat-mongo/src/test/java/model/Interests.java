package model;

import java.io.Serializable;

/**
 * Created by kangzz on 16/11/15.
 */
public class Interests implements Serializable{
    private static final long serialVersionUID = -1486321988249888988L;
    private String game;
    private String ball;

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getBall() {
        return ball;
    }

    public void setBall(String ball) {
        this.ball = ball;
    }
}
