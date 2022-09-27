package dao;

import resource.Service;
import resource.Department;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;





public class ServiceDAO extends AbstractDAO {
    public ServiceDAO(final Connection con){
        super(con);
    }
    private static final String SHOW_ALL_SERVICES_SQL = "SELECT id, name, description, EXTRACT(epoch FROM duration) AS duration, department FROM web_app.services ORDER BY id";
    private static final String GET_SERVICE_BY_ID_SQL = "SELECT id, name, EXTRACT(epoch FROM duration)::bigint as duration, description, department\n" +
            "FROM web_app.services\n" +
            "WHERE id = ?";
    private static final String GET_SERVICE_BY_DEPARTMENT_SQL = "SELECT web_app.services.id, name, description, EXTRACT(epoch FROM duration)::bigint as duration, department \n" +
            "FROM web_app.services " +
            "WHERE web_app.services.department=?";
    private static final String INSERT_SERVICE_SQL = "INSERT INTO web_app.services(name, duration, description, department)\n" +
            "\tVALUES (?, ?::interval, ?, ?);";
    private static final String UPDATE_SERVICE_SQL = "UPDATE web_app.services\n" +
            "\tSET name=?, duration=?::interval , description=?, department=?\n" +
            "\tWHERE id=?;";
    private static final String DELETE_SERVICE_SQL = "DELETE FROM web_app.services WHERE id=?;";



    /**
     * @return list of all services
     * @throws SQLException thrown if there is a problem in executing the query to obtain the list of services
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public ArrayList<Service> getAllServices() throws SQLException, NamingException {

        PreparedStatement stmnt = null;
        ResultSet result = null;

        try {
            //conn = DataSourceProvider.getDataSource().getConnection();

            //String query = "SELECT id, name, description, EXTRACT(epoch FROM duration) AS duration, department FROM web_app.services ORDER BY id";

            stmnt = con.prepareStatement(SHOW_ALL_SERVICES_SQL);

            result = stmnt.executeQuery();

            ArrayList<Service> services = new ArrayList<>();
            while (result.next()) {
                String[] durationFromDatabase = result.getString("duration").split(".");
                long durationInSeconds = durationFromDatabase.length > 0 ? Long.parseLong(durationFromDatabase[0]) : 0;
//                String durationAsInterval = result.getString("duration");
                //String[] splittedDuration = durationAsInterval.split(":");
//                durationInSeconds = 3600 * Long.parseLong(splittedDuration[0])
//                        + 60 * Long.parseLong(splittedDuration[1])
//                        + Long.parseLong(splittedDuration[2].split(".")[0]);

                Service service = new Service(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description"),
                        durationInSeconds,
                        result.getInt("department")
                );
                services.add(service);
            }
            return services;
        } catch (Exception e) {
            e.printStackTrace();
            ArrayList<Service> aa = new ArrayList<Service>();
            aa.add(new Service(1, "erro", e.getMessage(), 0, 0));
            return aa;

        } finally {
            cleaningOperations(stmnt, result);
        }
    }

    /**
     * @param serviceID identifying which service information should be retrived
     * @return Service object represented by service ID.
     * @throws SQLException thrown in case there was a problem executing the query to retrieve the service's data
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public Service getServiceById(int serviceID) throws SQLException, NamingException {

        PreparedStatement stmnt = null;
        ResultSet result = null;
        //Connection conn = null;

        try {
            //conn = DataSourceProvider.getDataSource().getConnection();

            /*String query = "SELECT id, name, EXTRACT(epoch FROM duration)::bigint as duration, description, department\n" +
                    "FROM web_app.services\n" +
                    "WHERE id = ?";
                */
            stmnt = con.prepareStatement(GET_SERVICE_BY_ID_SQL);
            stmnt.setInt(1, serviceID);

            result = stmnt.executeQuery();
            Service service = null;

