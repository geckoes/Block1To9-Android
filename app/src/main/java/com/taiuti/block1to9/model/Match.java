package com.taiuti.block1to9.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * The engine of the game
 */

public class Match {

    private String TAG = "Match";
    // the table of the game
    public final Integer COLUMNS = 5;
    public final Integer ROWS = 7;
    public final Integer LINETOSTART = 3;
    // moves after that a new row appears
    private final Integer MOVESTONEXTROW = 5;

    // how many moves you can merge in oblique combination
    public final Integer SPECIAL_MOVES = 5;

    // game statistic
    private int uid;

    private Integer blocksPlayed;

    private Integer movesDone;
    private Long score;

    private Integer movesToNextRow;
    private Integer[][] matrix;
    private List<Integer> rows;
    public Integer currentSpecialMoves;

    boolean gameOver;

    public Match() {
        startNewGame();
    }

    public void startNewGame() {
        blocksPlayed = 0;
        movesDone = 0;
        movesToNextRow = 5;
        score = 0L;
        gameOver = false;
        currentSpecialMoves = 0;

        rows = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            rows.add(-1);
        }

        matrix = new Integer[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                matrix[row][col] = 0;
            }
        }

        for  (int r = 0; r < LINETOSTART; r++) {
//            addRow();
        }

    }

    /**
     * Add row in matrix
     * return gameOver value
     */
    public List<Block> addRow(Context context) {
        List<Block> blocks = new ArrayList<>();

        for (int column = 0; column < COLUMNS; column++) {
            Integer row = getRows(column);
            row++;

            setRows(column, row);

            double random = Math.random();
            double value = 10 * random / 3;
            Integer valueInt = (int) value;
            if (valueInt == 0) {
                valueInt = 1;
            }

            Integer blocks_played = getBlocksPlayed()+1;
            setBlocksPlayed(blocks_played);

            Block block = new Block(context);
            block.set_Id(blocks_played);
            block.setColumn(column);
            block.setRow(row);
            block.setValue(valueInt);

            blocks.add(block);
            setMatrix(row , column, valueInt);

            if (row.equals(ROWS-1)) {
                gameOver = true;
            }

        }
        return blocks;
    }

    public Block makeTheMove(Block checked_block, Block block_removed) {
        // remove block
        setRows(block_removed.getColumn(), getRows(block_removed.getColumn())-1);
        Integer value = checked_block.getValue();

        // add score
        addScore(value);

//        checked_block.setText(String.valueOf(value+1));
        checked_block.setValue(value + 1);

        matrix[checked_block.getRow()][checked_block.getColumn()] = Integer.parseInt(checked_block.getText().toString());
        for (int row = block_removed.getRow(); row < ROWS - 1; row++) {
            matrix[row][block_removed.getColumn()] = matrix[row+1][block_removed.getColumn()];
        }
        matrix[ROWS - 1][block_removed.getColumn()] = 0;

        // statistics
        movesDone++;
        if ((value + 1) % 10 == 0) {
            if (currentSpecialMoves < 0) {
                currentSpecialMoves = 0;
            }
            currentSpecialMoves += SPECIAL_MOVES;
        } else {
            currentSpecialMoves--;
        }

        return checked_block;
    }

    public int checkAvailableMoves() {
        int availableMoves = 0;
        for (int col = 0; col < COLUMNS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (matrix[row][col] == 0) {
                    continue;
                }
                if (row < ROWS - 1) {
                    if (matrix[row][col] == matrix[row + 1][col]) {
                        availableMoves++;
                    }
                }
                if (col < COLUMNS-1) {
                    if (matrix[row][col] == matrix[row][col + 1]) {
                        availableMoves++;
                    }
                }
                if (currentSpecialMoves > 0 && row < ROWS-1 && col < COLUMNS-1) {
                    if (matrix[row][col] == matrix[row + 1][col + 1]) {
                        availableMoves++;
                    }
                }
            }
        }
        return availableMoves;
    }

    /**
     * return the first move checked.
     * Index 0 and 1 are the row and the column of the first block
     * Index 2 and 3 are the row and the colmn of the second block
     * @return Integer[]
     */
    public Integer[] checkTheFirstMove() {

        Integer[] nextMoveRow = new Integer[4];
        outerloop:
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (matrix[row][col] == 0) {
                    continue;
                }
                if (row < ROWS - 1) {
                    if (matrix[row][col] == matrix[row + 1][col]) {
                        nextMoveRow[0] = row;
                        nextMoveRow[1] = col;
                        nextMoveRow[2] = row + 1;
                        nextMoveRow[3] = col;
                        break outerloop;
                    }
                }
                if (col < COLUMNS-1) {
                    if (matrix[row][col] == matrix[row][col + 1]) {
                        nextMoveRow[0] = row;
                        nextMoveRow[1] = col;
                        nextMoveRow[2] = row;
                        nextMoveRow[3] = col + 1;
                        break outerloop;
                    }
                }
                if (currentSpecialMoves > 0 && row < ROWS-1 && col < COLUMNS-1) {
                    if (matrix[row][col] == matrix[row + 1][col + 1]) {
                        nextMoveRow[0] = row;
                        nextMoveRow[1] = col;
                        nextMoveRow[2] = row+1;
                        nextMoveRow[3] = col+1;
                        break outerloop;
                    }
                }
            }
        }

        return nextMoveRow;
    }

    public boolean isMovesToNextRow() {
        if (movesToNextRow == 0) {
            movesToNextRow = MOVESTONEXTROW;
            return true;
        }
        return false;
    }

    public Integer[][] getGameMatrix() {
        return matrix;
    }

    public void setMatrix(Integer row, Integer col, Integer value) {
        this.matrix[row][col] = value;
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

    public Long getScore() {
        return score;
    }

    public void addScore(Integer score) {
        this.score += score;
    }

    public Integer getRows(Integer col) {
        return rows.get(col);
    }

    public void setRows(Integer col, Integer value) {
        this.rows.set(col, value);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void delAMoveToNextRow() {
        this.movesToNextRow--;
    }

    public void resetMovesToNextRow() {
        movesToNextRow = MOVESTONEXTROW;
    }

    /**
     * Save the game in local db.
     * The game will be restored when the game start again
     */
    public void saveTheGame() {

    }

    public void saveTheScore() {

    }

}
