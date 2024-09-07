package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //lets just print out all the moves
        //for each possibility, print the row, and column
        Collection<ChessMove> allMoves = returnAllPosibilites(board, myPosition);
        for (ChessMove move : allMoves) {
            System.out.println("row: " + move.getEndPosition().getRow() + " column : " + move.getEndPosition().getColumn());
        }
        return allMoves;
    }

    //helper functions that im adding
    private Collection<ChessMove> returnAllPosibilites(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        Collection<ChessPosition> possibleEnd = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        switch (this.type) {
            case KING:
                possibleEnd.add(new ChessPosition(row + 1, column + 1));
                possibleEnd.add(new ChessPosition(row + 1, column));
                possibleEnd.add(new ChessPosition(row + 1, column - 1));
                possibleEnd.add(new ChessPosition(row, column + 1));
                possibleEnd.add(new ChessPosition(row , column - 1));
                possibleEnd.add(new ChessPosition(row -1 , column + 1));
                possibleEnd.add(new ChessPosition(row - 1, column));
                possibleEnd.add(new ChessPosition(row - 1, column - 1));
                break;
            case QUEEN:
                //horizontal logic
                while (row < 9) {
                    row++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                while (row > 0) {
                    row--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                while (column < 9) {
                    column++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                column = myPosition.getColumn();
                while (column > 0) {
                    column--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                column = myPosition.getColumn();
                //diagnal logic
                while (column > 0 && row > 0) {
                    column--;
                    row--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                column = myPosition.getColumn();
                while (column > 0 && row < 9) {
                    column--;
                    row++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                column = myPosition.getColumn();
                while (column < 9 && row < 9) {
                    column++;
                    row++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                column = myPosition.getColumn();
                while (column < 9 && row > 0) {
                    column++;
                    row--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                break;
            case BISHOP:
                //diagnal logic
                while (column < 9 && row < 9) { //up right
                    column++;
                    row++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                column = myPosition.getColumn();
                while (column < 9 && row > 0) { //down right
                    column++;
                    row--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                column = myPosition.getColumn();

                while (column > 0 && row > 0) { //down left
                    column--;
                    row--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                column = myPosition.getColumn();
                while (column > 0 && row < 9) { //up left
                    column--;
                    row++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                break;
            case KNIGHT:
                possibleEnd.add(new ChessPosition(row + 3, column + 2));
                possibleEnd.add(new ChessPosition(row + 3, column - 2));
                possibleEnd.add(new ChessPosition(row - 3, column + 2));
                possibleEnd.add(new ChessPosition(row - 3, column - 2));
                possibleEnd.add(new ChessPosition(row + 2, column + 3));
                possibleEnd.add(new ChessPosition(row + 2, column - 3));
                possibleEnd.add(new ChessPosition(row - 2, column + 3));
                possibleEnd.add(new ChessPosition(row - 2, column - 3));
                break;
            case ROOK:
                while (row < 9) {
                    row++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                while (row > 0) {
                    row--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                row = myPosition.getRow();
                while (column < 9) {
                    column++;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                column = myPosition.getColumn();
                while (column > 0) {
                    column--;
                    possibleEnd.add(new ChessPosition(row, column));
                }
                column = myPosition.getColumn();
                break;
            case PAWN:
                if (this.pieceColor == ChessGame.TeamColor.WHITE) {
                    possibleEnd.add(new ChessPosition(row + 2, column)); //up two
                    possibleEnd.add(new ChessPosition(row + 1, column)); //up one
                }
                if (this.pieceColor == ChessGame.TeamColor.BLACK) {
                    possibleEnd.add(new ChessPosition(row -2, column)); //up two
                    possibleEnd.add(new ChessPosition(row - 1, column)); //up one
                }
                possibleEnd.add(new ChessPosition(row + 1, column + 1)); //up right
                possibleEnd.add(new ChessPosition(row - 1, column + 1)); //down right
                possibleEnd.add(new ChessPosition(row - 1, column - 1)); //down left
                possibleEnd.add(new ChessPosition(row + 1, column - 1)); //up left
                break;
        }
        //create moves, and validate
        for (ChessPosition position : possibleEnd) {
            column = position.getColumn();
            row = position.getRow();
            if (column > 0 && column < 9 && row > 0 && row < 9) {
                allMoves.add(new ChessMove(myPosition, position, this.type));
            }
        }
        return allMoves;
    }

}