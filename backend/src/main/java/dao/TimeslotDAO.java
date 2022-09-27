package dao;

import resource.Appointment;
import resource.Service;
import resource.Timeslot;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
public class TimeslotDAO extends AbstractDAO {

    private static final String INSERT_QUERY = "INSERT INTO web_app.timeslots (service, datetime, places ) VALUES (?, ?, ?);";
    private static final String UPDATE_QUERY = "UPDATE web_app.timeslots SET places = ? WHERE (service = ? AND datetime = ?);";
    private static final String DELETE_QUERY = "DELETE FROM web_app.timeslots WHERE (service = ? AND datetime = ?);";
    private static final String GET_BY_SERVICE_QUERY = "SELECT * FROM web_app.timeslots WHERE (service = ?);";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM web_app.timeslots WHERE (service = ? AND datetime = ?);";
    private static final String AVAILABLE_PLACES_QUERY  = "SELECT reserved.s AS service, reserved.d AS datetime, web_app.timeslots.places - reserved.reservedPlaces AS available\n" +
            "FROM web_app.timeslots JOIN (\n" +
            "\tSELECT web_app.appointments.service AS s, web_app.appointments.datetime AS d, COUNT(*) AS reservedPlaces\n" +
            "\tFROM web_app.appointments JOIN web_app.timeslots ON web_app.appointments.datetime = web_app.timeslots.datetime AND web_app.appointments.service = web_app.timeslots.service\n" +
            "\tGROUP BY web_app.appointments.datetime, web_app.appointments.service\n" +
            "\t) AS reserved ON reserved.d = web_app.timeslots.datetime AND reserved.s = web_app.timeslots.service\n" +
            "WHERE reserved.s = ? AND reserved.d = ?";

  public TimeslotDAO(Connection con) {
    super(con);
  }

  /**
   * Insert new timeslot into database.
   *
   * @param timeslot New timeslot object that is going to be insterted
   * @return true if the operation is successful
   */
    public boolean insertTimeslot(Timeslot timeslot) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        int count;


        try{
            stmnt = con.prepareStatement(INSERT_QUERY);

            stmnt.setInt(1, timeslot.getServiceID());
            stmnt.setTimestamp(2, timeslot.getDatetime());
            stmnt.setInt(3, timeslot.getPlaces());

            count = stmnt.executeUpdate();

            return count > 0;

        } finally {
            cleaningOperations(stmnt);
        }
    }

  /**
   * Update a timeslot. Note that just Places field will be updated
   * and other fields won't be touched.
   *
   * @param timeslot Timeslot with updated Places field. ServiceID and datetime
   * is used to find existing timeslot
   * @return true if operation is successful
   */
    public boolean updateTimeslot(Timeslot timeslot) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        int count;

        try{
            stmnt = con.prepareStatement(UPDATE_QUERY);
            stmnt.setInt(1, timeslot.getPlaces());
            stmnt.setInt(2, timeslot.getServiceID());
            stmnt.setTimestamp(3, timeslot.getDatetime());
            count = stmnt.executeUpdate();

            return count > 0;

        } finally {
            cleaningOperations(stmnt);
        }
    }


  /**
   * Delete a timeslot
   *
   * @param t The timeslot to be deleted
   * @return true if operation is successful
   */
    public boolean deleteTimeslot(Timeslot t) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        int count;

        try{
            stmnt = con.prepareStatement(DELETE_QUERY);

            stmnt.setInt(1, t.getServiceID());
            stmnt.setTimestamp(2, t.getDatetime());

            count = stmnt.executeUpdate();
            return count > 0;

        } finally {
            cleaningOperations(stmnt);
        }
    }

  /**
   * Get list of timeslots belonging to a specific service
   *
   * @param service The service to find its timeslots
   * @return List of Timeslot objects
   */
    public List<Timeslot> getTimeslotsByService(Service service) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        ResultSet result = null;
        List<Timeslot> times = new ArrayList<Timeslot>();

        try{
            int sid = service.getID();
            stmnt = con.prepareStatement(GET_BY_SERVICE_QUERY);
            stmnt.setInt(1, sid);
            result = stmnt.executeQuery();
            while (result.next()) {
                Timeslot t = new Timeslot(result.getInt("service"), result.getTimestamp("datetime"), result.getInt("places"));
                times.add(t);
            }

            return times;

        } finally {
            cleaningOperations(stmnt);
        }
    }


  /**
   * Get a timeslot by its identifier. Since identifier of a timeslot is
   * service-id and datetime, both must be passed as input arguments
   *
   * @param serviceID First part of timeslot identifier
   * @param datetime Second part of timeslot identifier
   * @return Timeslot if it is found, null otherwise
   */
    public Timeslot  getTimeslotByID(int serviceID, Timestamp datetime) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        ResultSet result = null;
        Timeslot t = null;

        try{
            stmnt = con.prepareStatement(GET_BY_ID_QUERY);
            stmnt.setInt(1, serviceID);
            stmnt.setTimestamp(2, datetime);
            result = stmnt.executeQuery();
            if (result.next()){
                t = new Timeslot(result.getInt("service"), result.getTimestamp("datetime"), result.getInt("places"));
            }
            return t;

        } finally {
            cleaningOperations(stmnt, result);
        }
    }

  /**
   * Get available places for a specific timeslot. The timeslot is found
   * by its ID which is pair of service-id and datetime
   *
   * @param serviceID First part of timeslot identifier
   * @param datetime Second part of timeslot identifier
   * @return An ineteger number representing available places for this timeslot
   */
    public int availablePlacesById(int serviceID, Timestamp datetime) throws SQLException, NamingException {

        PreparedStatement stmnt = null;
        ResultSet result = null;

        try{
            stmnt = con.prepareStatement(AVAILABLE_PLACES_QUERY);
            stmnt.setInt(1, serviceID);
            stmnt.setTimestamp(2, datetime);
            result = stmnt.executeQuery();
            int  availablePlaces = -1;
            if (result.next()) {
                availablePlaces = result.getInt("available");
            }
            return availablePlaces;
        } finally {
            cleaningOperations(stmnt, result);
        }
    }
}
