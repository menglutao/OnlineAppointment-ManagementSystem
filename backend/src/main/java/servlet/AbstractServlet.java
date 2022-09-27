package servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ErrorMessage;

import java.io.IOException;

public class AbstractServlet extends HttpServlet {

    //override the init method: here you should put the initialization of your servlet
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }


    //override destroy method: here you should put the behaviour of your servlet when destroyed
    @Override
    public void destroy(){
        super.destroy();
    }

    /**
     * sends a redirect response to the error page with information concerning the error message
     * @param res HttpServletResponse to be prepared for responding to the request
     * @param em error message used to create the response
     * @throws IOException when an error occurred during the response writing
     */
    public void writeErrorGuiPage(HttpServletResponse res, ErrorMessage em) throws IOException{
        res.setStatus(em.getHttpErrorCode());
        res.sendRedirect(getServletContext().getContextPath() + "/jsp/error.jsp?code="+em.getHttpErrorCode()+"&message="+em.getMessage());
    }

    /**
     * writes a http response containing the error message in JSON
     * @param res HttpServletResponse to be prepared for responding to the request
     * @param em error message used to create the response
     * @throws IOException when an error occurred during the response writing
     */
    public void writeError(HttpServletResponse res, ErrorMessage em) throws IOException {
        res.setContentType("application/json");
        res.setStatus(em.getHttpErrorCode());
        res.getWriter().write(em.toJSON().toString());
    }
}
