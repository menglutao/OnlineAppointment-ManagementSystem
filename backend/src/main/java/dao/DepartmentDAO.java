package dao;

import resource.Company;
import resource.Department;
import resource.User;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO extends AbstractDAO {

    public DepartmentDAO(final Connection con) {
        super(con);
    }

    /**
     * find  specific department by id
     *
     * @param departmentID The department you can look for
     * @return an instance of Department if a department with you are looking for
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException The exception thrown by the naming classes.
     */
    public  Department getDepartmentById(int departmentID) throws SQLException, NamingException {

        PreparedStatement stmnt = null;
        ResultSet result = null;

        try{
            String query = "SELECT * FROM web_app.departments WHERE id=?;";

            stmnt = con.prepareStatement(query);
            stmnt.setInt(1, departmentID);
            result = stmnt.executeQuery();

            Department department=null;

            if (result.next()){

                department = new Department(
                        result.getInt("ID"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("company"),
                        result.getString("manager")
                );

            }
            return department;
        }
        finally{
            cleaningOperations(stmnt, result);

        }
    }

    /**
     * find the department through its manager
     *
     * @param managerEmail the manager we are looking for the associated department
     * @return department information managed by any manager
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException The exception thrown by the naming classes.
     */

    public  Department getDepartmentByManager(String managerEmail) throws SQLException, NamingException {

        PreparedStatement stmnt = null;
        ResultSet result = null;

        try{

            UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
            User u = dao.getUserByEmail(managerEmail);
            dao.closeConnection();

            if(u==null || !u.getRole().equals(User.Role.secretary)){
                return null;
            }

            String query = "SELECT * FROM web_app.departments WHERE manager=?;";

            stmnt = con.prepareStatement(query);
            stmnt.setString(1, managerEmail);
            result = stmnt.executeQuery();

            Department department=null;

            if (result.next()){

                department = new Department(
                        result.getInt("ID"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("company"),
                        result.getString("manager")
                );

            }
            return department;
        }
        finally{
            cleaningOperations(stmnt, result);

        }
    }

    /**
     * find the department through its secretaryEmail
     *
     * @param secretaryEmail the Secretary we are looking for the associated department
     * @return  the departments in which the secretary works.
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException The exception thrown by the naming classes.
     */

    public  Department getDepartmentBySecretary(String secretaryEmail) throws SQLException, NamingException {

        PreparedStatement stmnt = null;
        ResultSet result = null;

        try{
            UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
            User u = dao.getUserByEmail(secretaryEmail);
            dao.closeConnection();

            if(u==null || !u.getRole().equals(User.Role.secretary)){
                return null;
            }

            String query = "SELECT id, name, description, manager, company\n" +
                    "FROM web_app.departments JOIN web_app.secretaries ON web_app.departments.id =  web_app.secretaries.department\n" +
                    "WHERE \"user\" = ?";

            stmnt = con.prepareStatement(query);
            stmnt.setString(1, secretaryEmail);
            result = stmnt.executeQuery();

            Department department=null;

            if (result.next()){

                department = new Department(
                        result.getInt("ID"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("company"),
                        result.getString("manager")
                );

            }
            return department;
        }
        finally{
            cleaningOperations(stmnt, result);

        }
    }

    /**
     * finds departments of specific company
     *
     * @param company The company we are looking for the associated departments
     * @return list of  departments that belongs to the company
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException The exception thrown by the naming classes.
     */
    public  List<Department> getDepartmentsByCompany (Company company) throws SQLException, NamingException{

        PreparedStatement stmnt = null;
        ResultSet resultSet = null;
        String query = "SELECT * from web_app.departments WHERE company = ?;";
        //String query = "SELECT * FROM web_app.departments JOIN web_app.companies ON web_app.departments.company = web_app.companies.name" +
        //        "WHERE web_app.companies=?";

        try{

            stmnt = con.prepareStatement(query);
            stmnt.setString(1, company.getName());
            ResultSet result = stmnt.executeQuery();

            List<Department> departmentsList = new ArrayList<>();

            while (result.next()){

                Department dept = new Department(
                        result.getInt("ID"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("company"),
                        result.getString("manager")
                );

                departmentsList.add(dept);
            }


            return departmentsList;
        }
        finally {
            cleaningOperations(stmnt);
        }

    }

    /**
     * finds departments of specific company
     *
     * @param company The company we are looking for the associated departments
     * @return list of  departments that belongs to the company
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException The exception thrown by the naming classes.
     */
    public  List<Department> getDepartmentsByCompany (String companyName) throws SQLException, NamingException{

        PreparedStatement stmnt = null;
        ResultSet resultSet = null;
        String query = "SELECT * from web_app.departments WHERE company = ?;";
        //String query = "SELECT * FROM web_app.departments JOIN web_app.companies ON web_app.departments.company = web_app.companies.name" +
        //        "WHERE web_app.companies=?";

        try{

            stmnt = con.prepareStatement(query);
            stmnt.setString(1, companyName);
            ResultSet result = stmnt.executeQuery();

            List<Department> departmentsList = new ArrayList<>();

            while (result.next()){

                Department dept = new Department(
                        result.getInt("ID"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("company"),
                        result.getString("manager")
                );

                departmentsList.add(dept);
            }


            return departmentsList;
        }
        finally {
            cleaningOperations(stmnt);
        }

    }

    /**
     * Create new Department
     *
     * @param newDepartment  An instance of department to be inserted into database
     * @return True if new department is successfully created, null in any other
     *    cases such as existing department with the same name or other possible
     *    SQL errors.
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException  The exception thrown by the naming classes.
     */
    public  boolean insertDepartment(Department newDepartment) throws SQLException, NamingException{
        PreparedStatement stmnt = null;
        String query ="INSERT INTO web_app.departments (name,description, manager,company) values (?,?,?,?);";
        try{
            stmnt = con.prepareStatement(query);

            stmnt.setString(1,newDepartment.getName());
            stmnt.setString(2, newDepartment.getDescription());
            stmnt.setString(3,newDepartment.getManager());
            stmnt.setString(4,newDepartment.getCompanyID());
            return (stmnt.executeUpdate() > 0);
        }

        finally {
            cleaningOperations(stmnt);
        }

    }

    /**
     * Updates the desired information of the department expect departmantId
     *
     * @param department the department object in which we want to update the information
     * @return true if operation is successfully performed
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException The exception thrown by the naming classes.
     */

    public  boolean updateDepartment(Department department) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        String query = "UPDATE web_app.departments SET  name=?, description=?, company=? , manager=? WHERE id=?";

        try{

            CompanyDAO cdao = new CompanyDAO(DataSourceProvider.getDataSource().getConnection());
            Company company = cdao.getCompanyByName(department.getCompanyID());
            cdao.closeConnection();
            if(company==null){
                return false;
            }
            UserDAO udao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
            User user = udao.getUserByEmail(department.getManager());
            udao.closeConnection();
            if(user==null){
                return false;
            }

            stmnt = con.prepareStatement(query);

            stmnt.setString(1,department.getName());
            stmnt.setString(2, department.getDescription());
            stmnt.setString(3,department.getCompanyID());
            stmnt.setString(4,department.getManager());
            stmnt.setInt(5,department.getID());
            return (stmnt.executeUpdate() > 0);
        } finally {
            cleaningOperations(stmnt);
        }
    }

    /**
     * delete a specific department
     *
     * @param department The deparmant that is going to be deleted.
     * It is sufficent to fill just departmant's id
     * @return  true if operation is successfully performed
     * @throws SQLException An exception that provides information on a database access error
     * @throws NamingException  The exception thrown by the naming classes..
     */

    public  boolean deleteDepartment (Department department) throws SQLException, NamingException{
        PreparedStatement stmnt = null;
        String query="DELETE FROM web_app.departments WHERE id=?;";

        try{
            stmnt = con.prepareStatement(query); 
            stmnt.setInt(1,department.getID());

            return (stmnt.executeUpdate() > 0);
        } finally {
            cleaningOperations(stmnt);
        }
    }
}
