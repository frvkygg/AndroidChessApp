package com.isimeed.chess;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.View;
import android.app.AlertDialog;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int BOARD_SIZE = 8;
    private ChessBoard chessBoard;
    private GridLayout boardGrid;

    private FrameLayout selectedCell = null;
    private int selectedRow = -1, selectedCol = -1;

    private int[][] cellColors = new int[BOARD_SIZE][BOARD_SIZE];
    private List<FrameLayout> highlightedCells = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_main);

        chessBoard = new ChessBoard();
        boardGrid = findViewById(R.id.chessBoard);

        int light = ContextCompat.getColor(this, R.color.board_light);
        int dark = ContextCompat.getColor(this, R.color.board_dark);

        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                cellColors[r][c] = (r + c) % 2 == 0 ? light : dark;

        boardGrid.post(this::drawBoard);
    }


    private void drawBoard() {
        boardGrid.removeAllViews();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;


        int boardSize = Math.min(screenWidth, screenHeight);

        int cellSize = boardSize / BOARD_SIZE;


        boardGrid.getLayoutParams().width = boardSize;
        boardGrid.getLayoutParams().height = boardSize;

        boardGrid.setRowCount(BOARD_SIZE);
        boardGrid.setColumnCount(BOARD_SIZE);


        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                FrameLayout cell = new FrameLayout(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(r), GridLayout.spec(c));
                params.width = cellSize;
                params.height = cellSize;
                cell.setLayoutParams(params);
                cell.setBackgroundColor(cellColors[r][c]);

                int rr = r, cc = c;
                cell.setOnClickListener(v -> handleCellClick(rr, cc, cell));

                addPieceToCell(cell, r, c);
                boardGrid.addView(cell);
            }
        }
        boardGrid.requestLayout();
    }



    private void handleCellClick(int row, int col, FrameLayout cell) {
        Piece piece = chessBoard.board[row][col];

        if (selectedCell == null) {
            if (piece != null && piece.color == chessBoard.currentTurn) {
                selectedCell = cell;
                selectedRow = row;
                selectedCol = col;
                cell.setBackgroundColor(0xFFFFD700); // gold for selected piece

                List<int[]> legalMoves = chessBoard.getLegalMoves(row, col);
                highlightedCells.clear();
                for (int[] m : legalMoves) {
                    FrameLayout target = (FrameLayout) boardGrid.getChildAt(m[0] * BOARD_SIZE + m[1]);
                    addDotToCell(target);
                    highlightedCells.add(target);
                }
            }
        } else {
            boolean moved = chessBoard.movePiece(selectedRow, selectedCol, row, col);
            if (moved) {
                cell.removeAllViews();
                ImageView pieceView = (ImageView) selectedCell.getChildAt(0);
                selectedCell.removeView(pieceView);
                cell.addView(pieceView);

                if (chessBoard.isCheckmate(chessBoard.currentTurn))
                    showCheckmateDialog(chessBoard.currentTurn == PieceColor.WHITE ? "Black wins" : "White wins");
            }

            // Restore original colors and remove dots
            selectedCell.setBackgroundColor(cellColors[selectedRow][selectedCol]);
            for (FrameLayout f : highlightedCells) {
                int pos = boardGrid.indexOfChild(f);
                int r = pos / BOARD_SIZE;
                int c = pos % BOARD_SIZE;
                f.setBackgroundColor(cellColors[r][c]);
                removeDotsFromCell(f);
            }
            highlightedCells.clear();
            selectedCell = null;
        }
    }

    private void addPieceToCell(FrameLayout cell, int row, int col) {
        Piece piece = chessBoard.board[row][col];
        if (piece == null) return;
        int drawableId = getDrawableForPiece(piece);
        ImageView pieceView = new ImageView(this);
        pieceView.setImageResource(drawableId);
        pieceView.setAdjustViewBounds(true);
        pieceView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        pieceView.setLayoutParams(params);
        cell.addView(pieceView);
    }

    private int getDrawableForPiece(Piece piece) {
        switch (piece.type) {
            case KING:
                return piece.color == PieceColor.WHITE ? R.drawable.wk : R.drawable.bk;
            case QUEEN:
                return piece.color == PieceColor.WHITE ? R.drawable.wq : R.drawable.bq;
            case ROOK:
                return piece.color == PieceColor.WHITE ? R.drawable.wr : R.drawable.br;
            case BISHOP:
                return piece.color == PieceColor.WHITE ? R.drawable.wb : R.drawable.bb;
            case KNIGHT:
                return piece.color == PieceColor.WHITE ? R.drawable.wn : R.drawable.bn;
            case PAWN:
                return piece.color == PieceColor.WHITE ? R.drawable.wp : R.drawable.bp;
        }
        return 0;
    }

    private void showCheckmateDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Checkmate!")
                .setMessage(message)
                .setPositiveButton("Restart", (d, w) -> recreate())
                .setCancelable(false)
                .show();
    }

    private void addDotToCell(FrameLayout cell) {
        View dot = new View(this);
        int dotSize = cell.getWidth() / 6;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dotSize, dotSize);
        params.gravity = Gravity.CENTER;
        dot.setLayoutParams(params);
        dot.setBackgroundResource(R.drawable.black_dot);
        cell.addView(dot);
    }

    private void removeDotsFromCell(FrameLayout cell) {
        for (int i = cell.getChildCount() - 1; i >= 0; i--) {
            View v = cell.getChildAt(i);
            if (v.getBackground() != null && v.getBackground().getConstantState() != null) {
                cell.removeView(v);
            }
        }
    }
}
