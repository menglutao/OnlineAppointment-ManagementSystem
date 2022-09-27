package servlet;

import dao.CompanyDAO;
import dao.UserDAO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resource.Company;
import resource.User;
import utils.DataSourceProvider;
import utils.ErrorMessage;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static resource.User.generateSalt;
import static resource.User.hashPassword;
import static utils.Helper.*;
import static utils.JWTHandler.createJWT;
import static utils.JWTHandler.decodeJWT;

public class UserServlet extends AbstractServlet{

    private final String confimationEmailTemplate = "Hi {name},<br/>" +
            "You're almost there! Please take a second to make sure we've got your email right. <br/>" +
            "Just click the link below to confirm(within MAXIMUM ONE HOUR):<br/>" +
            "<a href=\"{verificationLink}\"> {verificationLink}</a><br/><br/>" +
            "Thanks!<br/>" +
            "Appointments App";

    /**
     * Dispatch based on the URL contained in the GET request to the method or JSP dedicated to serving it.
     *
     * @param req HTTP request to serve
     * @param res HTTP response associated with the managed request
     * @throws ServletException thrown in case there was an error in the JSPs dedicated to handling certain types of requests.
     * @throws IOException thrown in the event of any other backend problems
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length());

        switch (op){
            case "/logout":
                deleteCookie("authToken", res);
                req.getSession().invalidate();
                res.sendRedirect(req.getContextPath());
                break;
            case "/login":
                deleteCookie("authToken", res);
                req.getRequestDispatcher("/jsp/login.jsp").forward(req, res);
                break;
            case "/register":
                req.getRequestDispatcher("/jsp/register.jsp").forward(req, res);
                break;
            case "/profile":
                req.getRequestDispatcher("/jsp/profile.jsp").forward(req, res);
                break;
            case "/verify":
                doVerifyUser(req,res);
                break;
            default:
                writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }
    }

    /**
     * Dispatch based on the URL contained in the POST request to the method dedicated to serving it.
     *
     * @param req HTTP request to serve
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        op = op.substring(op.lastIndexOf(req.getContextPath()) + req.getContextPath().length());

        switch (op){
            case "/login":
                doLogin(req, res);
                break;
            case "/register":
                doRegistration(req, res);
                break;
            case "/profile":
                doUpdateUserInfo(req, res);
                break;
            default:
                writeErrorGuiPage(res, ErrorMessage.UNKNOWN_OPERATION);
        }
    }

    /**
     * Updates the data for the user specified by the email field if all the necessary fields are present and the password repetition is correct.
     * If either of these two conditions is not met or an error occurs in the execution of the query, the corresponding
     * message is forwarded to the dedicated servlet.
     *
     * @param req HTTP request containing the user's e-mail and the new values to be entered
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */
    private void doUpdateUserInfo(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            HttpSession session = req.getSession();
            String email = (String) session.getAttribute("email");
            User.Role role = (User.Role) session.getAttribute("role");
            String name = req.getParameter("name");
            String phone = req.getParameter("phone");
            String password = req.getParameter("password");
            String passwordCheck  = req.getParameter("passwordCheck");

            ErrorMessage em = null;
            boolean userEmptyFields = password==null||password.equals("") || name==null||name.equals("") || phone==null||phone.equals("");
            boolean differentPassword = !password.equals(passwordCheck);

            if(userEmptyFields){
                em = ErrorMessage.EMPTY_INPUT_FIELDS;
            }else if(differentPassword){
                em = ErrorMessage.DIFFERENT_PASSWORDS;
            }

            if (em!=null){
                res.setStatus(em.getHttpErrorCode());
                req.setAttribute("errorMessage", String.format("Error: %s",em.getMessage()));

                req.getRequestDispatcher("/jsp/profile.jsp").forward(req, res);
            }
            else {
                String salt = generateSalt();
                String passwordHashed = hashPassword(password,salt);

                UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());

                User u = new User(email, name, phone, passwordHashed, salt, role, true);
                dao.updateUser(u);

                dao.closeConnection();

                roleBasedRedirect(req,res);
            }
        } catch(SQLException | ServletException |NamingException e) {
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     * If the email is registered and the specified password matches the one associated with the email in question,
     * creation of the HTTP session to authenticate the user and redirect to the page associated with the user's role.
     * If the email or password are not specified or there is an error in retrieving the data from the database,
     * the request will be forwarded to the dedicated servlet with an error message.
     *
     * @param req HTTP request containing email and password to be verified
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */
    public void doLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String email = req.getParameter("email");
            String password = req.getParameter("password");

            if (email == null || email.equals("") || password == null || password.equals("")) {
                ErrorMessage em = ErrorMessage.EMPTY_INPUT_FIELDS;
                res.setStatus(em.getHttpErrorCode());
                req.setAttribute("errorMessage", String.format("Error: %s",em.getMessage()));

                req.getRequestDispatcher("/jsp/login.jsp").forward(req, res);
            }else{
                //try to authenticate the user
                UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
                User u = dao.getUserByEmail(email);
                dao.closeConnection();

                boolean authenticated = false;

                if(u!=null){
                    authenticated = hashPassword(password,u.getSalt()).equals(u.getPassword());
                }

                if (!authenticated){
                    ErrorMessage em = ErrorMessage.WRONG_CREDENTIALS;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s",em.getMessage()));

                    req.getRequestDispatcher("/jsp/login.jsp").forward(req, res);
                }
                else{
                    HttpSession session = req.getSession();

                    session.setAttribute("email", u.getEmail());
                    session.setAttribute("role", u.getRole());
                    session.setAttribute("name", u.getName());
                    session.setAttribute("phone", u.getPhone());

                    //set authToken cookie sent by the client
                    Cookie[] cookies = req.getCookies();
                    for (int i = 0; i < cookies.length; i++) {
                        if("authToken".equals(cookies[i].getName())){
                            Cookie tokenCookie = new Cookie("authToken", cookies[i].getValue());
                            //tokenCookie.setSecure(true); should be set to prevent sending the cookie over unsecure protocols
                            //default browser behavior is to set cookie sameSite=lax so that it is not forwarded to other domain requests
                            res.addCookie(tokenCookie);
                        }
                    }

                    roleBasedRedirect(req,res);
                }
            }
        } catch (Exception e){
            writeErrorGuiPage(res, ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     * If all the fields necessary for the registration of customer or admin users are present, the relevant user and company will
     * be created (only in the case of admin users) after checking that the e-mail has not already been used and that there is no company with the same name.
     * If one of the above conditions is not met the request will be sent JSP of the registration page together with an error message.
     * In the event that an error occurs when entering data into the database the request will be handled by the appropriate servlet to display the error message.
     * On the other hand, if the insertion is successful, a confirmation e-mail will be sent and the user will be redirected to the login page.
     *
     * @param req HTTP request containing data on the user and the possible company to be registered
     * @param res response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */
    public void doRegistration(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String email = req.getParameter("email");
            String name = req.getParameter("name");
            String phone = req.getParameter("phone");
            String password = req.getParameter("password");
            String role  = req.getParameter("role");
            String passwordCheck  = req.getParameter("passwordCheck");

            String companyName = req.getParameter("companyName");
            String companyAddress = req.getParameter("companyAddress");
            String companyLat = req.getParameter("companyLat");
            String companyLon = req.getParameter("companyLon");
            String companyPhone = req.getParameter("companyPhone");
            String companyEmail = req.getParameter("companyEmail");

            ErrorMessage em = null;
            boolean userEmptyFields = email==null||email.equals("")|| password==null||password.equals("") || name==null||name.equals("") || phone==null||phone.equals("") || role==null || role.equals("");
            boolean companyEmptyFields =  companyName==null||companyName.equals("") || companyAddress==null||companyAddress.equals("") ||  companyLat==null||companyLat.equals("") || companyLon==null||companyLon.equals("") || companyPhone==null||companyPhone.equals("") || companyEmail==null||companyEmail.equals("");
            boolean differentPassword = !password.equals(passwordCheck);

            if(userEmptyFields || (companyEmptyFields && role.equals("admin"))){
                em = ErrorMessage.EMPTY_INPUT_FIELDS;
            }else if(differentPassword){
                em = ErrorMessage.DIFFERENT_PASSWORDS;
            }

            if (em!=null){
                res.setStatus(em.getHttpErrorCode());
                req.setAttribute("errorMessage", String.format("Error: %s",em.getMessage()));

                req.getRequestDispatcher("/jsp/register.jsp").forward(req, res);
            }
            else {
                UserDAO getUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());

                User u = getUserDao.getUserByEmail(email);
                getUserDao.closeConnection();

                CompanyDAO cdao = new CompanyDAO(
                  DataSourceProvider.getDataSource().getConnection()
                  );
                Company company = cdao.getCompanyByName(companyName);
                cdao.closeConnection();

                if (u!=null){
                    em = ErrorMessage.MAIL_IN_USE;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s",em.getMessage()));

                    req.getRequestDispatcher("/jsp/register.jsp").forward(req, res);
                }else if(User.Role.valueOf(role)== User.Role.admin && company != null){
                    em = ErrorMessage.COMPANY_NAME_IN_USE;
                    res.setStatus(em.getHttpErrorCode());
                    req.setAttribute("errorMessage", String.format("Error: %s",em.getMessage()));

                    req.getRequestDispatcher("/jsp/register.jsp").forward(req, res);
                }
                else {
                    u = new User(email, name, phone, password, User.Role.valueOf(role));
                    UserDAO insertUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
                    boolean result = insertUserDao.insertUser(u);

                    insertUserDao.closeConnection();

                    

                    if (result) {
                        if(u.getRole()== User.Role.admin){
                            Company c = new Company(companyName, companyAddress,
                                    Double.parseDouble(companyLat),Double.parseDouble(companyLon),
                                    companyPhone, companyEmail,u.getEmail());

                            CompanyDAO cdao2 = new CompanyDAO(
                              DataSourceProvider.getDataSource().getConnection()
                              );
                            cdao2.insertCompany(c);
                            cdao2.closeConnection();
                        }

                        String verificationToken = createJWT(u.getEmail(),"UserServlet","user-verification", TimeUnit.HOURS.toMillis(1));
                        String verificationURL = String.format("%s/verify?token=%s",getBaseUrl(req),verificationToken);
                        String emailBody = confimationEmailTemplate.replace("{name}", u.getName()).replace("{verificationLink}",verificationURL);

                        sendEmail(u.getEmail(),"Please verify your email address",emailBody);

                        res.sendRedirect(String.format("%s/login?message=issuedVerification",req.getContextPath()));
                    } else {
                        writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
                    }
                }
            }
        } catch(SQLException | ServletException | NamingException e) {
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     * After verifying through the JWT contained in the request that the account in question is to be verified, it is enabled to
     * access the system by updating the appropriate field in the database. If the token is invalid or contains incorrect information,
     * the request is forwarded to the JSP of the login page with the relevant error. In the event that an error occurs
     * in accessing the database the request will be forwarded to the JSP to show error.
     *
     * @param req HTTP request containing the JWT needed for account verification
     * @param res HTTP response associated with the managed request
     * @throws IOException thrown in the event of backend problems
     */
    public void doVerifyUser(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String token = req.getParameter("token");

            if(token!=null || token.equals("")){
                Claims jwtToken = decodeJWT(token);
                boolean validClaims = jwtToken.getIssuer().equals("UserServlet") && jwtToken.getSubject().equals("user-verification") && (new Date()).before(jwtToken.getExpiration());
                UserDAO getUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
                User u = getUserDao.getUserByEmail(jwtToken.getId());
                getUserDao.closeConnection();

                if(validClaims && u!=null && !u.isVerified()){
                    User verifiedUser = new User(u.getEmail(), u.getName(), u.getPhone(), u.getPassword(), u.getSalt(), u.getRole(), true);

                    UserDAO updateUserDao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
                    updateUserDao.updateUser(verifiedUser);
                    updateUserDao.closeConnection();

                    res.sendRedirect(String.format("%s/login?message=verificationSuccess",req.getContextPath()));
                }
            }else{
                throw new IllegalArgumentException();
            }

        }catch (io.jsonwebtoken.ExpiredJwtException | io.jsonwebtoken.UnsupportedJwtException | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.SignatureException | IllegalArgumentException e){
            res.sendRedirect(String.format("%s/login?message=verificationError",req.getContextPath()));
        }catch (SQLException | NamingException e){
            e.printStackTrace();
            writeErrorGuiPage(res,ErrorMessage.INTERNAL_ERROR);
        }
    }

    /**
     * Removes a cookie from an HttpServletResponse
     * @param cookieName name of the cookie to be removed
     * @param res HTTP response where the cookie must be removed
     */
    private void deleteCookie(String cookieName, HttpServletResponse res){
        //delete auth token cookie
        Cookie tokenCookie = new Cookie(cookieName,"");
        tokenCookie.setMaxAge(0);
        res.addCookie(tokenCookie);
    }

}
