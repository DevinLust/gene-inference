package com.progressengine.geneinference.dto;

public class VisualNodeDTO {
    private String id;
    private String type;   // sheep, relationship
    private String label;
    private double x;
    private double y;
    private boolean center;

    public VisualNodeDTO() {}

    public VisualNodeDTO(String id, String type, String label, double x, double y, boolean center) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.x = x;
        this.y = y;
        this.center = center;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public boolean isCenter() { return center; }
    public void setCenter(boolean center) { this.center = center; }
}
