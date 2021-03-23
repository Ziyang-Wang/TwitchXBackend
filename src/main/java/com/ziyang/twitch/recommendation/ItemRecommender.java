package com.ziyang.twitch.recommendation;

import com.ziyang.twitch.db.MySQLConnection;
import com.ziyang.twitch.db.MySQLException;
import com.ziyang.twitch.entity.Game;
import com.ziyang.twitch.entity.Item;
import com.ziyang.twitch.entity.ItemType;
import com.ziyang.twitch.external.TwitchClient;
import com.ziyang.twitch.external.TwitchException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemRecommender {
  // top 3 games to recommend when no fav items
  private static final int DEFAULT_GAME_LIMIT = 3;
  private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
  private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;

  // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip].
  // Add items are related to the top games provided in the argument
  private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) throws RecommendationException {
    // recommendation to return
    List<Item> recommendItems = new ArrayList<>();
    TwitchClient client = new TwitchClient();

    for (Game game : topGames) {
      List<Item> items;
      try {
        items = client.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
      } catch (TwitchException e) {
        throw new RecommendationException("Failed to get recommendation result");
      }

      for (Item item : items) {
        if (recommendItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
          return recommendItems;
        }
        recommendItems.add(item);
      }
    }
    return recommendItems;
  }

  // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip]. All items are related to the items previously favorited by the user. E.g., if a user favorited some videos about game "Just Chatting", then it will return some other videos about the same game.
  private List<Item> recommendByFavoriteHistory(
      Set<String> favoriteItemIds, List<String> favoriteGameIds, ItemType type) throws RecommendationException {


    // favoriteGameIds -> [1234, 1234, 2345] -> {1234: 2, 2345: 1}
    // str -> str
    Map<String, Long> favoriteItemIdsByCount = favoriteGameIds.parallelStream().collect(
        Collectors.groupingBy(Function.identity(), Collectors.counting()));

    // favoriteGameIds {2345: 1, 1234: 2} -> {1234: 2, 2345: 1}
    List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount = new ArrayList<>(favoriteItemIdsByCount.entrySet());
    sortedFavoriteGameIdListByCount.sort((e1, e2) ->
        Long.compare(e2.getValue(), e1.getValue()));

    if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
      sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
    }

    List<Item> recommendItems = new ArrayList<>();
    TwitchClient client = new TwitchClient();

    // similar to recommend top games
    for (Map.Entry<String, Long> entry : sortedFavoriteGameIdListByCount) {
      List<Item> items;
      try {
        items = client.searchByType(entry.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
      } catch (TwitchException e) {
        throw new RecommendationException("Failed to get recommendation result");
      }

      for (Item item : items) {
        if (recommendItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
          return recommendItems;
        }

        // do not recommend same fav game
        if (!favoriteItemIds.contains(item.getId())) {
          recommendItems.add(item);
        }
      }
    }
    return recommendItems;
  }

  // Return a map of Item objects as the recommendation result. Keys of the may are [Stream, Video, Clip]. Each key is corresponding to a list of Items objects, each item object is a recommended item based on the top games currently on Twitch.
  public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
    Map<String, List<Item>> recommendedItemMap = new HashMap<>();
    TwitchClient client = new TwitchClient();
    List<Game> topGames;
    try {
      topGames = client.topGames(DEFAULT_GAME_LIMIT);
    } catch (TwitchException e) {
      throw new RecommendationException("Failed to get game data for recommendation");
    }

    for (ItemType type : ItemType.values()) {
      recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
    }
    return recommendedItemMap;
  }

  // Return a map of Item objects as the recommendation result. Keys of the may are [Stream, Video, Clip]. Each key is corresponding to a list of Items objects, each item object is a recommended item based on the previous favorite records by the user.
  public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
    Map<String, List<Item>> recommendedItemMap = new HashMap<>();
    Set<String> favoriteItemIds; // [aaaa, bbbb, cccc, dddd]
    Map<String, List<String>> favoriteGameIds; // {"video": ["1234", "1234", "2345"], "stream": ["3456"], "clip":[]}
    MySQLConnection connection = null;
    try {
      connection = new MySQLConnection();
      favoriteItemIds = connection.getFavoriteItemIds(userId);
      favoriteGameIds = connection.getFavoriteGameIds(favoriteItemIds);
    } catch (MySQLException e) {
      throw new RecommendationException("Failed to get user favorite history for recommendation");
    } finally {
      connection.close();
    }

    for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
      if (entry.getValue().size() == 0) {
        // topGame
        TwitchClient client = new TwitchClient();
        List<Game> topGames;
        try {
          topGames = client.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
          throw new RecommendationException("Failed to get game data for recommendation");
        }
        recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
      } else {
        // history
        recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
      }
    }
    return recommendedItemMap;
  }
}
