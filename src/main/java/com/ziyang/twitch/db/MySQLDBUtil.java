package com.ziyang.twitch.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MySQLDBUtil {

  public static String getMySQLAddress() throws IOException {

    Properties prop = new Properties();
    String propFileName = "config.properties";

    InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName);
    prop.load(inputStream);

    String username = prop.getProperty("user");
    String password = prop.getProperty("password");
    String instance = prop.getProperty("instance");
    String portNum = prop.getProperty("port_num");
    String dbName = prop.getProperty("db_name");

    return String.format(
        "jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
        instance, portNum, dbName, username, password);
  }
}
