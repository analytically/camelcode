package models;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;
import com.google.common.base.Objects;

/**
 * @author Mathias Bogaert
 */
@Embedded
public class CartesianLocation {
    @Property("e")
    public int eastings;

    @Property("n")
    public int northings;

    public CartesianLocation() {
    }

    public CartesianLocation(int eastings, int northings) {
        this.eastings = eastings;
        this.northings = northings;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("eastings", eastings)
                .add("northings", northings)
                .toString();
    }
}
