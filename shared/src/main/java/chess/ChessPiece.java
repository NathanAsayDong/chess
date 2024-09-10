package chess;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // this is where the memory address comparison happens
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o; // this is type cast
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
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
        Collection<ChessMove> allMoves = returnAllPosibilites(board, myPosition);
        //filter out all moves that take the spot of a current team piece
        return allMoves;
    }

    //helper functions that im adding
    private Collection<ChessMove> returnAllPosibilites(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        Collection<ChessPosition> possibleEnd = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        System.out.println("start position" + row + column);
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
                possibleEnd = getPossibleRookPositions(board, myPosition);
                possibleEnd.addAll(getPossibleBishopPositions(board, myPosition));
                break;
            case BISHOP:
                possibleEnd = getPossibleBishopPositions(board, myPosition);
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
                possibleEnd = getPossibleRookPositions(board, myPosition);
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
        for (ChessPosition position : possibleEnd) { //used for pawns, kings, and knights
            column = position.getColumn();
            row = position.getRow();
            if (column > 0 && column < 9 && row > 0 && row < 9) {
                allMoves.add(new ChessMove(myPosition, position, null));
            }
        }
        return allMoves;
    }

    private boolean encounterTeammate(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) == null) {
            return false;
        }
        ChessGame.TeamColor encounterColor = board.getPiece(position).pieceColor;
        return this.pieceColor == encounterColor;
    }

    private boolean encounterEnemy(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) == null) {
            return false;
        }
        ChessGame.TeamColor encounterColor = board.getPiece(position).pieceColor;
        if (this.pieceColor != encounterColor) {
            System.out.println("Hit enemy at" + position.toString());
            return true;
        } else {
            return false;
        }
    }

    //Helper Functions
    private Collection<ChessPosition> getPossibleBishopPositions(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessPosition> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        while (column < 8 && row < 8) { //up right
            column++;
            row++;
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) { // error
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        row = myPosition.getRow();
        column = myPosition.getColumn();
        while (column < 8 && row > 1) { //down right
            column++;
            row--;
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        row = myPosition.getRow();
        column = myPosition.getColumn();
        while (column > 1 && row > 1) { //down left
            column--;
            row--;
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        row = myPosition.getRow();
        column = myPosition.getColumn();
        while (column > 1 && row < 8) { //up left
            column--;
            row++;
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        return possibleMoves;
    }

    private Collection<ChessPosition> getPossibleRookPositions(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessPosition> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        while (row < 8) {
            row++;
            if (row == 0 || row == 9) { break; }
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        row = myPosition.getRow();
        while (row > 0) {
            row--;
            if (row == 0 || row == 9) { break; }
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        row = myPosition.getRow();
        while (column < 8) {
            column++;
            if (column == 0 || column == 9) { break; }
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        column = myPosition.getColumn();
        while (column > 0) {
            column--;
            if (column == 0 || column == 9) { break; }
            ChessPosition position = new ChessPosition(row, column);
            if (encounterTeammate(board, position)) {
                break;
            }
            if (encounterEnemy(board, position)) {
                possibleMoves.add(position);
                break;
            }
            possibleMoves.add(position);
        }
        return possibleMoves;
    }




}

