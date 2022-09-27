package dao;

import resource.Department;
import resource.User;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAO extends AbstractDAO {
  private static final String GETUSERSQL = "SELECT * FROM web_app.users WHERE email=?;";
  private static final String INSERT_USER_SQL = "INSERT INTO web_app.users "
    + "(email, name, phone, password,salt, role) VALUES (?, ?, ?, ?, ?, ?::web_app.role)";
  private static final String UPDATE_USER_SQL = "UPDATE web_app.users SET "
    + "email = ?, name = ?, phone = ?, password = ?, salt = ?, role = ?::web_app.role, verified=? WHERE "
    + "email = ?";
  private static final String DELETE_USER_SQL = "DELETE FROM web_app.users WHERE "
    + "email = ?";
  private static final String CHANGE_ROLE_SQL = "UPDATE web_app.users SET "
    + "role = ?::web_app.role WHERE email = ?";
  private static final String GET_SECRETARIES_BY_DEP_SQL = "SELECT * \n" +
          "FROM web_app.secretaries JOIN web_app.users ON web_app.secretaries.user = web_app.users.email\n" +
          "WHERE department = ?";
  private static final String GET_USER_BY_ROLE_SQL = "SELECT * FROM web_app.users WHERE role = ?::web_app.role;";
  private static final String INSERT_SECRETARY = "INSERT INTO web_app.secretaries(\"user\", department) VALUES (?, ?)\n" +
          "ON CONFLICT DO NOTHING";
  private static final String DELETE_SECRETARY_SQL = "DELETE FROM web_app.secretaries WHERE \"user\" = ?;";



  public UserDAO(final Connection con) {
    super(con);
  }

  /**
   * Retrieve from the web_app.users table all users who have the role specified by the parameter r.
   *
   * @param r role of which you want to have the list of users with it
   * @return list of users who have the role specified by the parameter r. The list will be empty if there is no user associated with the required role.
   * @throws SQLException thrown if there is a problem in executing the query to obtain the list of users with the requested role
   * @throws NamingException thrown in case there was a problem connecting to the database
   */
  public List<User> getUserByRole(User.Role r) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    ResultSet result = null;

    try {

      stmnt = con.prepareStatement(GET_USER_BY_ROLE_SQL);
      stmnt.setString(1, r.toString());
      result = stmnt.executeQuery();

      List<User> users = new ArrayList<>();

      while(result.next()) {
        String e = result.getString("email");
        String n = result.getString("name");
        String ph = result.getString("phone");
        String pwd = result.getString("password");
        String salt = result.getString("salt");
        User.Role role = User.Role.valueOf(result.getString("role"));
        boolean verified = result.getBoolean("verified");

        users.add(new User(e, n, ph, pwd, salt, role, verified));
      }

      return users;
    } finally {
      cleaningOperations(stmnt, result);
    }
  }

  /**
   * Retrieval from the web_app.users table of all the information associated with the user identified by the parameter email.
   *
   * @param email email identifying the user of which information should be retrieved
   * @return User object representing the user identified by the email specified as parameter. It will be null if the user does not exist.
   * @throws SQLException thrown in case there was a problem executing the query to retrieve the user's data
   * @throws NamingException thrown in case there was a problem connecting to the database
   */
  public User getUserByEmail(String email) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    ResultSet result = null;

    try {
      stmnt = con.prepareStatement(GETUSERSQL);
      stmnt.setString(1, email);
      result = stmnt.executeQuery();

      if (result.next()) {
        String e = result.getString("email");
        String n = result.getString("name");
        String ph = result.getString("phone");
        String pwd = result.getString("password");
        String salt = result.getString("salt");
        User.Role role = User.Role.valueOf(result.getString("role"));
        boolean verified = result.getBoolean("verified");

        return new User(e, n, ph, pwd, salt, role, verified);
      } else {
        return null;
      }
    } finally {
      cleaningOperations(stmnt, result);
    }
  }

  /**
   * Creation of a new entry in the web_app.users table using the data contained in the fields of the user parameter.
   *
   * @param user new user to be inserted in the database
   * @return true if the entry was successful false if the user is already present (i.e. the user email has already been used)
   * @throws SQLException thrown if there was a problem executing the query for the new user's entry
   * @throws NamingException thrown in case there was a problem connecting to the database
   */
  public boolean insertUser(User user) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    Connection conn = null;

    try {
      stmnt = con.prepareStatement(INSERT_USER_SQL);
      stmnt.setString(1, user.getEmail());
      stmnt.setString(2, user.getName());
      stmnt.setString(3, user.getPhone());
      stmnt.setString(4, user.getPassword());
      stmnt.setString(5, user.getSalt());
      stmnt.setString(6, user.getRole().toString());

      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }

  /**
   * Updating of web_app.users fields associated with the entry identified by the email field of the parameter user.
   *
   * @param user user whose information needs to be updated
   * @return true if the information has been updated correctly false otherwise
   * @throws SQLException thrown if a problem occurred in the execution of the query to update the information of the user in question
   * @throws NamingException thrown in case there was a problem connecting to the database
   */
  public boolean updateUser(User user) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    Connection conn = null;

    int result = 0;

    try {

      stmnt = con.prepareStatement(UPDATE_USER_SQL);
      stmnt.setString(1, user.getEmail());
      stmnt.setString(2, user.getName());
      stmnt.setString(3, user.getPhone());
      stmnt.setString(4, user.getPassword());
      stmnt.setString(5, user.getSalt());
      stmnt.setString(6, user.getRole().toString());
      stmnt.setBoolean(7, user.isVerified());
      stmnt.setString(8, user.getEmail());

      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }

  /**
   * Removal from the web_app.users table of the entry identified by the email field of the parameter user
   *
   * @param user user to be permanently removed
   * @return true if the user has been correctly deleted false otherwise
   * @throws SQLException thrown in case there is a problem in the execution of the query to delete the interested user
   * @throws NamingException thrown in case there was a problem connecting to the database
   */
  public boolean deleteUser(User user) throws SQLException, NamingException {
    PreparedStatement stmnt = null;

    try {
      stmnt = con.prepareStatement(DELETE_USER_SQL);
      stmnt.setString(1, user.getEmail());

      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }


  /**
   * To change role of a user. Once a user is associated to a company, then
   * they are no longer orfinary user and their role must be changed to
   * suitable role such as admin, manager or secretary. Therefore this method
   * can be utilized to change user's role to a new one
   *
   * @param user User object you want to change its role
   * @param newRole A new role for the given user
   * @return true if operation is successful
   */
  public boolean changeRole(User user, User.Role newRole)
    throws SQLException, NamingException {
    PreparedStatement stmnt = null;

    try {
      User userToBeUpdated = getUserByEmail(user.getEmail());

      if(userToBeUpdated==null){
        return false;
      }

      if(userToBeUpdated.getRole().equals(newRole)){
        return true;
      }

      stmnt = con.prepareStatement(CHANGE_ROLE_SQL);
      stmnt.setString(1, newRole.toString());
      stmnt.setString(2, userToBeUpdated.getEmail());

      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }

  public boolean addSecretary(User newSecretary, Department d) throws SQLException, NamingException {
    if(changeRole(newSecretary, User.Role.secretary)){

      PreparedStatement stmnt = null;

      try {
        stmnt = con.prepareStatement(INSERT_SECRETARY);
        stmnt.setString(1, newSecretary.getEmail());
        stmnt.setInt(2, d.getID());

        return (stmnt.executeUpdate() >= 0);
      } finally {
        cleaningOperations(stmnt);
      }
    }else{
      return false;
    }
  }

  public List<User> getSecretariesByDepartment(Department d) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    ResultSet result = null;

    try {
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department dept = depDao.getDepartmentById(d.getID());
      depDao.closeConnection();
      if(dept==null){
        return null;
      }

      stmnt = con.prepareStatement(GET_SECRETARIES_BY_DEP_SQL);
      stmnt.setInt(1, d.getID());
      result = stmnt.executeQuery();

      List<User> secretaries = new ArrayList<>();

      while(result.next()) {
        String e = result.getString("email");
        String n = result.getString("name");
        String ph = result.getString("phone");
        String pwd = result.getString("password");
        String salt = result.getString("salt");
        User.Role role = User.Role.valueOf(result.getString("role"));
        boolean verified = result.getBoolean("verified");

        secretaries.add(new User(e, n, ph, pwd, salt, role, verified));
      }

      return secretaries;
    } finally {
      cleaningOperations(stmnt, result);
    }
  }

  public boolean deleteSecretary(User secretary) throws SQLException, NamingException {
    PreparedStatement stmnt = null;

    try {

      if(!changeRole(secretary, User.Role.customer)){
        return false;
      }

      stmnt = con.prepareStatement(DELETE_SECRETARY_SQL);
      stmnt.setString(1, secretary.getEmail());

      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }
}
