package filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ErrorMessage;

import java.io.IOException;

public class AbstractFilter extends HttpFilter {

    /**
     * Forwards to the appropriate JSP req (i.e. the request that caused the problem) specifying through the em
     * parameter the message and the code associated with the error.
     *
     * @param req request that generated an error
     * @param res response containing the error message
     * @param em type of error associated with the handling of the request
     * @throws ServletException thrown in case there is a problem forwarding the request to the dedicated jsp
     * @throws IOException thrown in the event of any other backend problems
     */
    public void writeErrorGuiPage(HttpServletRequest req, HttpServletResponse res, ErrorMessage em) throws ServletException, IOException {
        res.setStatus(em.getHttpErrorCode());
        req.setAttribute("errorMessage", em.getMessage());
        req.setAttribute("errorCode", em.getHttpErrorCode());

        req.getRequestDispatcher("/jsp/error.jsp").forward(req, res);
    }
}
