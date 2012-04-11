package models.csv;

import com.google.common.base.Objects;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

/**
 * @author Mathias Bogaert
 */
@CsvRecord(separator = ",")
public class CodePointOpenCsvEntry {
    @DataField(pos = 1)
    private String postcode;

    @DataField(pos = 2)
    private String positionalQualityIndicator;

    @DataField(pos = 3)
    private String eastings;

    @DataField(pos = 4)
    private String northings;

    @DataField(pos = 5)
    private String countryCode;

    @DataField(pos = 6)
    private String nhsRegionalHa;

    @DataField(pos = 7)
    private String nhsHa;

    @DataField(pos = 8)
    private String adminCountryCode;

    @DataField(pos = 9)
    private String adminDistrictCode;

    @DataField(pos = 10)
    private String adminWardCode;

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPositionalQualityIndicator() {
        return positionalQualityIndicator;
    }

    public void setPositionalQualityIndicator(String positionalQualityIndicator) {
        this.positionalQualityIndicator = positionalQualityIndicator;
    }

    public String getEastings() {
        return eastings;
    }

    public void setEastings(String eastings) {
        this.eastings = eastings;
    }

    public String getNorthings() {
        return northings;
    }

    public void setNorthings(String northings) {
        this.northings = northings;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getNhsRegionalHa() {
        return nhsRegionalHa;
    }

    public void setNhsRegionalHa(String nhsRegionalHa) {
        this.nhsRegionalHa = nhsRegionalHa;
    }

    public String getNhsHa() {
        return nhsHa;
    }

    public void setNhsHa(String nhsHa) {
        this.nhsHa = nhsHa;
    }

    public String getAdminCountryCode() {
        return adminCountryCode;
    }

    public void setAdminCountryCode(String adminCountryCode) {
        this.adminCountryCode = adminCountryCode;
    }

    public String getAdminDistrictCode() {
        return adminDistrictCode;
    }

    public void setAdminDistrictCode(String adminDistrictCode) {
        this.adminDistrictCode = adminDistrictCode;
    }

    public String getAdminWardCode() {
        return adminWardCode;
    }

    public void setAdminWardCode(String adminWardCode) {
        this.adminWardCode = adminWardCode;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("postcode", postcode)
                .add("positionalQualityIndicator", positionalQualityIndicator)
                .add("eastings", eastings)
                .add("northings", northings)
                .add("countryCode", countryCode)
                .add("nhsRegionalHa", nhsRegionalHa)
                .add("nhsHa", nhsHa)
                .add("adminCountryCode", adminCountryCode)
                .add("adminDistrictCode", adminDistrictCode)
                .add("adminWardCode", adminWardCode)
                .toString();
    }
}
