package com.ziyang.twitch.servlet;

import com.ziyang.twitch.entity.Item;
import com.ziyang.twitch.recommendation.ItemRecommender;
import com.ziyang.twitch.recommendation.RecommendationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "RecommendationServlet", urlPatterns = {"/recommendation"})
public class RecommendationServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Session records user login status
    // find in prev session if any (user has logged in before)
    HttpSession session = request.getSession(false);
    ItemRecommender itemRecommender = new ItemRecommender();
    Map<String, List<Item>> itemMap;

    // If the user is successfully logged in, recommend by the favorite records, otherwise recommend by the top games.
    try {
      if (session == null) {
        // user has not successfully logged in
        itemMap = itemRecommender.recommendItemsByDefault();
      } else {
        // user has logged in
        String userId = (String) request.getSession().getAttribute("user_id");
        itemMap = itemRecommender.recommendItemsByUser(userId);
      }
    } catch (RecommendationException e) {
      throw new ServletException(e);
    }

    // write back to response body by utility func
    ServletUtil.writeItemMap(response, itemMap);
  }
}
