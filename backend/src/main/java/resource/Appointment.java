package resource;

import java.sql.Timestamp;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Appointment{
    public enum statuses {
        BOOKED,
        CONFIRMED,
        CHECKED_IN
    }

    private final int ID;
    private final int serviceID;
    private final statuses status;
    private final Timestamp datetime;
    private final String userID;


    public Appointment(int ID, int serviceID, statuses status, Timestamp datetime, String userID) {
        this.ID = ID;
        this.serviceID = serviceID;
        this.status = status;
        this.datetime = datetime;
        this.userID = userID;
    }

    public Appointment(int serviceID, statuses status, Timestamp datetime, String userID) {
        this.ID = -1;
        this.serviceID = serviceID;
        this.status = status;
        this.datetime = datetime;
        this.userID = userID;
    }

    public int getID() {
        return ID;
    }

    public int getServiceID() {
        return serviceID;
    }

    public resource.Appointment.statuses getStatus() {
        return status;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public String getUserID() {
        return userID;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "ID=" + ID +
                ", serviceID=" + serviceID +
                ", status=" + status +
                ", datetime=" + datetime +
                ", userID='" + userID + '\'' +
                '}';
    }


    public JSONObject toJSON(){
        JSONObject jobj  = new JSONObject();
        jobj.put("ID", ID);
        jobj.put("status", status.toString());
        jobj.put("serviceID", serviceID);
        jobj.put("datetime", datetime.toString());
        jobj.put("userID", userID);
        return jobj;
    }

    public final JSONObject toJSON(Iterable<Appointment> list){
        JSONObject jobj  = new JSONObject();
        JSONArray apps = new JSONArray();
        for (Appointment a : list )
            apps.put(a.toJSON());
        jobj.put("appointments list", apps);
        return jobj;
    }

    public static Appointment fromJSON(InputStream inputStream) throws IOException, JSONException, IllegalArgumentException {
        String dataString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        JSONObject jobj = new JSONObject(dataString);

        int serviceID = jobj.getInt("serviceID");
        statuses status = statuses.valueOf(jobj.getString("status"));
        Timestamp datetime = Timestamp.valueOf(jobj.getString("datetime"));
        String userID = jobj.getString("userID");

        if(jobj.has("ID"))
            return new Appointment(jobj.getInt("ID"), serviceID, status, datetime, userID);
        else
            return new Appointment(serviceID, status, datetime, userID);
    }
}