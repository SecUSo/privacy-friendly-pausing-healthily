package org.secuso.privacyfriendlybreakreminder.database.data;


public class Exercise {
    private int id;
    private int localId;
    private String section;
    private String execution;
    private String description;
    private String name;
    private String imageID;
    private String language;

    public Exercise() {
        this.localId = -1;
        this.id = -1;
        this.imageID = "-1";
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getLocalId() { return localId; }
    public void setLocalId(int exercise_id) { this.localId = exercise_id; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getName() {  return name; }
    public void setName(String name) { this.name = name; }

    public String getExecution() {
        return execution;
    }
    public void setExecution(String execution) {
        this.execution = execution;
    }

    public String getImageID() {
        return imageID;
    }
    public void setImageID(String imageID) {
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
