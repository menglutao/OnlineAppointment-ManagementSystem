package servlet;

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
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DepartmentServlet extends AbstractServlet{
  
  private enum Operation {
    NONE,
    ROOT,
    PROFILE,
    UPDATE,
    DELETE,
    NEW,
    ADD_SECRETARY,
    DELETE_SECRETARY
  }



  /* Is it a department's root page? Here are some valid examples:
   * /app1/company/department/2
   * /app1/Company/department/2/
   * /app2/company/Department/123
   * /app2/company/department/123/
   */
  static Pattern depIdPattern = Pattern.compile(".*/company/department/(\\d+)/*$",
                                                Pattern.CASE_INSENSITIVE);
  static Pattern profilePattern = Pattern.compile(".*/company/department/(\\d+)/profile/*$",
                                             Pattern.CASE_INSENSITIVE);
  static Pattern newPattern = Pattern.compile(".*/company/department/new/*$",
                                             Pattern.CASE_INSENSITIVE);

  static Pattern deletePattern = Pattern.compile(".*/company/department/(\\d+)/delete/*$",
          Pattern.CASE_INSENSITIVE);

  static Pattern addSecretaryPattern = Pattern.compile(".*/company/department/(\\d+)/secretary/add/*$",
          Pattern.CASE_INSENSITIVE);

  static Pattern deleteSecretaryPattern = Pattern.compile(".*/company/department/(\\d+)/secretary/delete$",
          Pattern.CASE_INSENSITIVE);


  /**
   * Dispatch the GET request to department's dashboard, its profile,
   * delete or create new department
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    String url = req.getRequestURI();
    Matcher matcher;
    int depId = 0;
    Department dep = null;
    Operation op = Operation.NONE;

    /* Test first regex */
    matcher = depIdPattern.matcher(url);
    if (matcher.matches()){
      depId = Integer.parseInt(matcher.group(1));
      op = Operation.ROOT;      
    }

    /* If it's not yet matched, test the next regex */
    if (op == Operation.NONE) {
      matcher = profilePattern.matcher(url);
      if (matcher.matches()) {
        depId = Integer.parseInt(matcher.group(1));
        op = Operation.PROFILE;
      }
    }

    if (op == Operation.NONE) {
      matcher = deletePattern.matcher(url);
      if (matcher.matches()){
        depId = Integer.parseInt(matcher.group(1));
        op = Operation.DELETE;
      }
    }

    if (op == Operation.NONE) {
      matcher = addSecretaryPattern.matcher(url);
      if (matcher.matches()){
        depId = Integer.parseInt(matcher.group(1));
        op = Operation.ADD_SECRETARY;
      }
    }

    /* If it's not yet matched, test the next regex */
    if (op == Operation.NONE) {
      matcher = newPattern.matcher(url);
      if (matcher.matches()) {
        op = Operation.NEW;
      }
    }


    /* Decide what to do based on operation */
    switch (op) {
      case NONE:
        writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        break;
      case ROOT:
        doShowDepartment(req,res,depId);
        break;
      case PROFILE:
        doShowDepartmentProfile(req,res,depId);
        break;
      case DELETE:
        doDepartmentDelete(req,res,depId);
        break;
      case NEW:
        req.getRequestDispatcher("/jsp/company/department/profile.jsp").forward(req, res);
        break;
      case ADD_SECRETARY:
        doShowAddSecretary(req,res,depId);
        break;
      }

  }

  /**
   * Show secretary of a department
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   * @param depId ID of the Department we want to show its secretary user
   */
  private void doShowAddSecretary(HttpServletRequest req, HttpServletResponse res, int depId) throws IOException {
    try {
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department dep = depDao.getDepartmentById(depId);
      depDao.closeConnection();

      if(dep!=null) {
        UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        List<User> customers = dao.getUserByRole(User.Role.customer);
        dao.closeConnection();

        req.setAttribute("customers", customers);
        req.getRequestDispatcher("/jsp/company/department/secretary.jsp").forward(req, res);
      }else{
        writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
//        writeErrorGuiPage(res, ErrorMessage.DEPARTMENT_NOT_FOUND);
      }

    } catch (SQLException | NamingException | ServletException | IOException e) {
      writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
    }
  }

  /**
   * Delete a department
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   * @param depId ID of the Department we want to delete it
   */
  private void doDepartmentDelete(HttpServletRequest req, HttpServletResponse res, int depId) throws IOException {
    try {
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department department = depDao.getDepartmentById(depId);

      if(department!=null){

        if(depDao.deleteDepartment(department)) {
          System.out.println(req.getContextPath() + "/company");
          res.sendRedirect(req.getContextPath() + "/company");
        }else{
          writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }

      }else{
        writeErrorGuiPage(res,ErrorMessage.DEPARTMENT_NOT_FOUND);
      }
      depDao.closeConnection();
    } catch(SQLException | NumberFormatException | NamingException e){
      e.printStackTrace();
      writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
    }
  }

  /**
   * Show department's profile
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   * @param depId ID of the Department we want to show its profile
   */
  private void doShowDepartmentProfile(HttpServletRequest req, HttpServletResponse res, int depId) throws IOException {
    try {
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department dep = depDao.getDepartmentById(depId);
      depDao.closeConnection();

      if(dep!=null) {
        String actionUrl = "/company/department/" + dep.getID() + "/profile";
        req.setAttribute("actionUrl", actionUrl);
        req.setAttribute("dep", dep);
        req.getRequestDispatcher("/jsp/company/department/profile.jsp").forward(req, res);
      }else{
        writeErrorGuiPage(res, ErrorMessage.DEPARTMENT_NOT_FOUND);
      }

    } catch (SQLException | NamingException | ServletException | IOException e) {
      writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
    }
  }

  /**
   * Show department's main page
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   * @param depId ID of the Department we want to show it
   */
  private void doShowDepartment(HttpServletRequest req, HttpServletResponse res, int depId) throws IOException {

    try {
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department dep = depDao.getDepartmentById(depId);
      depDao.closeConnection();

      if(dep!=null) {
        ServiceDAO Sdao = new ServiceDAO(DataSourceProvider.getDataSource().getConnection());
        List<Service> services = Sdao.getServicesByDepartment(dep.getID());
        Sdao.closeConnection();

        //List<Service> services = ServiceDAO.getServicesByDepartment(dep.getID());
        UserDAO secretaryDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        List<User> secretaries = secretaryDao.getSecretariesByDepartment(dep);
        secretaryDao.closeConnection();

        UserDAO customerDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        List<User> customers = customerDao.getUserByRole(User.Role.customer);
        customerDao.closeConnection();

        req.setAttribute("dep", dep);
        req.setAttribute("services", services);
        req.setAttribute("secretaries", secretaries);
        req.setAttribute("customers", customers);

        req.getRequestDispatcher("/jsp/company/department/index.jsp").forward(req, res);
      }else{
        writeErrorGuiPage(res, ErrorMessage.DEPARTMENT_NOT_FOUND);
      }
    } catch (java.sql.SQLException | javax.naming.NamingException | ServletException e) {
      e.printStackTrace();
      writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
    }
  }

  /**
   * Process POST request to department's URLs to update and create a new
   * department, also for adding or removing a secretary from a department.
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String profilePage = "[0-9]+/profile";
    String newPage = "new";
    String addSecretaryPage = "[0-9]+/secretary/add";
    String deleteSecretaryPage = "[0-9]+/secretary/delete";

    String op = req.getRequestURI();
    op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

    if(op.matches(profilePage)){
      updateProfile(req, res);
    }else if(op.matches(newPage)){
      newDepartment(req, res);
    }else if(op.matches(addSecretaryPage)){
      doAddSecretary(req, res);
    }else if(op.matches(deleteSecretaryPage)){
      doDeleteSecretary(req, res);
    }else{
      writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
    }
  }

  /**
   * Add secretary to a department. This method is used by doPost function.
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   */
  private void doAddSecretary(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String op = req.getRequestURI();
    op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

    int depId = Integer.parseInt(op.substring(0,op.indexOf("/secretary/")));

    try {

      String userEmail = req.getParameter("new-secretary");
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department dep = depDao.getDepartmentById(depId);
      depDao.closeConnection();

      boolean emailEmpty = userEmail==null || userEmail.equals("");

      if(dep==null){
        writeErrorGuiPage(res,ErrorMessage.DEPARTMENT_NOT_FOUND);
      }else if(emailEmpty){
        ErrorMessage em = ErrorMessage.EMPTY_INPUT_FIELDS;
        UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        List<User> customers = dao.getUserByRole(User.Role.customer);
        dao.closeConnection();

        req.setAttribute("errorMessage", "Error: "+em.getMessage());
        req.setAttribute("customers", customers);

        req.getRequestDispatcher("/jsp/company/department/secretary.jsp").forward(req, res);
      }else{
        UserDAO getUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        User futureSecretary = getUserDao.getUserByEmail(userEmail);
        getUserDao.closeConnection();

        if(futureSecretary!=null && futureSecretary.getRole().equals(User.Role.customer)){
          UserDAO addUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
          boolean result = addUserDao.addSecretary(futureSecretary, dep);
          addUserDao.closeConnection();

          if(result){
            res.sendRedirect(req.getContextPath()+"/company/department/"+dep.getID());
          }else{
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
          }
        }else{
          /*ErrorMessage em = ErrorMessage.INVALID_INPUT_FIELDS;
          UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
          List<User> customers = dao.getUserByRole(User.Role.customer);

          dao.closeConnection();*/

          res.sendRedirect(req.getContextPath()+"/company/department/"+dep.getID()+"?errorMessage=invalidSecretary");

          /*req.setAttribute("errorMessage", "Error: "+em.getMessage());
          req.setAttribute("customers", customers);

          req.getRequestDispatcher("/jsp/company/department/secretary.jsp").forward(req, res);*/
        }
      }
    } catch (SQLException | NamingException | ServletException  e) {
      e.printStackTrace();
      writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
    }
  }

  /**
   * Delete a secretary from a department
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   *
   */
  private void doDeleteSecretary(HttpServletRequest req, HttpServletResponse res) throws IOException {
    String op = req.getRequestURI();
    op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

    int depId = Integer.parseInt(op.substring(0,op.indexOf("/secretary/")));

    try {
      String userEmail = req.getParameter("secretary");
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      Department dep = depDao.getDepartmentById(depId);
      depDao.closeConnection();

      boolean emailEmpty = userEmail==null || userEmail.equals("");

      if(dep==null){
        writeErrorGuiPage(res,ErrorMessage.DEPARTMENT_NOT_FOUND);
      }else if(emailEmpty){
        writeErrorGuiPage(res,ErrorMessage.EMPTY_INPUT_FIELDS);
      }else{
        UserDAO getUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        User secretary = getUserDao.getUserByEmail(userEmail);
        getUserDao.closeConnection();

        if(secretary!=null && secretary.getRole().equals(User.Role.secretary)){
          UserDAO deleteUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
          boolean result = deleteUserDao.deleteSecretary(secretary);
          deleteUserDao.closeConnection();

          if(result){
            res.sendRedirect(req.getContextPath()+"/company/department/"+dep.getID());
          }else{
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
          }
        }else{
          writeErrorGuiPage(res,ErrorMessage.INVALID_INPUT_FIELDS);
        }
      }
    } catch (SQLException | NamingException e) {
      e.printStackTrace();
      writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
    }
  }

  /**
   * Update department's profile
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   */
  private void updateProfile(HttpServletRequest req, HttpServletResponse res) throws IOException {
      String op = req.getRequestURI();
      op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length() + "/company/department/".length() );

      int depId = Integer.parseInt(op.substring(0,op.indexOf("/profile")));
      Department dep = null;
      try{
        DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
        dep = depDao.getDepartmentById(depId);
        depDao.closeConnection();

      }catch (SQLException | NamingException e) {
        e.printStackTrace();
        writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
      }

      if(dep==null){
        writeErrorGuiPage(res,ErrorMessage.DEPARTMENT_NOT_FOUND);
      }

    String name = req.getParameter("name");
      String desc = req.getParameter("desc");
      String manager = req.getParameter("manager");

      Department updated = new Department(dep.getID(), name, desc, dep.getCompanyID(), manager);

      boolean ret = false;
      ErrorMessage em = ErrorMessage.OK;
      /* Department object to be sent to the page */
      Department outDep = null;

      try {
        DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
        ret = depDao.updateDepartment(updated);
        depDao.closeConnection();

        if (ret) {
          res.sendRedirect(req.getContextPath()+"/company/department/"+updated.getID());
        } else {
          em = ErrorMessage.INVALID_INPUT_FIELDS;
          res.setStatus(em.getHttpErrorCode());
          req.setAttribute("errorMessage", "Error: "+em.getMessage());

          String actionUrl = "/company/department/" + dep.getID() + "/profile";
          req.setAttribute("actionUrl", actionUrl);
          req.setAttribute("dep", dep);

          req.getRequestDispatcher("/jsp/company/department/profile.jsp").forward(req, res);
        }
      }catch (SQLException | NamingException | ServletException e) {
        writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
      }


  }

  /**
   * Create a new department
   *
   * @param req Incomig HTTP request
   * @param res Outgoing HTTP response
   */
  private void newDepartment(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String name = req.getParameter("name");
    String desc = req.getParameter("desc");
    String manager_email = req.getParameter("manager");
    ErrorMessage em = ErrorMessage.OK;

    /* Get company belonging to logged-in user */
    HttpSession session = req.getSession();
    String email = (String) session.getAttribute("email");
    User user = null;
    User manager = null;
    Company company = null;


    try {
      UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
      CompanyDAO cdao = new CompanyDAO(
        DataSourceProvider.getDataSource().getConnection()
        );

      user = dao.getUserByEmail(email);
      dao.closeConnection();
      company = cdao.getCompanyByAdmin(user);
      cdao.closeConnection();
    } catch(SQLException | NamingException e) {
      writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
    }

    Department dep = new Department(name, desc, company.getName(), manager_email);
    boolean ret = false;

    try {
      DepartmentDAO depDao = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
      ret = depDao.insertDepartment(dep);
      depDao.closeConnection();

      if (ret) {
        em = ErrorMessage.OK;
      } else {
        writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
      }
    }catch (SQLException | NamingException e) {
      em = ErrorMessage.INTERNAL_ERROR;
      writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
    }

    if (em == ErrorMessage.OK) {
      /* Change given user role to Manager if it's necessary */
      try {
        UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
        manager = dao.getUserByEmail(manager_email);
        dao.changeRole(manager, User.Role.manager);
        dao.closeConnection();
      } catch (SQLException | NamingException e) {
        writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
      }
      res.sendRedirect(req.getContextPath() + "/company");
    } else {
      req.setAttribute("errorMessage", em.getMessage());
      req.setAttribute("dep", dep);
      req.getRequestDispatcher("/jsp/company/department/profile.jsp").forward(req, res);
    }
  }

}
