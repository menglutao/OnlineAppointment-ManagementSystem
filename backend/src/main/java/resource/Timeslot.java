package resource;

import java.sql.Timestamp;
import java.util.Date;

public class Timeslot {

    private final int serviceID;
    private final Timestamp datetime;
    private final long datetimeLong;
    private final int places;

    public Timeslot(int serviceID, Timestamp datetime, int places) {
        this.serviceID = serviceID;
        this.datetime = datetime;
        datetimeLong = datetime.getTime();
        this.places = places;
    }

    public Timeslot(int serviceID, Timestamp datetime) {
        this.serviceID = serviceID;
        this.datetime = datetime;
        datetimeLong = datetime.getTime();
        this.places = -1;
    }

    public int getServiceID() {
        return serviceID;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public long getDatetimeLong() { return datetimeLong; }

    public int getPlaces() {
        return places;
    }

    @Override
    public String toString() {
        return "Timeslot{" +
                "serviceID=" + serviceID +
                ", datetime=" + datetime +
                ", datetimeLong=" + datetimeLong +
                ", places=" + places +
                '}';
    }
}