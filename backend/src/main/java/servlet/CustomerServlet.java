package servlet;

//import com.google.gson.Gson;
//import dao.AppointmentDAO;
import com.google.gson.Gson;
import dao.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resource.*;
import utils.DataSourceProvider;
import utils.ErrorMessage;

import javax.naming.NamingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import utils.DataSourceProvider;
import dao.AppointmentDAO.*;
import dao.ServiceDAO.*;
import utils.Helper.*;

public class CustomerServlet extends AbstractServlet {
    /**
     * Dispatch based on the URL contained in the GET request to the method or JSP
     * dedicated to serving it.
     * 
     * @param req HTTP request to serve
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of any other backend problems
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        // if (1 == 1)
        // throw new RuntimeException("EEEEE " + op + "\n" + req.getContextPath() + "\n"
        // + req.getContextPath()
        // + "\n" + req.getContextPath().length());
        op = op.substring(op.indexOf(req.getContextPath()) + req.getContextPath().length() + "/customer".length());

        System.out.println(op);

        if (op.matches("")) {
            showCustomersAppointments(req, res);
        } else {
            if (op.matches("/appointment/new")) {
                showBookingPage(req, res);
            } else if (op.matches("/getTimeslotsByServiceId")) {
                getTimeslotsByServiceId(req, res);
            } else if (op.matches("/appointment/[0-9]+/delete")) {
                deleteAppointment(req, res);
            } else if (op.matches("/getServicesByCompany")) {
                getServicesByCompany(req, res);
            } else if (op.matches("/appointment/update")) {
                showUpdateAppointmentPage(req, res);
            } else {
                writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
            }
        }
    }

    /**
     * Dispatch based on the URL contained in the POST request to the method
     * dedicated to serving it.
     * 
     * @param req HTTP request to serve
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String op = req.getRequestURI();
        op = op.substring(op.indexOf(req.getContextPath()) + req.getContextPath().length() + "/customer".length());

        if (op.matches("/appointment/new")) {
            makeAppointments(req, res);
        } else if (op.matches("/appointment/update")) {
            updateAppointment(req, res);
            // showCustomersAppointments(req,res);
        } else {
            writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }

    }

    /**
     * Delete the appointment based on appointmentID,if appointmentID isn't null and
     * deleteAppointmentById return value equals to 0,means delete is successful
     * will return to customer page
     * Otherwise return error.
     * 
     * @param req HTTP request containing appointmentURI
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */

