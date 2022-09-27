package resource;

public class Company{


    private final String name;
    private final String phone;
    private final double lat;
    private final double lon;
    private final String email;
    private final String adminID;
    private final String address;

    public Company(String name, String address, double lat, double lon, String phone, String email, String adminID) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.phone = phone;
        this.email = email;
        this.adminID = adminID;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getAdminID() {
        return adminID;
    }

    public String getEmail() {
        return email;
    }
}
