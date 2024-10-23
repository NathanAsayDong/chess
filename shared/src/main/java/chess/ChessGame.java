package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor currentTeam;
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard(); // Get a fresh board
        this.setTeamTurn(TeamColor.WHITE); // White Starts
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = this.board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> allMoves = piece.pieceMoves(this.board, startPosition);
        allMoves = allMoves.stream().filter(
                move -> {
                    try {
                        ChessGame game = new ChessGame();
                        game.setBoard(this.board.makeCopy());
                        game.setTeamTurn(piece.getTeamColor());
                        game.makeMove(move);
                        return true;
                    } catch (InvalidMoveException e) {
                        return false;
                    }
                }
        ).toList();
        return allMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        boolean caputuredPiece = false;
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        ChessPiece endPiece = this.board.getPiece(move.getEndPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(this.board, move.getStartPosition());
        if (piece.getTeamColor() != this.currentTeam) {
            throw new InvalidMoveException("Not this team's turn");
        }
        if (endPiece != null && endPiece.getTeamColor() == this.currentTeam) {
            throw new InvalidMoveException("Cannot take own piece");
        }
        if (move.getPromotionPiece() != null) {
            piece.setPieceType(move.getPromotionPiece());
        }
        if (!possibleMoves.contains(move)) {
            throw new InvalidMoveException("Move not possible for piece");
        }
        ChessGame game = new ChessGame();
        game.setBoard(this.board.makeCopy());
        game.getBoard().removePiece(move.getStartPosition());
        game.getBoard().addPiece(move.getEndPosition(), piece);
        game.setTeamTurn(piece.getTeamColor());
        if (game.isInCheck(piece.getTeamColor())) {
            throw new InvalidMoveException("Move puts own king in check");
        }
        if (endPiece != null && endPiece.getTeamColor() != this.currentTeam) {
            caputuredPiece = true;
        }
        this.board.removePiece(move.getStartPosition());
        this.board.addPiece(move.getEndPosition(), piece);
        if (!caputuredPiece) {
            this.setTeamTurn(this.currentTeam == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        }
//        System.out.println("board is now \n" + this.board.toString());
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessPiece> allPieces = this.board.getAllPieces();
        for (ChessPiece piece : allPieces) {
            if (piece.getTeamColor() != teamColor) {
                Collection<ChessMove> moves = piece.pieceMoves(this.board, this.board.getPiecePosition(piece));
                for (ChessMove move : moves) {
                    ChessPiece endPiece = this.board.getPiece(move.getEndPosition());
                    if (endPiece == null) {
                        continue;
                    }
                    if (this.board.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.KING && this.board.getPiece(move.getEndPosition()).getTeamColor() == teamColor) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Collection<ChessPiece> allPieces = this.board.getAllPieces();
        for (ChessPiece piece : allPieces) {
            if (piece.getTeamColor() != teamColor) {
                continue;
            } else {
                Collection<ChessMove> moves = piece.pieceMoves(this.board, this.board.getPiecePosition(piece));
                for (ChessMove move : moves) {
                    ChessGame game = new ChessGame();
                    game.setBoard(this.board.makeCopy());
                    game.setTeamTurn(teamColor);
                    try {
                        game.makeMove(move);
                        return false;
                    } catch (InvalidMoveException e) {
                        continue;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (this.isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessPiece> allPieces = this.board.getAllPieces();
        //returns true if the given team has no legal moves but the king is not in immediate danger
        for (ChessPiece piece : allPieces) {
            if (piece.getTeamColor() != teamColor) {
                continue;
            } else {
                Collection<ChessMove> moves = piece.pieceMoves(this.board, this.board.getPiecePosition(piece));
                for (ChessMove move : moves) {
                    ChessGame game = new ChessGame();
                    game.setBoard(this.board.makeCopy());
                    game.setTeamTurn(teamColor);
                    ChessPiece endPiece = game.getBoard().getPiece(move.getEndPosition());
                    try {
                        game.makeMove(move);
                        if (!game.isInCheck(teamColor)) {
                            return false;
                        }
                    } catch (InvalidMoveException e) {
                        continue;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
