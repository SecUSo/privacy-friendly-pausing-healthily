package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;


public class Exercise {
    private int id, imageID;
    private String section, execution, description;

    public Exercise(int id, String description, String section, int imageID, String execution) {
        this.id = id;
        this.imageID = imageID;
        this.section = section;
        this.execution = execution;
        this.description = description;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
