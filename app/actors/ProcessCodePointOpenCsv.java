package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
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
import org.geotools.measure.AngleFormat;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Mathias Bogaert
 */
public class ProcessCodePointOpenCsv extends UntypedActor {
    private CoordinateOperation coordinateOperation;

    private final Counter postcodesProcessed = Metrics.newCounter(ProcessCodePointOpenCsv.class, "postcodes-processed");
    private final Timer latLongTransform = Metrics.newTimer(ProcessCodePointOpenCsv.class, "latitude-longitude-transform", TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
    private final Timer savePostcodeUnit = Metrics.newTimer(ProcessCodePointOpenCsv.class, "save-postcode-unit-mongodb", TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);

    @Override
    public void preStart() {
        CRSAuthorityFactory crsFac = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);

        try {
            CoordinateReferenceSystem wgs84crs = crsFac.createCoordinateReferenceSystem("4326");
            CoordinateReferenceSystem osgbCrs = crsFac.createCoordinateReferenceSystem("27700");

            coordinateOperation = new DefaultCoordinateOperationFactory().createOperation(osgbCrs, wgs84crs);
        } catch (FactoryException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CodePointOpenCsvEntry) {
            CodePointOpenCsvEntry entry = (CodePointOpenCsvEntry) message;

            PostcodeUnit unit = new PostcodeUnit(CharMatcher.WHITESPACE.removeFrom(entry.getPostcode()));
            unit.pqi = entry.getPositionalQualityIndicator();
            unit.cartesianLocation = new CartesianLocation(Integer.parseInt(entry.getEastings()), Integer.parseInt(entry.getNorthings()));

            final TimerContext latLongCtx = latLongTransform.time();
            try {
                DirectPosition eastNorth = new GeneralDirectPosition(Integer.parseInt(entry.getEastings()), Integer.parseInt(entry.getNorthings()));
                DirectPosition latLng = coordinateOperation.getMathTransform().transform(eastNorth, eastNorth);

                unit.location = new Location(round(latLng.getOrdinate(0), 7), round(latLng.getOrdinate(1), 7));
            } finally {
                latLongCtx.stop();
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
