package com.isimeed.chess;

public class Move {
    public int fromRow, fromCol, toRow, toCol;
    public Piece capturedPiece;

    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece capturedPiece) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = capturedPiece;
    }
}
