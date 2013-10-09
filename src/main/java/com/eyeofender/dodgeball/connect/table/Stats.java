package com.eyeofender.dodgeball.connect.table;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

@Entity
@Table(name = "db_stats")
public class Stats {

    @Id
    @Length(max = 16)
    private String name;

    @NotNull
    private int gamesPlayed;

    @NotNull
    private int gamesWon;

    @NotNull
    private int gamesLost;

    @NotNull
    private int totalHits;

    @NotNull
    private int totalMisses;

    @NotNull
    private int totalHarm;

    @NotNull
    private int trippleShotsFired;

    @NotNull
    private int airstrikesFired;

    @Column
    private Date lastSeen;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayer(Player player) {
        this.name = player.getName();
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public int getTotalMisses() {
        return totalMisses;
    }

    public void setTotalMisses(int totalMisses) {
        this.totalMisses = totalMisses;
    }

    public int getTotalHarm() {
        return totalHarm;
    }

    public void setTotalHarm(int totalHarm) {
        this.totalHarm = totalHarm;
    }

    public int getTrippleShotsFired() {
        return trippleShotsFired;
    }

    public void setTrippleShotsFired(int trippleShotsFired) {
        this.trippleShotsFired = trippleShotsFired;
    }

    public int getAirstrikesFired() {
        return airstrikesFired;
    }

    public void setAirstrikesFired(int airstrikesFired) {
        this.airstrikesFired = airstrikesFired;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

}
