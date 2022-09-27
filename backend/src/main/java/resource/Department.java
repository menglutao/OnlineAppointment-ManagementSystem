package resource;

public class Department {

    private final int ID;
    private final String name;
    private final String description;
    private final String companyID;
    private final String manager;


    public Department(int ID, String name, String description, String companyID, String manager) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.companyID = companyID;
        this.manager = manager;
    }

    public Department(String name, String description, String companyID, String manager) {
        this.ID = -1;
        this.name = name;
        this.description = description;
        this.companyID = companyID;
        this.manager = manager;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCompanyID() {
        return companyID;
    }

    public String getManager() {
        return manager;
    }
}
