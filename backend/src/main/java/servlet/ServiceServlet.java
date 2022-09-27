package servlet;

import dao.AppointmentDAO;
import dao.ServiceDAO;
import dao.TimeslotDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import resource.Appointment;
import resource.Department;
import resource.Service;
import resource.Timeslot;
import utils.ErrorMessage;
import utils.DataSourceProvider;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ServiceServlet extends AbstractServlet{

    private final String serviceIndexPage = "[0-9]+/service/[0-9]+";
    private final String serviceCreatePage = "[0-9]+/service/create";
    private final String serviceDeletePage = "[0-9]+/service/[0-9]+/delete";
    
    /**
     *By depending on the request, it will show Service's indexpage or create page or delete page.
     *If it is non of them, an error message will be returned.
     *
     * @param req Incoming HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException The IOException is the class for exceptions thrown
     *  while accessing information using streams, files and directories.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

        if(op.matches(serviceIndexPage)){
            doShowUpdateService(req, res);
        }else if(op.matches(serviceCreatePage)){
            doShowCreateService(req, res);
        }else if(op.matches(serviceDeletePage)){
            doShowDeleteService(req, res);
        }else{
            writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }
    }
    /**
     * POST request will be processsed and it will be used to update or create a service.
     * Additionally it will be sued in order to update or create service.
     * And if it is non of them, error message will be returned.
     *
     * @param req Incomig HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException  The IOException is the class for exceptions thrown
     *   while accessing information using streams, files and directories.
     *
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

        if(op.matches(serviceIndexPage)){
            doUpdateService(req, res);
        }else if(op.matches(serviceCreatePage)){
            doCreateService(req, res);
        }else{
            writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }
    }
    
    /**
     * Shows the created service
     *
     * @param req Incomig HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException The IOException is the class for exceptions thrown
     *  while accessing information using streams, files and directories.
     */
    private void doShowCreateService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try{
            req.getRequestDispatcher("/jsp/company/department/service/index.jsp").forward(req, res);
        } catch (ServletException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }
    
     /**
     *Creates service which belongs to a specific Department
     *
     * @param req Incomig HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException The IOException is the class for exceptions thrown
     *  while accessing information using streams, files and directories.
     */
    private void doCreateService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

        int depId = Integer.parseInt(op.substring(0,op.indexOf("/service/")));

        try{
            String name = req.getParameter("name");
            String description = req.getParameter("description");

            long duration = Long.parseLong(req.getParameter("duration"));

            System.out.println(duration);

            boolean notEmptyTextFields = name==null || name.equals("") || description==null || description.equals("");

            if(!notEmptyTextFields){
                Service s = new Service(name,description,duration,depId);
                ServiceDAO serDao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
                int result = serDao.insertService(s);
                serDao.closeConnection();
                if(result>0){
                    res.sendRedirect(req.getContextPath() + "/company/department/" + depId);
                }else if (result == -2){
                    writeErrorGuiPage(res,ErrorMessage.DEPARTMENT_NOT_FOUND);
                }else{
                    writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                }
            }else{
                ErrorMessage em = ErrorMessage.EMPTY_INPUT_FIELDS;
                res.setStatus(em.getHttpErrorCode());
                req.setAttribute("errorMessage", "Error: "+em.getMessage());

                req.getRequestDispatcher("/jsp/company/department/service/index.jsp").forward(req, res);
            }
        } catch (SQLException | NamingException | ServletException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }
    
    /**
     * Shows the updated service page
     *
     * @param req Incomig HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException The IOException is the class for exceptions thrown
     *  while accessing information using streams, files and directories.
     */
    private void doShowUpdateService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

        try {
            int depId = Integer.parseInt(op.substring(0,op.indexOf("/service/")));
            int serviceId = Integer.parseInt(op.substring(op.indexOf("/service/")+"/service/".length()));
            ServiceDAO serDao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
            Service service = serDao.getServiceById(serviceId);

            if(service!=null && depId==service.getDepartmentID()){
                TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
                List<Timeslot> timeslots = tdao.getTimeslotsByService(service);
                tdao.closeConnection();

                req.setAttribute("service", service);
                req.setAttribute("timeslots", timeslots);

                req.getRequestDispatcher("/jsp/company/department/service/index.jsp").forward(req, res);
            }else{
                throw new NullPointerException();
            }

        } catch(NullPointerException | NumberFormatException  e){
            writeErrorGuiPage(res, ErrorMessage.SERVICE_NOT_FOUND);
        } catch (ServletException | SQLException | NamingException  e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     *Updates the service
     *
     * @param req Incomig HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException The IOException is the class for exceptions thrown
     *  while accessing information using streams, files and directories.
     */     
    private void doUpdateService(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

        try {
            int depId = Integer.parseInt(op.substring(0,op.indexOf("/service/")));
            int serviceId = Integer.parseInt(op.substring(op.indexOf("/service/")+"/service/".length()));
            ServiceDAO serDao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
            Service service = serDao.getServiceById(serviceId);
            serDao.closeConnection();

            if(service==null || depId!=service.getDepartmentID()){
                writeErrorGuiPage(res, ErrorMessage.SERVICE_NOT_FOUND);
            }

            String name = req.getParameter("name");
            String description = req.getParameter("description");

            boolean notEmptyTextFields = name==null || name.equals("") || description==null || description.equals("");

            if(!notEmptyTextFields){
                Service s = new Service(serviceId,name,description,service.getDuration(),depId);
                ServiceDAO serDao1 = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
                int result = serDao1.updateService(s);
                serDao1.closeConnection();
                System.out.println(result);

                if(result==0){
                    TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
                    List<Timeslot> timeslots = tdao.getTimeslotsByService(s);
                    tdao.closeConnection();

                    req.setAttribute("service", s);
                    req.setAttribute("timeslots", timeslots);

                    req.getRequestDispatcher("/jsp/company/department/service/index.jsp").forward(req, res);
                }else{
                    writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                }

            }else{
                ErrorMessage em = ErrorMessage.EMPTY_INPUT_FIELDS;
                ServiceDAO serDao2 = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
                Service s = serDao2.getServiceById(serviceId);
                serDao2.closeConnection();

                TimeslotDAO tdao2 = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
                List<Timeslot> timeslots = tdao2.getTimeslotsByService(s);
                tdao2.closeConnection();

                req.setAttribute("service", s);
                req.setAttribute("timeslots", timeslots);

                req.setAttribute("errorMessage", "Error: "+em.getMessage());

                req.getRequestDispatcher("/jsp/company/department/service/index.jsp").forward(req, res);
            }

        } catch (SQLException | NamingException | ServletException  e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }
    
    /**
     *Deletes specified service of the department.
     *
     * @param req Incomig HTTP request
     * @param res Outgoing HTTP response
     * @throws IOException The IOException is the class for exceptions thrown
     *  while accessing information using streams, files and directories.
     */
    private void doShowDeleteService(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

        op = op.replace("/delete","");

        int depId = Integer.parseInt(op.substring(0,op.indexOf("/service/")));
        int serviceId = Integer.parseInt(op.substring(op.indexOf("/service/")+"/service/".length()));

        try {
            ServiceDAO serDao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
            Service toBeDeleted = serDao.getServiceById(serviceId);
            serDao.closeConnection();

            if(toBeDeleted!=null && toBeDeleted.getDepartmentID()==depId){
                ServiceDAO serDao3 = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());

                if(serDao3.deleteService(toBeDeleted.getID())==0) {
                    res.sendRedirect(req.getContextPath() + "/company/department/" + toBeDeleted.getDepartmentID());
                    serDao3.closeConnection();
                }else{
                    writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                }
            }else{
                writeErrorGuiPage(res,ErrorMessage.SERVICE_NOT_FOUND);
            }

        } catch(SQLException | NumberFormatException | NamingException e){
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
        }
    }
}
