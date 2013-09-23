package com.eyeofender.dodgeball.connect.table;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

@Entity
@Table(name = "db_perks")
public class Perks {

    @Id
    @Length(max = 16)
    private String name;

    @NotNull
    private int lifeBoost;

    @NotNull
    private int ballBoost;

    @NotNull
    private int airstrikes;

    @NotNull
    private int tripleShots;

    @NotNull
    private int startingBalls;

    @NotNull
    private int extraLives;

    @NotNull
    private boolean lifeGainedOnHit;

    @NotNull
    private boolean speedBoost;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayer(Player player) {
        this.name = player.getName();
    }

    public int getLifeBoost() {
        return lifeBoost;
    }

    public void setLifeBoost(int lifeBoost) {
        this.lifeBoost = lifeBoost;
    }

    public int getBallBoost() {
        return ballBoost;
    }

    public void setBallBoost(int ballBoost) {
        this.ballBoost = ballBoost;
    }

    public int getAirstrikes() {
        return airstrikes;
    }

    public void setAirstrikes(int airstrikes) {
        this.airstrikes = airstrikes;
    }

    public int getTripleShots() {
        return tripleShots;
    }

    public void setTripleShots(int tripleShots) {
        this.tripleShots = tripleShots;
    }

    public int getStartingBalls() {
        return startingBalls;
    }

    public void setStartingBalls(int startingBalls) {
        this.startingBalls = startingBalls;
    }

    public int getExtraLives() {
        return extraLives;
    }

    public void setExtraLives(int extraLives) {
        this.extraLives = extraLives;
    }

    public boolean isLifeGainedOnHit() {
        return lifeGainedOnHit;
    }

    public void setLifeGainedOnHit(boolean lifeGainedOnHit) {
        this.lifeGainedOnHit = lifeGainedOnHit;
    }

    public boolean isSpeedBoost() {
        return speedBoost;
    }

    public void setSpeedBoost(boolean speedBoost) {
        this.speedBoost = speedBoost;
    }

}