    private void deleteAppointment(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();

        int appointmentId = Integer
                .parseInt(op.substring(op.indexOf("/appointment/") + "/appointment/".length(), op.indexOf("/delete")));

        try {

            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            Appointment toBeDeleted = dao.getAppointmentById(appointmentId);

            if (toBeDeleted != null) {
                if (dao.deleteAppointmentById(toBeDeleted.getID()) == 0) {
                    // res.sendRedirect(req.getContextPath() + "/customer");
                    res.sendRedirect(String.format("%s/customer", req.getContextPath()));
                } else {
                    writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
                }

            } else {
                throw new NullPointerException();
            }
            dao.closeConnection();

        } catch (NullPointerException | SQLException | NumberFormatException | NamingException e) {
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.TIMESLOT_NOT_FOUND);
        }
    }

    private void showUpdateAppointmentPage(HttpServletRequest req, HttpServletResponse res) {
        // String op = req.getRequestURI();

        int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));// Integer.parseInt(op.substring(op.indexOf("/appointment/")+"/appointment/".length(),op.indexOf("/update")));
        try {
            AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
            Appointment toBeUpdated = dao.getAppointmentById(appointmentId);
            int serviceID = toBeUpdated.getServiceID();

            TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
            ServiceDAO Sdao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
            DepartmentDAO Ddao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());

            Service service = Sdao.getServiceById(serviceID);
            Department department = Ddao.getDepartmentById(service.getDepartmentID());

            String companyName = department.getCompanyID();
            String serviceName = service.getName();
            List<Timeslot> timeslots = tdao.getTimeslotsByService(service);

            dao.closeConnection();
            tdao.closeConnection();
            Sdao.closeConnection();
            Ddao.closeConnection();


            req.setAttribute("companyName",companyName);
            req.setAttribute("serviceName",serviceName);
            req.setAttribute("timeslots", timeslots);
            req.setAttribute("appointmentId", appointmentId);

            req.getRequestDispatcher("/jsp/customer/appointment.jsp").forward(req, res);
        } catch (Exception e) {
            //
        }

    }

    /**
     * Show customer's appointments based on customer's email and role from current
     * session
     * If email is not null and role is customer means successfully otherwise
     * redirect to error page
     * 
     * @param req HTTP request current session to get email and role
     * @param res response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */

    public void showCustomersAppointments(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession session = req.getSession();
            // getAttribute-->pass data from server to jsp
            String email = (String) session.getAttribute("email");
            User.Role role = (User.Role) session.getAttribute("role");

            if (email == null) {// check current status
                req.setAttribute("error_msg", "please login");
                req.getRequestDispatcher("error.jsp").forward(req, res);
            } else if (role == User.Role.customer) {
                // show all the appointments including future ones and past ones
                Connection con = DataSourceProvider.getDataSource().getConnection();
                UserDAO uDao = new UserDAO(con);
                User u = uDao.getUserByEmail(email);
                AppointmentDAO aDao = new AppointmentDAO(con);
                List<Appointment> appointmentsList = aDao.getAppointmentsByUser(u);

                List<Appointment[]> appointmentsPairs = new ArrayList<>();
                HashMap<Integer, Service> servicesMap = new HashMap<>();
                HashMap<Integer, String> companyNamesMap = new HashMap<>();
                for (int i = 0; i < appointmentsList.size() - (appointmentsList.size() % 2); i++) {
                    // retrieve service
                    ServiceDAO Sdao = new ServiceDAO(con);
                    Service s = Sdao.getServiceById(appointmentsList.get(i).getServiceID());
                    servicesMap.put(s.getID(), s);

                    // retrieve company name
                    DepartmentDAO Ddao = new DepartmentDAO(con);
                    String companyID = Ddao.getDepartmentById(s.getDepartmentID()).getCompanyID();
                    companyNamesMap.put(s.getID(), companyID);

                    // add appointment to pair
                    if (i % 2 == 0) { // even index
                        Appointment[] pair = new Appointment[2];
                        pair[0] = appointmentsList.get(i);
                        appointmentsPairs.add(pair);
                    } else { // odd index
                        Appointment[] pair = appointmentsPairs.get((i - 1) / 2);
                        pair[1] = appointmentsList.get(i);
                        appointmentsPairs.set((i - 1) / 2, pair);
                    }
                }

                if (appointmentsList.size() % 2 != 0) {
                    // number of appointments is odd, return the last appointment in separate
                    // variable
                    req.setAttribute("lastAppointment", appointmentsList.get(appointmentsList.size() - 1));

                    // retrieve service
                    ServiceDAO Sdao = new ServiceDAO(con);
                    Service s = Sdao.getServiceById(appointmentsList.get(appointmentsList.size() - 1).getServiceID());
                    servicesMap.put(s.getID(), s);

                    // retrieve company name
                    DepartmentDAO Ddao = new DepartmentDAO(con);
                    String companyID = Ddao.getDepartmentById(s.getDepartmentID()).getCompanyID();
                    companyNamesMap.put(s.getID(), companyID);
                }
                aDao.closeConnection();
                req.setAttribute("appointmentsPairs", appointmentsPairs);
                req.setAttribute("servicesMap", servicesMap);
                req.setAttribute("companyNamesMap", companyNamesMap);
                req.getRequestDispatcher("/jsp/customer/index.jsp").forward(req, res);
            } else {// role != customer --> error message
                ErrorMessage em = ErrorMessage.WRONG_CREDENTIALS;
                res.setStatus(em.getHttpErrorCode());
                req.setAttribute("errorMessage", "Error: " + em.getMessage());
                // req.getRequestDispatcher("/jsp/login.jsp").forward(req, res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     * Get available timeslots by input serviceID
     * 
     * @param req      HTTP request the selected serviceID
     * @param response response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */

    private void getTimeslotsByServiceId(HttpServletRequest req, HttpServletResponse response) throws IOException {
        try {
            int serviceID = Integer.parseInt(req.getParameter("serviceID"));
            Connection con = DataSourceProvider.getDataSource().getConnection();
            TimeslotDAO tdao = new TimeslotDAO(con);
            ServiceDAO sdao = new ServiceDAO(con);
            List<Timeslot> timeslots = tdao.getTimeslotsByService(sdao.getServiceById(serviceID));
            final long serviceDuration = sdao.getServiceById(serviceID).getDuration();


            AppointmentDAO adao = new AppointmentDAO(con);
            final String userEmail = (String) req.getSession(false).getAttribute("email");
            List<Timeslot> toBeRemoved = new ArrayList<Timeslot>();

            String appID = req.getParameter("appointmentID");
            if(appID != null && ("".equals(appID))) {
                //update appointment
                Appointment old = adao.getAppointmentById(Integer.parseInt(appID));
                for (Timeslot t: timeslots) {
                    if ( !(t.getDatetime().equals(old.getDatetime())) &&
                         !(adao.getAppointmentsByUserTimestamp(userEmail, t.getDatetime(), serviceDuration).isEmpty())
                    ) {
                        toBeRemoved.add(t);
                    }
                }
            }else {
                //book appointment
                for (Timeslot t: timeslots) {
                    if (!(adao.getAppointmentsByUserTimestamp(userEmail, t.getDatetime(), serviceDuration).isEmpty())) {
                        toBeRemoved.add(t);
                    }
                }
            }
            adao.closeConnection();
            //remove timeslots
            for (Timeslot t: toBeRemoved) {
                timeslots.remove(t);
            }



            String json = new Gson().toJson(timeslots);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            writeErrorGuiPage(response, ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     * ShowBookingPage is to create a new appointment by selecting the services
     * 
     * @param req      HTTP request is to set an attribute to a servlet request in a
     *                 web application
     * @param response response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */
    private void getServicesByCompany(HttpServletRequest req, HttpServletResponse response) throws IOException {
        String companyName = req.getParameter("companyName");
        try {
            DepartmentDAO dDAO = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
            List<Department> departmentList = dDAO.getDepartmentsByCompany(companyName);
            dDAO.closeConnection();
            ArrayList<Service> services = new ArrayList();// fetching all services
            for (Department department : departmentList) {
                ServiceDAO Sdao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
                List<Service> innerServices = Sdao.getServicesByDepartment(department.getID());
                services.addAll(innerServices);
                // innerServices.forEach(service -> services.add(service));
                Sdao.closeConnection();
            }

            String json = new Gson().toJson(services);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            writeErrorGuiPage(response, ErrorMessage.INTERNAL_ERROR);
        }

    }



    private void showUpdatePage(HttpServletRequest req, HttpServletResponse res, Appointment toBeUpdated) {
        try {
            int serviceID = toBeUpdated.getServiceID();

            TimeslotDAO tdao = new TimeslotDAO(DataSourceProvider.getDataSource().getConnection());
            ServiceDAO Sdao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
            DepartmentDAO Ddao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());

            Service service = Sdao.getServiceById(serviceID);
            Department department = Ddao.getDepartmentById(service.getDepartmentID());

            String companyName = department.getCompanyID();
            String serviceName = service.getName();
            List<Timeslot> timeslots = tdao.getTimeslotsByService(service);

            tdao.closeConnection();
            Sdao.closeConnection();
            Ddao.closeConnection();

            req.setAttribute("companyName",companyName);
            req.setAttribute("serviceName",serviceName);
            req.setAttribute("timeslots", timeslots);
            req.setAttribute("appointmentId", toBeUpdated.getID());

            req.getRequestDispatcher("/jsp/customer/appointment.jsp").forward(req, res);
        } catch (Exception e) {
            req.setAttribute("MyErrorMessage", e.getMessage());
            // PrintWriter pw = res.getWriter();
            // pw.println("aaaaaaa");// test getWriter()
            try {
                req.getRequestDispatcher("/jsp/customer/index.jsp").forward(req, res);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }

    }



    private void showBookingPage(HttpServletRequest req, HttpServletResponse res) {
        try {
            CompanyDAO cDao = new CompanyDAO(DataSourceProvider.getDataSource().getConnection());
            ArrayList<Company> companyList = cDao.getAllCompanies();
            cDao.closeConnection();
            req.setAttribute("companies", companyList);
            req.setAttribute("services", new ArrayList<Service>());
            req.getRequestDispatcher("/jsp/customer/appointment.jsp").forward(req, res);
        } catch (Exception e) {
            req.setAttribute("MyErrorMessage", e.getMessage());
            // PrintWriter pw = res.getWriter();
            // pw.println("aaaaaaa");// test getWriter()
            try {
                req.getRequestDispatcher("/jsp/customer/index.jsp").forward(req, res);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }

    }

    /**
     * makeAppointment is to insert a new appointment into a list based on the
     * selected datetime and service
     * 
     * @param req HTTP request the email and role from current session, also get the
     *            selected datetime.
     * @param res response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */

    public void makeAppointments(HttpServletRequest req, HttpServletResponse res) throws IOException {

        try {
            HttpSession session = req.getSession();
            String email = (String) session.getAttribute("email");
            User.Role role = (User.Role) session.getAttribute("role");

            if (email == null) {
                req.setAttribute("error_msg", "please login");
                req.getRequestDispatcher("error.jsp").forward(req, res);
            } else {
//                String[] serviceIDAndDatetime = req.getParameter("datetime").split("---");
                //Date date = new Date(req.getParameter("datetime"));
                int serviceID = Integer.parseInt(req.getParameter("services"));
                long datetimeLong = Long.parseLong(req.getParameter("datetime_long"));//date.getTime();


                Timestamp tm = new Timestamp(datetimeLong);

                Appointment appointment = new Appointment(
                        serviceID,
                        Appointment.statuses.BOOKED,
                        tm,
                        email);

                AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
                int insertCode = dao.insertAppointment(appointment);
                dao.closeConnection();
                System.out.println(insertCode);
                if (insertCode > 0) {
                    // res.sendRedirect(req.getContextPath() + "/customer");
                    res.sendRedirect(String.format("%s/customer", req.getContextPath()));
                } else if (insertCode == -2 || insertCode == -3 || insertCode == -4) {

                    ErrorMessage em = ErrorMessage.INVALID_INPUT_FIELDS;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s", em.getMessage()));

                    showBookingPage(req, res);
                    showBookingPage(req, res);
                } else if (insertCode == -5) {
                    ErrorMessage em = ErrorMessage.TIMESLOT_FULL;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s", em.getMessage()));

                    showBookingPage(req, res);
                } else if (insertCode == -6) {
                    ErrorMessage em = ErrorMessage.OVERLAPPING_APPOINTMENT;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s", em.getMessage()));

                    showBookingPage(req, res);
                }

            }
        } catch (ServletException | SQLException | NamingException e) {
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
        }
    }

    public void updateAppointment(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession session = req.getSession();
            String email = (String) session.getAttribute("email");
            User.Role role = (User.Role) session.getAttribute("role");

            if (email == null) {
                req.setAttribute("error_msg", "please login");
                req.getRequestDispatcher("error.jsp").forward(req, res);
            } else {
                long datetimeLong = Long.parseLong(req.getParameter("datetime_long"));
                int appointmentId = Integer.parseInt(req.getParameter("appointmentId"));

                AppointmentDAO dao = new AppointmentDAO(DataSourceProvider.getDataSource().getConnection());
                Appointment toBeUpdated = dao.getAppointmentById(appointmentId);
                int serviceID = toBeUpdated.getServiceID();

                Timestamp tm = new Timestamp(datetimeLong);

                Appointment appointment = new Appointment(
                        appointmentId,
                        serviceID,
                        Appointment.statuses.BOOKED,
                        tm,
                        email);

                int updateCode = dao.updateAppointment(appointment);
                dao.closeConnection();
                System.out.println(updateCode);
                if (updateCode == 0) {
                    // res.sendRedirect(req.getContextPath() + "/customer");
                    res.sendRedirect(String.format("%s/customer", req.getContextPath()));
                } else if (updateCode == -2 || updateCode == -3 || updateCode == -4) {
                    ErrorMessage em = ErrorMessage.INVALID_INPUT_FIELDS;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s", em.getMessage()));

                    showUpdatePage(req,res,toBeUpdated);
                } else if (updateCode == -5) {
                    ErrorMessage em = ErrorMessage.TIMESLOT_FULL;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s", em.getMessage()));

                    showUpdatePage(req,res,toBeUpdated);
                } else if (updateCode == -6) {
                    ErrorMessage em = ErrorMessage.OVERLAPPING_APPOINTMENT;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s", em.getMessage()));

                    showUpdatePage(req,res,toBeUpdated);
                }

            }
        } catch (ServletException | SQLException | NamingException e) {
            e.printStackTrace();
            writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
        }
    }

}
