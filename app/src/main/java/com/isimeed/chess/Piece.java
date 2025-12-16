package com.isimeed.chess;


import com.isimeed.chess.PieceType;
import com.isimeed.chess.PieceColor;

public class Piece {
    public PieceType type;
    public PieceColor color;

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }
}
