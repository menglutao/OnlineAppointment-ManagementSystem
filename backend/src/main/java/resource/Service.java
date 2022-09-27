package resource;

public class Service {

    private final int ID;
    private final String name;
    private final String description;
    private final long duration;
    private final int departmentID;

    public Service(int ID, String name, String description, long duration, int departmentID) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.departmentID = departmentID;
    }

    public Service(String name, String description, long duration, int departmentID) {
        this.ID = -1;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.departmentID = departmentID;
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

    public long getDuration() {
        return duration;
    }

    public int getDepartmentID() {
        return departmentID;
    }

    public static String durationToString(long duration){
        return String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
    }

    public  static long stringToDuration(String durationAsInterval){
        String[] splittedDuration = durationAsInterval.split(":");
        return 3600 * Long.parseLong(splittedDuration[0])
                + 60 * Long.parseLong(splittedDuration[1])
                + Long.parseLong(splittedDuration[2]);
    }
}
