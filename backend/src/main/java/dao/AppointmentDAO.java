package dao;

import org.apache.logging.log4j.core.config.AppendersPlugin;
import resource.Appointment;
import resource.Timeslot;
import resource.User;
import resource.Service;
import dao.UserDAO;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AppointmentDAO extends AbstractDAO {

    private static final String getByIdQuery = "SELECT * FROM web_app.appointments WHERE id=?;";
    private static final String getByTimeslotQuery = "SELECT web_app.appointments.id, service, status, datetime, \"user\"\n" +
            "FROM web_app.appointments " +
            "WHERE web_app.appointments.\"service\"=? AND web_app.appointments.\"datetime\"=?;";
    private static final String getByUserQuery = "SELECT web_app.appointments.id, service, status, datetime, \"user\"\n" +
            "FROM web_app.appointments " +
            "WHERE web_app.appointments.\"user\"=?";
    private static final String insertQuery = "INSERT INTO web_app.appointments(service, status, datetime, \"user\")\n" +
            "\tVALUES ( ?, ?, ?, ?);";
    private static final String getByUserTimestampQuery = "SELECT web_app.appointments.id, service, status, datetime, \"user\"\n" +
            "FROM web_app.appointments JOIN web_app.services ON web_app.appointments.service = web_app.services.id\n" +
            "WHERE web_app.appointments.\"user\"=? AND (" +
                    "(? >= datetime AND ? <= datetime+duration)" +
                    "OR" +
                    "((?::timestamp with time zone)+?::interval >= datetime AND (?::timestamp with time zone)+?::interval <= datetime+duration)" +
                    "OR" +
                    "(? <= datetime AND (?::timestamp with time zone)+?::interval >= datetime+duration)" +
                ");";


    private static final String deleteQuery = "DELETE FROM web_app.appointments WHERE id=?;";
    private static final String  updateQuery = "UPDATE web_app.appointments\n" +
            "\tSET service=?, status=?, datetime=?, \"user\"=?\n" +
            "\tWHERE id=?;";

    /**
     * creates an instance of AppointmentDAO class
     * @param con connection to the database to be used by the instance
     */
    public AppointmentDAO(final Connection con) {
        super(con);
    }

    /**
     * retrieves an Appointment from the database from its ID
     * @param appointmentID the ID of the appointment to be retrieved
     * @return the Appointment object corresponding to the specified ID if an appointment with that id exists, null otherwise
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     */
    public Appointment getAppointmentById(int appointmentID) throws SQLException {

        PreparedStatement stmnt = null;
        ResultSet result = null;

        try{
            stmnt = con.prepareStatement(getByIdQuery);
            stmnt.setInt(1, appointmentID);

            result = stmnt.executeQuery();

            Appointment appointment = null;

            if (result.next()) {

                appointment = new Appointment(
                        result.getInt("id"),
                        result.getInt("service"),
                        Appointment.statuses.valueOf(result.getString("status")),
                        result.getTimestamp("datetime"),
                        result.getString("user")
                );
            }

            return appointment;
        } finally {
            cleaningOperations(stmnt, result);
        }
    }

    /**
     * retrieves the list of appointments associated to the provided timeslot of any current status
     * @param timeslot the timeslot of interest
     * @return List of Appointments associated to timeslot
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     */
    public List<Appointment> getAppointmentsByTimeslot(Timeslot timeslot) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        ResultSet result = null;

        try {

            stmnt = con.prepareStatement(getByTimeslotQuery);
            stmnt.setInt(1, timeslot.getServiceID());
            stmnt.setTimestamp(2, new Timestamp(timeslot.getDatetime().getTime()));


            result = stmnt.executeQuery();

            List<Appointment> appointmentsList = new ArrayList<>();

            while (result.next()) {
                Appointment tmp = new Appointment(
                        result.getInt("id"),
                        result.getInt("service"),
                        Appointment.statuses.valueOf(result.getString("status")),
                        result.getTimestamp("datetime"),
                        result.getString("user")
                );

                appointmentsList.add(tmp);
            }

            return appointmentsList;

        } finally {
            cleaningOperations(stmnt, result);
        }
    }

    /**
     * retrieves the list of appointments of the provided user from the database
     * @param user the user of interest
     * @return List of Appointments created by user, of any current status
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     */
    public List<Appointment> getAppointmentsByUser(User user) throws SQLException {
        PreparedStatement stmnt = null;
        ResultSet result = null;

        try {
            stmnt = con.prepareStatement(getByUserQuery);
            stmnt.setString(1, user.getEmail());

            result = stmnt.executeQuery();

            List<Appointment> appointmentsList = new ArrayList<>();

            while (result.next()) {
                Appointment tmp = new Appointment(
                        result.getInt("id"),
                        result.getInt("service"),
                        Appointment.statuses.valueOf(result.getString("status")),
                        result.getTimestamp("datetime"),
                        result.getString("user")
                );

                appointmentsList.add(tmp);
            }

            return appointmentsList;

        } finally {
            cleaningOperations(stmnt, result);
        }
    }

    /**
     * inserts a new appointment in the database
     * @param newAppointment the new Appointment object to be inserted in the database
     * @return the id of the new appointment if the insertion was successful, otherwise a negative integer is returned that corresponds to a specific error.
     * -1 is for generic error
     * -2 is for nonexistent user
     * -3 is for nonexistent service
     * -4 is for nonexistent timeslot
     * -5 is for not available places for the specified timeslot
     * -6 is for user has another appointment booked in the same timeslot
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public int insertAppointment(Appointment newAppointment) throws SQLException, NamingException {
        PreparedStatement stmnt = null;
        ResultSet resultSet = null;

        try {

            UserDAO uDao = new UserDAO(con);
            boolean userExist = uDao.getUserByEmail(newAppointment.getUserID())==null;
            if (userExist){
                return -2;
            }

            ServiceDAO sDao = new ServiceDAO(con);
            if (sDao.getServiceById(newAppointment.getServiceID())==null){
                return -3;
            }

            TimeslotDAO tDao = new TimeslotDAO(con);
            if (tDao.getTimeslotByID(newAppointment.getServiceID(),newAppointment.getDatetime())==null){
                return -4;
            }

            if(tDao.availablePlacesById(newAppointment.getServiceID(),newAppointment.getDatetime()) == 0){
                return -5;
            }

            final long appointmentDuration = sDao.getServiceById(newAppointment.getServiceID()).getDuration();
            if(!getAppointmentsByUserTimestamp(newAppointment.getUserID(), newAppointment.getDatetime(), appointmentDuration).isEmpty()){
                return -6;
            }

            stmnt = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

            stmnt.setInt(1,newAppointment.getServiceID());
            stmnt.setObject(2,newAppointment.getStatus().name(),Types.OTHER);
            stmnt.setTimestamp(3, newAppointment.getDatetime());
            stmnt.setString(4,newAppointment.getUserID());

            int result = stmnt.executeUpdate();

            if (result==1){
                resultSet = stmnt.getGeneratedKeys();
                resultSet.next();
                return resultSet.getInt(1);
            }
            else {
                return -1;
            }

        } finally {
            cleaningOperations(stmnt, resultSet);
        }
    }


    /**
     * retrieves the list of appointments of the provided user that overlap the given datetime
     * @param userId the user who booked the appointments to be searched
     * @param time the datetime of interest
     * @param duration duration of the service of the appointment to check
     * @return the list of appointments of the provided user that overlap the given datetime, if no appointment is found the list will be empty
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     */
    public List<Appointment> getAppointmentsByUserTimestamp(String userId, Timestamp time, long duration) throws SQLException {
        PreparedStatement stmnt = null;
        ResultSet result = null;

        String durationString = Service.durationToString(duration);
        System.out.println(durationString);
        try {
            stmnt = con.prepareStatement(getByUserTimestampQuery);

            stmnt.setString(1,userId);
            stmnt.setTimestamp(2,time);
            stmnt.setTimestamp(3,time);
            stmnt.setTimestamp(4,time);
            stmnt.setString(5, durationString);
            stmnt.setTimestamp(6,time);
            stmnt.setString(7, durationString);
            stmnt.setTimestamp(9,time);
            stmnt.setTimestamp(8,time);
            stmnt.setString(10, durationString);

            result = stmnt.executeQuery();

            List<Appointment> appointmentsList = new ArrayList<>();

            while (result.next()) {
                Appointment tmp = new Appointment(
                        result.getInt("id"),
                        result.getInt("service"),
                        Appointment.statuses.valueOf(result.getString("status").toUpperCase()),
                        result.getTimestamp("datetime"),
                        result.getString("user")
                );

                appointmentsList.add(tmp);
            }

            return appointmentsList;

        } finally {
            cleaningOperations(stmnt, result);
        }
    }

    /**
     * deletes the appointment associated with the provided id form the database
     * @param appointmentID the id of the appointment to be deleted
     * @return 0 if the appointment has been successfully deleted from the database, -1 if an error occurred
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     */
    public int deleteAppointmentById(int appointmentID) throws SQLException {
        PreparedStatement stmnt = null;

        try {
            stmnt = con.prepareStatement(deleteQuery);
            stmnt.setInt(1,appointmentID);

            int result = stmnt.executeUpdate();

            if (result==1){
                return 0;
            }
            else {
                return -1;
            }

        } finally {
            cleaningOperations(stmnt);
        }
    }

    /**
     * updates the provided appointment in the database
     * @param a the appointment object to be updated
     * @return 0 if the appointment has been successfully updated, otherwise a negative integer is returned that corresponds to a specific error.
     * -1 is for generic error
     * -2 is for nonexistent user
     * -3 is for nonexistent service
     * -4 is for nonexistent timeslot
     * -5 is for not available places for the specified timeslot
     * -6 is for user has another appointment booked in the same timeslot
     * @throws SQLException when bad SQL parameters are set or retrieved or a SQL error occurs
     * @throws NamingException thrown in case there was a problem connecting to the database
     */
    public int updateAppointment(Appointment a) throws SQLException, NamingException{
        PreparedStatement stmnt = null;

        try {
            UserDAO dao = new UserDAO(con);
            if(dao.getUserByEmail(a.getUserID())==null){
                return -2;
            }

            ServiceDAO sDao = new ServiceDAO(con);
            if (sDao.getServiceById(a.getServiceID())==null){
                return -3;
            }

            TimeslotDAO tDao = new TimeslotDAO(con);
            if (tDao.getTimeslotByID(a.getServiceID(),a.getDatetime())==null){
                return -4;
            }

            if(tDao.availablePlacesById(a.getServiceID(),a.getDatetime()) == 0){
                Appointment old = getAppointmentById(a.getID());
                if(old.getServiceID() != a.getServiceID() || !(old.getDatetime().equals(a.getDatetime()))) {
                    return -5; //appointment coming from a different timeslot or service is not allowed because this timeslot is full
                }
            }

            final long appointmentDuration = sDao.getServiceById(a.getServiceID()).getDuration();
            if(!getAppointmentsByUserTimestamp(a.getUserID(), a.getDatetime(), appointmentDuration).isEmpty()){
                Appointment old = getAppointmentById(a.getID());
                if(old.getServiceID() != a.getServiceID() ||
                        !(old.getDatetime().equals(a.getDatetime())) ||
                        !(old.getUserID().equals(a.getUserID())))
                    return -6; //if not updating only the status, then this user ha no time for this appointment
            }

            stmnt = con.prepareStatement(updateQuery);
            stmnt.setInt(1,a.getServiceID());
            stmnt.setObject(2,a.getStatus().name(),Types.OTHER);
            stmnt.setTimestamp(3, a.getDatetime());
            stmnt.setString(4,a.getUserID());
            stmnt.setInt(5,a.getID());

            int result = stmnt.executeUpdate();

            if (result==1){
                return 0;
            }
            else {
                return -1;
            }

        } finally {
            cleaningOperations(stmnt);
        }

    }


}
