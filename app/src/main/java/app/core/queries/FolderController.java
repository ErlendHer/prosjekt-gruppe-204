package app.core.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import app.core.ConnectionHandler;
import app.core.folder.Folder;
import app.core.folder.ThreadPost;

public class FolderController {

  private PreparedStatement getFoldersStatement;
  private PreparedStatement getCoursesStatement;
  private PreparedStatement getThreadsStatement;

  /**
   * Setup the prepared query statements for the controller.
   */
  public void setupQuery() {
    try {
      Connection conn = ConnectionHandler.getConnection();
      getFoldersStatement = conn.prepareStatement(
          "SELECT folderName, folderID, parentID FROM Folder NATURAL JOIN Course WHERE Course.courseCode=(?)");

      getCoursesStatement = conn.prepareStatement("SELECT CourseCode from Course");

      getThreadsStatement = conn
          .prepareStatement("SELECT title, threadId FROM Thread NATURAL JOIN Folder WHERE folderId=(?)");

    } catch (Exception e) {
      System.out.println("Error occured during prepared SELECT statement");
      e.printStackTrace();
    }
  }

  /**
   * Execute an sql query that gets all available folders of the given course and
   * returns a list of therof generated Folder objects.
   * 
   * @param courseCoude course code
   * @return List of folders.
   */
  public ArrayList<Folder> executeGetFolders(String courseCoude) {
    try {
      getFoldersStatement.setString(1, courseCoude);
      var result = getFoldersStatement.executeQuery();

      ArrayList<Folder> folders = new ArrayList<>();

      while (result.next()) {
        folders.add(new Folder(result.getString("folderName"), result.getInt("folderID"), result.getInt("parentID"),
            executeGetThreads(result.getInt("folderId"))));
      }

      return folders;

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Execute an SQL query that returns all availible course codes.
   * 
   * @return
   */
  public ArrayList<String> executeGetCourses() {
    try {
      var result = getCoursesStatement.executeQuery();
      ArrayList<String> courseCodes = new ArrayList<>();

      while (result.next()) {
        courseCodes.add(result.getString("CourseCode"));
      }

      return courseCodes;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get all thread titles of a given folderID
   * 
   * @param folderId folderID
   * @return List of thread ids
   */
  public ArrayList<ThreadPost> executeGetThreads(int folderId) {
    try {
      getThreadsStatement.setInt(1, folderId);
      var result = getThreadsStatement.executeQuery();

      ArrayList<ThreadPost> threads = new ArrayList<>();

      while (result.next()) {
        threads.add(new ThreadPost(result.getString("title"), result.getInt("threadId")));
      }

      return threads;

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }
}
