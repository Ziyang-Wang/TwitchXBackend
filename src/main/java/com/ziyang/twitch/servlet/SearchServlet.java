package com.ziyang.twitch.servlet;

import com.ziyang.twitch.external.TwitchClient;
import com.ziyang.twitch.external.TwitchException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
    name = "SearchServlet",
    urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String gameId = request.getParameter("game_id");
    // wrong request from front-end
    if (gameId == null) {
      // bad request
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    TwitchClient client = new TwitchClient();
    try {
      // search: return all types; recommendation: recommend by type
      // why convert from json String to object beforehand
      // and here convert from object to json String? necessary?
      // Yes, 1. filter invalid(null, unknown) data 2. add new key-value pairs(url, type)
      ServletUtil.writeItemMap(response, client.searchItems(gameId));
    } catch (TwitchException e) {
      throw new ServletException(e);
    }
  }
}
