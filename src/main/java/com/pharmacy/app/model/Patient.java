package com.pharmacy.app.model;

public class Patient {
    private int id;
    private String name;
    private String phone;
    private String dob;
    private String allergies;
    private String notes;

    public Patient() {}

    public Patient(int id, String name, String phone, String dob, String allergies, String notes) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.dob = dob;
        this.allergies = allergies;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return name + (phone != null && !phone.isBlank() ? " (" + phone + ")" : "");
    }
}