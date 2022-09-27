package dao;

import java.util.ArrayList;
import org.postgresql.geometric.PGpoint;
import resource.Company;
import resource.User;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CompanyDAO extends AbstractDAO {
  public static final String GET_ALL_COMPANIES_QUERY = "SELECT * from web_app.companies ORDER BY name";
  public static final String SEARCH_COMPANY_BY_ADMIN_QUERY = "SELECT name AS company from web_app.companies WHERE admin = ?";
  public static final String SEARCH_COMPANY_BY_MANAGER_QUERY = "SELECT company FROM web_app.departments WHERE manager = ?;";
  public static final String SEARCH_COMPANY_BY_SECRETARY_QUERY = "SELECT company\n" +
          "FROM web_app.secretaries JOIN web_app.departments ON web_app.secretaries.department = web_app.departments.id\n" +
          "WHERE \"user\" = ?  ";
  private static final String GETCOMPANYSQL = "SELECT * FROM web_app.companies WHERE name=?";
  private static final String INSERT_COMPANY_SQL = "INSERT INTO web_app.companies "
    + "(name, address, location, phone, email, admin) VALUES (?, ?, point(?,?), ?, ?, ?)";
  private static final String UPDATE_COMPANY_SQL = "UPDATE web_app.companies SET "
    + "address = ?, location = ?, phone = ?, email = ?, admin = ? WHERE "
    + "name = ?";
  private static final String DELETE_COMPANY_SQL = "DELETE FROM web_app.companies WHERE "
          + "name = ?";
  private static final String GET_COMPANY_BY_ADMIN_SQL = "SELECT * from web_app.companies WHERE "
          + "admin = ?";

  public CompanyDAO(final Connection con) {
    super(con);
  }

  /**
   * @return arraylist of all the companies
   * @throws SQLException
   * @throws NamingException
   */
  public ArrayList<Company> getAllCompanies() throws SQLException, NamingException{
    PreparedStatement stmnt = null;
    ResultSet result = null;
    Company company = null;
    ArrayList<Company> CompanyList = new ArrayList();

    try {
      stmnt = con.prepareStatement(GET_ALL_COMPANIES_QUERY);
      result = stmnt.executeQuery();

      while (result.next()) {
        PGpoint location = (PGpoint)result.getObject("location");
        company = new Company(
                result.getString("name"),
                result.getString("address"),
                location.x,
                location.y,
                result.getString("phone"),
                result.getString("email"),
                result.getString("admin"));
        CompanyList.add(company);
      }
      return CompanyList;
    } finally {
      cleaningOperations(stmnt, result);
    }

  }


  /**
   * Search in companies by name
   *
   * @param name The company name you are looking for
   * @return An instance of Company if a company with given name
   * is found, null otherwise
   */
  public Company getCompanyByName(String name) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    ResultSet result = null;
    Company company = null;
    
      
    try {
      stmnt = con.prepareStatement(GETCOMPANYSQL);
      stmnt.setString(1, name);
      result = stmnt.executeQuery();

      if (result.next()) {
        PGpoint location = (PGpoint)result.getObject("location");
        company = new Company(
          result.getString("name"),
          result.getString("address"),
          location.x,
          location.y,
          result.getString("phone"),
          result.getString("email"),
          result.getString("admin"));
      }
      return company;
    } finally {
      cleaningOperations(stmnt, result);
    }
  }

  /**
   * Search in companies by its administrator user
   *
   * @param user The admin user you are looking for the associated company
   * @return An instance of Company there is any company with the given
   * administrator, null otherwise
   */
  public Company getCompanyByAdmin(User user) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    ResultSet result = null;
    Company company = null;

    try {
      stmnt = con.prepareStatement(GET_COMPANY_BY_ADMIN_SQL);
      stmnt.setString(1, user.getEmail());
      result = stmnt.executeQuery();

      if (result.next()) {
        PGpoint location = (PGpoint)result.getObject("location");
        company = new Company(
          result.getString("name"),
          result.getString("address"),
          location.x,
          location.y,
          result.getString("phone"),
          result.getString("email"),
          result.getString("admin"));
      }
      return company;
    } finally {
      cleaningOperations(stmnt, result);
    }
  }


  /**
   * Create a new company
   *
   * @param company An instance of Company to be inserted into database
   * @return True if new company is successfully created, null in any other
   * cases such as existing company with the same name or other possible
   * SQL errors.
   */
  public boolean insertCompany(Company company) throws SQLException, NamingException {
    PreparedStatement stmnt = null;

    try {
      stmnt = con.prepareStatement(INSERT_COMPANY_SQL);
      stmnt.setString(1, company.getName());
      stmnt.setString(2, company.getAddress());
      stmnt.setDouble(3, company.getLat());
      stmnt.setDouble(4, company.getLat());
      stmnt.setString(5, company.getPhone());
      stmnt.setString(6, company.getEmail());
      stmnt.setString(7, company.getAdminID());
      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }

  /**
   * Update company's information. All information can be changed except
   * company name which is the identifier.
   *
   * @param company Company object with updated information
   * @return true if operation is successfully performed
   */
  public boolean updateCompany(Company company) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    int result = 0;
    PGpoint location = new PGpoint(company.getLat(), company.getLon());

    try {
      stmnt = con.prepareStatement(UPDATE_COMPANY_SQL);
      // SET part
      stmnt.setString(1, company.getAddress());
      stmnt.setObject(2, location);
      stmnt.setString(3, company.getPhone());
      stmnt.setString(4, company.getEmail());
      stmnt.setString(5, company.getAdminID());
      // WHERE part
      stmnt.setString(6, company.getName());
      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }

  /**
   * Delete a company
   *
   * @param company The company that is going to be deleted. It is sufficent
   * to fill just company's name and any other fields are not important and they
   * can be presented or not.
   * @return true if operation is successfully performed
   */
  public boolean deleteCompany(Company company) throws SQLException, NamingException {
    PreparedStatement stmnt = null;
    int result = 0;

    try {
      stmnt = con.prepareStatement(DELETE_COMPANY_SQL);
      stmnt.setString(1, company.getName());

      return (stmnt.executeUpdate() > 0);
    } finally {
      cleaningOperations(stmnt);
    }
  }

  /**
   * Get a company associated to an admin, manager or secretary user.
   *
   * @param userId The user identification to search for their company. Note
   * that user ID is their email address
   * @return Instance of Company if operation is successfully performed,
   * null otherwise
   */
  public Company getCompanyByUser(String userId) throws SQLException, NamingException {
    PreparedStatement stmnt = null;

    ResultSet result = null;

    try {

      UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
      User user = dao.getUserByEmail(userId);
      dao.closeConnection();

      if(user==null || user.getRole().equals(User.Role.customer) ){
        return null;
      }

      switch (user.getRole()){
        case admin:
          stmnt = con.prepareStatement(SEARCH_COMPANY_BY_ADMIN_QUERY);
          break;
        case manager:
          stmnt = con.prepareStatement(SEARCH_COMPANY_BY_MANAGER_QUERY);
          break;
        case secretary:
          stmnt = con.prepareStatement(SEARCH_COMPANY_BY_SECRETARY_QUERY);
          break;
      }

      stmnt.setString(1, userId);
      result = stmnt.executeQuery();

      if (result.next()) {
        return getCompanyByName(result.getString("company"));
      }else{
        return null;
      }
    } finally {
      cleaningOperations(stmnt);
    }
  }
}
