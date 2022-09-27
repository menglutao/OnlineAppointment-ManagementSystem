package rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dao.AppointmentDAO;
import org.json.*;
import java.io.IOException;
import java.sql.*;

import resource.Appointment;
import utils.DataSourceProvider;
import utils.ErrorMessage;
import utils.Message;

import javax.naming.NamingException;

public class AppointmentRestResource {

    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private ErrorMessage em;
    private String response;
    private final String[] tokens;
    private final int ID_TOKEN_INDEX = 4;

    public AppointmentRestResource(HttpServletRequest req, HttpServletResponse res) {
        this.req = req;
        this.res = res;
        em = ErrorMessage.INTERNAL_ERROR;
        response = em.toJSON().toString();
        tokens = req.getRequestURI().split("/");
    }

    /**
     * Inserts an appointment received in JSON format through a REST api call in the database,
     * write the correct error message in the http response if and error occurred, otherwise writes a successful response
     * @throws IOException when an error occurred during the response writing
     */
    public void insertAppointment() throws IOException{
        try {
            Appointment appointment = Appointment.fromJSON(req.getInputStream());
            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            int res = dao.insertAppointment(appointment);
            dao.closeConnection();
            switch (res){
                case -1:
                    initError(ErrorMessage.INTERNAL_ERROR);
                    break;
                case -2:
                    initError(ErrorMessage.USER_NOT_FOUND);
                    break;
                case -3:
                    initError(ErrorMessage.SERVICE_NOT_FOUND);
                    break;
                case -4:
                    initError(ErrorMessage.TIMESLOT_NOT_FOUND);
                    break;
                case -5:
                    initError(ErrorMessage.TIMESLOT_FULL);
                    break;
                case -6:
                    initError(ErrorMessage.APPOINTMENT_ALREADY_BOOKED);
                    break;
                default:
                    em = ErrorMessage.OK;
                    response = new Message("appointment created successfully").toJSON().toString();
            }
        } catch (JSONException e){
            initError(ErrorMessage.BADLY_FORMATTED_JSON);
        } catch (SQLException | NamingException e){
            e.printStackTrace();
            initError(ErrorMessage.INTERNAL_ERROR);
        } finally { respond(); }
    }

    /**
     * Retrieves the appointment associated with the id provided in the REST api request.
     * Writes the http response with the JSON representation of the appointment if it has been retrieved successfully,
     * otherwise writes the correct error response.
     * @throws IOException when an error occurred during the response writing
     */
    public void getAppointment() throws IOException{
        try {
            int appointmentID = Integer.parseInt(tokens[ID_TOKEN_INDEX]);
            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            Appointment appointment = dao.getAppointmentById(appointmentID);
            dao.closeConnection();
            if (appointment != null){
                em = ErrorMessage.OK;
                response = appointment.toJSON().toString();
            } else {
                initError(ErrorMessage.APPOINTMENT_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            initError(ErrorMessage.WRONG_REST_FORMAT);
        } catch (SQLException | NamingException e) {
            initError(ErrorMessage.INTERNAL_ERROR);
        } finally { respond(); }
    }

    /**
     * Updates the appointment associated with the id provided in the REST api request, with the appointment received in JSON format in the same request.
     * Writes the correct error message in the http response if and error occurred, otherwise writes a successful response
     * @throws IOException when an error occurred during the response writing
     */
    public void updateAppointment() throws IOException {
        try{
            int appointmentID = Integer.parseInt(tokens[ID_TOKEN_INDEX]);
            Appointment appointment = Appointment.fromJSON(req.getInputStream());

            if (appointmentID  != appointment.getID()){
                initError(ErrorMessage.APPOINTMENT_ID_MISMATCH);
            } else {
                AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
                int res = dao.updateAppointment(appointment);
                dao.closeConnection();
                switch (res){
                    case 0:
                        em = ErrorMessage.OK;
                        response = new Message("Appointment updated correctly").toJSON().toString();
                        break;
                    case -2:
                        initError(ErrorMessage.USER_NOT_FOUND);
                        break;
                    case -3:
                        initError(ErrorMessage.SERVICE_NOT_FOUND);
                        break;
                    case -4:
                        initError(ErrorMessage.TIMESLOT_NOT_FOUND);
                        break;
                    case -5:
                        initError(ErrorMessage.TIMESLOT_FULL);
                        break;
                    case -6:
                        initError(ErrorMessage.APPOINTMENT_ALREADY_BOOKED);
                        break;
                    default: initError(ErrorMessage.INTERNAL_ERROR);
                }
            }
        } catch (NumberFormatException e) {
            initError(ErrorMessage.WRONG_REST_FORMAT);
        } catch (JSONException e){
            initError(ErrorMessage.BADLY_FORMATTED_JSON);
        } catch (SQLException | NamingException e){
            initError(ErrorMessage.INTERNAL_ERROR);
        } finally { respond(); }
    }

    /**
     * Deletes the appointment associated with the id provided in the REST api request.
     * Writes the http response with the outcome of the operation
     * @throws IOException when an error occurred during the response writing
     */
    public void deleteAppointment() throws IOException {
        try {
            int appointmentID = Integer.parseInt(tokens[ID_TOKEN_INDEX]);
            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            int res = dao.deleteAppointmentById(appointmentID);
            dao.closeConnection();
            if (res == 0){
                em = ErrorMessage.OK;
                response = new Message("Appointment deleted correctly").toJSON().toString();
            } else {
                initError(ErrorMessage.INTERNAL_ERROR);
            }
        } catch (NumberFormatException e) {
            initError(ErrorMessage.WRONG_REST_FORMAT);
        } catch (SQLException | NamingException e) {
            initError(ErrorMessage.INTERNAL_ERROR);
        } finally { respond(); }
    }

    /**
     *  sets the response string with the JSON representation of the error message provided
     * @param em error message that will be sent as response to the client
     */
    private void initError(ErrorMessage em){
        this.em = em;
        response = em.toJSON().toString();
    }

    /**
     *  writes the response message in the HttpServletResponse provided and sets the correct http status code
     * @throws IOException when an error occurred during the response writing
     */
    private void respond() throws IOException {
        res.setContentType("application/json");
        res.setStatus(em.getHttpErrorCode());
        res.getWriter().write(response);
    }
}




