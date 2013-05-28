package models;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Property;
import com.google.code.morphia.utils.IndexDirection;
import com.google.common.base.Objects;

/**
 * @author Mathias Bogaert
 */
@Entity(value = "pcu", noClassnameStored = true, concern = "NORMAL")
public class PostcodeUnit extends Model {
    @Property("p")
    @Indexed(unique = true)
    public String postcode;

    @Property("q")
    public String pqi; // quality indicator, 10 = best, 90 = least

    @Embedded("c")
    public CartesianLocation cartesianLocation;

    @Embedded("l")
    @Indexed(IndexDirection.GEO2D)
    public Location location;

    // FINDERS ----------

    public static final Finder<PostcodeUnit> find = new Finder<PostcodeUnit>(PostcodeUnit.class);

    public PostcodeUnit() {
    }

    public PostcodeUnit(String postcode) {
        this.postcode = postcode;
    }

    public PostcodeUnit(String postcode, String pqi, Location location) {
        this.postcode = postcode;
        this.pqi = pqi;
        this.location = location;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("postcode", postcode)
                .add("pqi", pqi)
                .add("cartesianLocation", cartesianLocation)
                .add("location", location)
                .toString();
    }
}
