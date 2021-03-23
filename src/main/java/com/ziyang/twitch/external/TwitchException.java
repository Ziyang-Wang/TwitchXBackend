package com.ziyang.twitch.external;

public class TwitchException extends RuntimeException {
  public TwitchException(String errorMessage) {
    super(errorMessage);
  }
}
