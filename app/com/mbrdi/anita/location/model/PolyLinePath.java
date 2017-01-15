package com.mbrdi.anita.location.model;

import java.util.List;

public class PolyLinePath {
    public List<GPLocation> path_locations;
    public String encoded_path;

    public Location centroid;
    public Location office_location;

    public PolyLinePath() {}

    public PolyLinePath(List<GPLocation> path_locations) {
        this.path_locations = path_locations;
    }
}
