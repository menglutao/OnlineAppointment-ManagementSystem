package utils;

import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

public enum ErrorMessage {

    OK("OK",200, HttpServletResponse.SC_OK),
    UNKNOWN_OPERATION("Unknown operation",-100, HttpServletResponse.SC_BAD_REQUEST),
    EMPTY_INPUT_FIELDS("One or more input fields are empty.",-101, HttpServletResponse.SC_BAD_REQUEST),
    DIFFERENT_PASSWORDS("Different password.",-102, HttpServletResponse.SC_BAD_REQUEST),
    MAIL_IN_USE("Mail already in use.",-103, HttpServletResponse.SC_BAD_REQUEST),
    COMPANY_NAME_IN_USE("Company name already in use.",-105, HttpServletResponse.SC_BAD_REQUEST),
    WRONG_CREDENTIALS("Wrong credentials",-104, HttpServletResponse.SC_BAD_REQUEST),
    OVERLAPPING_APPOINTMENT("Overlapping appointment",-107, HttpServletResponse.SC_BAD_REQUEST),
    INVALID_INPUT_FIELDS("Invalid input fields",-106, HttpServletResponse.SC_BAD_REQUEST),
    WRONG_REST_FORMAT("Wrong rest request format",-113, HttpServletResponse.SC_BAD_REQUEST),
    APPOINTMENT_NOT_FOUND("appointment not found",-115, HttpServletResponse.SC_NOT_FOUND),
    SERVICE_NOT_FOUND("service not found",-116, HttpServletResponse.SC_NOT_FOUND),
    TIMESLOT_NOT_FOUND("timeslot not found",-117, HttpServletResponse.SC_NOT_FOUND),
    DEPARTMENT_NOT_FOUND("department not found",-118, HttpServletResponse.SC_NOT_FOUND),
    USER_NOT_FOUND("The user specified does not exists",-119, HttpServletResponse.SC_CONFLICT),
    BADLY_FORMATTED_JSON("The input json is in the wrong format",-120, HttpServletResponse.SC_BAD_REQUEST),
    INTERNAL_ERROR("Internal server error",-201, HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    WRONG_AUTH_SCHEME("The auth scheme used is not the expected one",-211, HttpServletResponse.SC_BAD_REQUEST),
    BAD_CREDENTIALS_FORMAT("The credentials are badly formatted",-212, HttpServletResponse.SC_BAD_REQUEST),
    UNSUPPORTED_MEDIA_TYPE("The request entity has an unsupported media type",-213, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE),
    NOT_ACCEPTABLE("The server can not generate content acceptable by client",-214, HttpServletResponse.SC_NOT_ACCEPTABLE),
    AUTH_HEADER_MISSING("The Authorization header was not specified",-215, HttpServletResponse.SC_BAD_REQUEST),
    ACCEPT_HEADER_MISSING("The accept header was not specified",-216, HttpServletResponse.SC_BAD_REQUEST),
    CONTENT_TYPE_HEADER_MISSING("The content-type header was not specified",-217, HttpServletResponse.SC_BAD_REQUEST),
    WRONG_TOKEN_DOMAIN("The token specified is in the wrong domain",-220, HttpServletResponse.SC_BAD_REQUEST),
    EXPIRED_TOKEN("The token specified has expired",-221, HttpServletResponse.SC_BAD_REQUEST),
    INVALID_TOKEN("The token specified is not valid",-222, HttpServletResponse.SC_BAD_REQUEST),
    WRONG_TOKEN_FORMAT("The token is specified in a bad format",-223, HttpServletResponse.SC_BAD_REQUEST),
    HIGHER_PRIVILEGE_REQUIRED("higher permission required",-230, HttpServletResponse.SC_FORBIDDEN),
    APPOINTMENT_ID_MISMATCH("appointment requested and appointment provided do not match",-240, HttpServletResponse.SC_CONFLICT),
    TIMESLOT_FULL("The selected timeslot has reached the maximum number of appointments",-250, HttpServletResponse.SC_CONFLICT),
    APPOINTMENT_ALREADY_BOOKED("The user has an appointment already booked in the same timeslot",-260, HttpServletResponse.SC_CONFLICT),
    METHOD_NOT_ALLOWED("The method is not allowed", -500, HttpServletResponse.SC_METHOD_NOT_ALLOWED);


    private final String message;
    private final int code;
    private final int httpErrorCode;

    ErrorMessage(String message, int errorCode, int httpErrorCode) {
        this.message = message;
        this.code = errorCode;
        this.httpErrorCode = httpErrorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();
        data.put("code", code);
        data.put("message", message);
        JSONObject info = new JSONObject();
        info.put("error", data);
        return info;
    }
}
