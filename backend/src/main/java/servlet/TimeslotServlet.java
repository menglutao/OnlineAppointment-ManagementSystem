package servlet;

import dao.AppointmentDAO;
import dao.TimeslotDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import resource.Appointment;
import resource.Timeslot;
import utils.ErrorMessage;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class TimeslotServlet extends AbstractServlet{

    private final String timeslotEditPage = "[0-9]+/timeslot/[0-9]+";
    private final String timeslotCreatePage = "[0-9]+/timeslot/create";
    private final String timeslotDelete = "[0-9]+/timeslot/[0-9]+/delete";

    private final String appointmentsUpdate = "[0-9]+/timeslot/[0-9]+/syncAppointments";
    private final String appointmentDelete = "[0-9]+/timeslot/[0-9]+/appointment/[0-9]+/delete";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.indexOf("/service/")+"/service/".length());

        if(op.matches(timeslotEditPage)){
            doShowUpdateTimeslot(req, res);
        }else if(op.matches(timeslotCreatePage)){
            doShowCreateTimeslot(req, res);
        }else if(op.matches(timeslotDelete)){
            doDeleteTimeslot(req, res);
        }else if(op.matches(appointmentDelete)){
            doDeleteAppointment(req, res);
        }else{
            writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.indexOf("/service/")+"/service/".length());

        if(op.matches(timeslotEditPage)){
            doUpdateTimeslot(req, res);
        }else if(op.matches(timeslotCreatePage)){
            doCreateTimeslot(req, res);
        } else if(op.matches(appointmentsUpdate)){
            doUpdateAppointments(req, res);
        }else{
            writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }
    }

    private void doShowCreateTimeslot(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try{
            req.getRequestDispatcher("/jsp/company/department/service/timeslot.jsp").forward(req, res);
        } catch (ServletException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    private void doCreateTimeslot(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();

        int departmentId = Integer.parseInt(op.substring(op.indexOf("/department/")+"/department/".length(), op.indexOf("/service/")));

        op = op.substring(op.indexOf("/service/")+"/service/".length());

        int serviceId = Integer.parseInt(op.substring(0,op.indexOf("/timeslot/")));

        try{
            int places = Integer.parseInt(req.getParameter("places"));
            Date claimedDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req.getParameter("datetime").replace("T"," "));

            if(!claimedDatetime.before(new Date())) {
                Timestamp datetime = new Timestamp(claimedDatetime.getTime());
                Timeslot timeslot = new Timeslot(serviceId,datetime,places);

                TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());

                if (tdao.insertTimeslot(timeslot)) {
                    req.setAttribute("timeslot", timeslot);

                    res.sendRedirect(req.getContextPath() + "/company/department/" + departmentId + "/service/" + timeslot.getServiceID());
                } else {
                    writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
                }
                tdao.closeConnection();
            }else{
                ErrorMessage em = ErrorMessage.INVALID_INPUT_FIELDS;
                res.setStatus(em.getHttpErrorCode());
                req.setAttribute("errorMessage", "Error: "+em.getMessage());

                try {
                    req.getRequestDispatcher("/jsp/company/department/service/timeslot.jsp").forward(req, res);
                } catch (ServletException ex) {
                    writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                }
            }

        }catch (NumberFormatException | ParseException | NullPointerException e) {
            e.printStackTrace();

            ErrorMessage em = ErrorMessage.INVALID_INPUT_FIELDS;
            res.setStatus(em.getHttpErrorCode());
            req.setAttribute("errorMessage", "Error: "+em.getMessage());

            try {
                req.getRequestDispatcher("/jsp/company/department/service/timeslot.jsp").forward(req, res);
            } catch (ServletException ex) {
                writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
            }

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    private void doShowUpdateTimeslot(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.indexOf("/service/")+"/service/".length());

        int serviceId = Integer.parseInt(op.substring(0,op.indexOf("/timeslot/")));
        Timestamp datetime = new Timestamp(Long.parseLong(op.substring(op.indexOf("/timeslot/")+"/timeslot/".length())));

        try {
            TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
            Timeslot timeslot = tdao.getTimeslotByID(serviceId, datetime);
            tdao.closeConnection();

            if(timeslot!=null){
                AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
                List<Appointment> appointments = dao.getAppointmentsByTimeslot(timeslot);
                dao.closeConnection();

                req.setAttribute("timeslot", timeslot);
                req.setAttribute("appointments", appointments);

                req.getRequestDispatcher("/jsp/company/department/service/timeslot.jsp").forward(req, res);
            }else{
                writeErrorGuiPage(res, ErrorMessage.TIMESLOT_NOT_FOUND);
            }
        } catch (SQLException | NamingException | ServletException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    private void doUpdateTimeslot(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.indexOf("/service/")+"/service/".length());

        int serviceId = Integer.parseInt(op.substring(0,op.indexOf("/timeslot/")));
        Timestamp datetime = new Timestamp(Long.parseLong(op.substring(op.indexOf("/timeslot/")+"/timeslot/".length())));

        try{
            int places = Integer.parseInt(req.getParameter("places"));

            Timeslot updatedTimeslot = new Timeslot(serviceId,datetime,places);

            TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
            if(tdao.updateTimeslot(updatedTimeslot)){
                AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
                List<Appointment> appointments = dao.getAppointmentsByTimeslot(updatedTimeslot);
                dao.closeConnection();
                req.setAttribute("timeslot", updatedTimeslot);
                req.setAttribute("appointments", appointments);

                req.getRequestDispatcher("/jsp/company/department/service/timeslot.jsp").forward(req, res);
            }else{
                writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
            }
            tdao.closeConnection();

        }catch (NumberFormatException e) {
            writeErrorGuiPage(res,ErrorMessage.INVALID_INPUT_FIELDS);

        } catch (SQLException | NamingException | ServletException  e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    private void doDeleteTimeslot(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();

        int departmentId = Integer.parseInt(op.substring(op.indexOf("/department/")+"/department/".length(), op.indexOf("/service/")));

        op = op.substring(op.indexOf("/service/")+"/service/".length());
        op = op.replace("/delete","");

        int serviceId = Integer.parseInt(op.substring(0,op.indexOf("/timeslot/")));
        Timestamp datetime = new Timestamp(Long.parseLong(op.substring(op.indexOf("/timeslot/")+"/timeslot/".length())));

        try {
            TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
            Timeslot timeslot = tdao.getTimeslotByID(serviceId,datetime);

            if(timeslot!=null){

                if(tdao.deleteTimeslot(timeslot)) {
                    res.sendRedirect(req.getContextPath() + "/company/department/"+departmentId+"/service/" + timeslot.getServiceID());
                }else{
                    writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                }

            }else{
                throw new NullPointerException();
            }
            tdao.closeConnection();

        } catch(NullPointerException | SQLException | NumberFormatException | NamingException e){
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.TIMESLOT_NOT_FOUND);
        }
    }

    private void doUpdateAppointments(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();

        int departmentId = Integer.parseInt(op.substring(op.indexOf("/department/")+"/department/".length(), op.indexOf("/service/")));

        op = op.substring(op.indexOf("/service/")+"/service/".length());
        op = op.replace("/syncAppointments","");

        int serviceId = Integer.parseInt(op.substring(0,op.indexOf("/timeslot/")));
        Timestamp datetime = new Timestamp(Long.parseLong(op.substring(op.indexOf("/timeslot/")+"/timeslot/".length())));

        Enumeration<String> parameterNames = req.getParameterNames();

        try {
            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            List<Appointment> oldAppointments =  dao.getAppointmentsByTimeslot(new Timeslot(serviceId,datetime,10));
            boolean result = true;

            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                int appointmentId = Integer.parseInt(paramName.substring(0, paramName.indexOf("-status")));
                Appointment.statuses newStatus = Appointment.statuses.valueOf(req.getParameter(paramName));

                Appointment oldAppointment = null;

                for(Appointment tmp: oldAppointments){
                    if(tmp.getID()==appointmentId){
                        oldAppointment = tmp;
                        break;
                    }
                }

                if(oldAppointment!=null){
                    Appointment updatedAppointment = new Appointment(
                            oldAppointment.getID(),
                            oldAppointment.getServiceID(),
                            newStatus,
                            oldAppointment.getDatetime(),
                            oldAppointment.getUserID()
                    );

                    int returnResult = dao.updateAppointment(updatedAppointment);

                    result = returnResult==0 && result;
                }
            }
            dao.closeConnection();

            if(result){
                res.sendRedirect(req.getContextPath() + "/company/department/" + departmentId + "/service/" + serviceId + "/timeslot/" + datetime.getTime());
            }else{
                writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
            }


        }catch (IllegalArgumentException | IndexOutOfBoundsException e){
            writeErrorGuiPage(res,ErrorMessage.INVALID_INPUT_FIELDS);
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    private void doDeleteAppointment(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();

        int departmentId = Integer.parseInt(op.substring(op.indexOf("/department/")+"/department/".length(), op.indexOf("/service/")));

        op = op.substring(op.indexOf("/service/")+"/service/".length());

        int serviceId = Integer.parseInt(op.substring(0,op.indexOf("/timeslot/")));
        Timestamp datetime = new Timestamp(Long.parseLong(op.substring(op.indexOf("/timeslot/")+"/timeslot/".length(),op.indexOf("/appointment/"))));
        int appointmentId = Integer.parseInt(op.substring(op.indexOf("/appointment/") + "/appointment/".length(), op.indexOf("/delete")));


        try {
            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            Appointment toBeDeleted = dao.getAppointmentById(appointmentId);

            if(toBeDeleted!=null && toBeDeleted.getServiceID()==serviceId && toBeDeleted.getDatetime().equals(datetime)){

                if(dao.deleteAppointmentById(appointmentId)==0) {
                    res.sendRedirect(req.getContextPath() + "/company/department/"+ departmentId +"/service/" + serviceId + "/timeslot/" + datetime.getTime());
                }else{
                    writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                }
            }else{
                throw new NullPointerException();
            }
            dao.closeConnection();

        } catch(NullPointerException | SQLException | NumberFormatException | NamingException e){
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.APPOINTMENT_NOT_FOUND);
        }
    }
}
