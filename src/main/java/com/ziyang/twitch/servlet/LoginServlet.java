package com.ziyang.twitch.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziyang.twitch.db.MySQLConnection;
import com.ziyang.twitch.db.MySQLException;
import com.ziyang.twitch.entity.LoginRequestBody;
import com.ziyang.twitch.entity.LoginResponseBody;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);
    if (body == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String username = "";
    MySQLConnection connection = null;
    try {
      connection = new MySQLConnection();
      String userId = body.getUserId();
      String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
      username = connection.verifyLogin(userId, password);
    } catch (MySQLException e) {
      throw new ServletException(e);
    } finally {
      connection.close();
    }

    if (!username.isEmpty()) {
      HttpSession session = request.getSession();
      session.setAttribute("user_id", body.getUserId());
      session.setMaxInactiveInterval(600);

      LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), username);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().print(new ObjectMapper().writeValueAsString(loginResponseBody));
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