            if (result.next()) {
                service = new Service(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getLong("duration"),
                        result.getInt("department"));
            }
            return service;

        } finally {
            cleaningOperations(stmnt, result);
        }
    }

    /**
     * @param department ,here is departmentID used to retrive services
     * @return list of services corresponding to the department
     * @throws SQLException thrown in case there was a problem executing the query to retrieve the service's data
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public List<Service> getServicesByDepartment(int department) throws SQLException, NamingException {
        //Connection conn = null;
        PreparedStatement stmnt = null;
        /*String query = "SELECT web_app.services.id, name, description, EXTRACT(epoch FROM duration)::bigint as duration, department \n" +
                "FROM web_app.services " +
                "WHERE web_app.services.department=?";
        */
        try {
            stmnt = con.prepareStatement(GET_SERVICE_BY_DEPARTMENT_SQL);
            stmnt.setInt(1,department);

            ResultSet result = stmnt.executeQuery();

            List<Service> serviceList = new ArrayList<>();

            while (result.next()) {

                Service svc = new Service(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getLong("duration"),
                        result.getInt("department"));
                serviceList.add(svc);
            }
            return serviceList;

        } finally {
            cleaningOperations(stmnt);
        }
    }

    /**
     * @param newService identifying the information needed to be inserted
     * @return numbers indicating if the insert operation is successful or not. return -1 or -2 is failed
     * @throws SQLException thrown in case there was a problem executing the query to insert the service's data
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public int insertService(Service newService) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        ResultSet resultSet = null;

        //Connection conn = null;
        /*String query = "INSERT INTO web_app.services(name, duration, description, department)\n" +
                "\tVALUES (?, ?::interval, ?, ?);";
        */
        try {
            DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
            Department dept = depDao.getDepartmentById(newService.getDepartmentID());
            depDao.closeConnection();
            if (dept == null) {
                return -2;
            }

            stmnt = con.prepareStatement(INSERT_SERVICE_SQL, Statement.RETURN_GENERATED_KEYS);
            stmnt.setString(1, newService.getName());
            stmnt.setString(2, Service.durationToString(newService.getDuration()));
            stmnt.setString(3, newService.getDescription());
            stmnt.setInt(4, newService.getDepartmentID());
            int result = stmnt.executeUpdate();

            if (result == 1) {
                resultSet = stmnt.getGeneratedKeys();
                resultSet.next();
                return resultSet.getInt(1);
            } else {
                return -1;
            }

        }
        finally {
            cleaningOperations(stmnt);

        }

    }

    /**
     * @param s service object to be updated
     * @return number indicating if the update operation is successful or not. return -1 or -2 or -3 is failed
     * @throws SQLException thrown in case there was a problem executing the query to update the service's data
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public int updateService(Service s) throws SQLException, NamingException {
        //Connection conn = null;
        PreparedStatement stmnt = null;
        /*String query = "UPDATE web_app.services\n" +
                "\tSET name=?, duration=?::interval , description=?, department=?\n" +
                "\tWHERE id=?;";
        */
        try {

            //conn = DataSourceProvider.getDataSource().getConnection();
            ServiceDAO Sdao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());

            if (Sdao.getServiceById(s.getID()) == null) {
                Sdao.closeConnection();
                return -2;
            }
            DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
            Department dept = depDao.getDepartmentById(s.getDepartmentID());
            depDao.closeConnection();

            if (dept == null) {
                return -3;
            }

            stmnt = con.prepareStatement(UPDATE_SERVICE_SQL);

            stmnt.setString(1, s.getName());
            stmnt.setString(2, Service.durationToString(s.getDuration()));
            stmnt.setString(3, s.getDescription());
            stmnt.setInt(4, s.getDepartmentID());
            stmnt.setInt(5, s.getID());

            int result = stmnt.executeUpdate();
            if (result == 1) {
                return 0;
            } else {
                return -1;
            }

        } finally {
            cleaningOperations(stmnt);

        }
    }

    /**
     * @param serviceID indicating which service needed to be deleted
     * @return numbers indicating if the delete operation is successful or not. return -1 or -2 is failed
     * @throws SQLException thrown in case there was a problem executing the query to delete the service
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public int deleteService(int serviceID) throws SQLException, NamingException {
        //Connection conn = null;
        PreparedStatement stmnt = null;
//        String query = "DELETE FROM web_app.services WHERE id=?;";

        try {
            ServiceDAO Sdao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());


            if (Sdao.getServiceById(serviceID) == null) {
                Sdao.closeConnection();
                return -2;

            }

            //conn = DataSourceProvider.getDataSource().getConnection();

            stmnt = con.prepareStatement(DELETE_SERVICE_SQL);
            stmnt.setInt(1, serviceID);

            int result = stmnt.executeUpdate();

            if (result == 1) {
                return 0;
            } else {
                return -1;
            }

        } finally {
            cleaningOperations(stmnt);
        }

    }


}
