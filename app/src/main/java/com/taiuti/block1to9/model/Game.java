package com.taiuti.block1to9.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Game statistics
 */

@Entity(tableName="games")
public class Game {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name="block_played")
    private Integer blocksPlayed;

    @ColumnInfo(name="moves_done")
    private Integer movesDone;
    @ColumnInfo(name="score")
    private Long score;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Integer getBlocksPlayed() {
        return blocksPlayed;
    }

    public void setBlocksPlayed(Integer blocksPlayed) {
        this.blocksPlayed = blocksPlayed;
    }

    public Integer getMovesDone() {
        return movesDone;
    }

    public void setMovesDone(Integer movesDone) {
        this.movesDone = movesDone;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}
