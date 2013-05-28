package models;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Property;
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
