package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resource.User;
import utils.ErrorMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

public class RoleBasedFilter extends AbstractFilter{
    private final HashMap<String, List<User.Role>> acl = new HashMap<>();
    private String subPathServlet;

    /**
     * On the basis of the data contained in the config parameter, initialisation of the table of request types
     * (HTTP method and sub-path) which will be handled by the filter and the relative lists of user role
     * which are authorised to make them.
     *
     * @param config list of request types to be handled (HTTP method and sub-path) and user roles authorised to access them
     */
    @Override
    public void init(FilterConfig config) {

        Enumeration<String> paramsName = config.getInitParameterNames();

        while (paramsName.hasMoreElements()) {
            String param = paramsName.nextElement();

            if(param.contains(",")) {
                List<User.Role> rolesHasAccess = new ArrayList<>();
                String[] rolesHasAccessString = config.getInitParameter(param).split(",");

                for (String roleString : rolesHasAccessString) {
                    rolesHasAccess.add(User.Role.valueOf(roleString));
                }

                acl.put(param, rolesHasAccess);
            }
        }

        this.subPathServlet = config.getInitParameter("subPathServlet");

    }

    /**
     * For each request submitted to the filter, the ACL checks whether it is the task of the filter to handle it.
     * If not, the request is directly forwarded to the next element in the chain. If yes, it is checked that the user role
     * associated with the request is in the list of authorised roles. If this condition is not met, the request is sent
     * to the servlet to send error messages specifying that higher privileges are required.
     *
     * @param req HTTP request to be authorised
     * @param res HTTP response associated with the request to be handled
     * @param chain chain of filters to manage the response
     * @throws IOException thrown in the event of any other backend problems
     * @throws ServletException launched in case there was a problem forwarding the request to the next element of the chain
     */
    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        User.Role userRole = (User.Role) session.getAttribute("role");
        String op = req.getRequestURI().replace(req.getContextPath(),"");


        for (String resource : acl.keySet()) {

            String pattern = resource.substring(0,resource.indexOf(" , "));
            String methods = resource.substring(resource.indexOf(" , ")+" , ".length());

            /*System.out.println("-----------");
            System.out.println("userRole: "+userRole);
            System.out.println("pattern: "+pattern);
            System.out.println("methods: "+methods);
            System.out.println("url: "+op);
            System.out.println("pattern: "+op.matches(pattern));
            System.out.println("method: "+methods.contains(req.getMethod()));
            System.out.println("-----------");*/

            if(op.matches(pattern) && methods.contains(req.getMethod())){
                if(acl.get(resource).contains(userRole)) {
                    //chain.doFilter(req, res);
                    ServletContext context = req.getSession(false).getServletContext();
                    context.getNamedDispatcher(subPathServlet).forward(req,res);
                }else{
                    writeErrorGuiPage(req, res, ErrorMessage.HIGHER_PRIVILEGE_REQUIRED);
                }
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
