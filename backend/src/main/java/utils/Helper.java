package utils;

import dao.DepartmentDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resource.User;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class Helper {
    public static final String customerHome = "/customer";
    public static final String adminHome = "/company";
    public static final String managerHome = "/company/department";
    public static final String secretaryHome = "/company/department";

    public static void roleBasedRedirect(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, NamingException {
        HttpSession session = req.getSession(false);
        User.Role userRole = (User.Role) session.getAttribute("role");
        String userEmail = (String) session.getAttribute("email");

        switch (userRole) {
            case admin:
                res.sendRedirect(req.getContextPath() + adminHome);
                break;
            case customer:
                res.sendRedirect(req.getContextPath() + customerHome);
                break;
            case secretary:
                DepartmentDAO dDaoSec = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
                res.sendRedirect(req.getContextPath() + secretaryHome + "/" + dDaoSec.getDepartmentBySecretary(userEmail).getID());
                dDaoSec.closeConnection();
                break;
            case manager:
                DepartmentDAO dDaoMan = new DepartmentDAO(DataSourceProvider.getDataSource().getConnection());
                res.sendRedirect(req.getContextPath() + managerHome + "/" + dDaoMan.getDepartmentByManager(userEmail).getID());
                dDaoMan.closeConnection();
                break;
        }
    }

    public static void sendEmail(String to, String subject, String messageBody) {

        String from = "noreply@appointmentsApp.com";
        String host = "192.168.1.168";
        String port = "2525";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message.setSubject(subject);

            message.setContent(messageBody, "text/html; charset=utf-8");

            Transport.send(message);

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public static String getBaseUrl( HttpServletRequest request ) {
        if ( ( request.getServerPort() == 80 ) ||
                ( request.getServerPort() == 443 ) )
            return request.getScheme() + "://" +
                    request.getServerName() +
                    request.getContextPath();
        else
            return request.getScheme() + "://" +
                    request.getServerName() + ":" + request.getServerPort() +
                    request.getContextPath();
    }
}
