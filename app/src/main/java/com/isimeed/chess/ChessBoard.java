package com.isimeed.chess;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    public Piece[][] board = new Piece[8][8];
    public PieceColor currentTurn = PieceColor.WHITE;
    public List<Move> moveHistory = new ArrayList<>();

    public ChessBoard() {
        setupBoard();
    }

    private void setupBoard() {
        // Pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, PieceColor.BLACK);
            board[6][i] = new Piece(PieceType.PAWN, PieceColor.WHITE);
        }

        // Back rank
        PieceType[] order = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.ROOK};

        for (int i = 0; i < 8; i++) {
            board[0][i] = new Piece(order[i], PieceColor.BLACK);
            board[7][i] = new Piece(order[i], PieceColor.WHITE);
        }
    }

    private boolean isInside(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    private List<int[]> getPseudoMoves(int row, int col) {
        List<int[]> moves = new ArrayList<>();
        Piece piece = board[row][col];
        if (piece == null) return moves;

        switch (piece.type) {
            case PAWN:
                int dir = piece.color == PieceColor.WHITE ? -1 : 1;
                int startRow = piece.color == PieceColor.WHITE ? 6 : 1;

                // Forward moves
                if (isInside(row + dir, col) && board[row + dir][col] == null)
                    moves.add(new int[]{row + dir, col});
                if (row == startRow && board[row + dir][col] == null && board[row + 2*dir][col] == null)
                    moves.add(new int[]{row + 2*dir, col});

                // Diagonal captures
                for (int dc = -1; dc <= 1; dc += 2) {
                    int r = row + dir;
                    int c = col + dc;
                    if (isInside(r, c) && board[r][c] != null && board[r][c].color != piece.color)
                        moves.add(new int[]{r, c});
                }
                break;

            case KNIGHT:
                int[][] knight = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
                for (int[] m : knight) {
                    int r = row + m[0], c = col + m[1];
                    if (isInside(r, c) && (board[r][c] == null || board[r][c].color != piece.color))
                        moves.add(new int[]{r, c});
                }
                break;

            case BISHOP:
                addSliding(moves, row, col, piece, new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}});
                break;

            case ROOK:
                addSliding(moves, row, col, piece, new int[][]{{-1,0},{1,0},{0,-1},{0,1}});
                break;

            case QUEEN:
                addSliding(moves, row, col, piece, new int[][]{{-1,-1},{-1,1},{1,-1},{1,1},{-1,0},{1,0},{0,-1},{0,1}});
                break;

            case KING:
                int[][] king = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
                for (int[] m : king) {
                    int r = row + m[0], c = col + m[1];
                    if (isInside(r, c) && (board[r][c] == null || board[r][c].color != piece.color))
                        moves.add(new int[]{r, c});
                }
                break;
        }
        return moves;
    }

    private void addSliding(List<int[]> moves, int row, int col, Piece piece, int[][] dirs) {
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            while (isInside(r, c)) {
                if (board[r][c] == null)
                    moves.add(new int[]{r, c});
                else {
                    if (board[r][c].color != piece.color)
                        moves.add(new int[]{r, c}); // capture
                    break;
                }
                r += d[0];
                c += d[1];
            }
        }
    }

    public boolean isInCheck(PieceColor color) {
        int kingRow=-1, kingCol=-1;
        for(int r=0;r<8;r++) {
            for(int c=0;c<8;c++) {
                Piece p = board[r][c];
                if(p != null && p.type == PieceType.KING && p.color==color) {
                    kingRow=r; kingCol=c; break;
                }
            }
        }
        if(kingRow==-1) return false;
        PieceColor enemy = color==PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        for(int r=0;r<8;r++)
            for(int c=0;c<8;c++) {
                Piece p = board[r][c];
                if(p!=null && p.color==enemy) {
                    List<int[]> moves = getPseudoMoves(r,c);
                    for(int[] m : moves)
                        if(m[0]==kingRow && m[1]==kingCol) return true;
                }
            }
        return false;
    }

    public List<int[]> getLegalMoves(int row,int col) {
        List<int[]> pseudo = getPseudoMoves(row,col);
        List<int[]> legal = new ArrayList<>();
        Piece piece = board[row][col];
        if(piece==null) return legal;

        for(int[] m : pseudo) {
            Piece captured = board[m[0]][m[1]];
            board[m[0]][m[1]] = piece;
            board[row][col] = null;
            boolean inCheck = isInCheck(piece.color);
            board[row][col] = piece;
            board[m[0]][m[1]] = captured;
            if(!inCheck) legal.add(m);
        }
        return legal;
    }

    public boolean movePiece(int fromRow,int fromCol,int toRow,int toCol) {
        List<int[]> legal = getLegalMoves(fromRow,fromCol);
        for(int[] m : legal){
            if(m[0]==toRow && m[1]==toCol){
                Piece captured = board[toRow][toCol];
                board[toRow][toCol] = board[fromRow][fromCol];
                board[fromRow][fromCol] = null;
                moveHistory.add(new Move(fromRow,fromCol,toRow,toCol,captured));
                currentTurn = currentTurn==PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
                return true;
            }
        }
        return false;
    }

    public void undoLastMove() {
        if(moveHistory.isEmpty()) return;
        Move last = moveHistory.remove(moveHistory.size()-1);
        board[last.fromRow][last.fromCol] = board[last.toRow][last.toCol];
        board[last.toRow][last.toCol] = last.capturedPiece;
        currentTurn = currentTurn==PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }

    public boolean isCheckmate(PieceColor color) {
        for(int r=0;r<8;r++)
            for(int c=0;c<8;c++){
                Piece p = board[r][c];
                if(p!=null && p.color==color && !getLegalMoves(r,c).isEmpty())
                    return false;
            }
        return isInCheck(color);
    }
}
