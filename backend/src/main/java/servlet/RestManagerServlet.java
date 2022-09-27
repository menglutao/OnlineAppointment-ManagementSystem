package servlet;

import dao.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import resource.User;
import resource.User.Role;
import rest.AppointmentRestResource;
import utils.DataSourceProvider;
import utils.ErrorMessage;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static utils.JWTHandler.createJWT;
import static utils.JWTHandler.decodeJWT;

public class RestManagerServlet extends AbstractServlet{

    final long TOKEN_EXPIRATION_MINUTES = 150;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!checkMethodMediaType(req, res)) {
            return;
        }
        if(processAppointment(req, res)){
            return;
        }
        if(processAuthorization(req, res)){
            return;
        }
        writeError(res, ErrorMessage.UNKNOWN_OPERATION);
    }

    /**
     * Handles a GET request to backend/rest/auth/ with basic authentication scheme
     * Responds with an authorization token if the login is successful
     * @param req HttpServletRequest received to be processed
     * @param res HttpServletResponse to be prepared for responding to the request
     * @return True if it was an authorization request, false otherwise
     * @throws IOException when an error occurred during the error response writing
     */
    private boolean processAuthorization(HttpServletRequest req, HttpServletResponse res) throws IOException  {
        String op = req.getRequestURI();
        String[] tokens = op.split("/");
        final int MIN_TOKEN = 4;
        final int APPOINTMENT_TOKEN_INDEX = 3;
        if (tokens.length != MIN_TOKEN || !("auth".equals(tokens[APPOINTMENT_TOKEN_INDEX]))){
            return false;
        }
        // /backend/rest/auth/

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            writeError(res, ErrorMessage.AUTH_HEADER_MISSING);
        } else {
            String[] authTokens = authHeader.split(" ");
            if(authTokens.length < 2 || !("Basic".equals(authTokens[0]))){
                writeError(res, ErrorMessage.WRONG_AUTH_SCHEME);
            } else {
                try {
                    String credentials = new String(Base64.getDecoder().decode(authTokens[1]));
                    int d = credentials.indexOf(":");
                    if (d == -1) {
                        writeError(res, ErrorMessage.BAD_CREDENTIALS_FORMAT);
                    } else {
                        String email = credentials.substring(0, d).trim();
                        String password = credentials.substring(d + 1).trim();

                        if ( email.equals("") || password.equals("")) {
                            writeError(res, ErrorMessage.EMPTY_INPUT_FIELDS);
                        } else {
                            UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
                            User u = dao.getUserByEmail(email);
                            dao.closeConnection();
                            if(u == null || !User.hashPassword(password, u.getSalt()).equals(u.getPassword())){
                                writeError(res, ErrorMessage.WRONG_CREDENTIALS);
                            } else {
                                //user authenticated
                                String authToken = createJWT(email,"RestManagerServlet","rest_auth_token", TimeUnit.MINUTES.toMillis(TOKEN_EXPIRATION_MINUTES));
                                JSONObject jobj  = new JSONObject();
                                jobj.put("access_token", authToken);
                                jobj.put("token_type", "Bearer");
                                jobj.put("expires_in", TOKEN_EXPIRATION_MINUTES*60);
                                res.setContentType("application/json");
                                res.setStatus(ErrorMessage.OK.getHttpErrorCode());
                                res.getWriter().write(jobj.toString());
                            }
                        }
                    }
                } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                    writeError(res, ErrorMessage.BAD_CREDENTIALS_FORMAT);
                } catch (SQLException | NamingException e) {
                    writeError(res, ErrorMessage.INTERNAL_ERROR);
                }
            }
        }
        return true;
    }


    /**
     * Handles a request to backend/rest/appointment/* , the request must come with a valid authorization token
     * @param req HttpServletRequest received to be processed
     * @param res HttpServletResponse to be prepared for responding to the request
     * @return True if it was a request concerning an appointment that is an URI /backend/rest/appointment/*, false otherwise
     * @throws IOException when an error occurred during the error response writing
     */
    private boolean processAppointment(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String op = req.getRequestURI();
        String[] tokens = op.split("/");
        final int MIN_TOKEN = 4;
        final int APPOINTMENT_TOKEN_INDEX = 3;
        final int ID_TOKEN_INDEX = 4;
        if (tokens.length < MIN_TOKEN || !("appointment".equals(tokens[APPOINTMENT_TOKEN_INDEX]))){
            return false;
        }
        // /backend/rest/appointment/

        String token = getAuthToken(req, res);
        if(token != null) {
            //auth token present
            User u = verifyToken(token, res);
            if(u != null) {
                //valid token
                try{
                    if (tokens.length == MIN_TOKEN){
                        // /backend/rest/appointment/
                        AppointmentRestResource arr = new AppointmentRestResource(req, res);
                        if ("POST".equals(req.getMethod())){
                            if (userHasAnyRoleIn(u, new Role[]{Role.admin, Role.manager, Role.secretary, Role.customer})) {
                                arr.insertAppointment();
                            } else { writeError(res, ErrorMessage.HIGHER_PRIVILEGE_REQUIRED);}
                        } else { writeError(res, ErrorMessage.METHOD_NOT_ALLOWED);}
                    } else if (tokens.length == MIN_TOKEN + 1) {
                        // /backend/rest/appointment/{id}
                        Integer.parseInt(tokens[ID_TOKEN_INDEX]);
                        AppointmentRestResource arr = new AppointmentRestResource(req, res);
                        switch (req.getMethod()) {
                            case "GET" :
                                if (userHasAnyRoleIn(u, new Role[]{Role.admin, Role.manager, Role.secretary, Role.customer})) {
                                    arr.getAppointment();
                                } else {
                                    writeError(res, ErrorMessage.HIGHER_PRIVILEGE_REQUIRED);
                                }
                                break;
                            case "PUT" :
                                if (userHasAnyRoleIn(u, new Role[]{Role.admin, Role.manager, Role.secretary})) {
                                    arr.updateAppointment();
                                } else {
                                    writeError(res, ErrorMessage.HIGHER_PRIVILEGE_REQUIRED);
                                }
                                break;
                            case "DELETE" :
                                if (userHasAnyRoleIn(u, new Role[]{Role.admin, Role.manager, Role.secretary, Role.customer})) {
                                    arr.deleteAppointment();
                                } else {
                                    writeError(res, ErrorMessage.HIGHER_PRIVILEGE_REQUIRED);
                                }
                                break;
                            default : writeError(res, ErrorMessage.METHOD_NOT_ALLOWED);
                        }

                    }  else {
                        return  false;
                    }

                } catch (NumberFormatException e){
                    writeError(res, ErrorMessage.WRONG_REST_FORMAT);
                }
            }
        }
        return true;
    }


    /**
     * search a request for an authorization token with Bearer auth scheme.
     * If it founds a basic auth scheme respond with a redirect to the authorization endpoint,
     * otherwise if the token is not found responds with the correct error message
     * @param req HttpServletRequest received to be processed
     * @param res HttpServletResponse to be prepared for responding to the request
     * @return the token string if found, null otherwise
     * @throws IOException when an error occurred during the error response writing
     */
    private String getAuthToken(HttpServletRequest req,  HttpServletResponse res) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            writeError(res, ErrorMessage.AUTH_HEADER_MISSING);
        } else {
            int bearerIndex = authHeader.indexOf("Bearer");
            if(bearerIndex == -1) {
                if(authHeader.contains("Basic")) {
                    redirectToAuth(res);
                } else {
                    writeError(res, ErrorMessage.WRONG_AUTH_SCHEME);
                }
            } else {
                String token = authHeader.substring(bearerIndex + "Bearer".length()).trim();
                if(token.length() < 2) {
                    writeError(res, ErrorMessage.WRONG_TOKEN_FORMAT);
                } else {
                    return token;
                }
            }
        }
        return null;
    }

    /**
     *  prepares a redirect response to the authorization endpoint
     * @param res HttpServletResponse to be prepared for responding to the request
     * @throws IOException when an error occurred during the error response writing
     */
    private void redirectToAuth(HttpServletResponse res) throws IOException{
        res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        String authURI = String.format("%s/rest/auth", getServletContext().getContextPath());
        res.sendRedirect(authURI);
    }

    /**
     * check if the provided token is valid and in the positive case returns the corresponding user
     * @param token token string on which the validity check has to be performed
     * @param res HttpServletResponse to be prepared for responding to the request
     * @return the user corresponding to the authorization token if the token is valid, null otherwise
     * @throws IOException when an error occurred during the error response writing
     */
    private User verifyToken(String token, HttpServletResponse res) throws IOException {
        try {
            io.jsonwebtoken.Claims jwtToken = decodeJWT(token);
            if(!("RestManagerServlet".equals(jwtToken.getIssuer()))
                    || !("rest_auth_token".equals(jwtToken.getSubject()))) {
                writeError(res, ErrorMessage.WRONG_TOKEN_DOMAIN);
            } else if((new Date()).after(jwtToken.getExpiration())) {
                writeError(res, ErrorMessage.EXPIRED_TOKEN);
            } else {
                //valid token
                UserDAO dao = new UserDAO(DataSourceProvider.getDataSource().getConnection());
                User user = dao.getUserByEmail(jwtToken.getId());
                dao.closeConnection();

                return user;
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e){
            writeError(res, ErrorMessage.EXPIRED_TOKEN);
        } catch (io.jsonwebtoken.JwtException e){
            writeError(res, ErrorMessage.INVALID_TOKEN);
        } catch (SQLException | NamingException e){
            writeError(res, ErrorMessage.INTERNAL_ERROR);
        }
        return null;
    }

    /**
     * checks if the provided user roles is contained in the provided list of user roles
     * @param u user to be checked the role
     * @param roles array of roles
     * @return True if the user role is contained in roles
     */
    private boolean userHasAnyRoleIn(User u, User.Role[] roles) {
        for (Role r: roles) {
            if (r.equals(u.getRole())) {
                return true;
            }
        }
        return false;
    }

    /**
     *  performs preliminary checks on an incoming request. The Accept and Content-Type headers presence is verified
     *  and their values are inspected to comply with rest api specifications. Writes the correct error message in http response.
     * @param req HttpServletRequest received to be processed
     * @param res HttpServletResponse to be prepared for responding to the request
     * @return True if the request complies with rest api specifications and is good to be processed, False otherwise
     * @throws IOException when an error occurred during the error response writing
     */
    private boolean checkMethodMediaType(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
        final String method = req.getMethod();
        final String contentType = req.getHeader("Content-Type");
        final String accept = req.getHeader("Accept");
        final String JSON_MEDIA_TYPE = "application/json";
        final String ALL_MEDIA_TYPE = "*/*";

        if(accept == null) {
            writeError(res, ErrorMessage.ACCEPT_HEADER_MISSING);
            return false;
        }

        if(!accept.contains(JSON_MEDIA_TYPE) && !accept.equals(ALL_MEDIA_TYPE)) {
            writeError(res, ErrorMessage.NOT_ACCEPTABLE);
            return false;
        }

        switch(method) {
            case "GET":
            case "DELETE":
                // nothing to do
                break;

            case "POST":
            case "PUT":
                if(contentType == null) {
                    writeError(res, ErrorMessage.CONTENT_TYPE_HEADER_MISSING);
                    return false;
                }
                if(!contentType.contains(JSON_MEDIA_TYPE)) {
                    writeError(res, ErrorMessage.UNSUPPORTED_MEDIA_TYPE);
                    return false;
                }
                break;
            default:
                writeError(res, ErrorMessage.UNKNOWN_OPERATION);
                return false;
        }
        return true;
    }
}
