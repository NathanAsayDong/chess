package model;

import chess.ChessGame;

public record GameData(Integer gameId, String whiteUsername, String blackUsername, String gameName, ChessGame game) {}
