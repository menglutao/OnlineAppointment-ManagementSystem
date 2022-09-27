package filter;

import dao.UserDAO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resource.User;
import utils.DataSourceProvider;
import utils.ErrorMessage;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;

public class ProtectedResourceFilter extends AbstractFilter {

    /**
     * Checks through the HTTP session that the user associated with the request has been authenticated and the account associated with
     * it has been verified beforehand. If so, it forwards the request to the next element in the chain. On the other hand, if one of
     * the two conditions is not met, it forces a redirect to the login page, displaying a message if the account has not been verified.
     *
     * @param req HTTP request to authenticate
     * @param res HTTP response associated with the request to be handled
     * @param chain chain of filters to manage the response
     * @throws IOException thrown in the event of any other backend problems
     * @throws ServletException launched in case there was a problem forwarding the request to the next element of the chain
     */
    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpSession session = req.getSession(false);
        String loginURI = req.getContextPath() + "/login";
        UserDAO dao = null;

        boolean loggedIn = session != null && session.getAttribute("email") != null;

        if (loggedIn) {
            try {
                dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());

                User u = dao.getUserByEmail((String) session.getAttribute("email"));
                dao.closeConnection();

                if(u.isVerified()){
                    res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    res.setHeader("Pragma", "no-cache");
                    chain.doFilter(req, res);
                    return;
                }else{
                    res.sendRedirect(req.getContextPath() + "/login?message=verificationPending");
                }
            } catch (SQLException | NamingException e) {
                e.printStackTrace();
                writeErrorGuiPage(req,res, ErrorMessage.INTERNAL_ERROR);
            }
        } else {
            res.sendRedirect(loginURI);
        }
    }
}