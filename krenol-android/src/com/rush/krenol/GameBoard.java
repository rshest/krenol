package com.rush.krenol;

public class GameBoard {
    public static final byte CELL_EMPTY = 0;
    public static final byte CELL_BLACK = 1;
    public static final byte CELL_WHITE = 2;

    public static final int NUM_TO_WIN = 5; // 5 in a row to win

    public byte[] mCells;
    public int mBoardW;
    public int mBoardH;

    public GameBoard(int boardW, int boardH) {
        mBoardW = boardW;
        mBoardH = boardH;
        mCells = new byte[boardW*boardH];
    }

    public void setCell(int cellX, int cellY, byte cellState) {
        mCells[cellX + cellY*mBoardW] = cellState;
    }

    public int getNumCells() {
        return mBoardH*mBoardW;
    }

    public byte getCell(int cellX, int cellY) {
        return mCells[cellX + cellY*mBoardW];
    }

    public byte getWinner() {
        // rows
        int curCell = 0;
        for (int j = 0; j < mBoardH; j++) {
            int score = 0;
            //  traverse the cells in the row
            for (int i = 0; i < mBoardW; i++) {
                byte c = mCells[curCell];
                if (c == GameBoard.CELL_EMPTY) {
                    score = 0;
                } else {
                    score += (c == GameBoard.CELL_WHITE) ? 1 : -1;
                }
                if (Math.abs(score) == NUM_TO_WIN) {
                    return (score > 0) ? GameBoard.CELL_WHITE : GameBoard.CELL_BLACK;
                }
                curCell++;
            }
        }

        // columns
        for (int i = 0; i < mBoardW; i++) {
            curCell = i;
            int score = 0;
            //  traverse the cells in the column
            for (int j = 0; j < mBoardH; j++) {
                byte c = mCells[curCell];
                if (c == GameBoard.CELL_EMPTY) {
                    score = 0;
                } else {
                    score += (c == GameBoard.CELL_WHITE) ? 1 : -1;
                }
                if (Math.abs(score) == NUM_TO_WIN) return (score > 0) ? GameBoard.CELL_WHITE : GameBoard.CELL_BLACK;
                curCell += mBoardW;
            }
        }

        //  diagonals south-east
        //for (int i = mBoardH - NUM_TO_WIN)

        //  diagonals south-west

        return GameBoard.CELL_EMPTY;
    }
}
