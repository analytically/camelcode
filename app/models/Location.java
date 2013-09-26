package models;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;
import com.google.common.base.Objects;

/**
 * @author Mathias Bogaert
 */
@Embedded
public class Location {
    @Property("lat")
    public double latitude;

    @Property("lng")
    public double longitude;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .toString();
    }
}

