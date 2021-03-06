package app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import app.core.models.User;
import app.core.views.StatisticsView;

/**
 * The Class UserDao, a Data Access Object for fetching User data from Database.
 * 
 * @see User
 */
public class UserDao {

  /**
   * Login user by looking up a user by username and password.
   *
   * @param inputEmail the input email
   * @param inputPassword the input password
   * 
   * @return the logged in user
   * 
   * @throws SQLException the SQL exception
   */
  public User loginUser(String inputEmail, String inputPassword) throws SQLException {
    User result = null;
    Connection conn = ConnectionHandler.getConnection();
    PreparedStatement ps = conn
        .prepareStatement("SELECT * FROM User WHERE email=? AND password=? LIMIT 1");
    ps.setString(1, inputEmail);
    ps.setString(2, inputPassword);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      // Success, update last logged in timestamp
      result = new User(rs);
      updateLastLoggedIn(result);
    }
    conn.close();
    return result;
  }

  /**
   * Update last logged in timestamp.
   *
   * @param user the user
   * @throws SQLException the SQL exception
   */
  private void updateLastLoggedIn(User user) throws SQLException {
    Connection conn = ConnectionHandler.getConnection();
    PreparedStatement ps = conn.prepareStatement(
        "UPDATE User SET lastLoggedIn=current_timestamp() WHERE userID=? LIMIT 1");
    ps.setInt(1, user.getId());
    ps.executeUpdate();
    conn.close();
  }

  /**
   * Gets the user statistics.
   *
   * @return the user statistics
   * @throws SQLException the SQL exception
   */
  public void getUserStatistics() throws SQLException {
    Connection conn = ConnectionHandler.getConnection();
    // Select posts read
    String postsReadQuery = "SELECT userID, count(*) AS PR FROM UserViews" + " GROUP BY userID";
    // Select posts created
    String postsCommentedQuery = "SELECT userID, count(*) AS PC FROM"
        + " Post WHERE parentID IS NULL" + " GROUP BY userID";
    String statisticsQuery = String.format(
        "SELECT email AS 'Username', UV.PR AS 'Number of posts read', P.PC AS 'Number of posts created'"
            + " FROM User as U" + " LEFT JOIN (%s) AS UV ON UV.userID = U.userID"
            + " LEFT JOIN (%s) AS P ON P.userID = U.userID",
        postsReadQuery, postsCommentedQuery);
    PreparedStatement ps = conn.prepareStatement(statisticsQuery);
    ResultSet rs = ps.executeQuery();
    StatisticsView.printStatistics(rs);
    conn.close();
  }

}
