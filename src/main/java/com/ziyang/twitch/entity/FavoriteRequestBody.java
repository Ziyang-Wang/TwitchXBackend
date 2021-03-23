package com.ziyang.twitch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class FavoriteRequestBody {
  // only one property, no need builder pattern
  private final Item favoriteItem;

  @JsonCreator
  public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
    this.favoriteItem = favoriteItem;
  }

  public Item getFavoriteItem() {
    return favoriteItem;
  }
}
