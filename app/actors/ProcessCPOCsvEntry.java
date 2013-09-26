package actors;

import akka.actor.UntypedActor;
import com.google.common.base.CharMatcher;
import com.google.common.base.Throwables;
import com.mongodb.MongoException;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import models.CartesianLocation;
import models.Location;
import models.PostcodeUnit;
import models.csv.CodePointOpenCsvEntry;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.concurrent.TimeUnit;

/**
 * @author Mathias Bogaert
 */
public class ProcessCPOCsvEntry extends UntypedActor {
    private MathTransform osgbToWgs84Transform;

    private final Counter postcodesProcessed = Metrics.newCounter(ProcessCPOCsvEntry.class, "postcodes-processed");
    private final Timer latLongTransform = Metrics.newTimer(ProcessCPOCsvEntry.class, "latitude-longitude-transform", TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
    private final Timer savePostcodeUnit = Metrics.newTimer(ProcessCPOCsvEntry.class, "save-postcode-unit-mongodb", TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);

    @Override
    public void preStart() {
        try {
            CoordinateReferenceSystem osgbCrs = CRS.decode("EPSG:27700"); // OSGB 1936 / British National Grid
            CoordinateReferenceSystem wgs84crs = DefaultGeographicCRS.WGS84; // WGS 84, GPS

            osgbToWgs84Transform = CRS.findMathTransform(osgbCrs, wgs84crs);
        } catch (FactoryException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof CodePointOpenCsvEntry) {
            CodePointOpenCsvEntry entry = (CodePointOpenCsvEntry) message;

            PostcodeUnit unit = new PostcodeUnit(CharMatcher.WHITESPACE.removeFrom(entry.getPostcode()));
            unit.pqi = entry.getPositionalQualityIndicator();

            try {
                int eastings = Integer.parseInt(entry.getEastings());
                int northings = Integer.parseInt(entry.getNorthings());

                unit.cartesianLocation = new CartesianLocation(eastings, northings);

                final TimerContext latLongCtx = latLongTransform.time();
                try {
                    DirectPosition eastNorth = new GeneralDirectPosition(eastings, northings);
                    DirectPosition latLng = osgbToWgs84Transform.transform(eastNorth, eastNorth);

                    unit.location = new Location(round(latLng.getOrdinate(1), 8), round(latLng.getOrdinate(0), 8));
                } finally {
                    latLongCtx.stop();
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("NumberFormatException parsing easting/northings '" + entry.getEastings() + ", " + entry.getNorthings() + "'.");
            } catch (TransformException e) {
                throw Throwables.propagate(e);
            }

            final TimerContext saveCtx = savePostcodeUnit.time();
            try {
                unit.save();

                postcodesProcessed.inc();
            } catch (MongoException.DuplicateKey e) {
                // ignore
            } finally {
                saveCtx.stop();
            }
        }
    }

    public static double round(double valueToRound, int numberOfDecimalPlaces) {
        double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
        double interestedInZeroDPs = valueToRound * multipicationFactor;
        return Math.round(interestedInZeroDPs) / multipicationFactor;
    }
}
