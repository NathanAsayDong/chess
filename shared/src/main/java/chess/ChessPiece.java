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
    private ChessPiece.PieceType type;

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
     * Updates a piece type
     *
     * @param type which type of chess piece to set this piece too
     */
    public void setPieceType(ChessPiece.PieceType type) {
        this.type = type;
    }


    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return returnAllPosibilites(board, myPosition);
    }

    //helper functions that im adding
    private Collection<ChessMove> returnAllPosibilites(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        Collection<ChessPosition> possibleEnd = new ArrayList<>();
        switch (this.type) {
            case KING:
                possibleEnd = getPossibleKingPositions(board, myPosition);
                break;
            case QUEEN:
                possibleEnd = getPossibleRookPositions(board, myPosition);
                possibleEnd.addAll(getPossibleBishopPositions(board, myPosition));
                break;
            case BISHOP:
                possibleEnd = getPossibleBishopPositions(board, myPosition);
                break;
            case KNIGHT:
                possibleEnd = getPossibleKnightPositions(board, myPosition);
                break;
            case ROOK:
                possibleEnd = getPossibleRookPositions(board, myPosition);
                break;
            case PAWN:
                possibleEnd = getPossiblePawnPositions(board, myPosition);
                break;
        }
        for (ChessPosition position : possibleEnd) {
            if (this.type == PieceType.PAWN) {
                allMoves.addAll(handlePawnPromotion(board, position, myPosition));
            } else {
                allMoves.add(new ChessMove(myPosition, position, null));
            }
        }
        return allMoves;
    }


    //Helper Functions

    private Collection<ChessMove> handlePawnPromotion(ChessBoard board, ChessPosition position, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        //if its white and top row
        if (this.pieceColor == ChessGame.TeamColor.WHITE && position.getRow() == 8) {
            moves.add(new ChessMove(myPosition, position, PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, position, PieceType.KNIGHT));
            moves.add(new ChessMove(myPosition, position, PieceType.ROOK));
            moves.add(new ChessMove(myPosition, position, PieceType.QUEEN));
        }
        else if (this.pieceColor == ChessGame.TeamColor.BLACK && position.getRow() == 1) {
            moves.add(new ChessMove(myPosition, position, PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, position, PieceType.KNIGHT));
            moves.add(new ChessMove(myPosition, position, PieceType.ROOK));
            moves.add(new ChessMove(myPosition, position, PieceType.QUEEN));
        }
        else {
            moves.add(new ChessMove(myPosition, position, null));
        }
        return moves;
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
            return true;
        } else {
            return false;
        }
    }

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

    private Collection<ChessPosition> getPossibleKingPositions(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessPosition> possibleEnd = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        if (row > 1) {
            if (column > 1) {
                possibleEnd.add(new ChessPosition(row - 1, column - 1));
            }
            if (column < 8) {
                possibleEnd.add(new ChessPosition(row - 1, column + 1));
            }
            possibleEnd.add(new ChessPosition(row - 1, column));
        }
        if (row < 8) {
            if (column > 1) {
                possibleEnd.add(new ChessPosition(row + 1, column - 1));
            }
            if (column < 8) {
                possibleEnd.add(new ChessPosition(row + 1, column + 1));
            }
            possibleEnd.add(new ChessPosition(row + 1, column));
        }
        if (column > 1) {
            possibleEnd.add(new ChessPosition(row, column - 1));
        }
        if (column < 8) {
            possibleEnd.add(new ChessPosition(row, column + 1));
        }
        possibleEnd.removeIf(position -> encounterTeammate(board, position));
        return possibleEnd;
    }

    private Collection<ChessPosition> getPossibleKnightPositions(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessPosition> possibleEnd = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        if (column > 1 && row > 2) {
            possibleEnd.add(new ChessPosition(row - 2, column - 1));
        }
        if (column > 1 && row < 7) {
            possibleEnd.add(new ChessPosition(row + 2, column - 1));
        }
        if (column > 2) {
            if (row > 1) {
                possibleEnd.add(new ChessPosition(row - 1, column - 2));
            }
            if (row < 8) {
                possibleEnd.add(new ChessPosition(row + 1, column - 2));
            }
        }
        if (column < 8) {
            if (row < 7) {
                possibleEnd.add(new ChessPosition(row + 2, column + 1));
            }
            if (row > 2) {
                possibleEnd.add(new ChessPosition(row - 2, column + 1));
            }
        }
        if (column < 7) {
            if (row < 8) {
                possibleEnd.add(new ChessPosition(row + 1, column + 2));
            }
            if (row > 1) {
                possibleEnd.add(new ChessPosition(row - 1, column + 2));
            }
        }
        possibleEnd.removeIf(position -> encounterTeammate(board, position));
        return possibleEnd;
    }

    private Collection<ChessPosition> getPossiblePawnPositions(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessPosition> possibleEnd = new ArrayList<>();
        Collection<ChessPosition> diagnal = new ArrayList<>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        if (this.pieceColor == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 2) {
                ChessPosition singleJump = (new ChessPosition(row + 1, column));
                if (!encounterEnemy(board, singleJump) && !encounterTeammate(board, singleJump)) {
                    possibleEnd.add(new ChessPosition(row + 2, column)); //up two
                }
            }
            if (row < 8) {
                possibleEnd.add(new ChessPosition(row + 1, column)); //up one
            }
            if (row < 8 && column < 8 ) {
                diagnal.add(new ChessPosition(row + 1, column + 1)); //up right
            }
            if (row < 8 && column > 1 ) {
                diagnal.add(new ChessPosition(row + 1, column - 1)); //up left
            }
        }
        if (this.pieceColor == ChessGame.TeamColor.BLACK) {
            if (myPosition.getRow() == 7) {
                ChessPosition singleJump = new ChessPosition(row - 1, column);
                if (!encounterTeammate(board, singleJump) && !encounterEnemy(board, singleJump)) {
                    possibleEnd.add(new ChessPosition(row - 2, column)); //up two
                }
            }
            if (row > 1) {
                possibleEnd.add(new ChessPosition(row - 1, column)); //up one
            }
            if (row > 1 && column > 1 ) {
                diagnal.add(new ChessPosition(row - 1, column - 1)); //down left
            }
            if (row > 1  && column < 8 ) {
                diagnal.add(new ChessPosition(row - 1, column + 1)); //down right
            }
        }
        possibleEnd.removeIf(position -> encounterEnemy(board, position) || encounterTeammate(board, position));
        diagnal.removeIf(position -> !encounterEnemy(board, position));
        possibleEnd.addAll(diagnal);
        return possibleEnd;
    }



}

