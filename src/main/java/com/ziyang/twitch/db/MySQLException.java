package com.ziyang.twitch.db;

public class MySQLException extends RuntimeException {
  public MySQLException(String errorMessage) {
    super(errorMessage);
  }
}
