package com.pharmacy.app.model;

public class Doctor {
    private int id;
    private String name;
    private String specialization;

    public Doctor() {}

    public Doctor(int id, String name, String specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    /** How this appears in dropdowns, e.g. "Dr. Sara (Orthopedic)" */
    @Override
    public String toString() {
        String display = name.toLowerCase().startsWith("dr.") || name.toLowerCase().startsWith("dr ") ? name : "Dr. " + name;
        return (specialization != null && !specialization.isBlank()) ? display + " (" + specialization + ")" : display;
    }
}