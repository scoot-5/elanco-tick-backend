package com.example.ticktracker;

// Each object represents one sighting from Elanco's API.
public class TickSighting {
    // we encapsulate these variables so that other classes cannot change it.
    private String id;
    private String date;
    private String location;
    private String species;
    private String latinName;

    public TickSighting(String id, String date, String location,
                        String species, String latinName) {
        this.id = id;
        this.date = date;
        this.location = location;
        this.species = species;
        this.latinName = latinName;
    }
    // getter method here that allows other classes to read the variables
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getSpecies() { return species; }
    public String getLatinName() { return latinName; }
}
