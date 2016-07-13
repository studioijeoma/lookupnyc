package com.ekeneijeoma.lookup;

import android.location.Location;

class Intersection {
    String name = "";

    int injuries = 0;
    int fatalities = 0;

    float points = 0;
    int score = 0;

    float distance = 0;

    Location location = new Location("test");

    boolean equals(Intersection o) {
        return name.equals(o.name);
    }
}
