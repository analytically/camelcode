package controllers;

import com.google.common.base.Strings;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Mathias Bogaert
 */
public class Server extends Controller {
    public static Result metrics(String classPrefix, boolean pretty) throws Exception {
        response().setContentType("application/json");
        response().setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        StringWriter writer = new StringWriter();
        JsonFactory factory = new JsonFactory(new ObjectMapper());
        final JsonGenerator json = factory.createJsonGenerator(writer);
        if (pretty) {
            json.useDefaultPrettyPrinter();
        }
        json.writeStartObject();
        {
            if (("jvm".equals(classPrefix) || Strings.isNullOrEmpty(classPrefix))) {
                writeVmMetrics(json);
            }

            ServerMetrics metricProcessor = new ServerMetrics();
            writeRegularMetrics(json, classPrefix, metricProcessor);
        }
        json.writeEndObject();
        json.close();

        return ok(writer.toString());
    }

    private static void writeVmMetrics(JsonGenerator json) throws IOException {
        VirtualMachineMetrics vm = VirtualMachineMetrics.getInstance();

        json.writeFieldName("jvm");
        json.writeStartObject();
        {

            json.writeFieldName("vm");
            json.writeStartObject();
            {
                json.writeStringField("name", vm.name());
                json.writeStringField("version", vm.version());
            }
            json.writeEndObject();
            json.writeFieldName("memory");
            json.writeStartObject();
            {
                json.writeNumberField("totalInit", vm.totalInit());
                json.writeNumberField("totalUsed", vm.totalUsed());
                json.writeNumberField("totalMax", vm.totalMax());
                json.writeNumberField("totalCommitted", vm.totalCommitted());

                json.writeNumberField("heapInit", vm.heapInit());
                json.writeNumberField("heapUsed", vm.heapUsed());
                json.writeNumberField("heapMax", vm.heapMax());
                json.writeNumberField("heapCommitted", vm.heapCommitted());

                json.writeNumberField("heap_usage", vm.heapUsage());
                json.writeNumberField("non_heap_usage", vm.nonHeapUsage());
                json.writeFieldName("memory_pool_usages");
                json.writeStartObject();
                {
                    for (Map.Entry<String, Double> pool : vm.memoryPoolUsage().entrySet()) {
                        json.writeNumberField(pool.getKey(), pool.getValue());
                    }
                }
                json.writeEndObject();
            }
            json.writeEndObject();

            json.writeNumberField("daemon_thread_count", vm.daemonThreadCount());
            json.writeNumberField("thread_count", vm.threadCount());
            json.writeNumberField("current_time", Clock.defaultClock().time());
            json.writeNumberField("uptime", vm.uptime());
            json.writeNumberField("fd_usage", vm.fileDescriptorUsage());

            json.writeFieldName("thread-states");
            json.writeStartObject();
            {
                for (Map.Entry<Thread.State, Double> entry : vm.threadStatePercentages()
                        .entrySet()) {
                    json.writeNumberField(entry.getKey().toString().toLowerCase(),
                            entry.getValue());
                }
            }
            json.writeEndObject();

            json.writeFieldName("garbage-collectors");
            json.writeStartObject();
            {
                for (Map.Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.garbageCollectors()
                        .entrySet()) {
                    json.writeFieldName(entry.getKey());
                    json.writeStartObject();
                    {
                        final VirtualMachineMetrics.GarbageCollectorStats gc = entry.getValue();
                        json.writeNumberField("runs", gc.getRuns());
                        json.writeNumberField("time", gc.getTime(TimeUnit.MILLISECONDS));
                    }
                    json.writeEndObject();
                }
            }
            json.writeEndObject();
        }
        json.writeEndObject();
    }

    public static void writeRegularMetrics(JsonGenerator json, String classPrefix, MetricProcessor<Context> processor) throws IOException {
        for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : Metrics.defaultRegistry().groupedMetrics().entrySet()) {
            if (classPrefix == null || entry.getKey().startsWith(classPrefix)) {
                json.writeFieldName(entry.getKey());
                json.writeStartObject();
                {
                    for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                        json.writeFieldName(subEntry.getKey().getName());
                        try {
                            subEntry.getValue().processWith(processor, subEntry.getKey(), new Context(json, true));
                        } catch (Exception e) {
                            Logger.warn("Error writing out " + subEntry.getKey(), e);
                        }
                    }
                }
                json.writeEndObject();
            }
        }
    }

    static final class Context {
        final boolean showFullSamples;
        final JsonGenerator json;

        Context(JsonGenerator json, boolean showFullSamples) {
            this.json = json;
            this.showFullSamples = showFullSamples;
        }
    }

    public static class ServerMetrics implements MetricProcessor<Context> {
        @Override
        public void processMeter(MetricName name, Metered meter, Context context) throws Exception {
            final JsonGenerator json = context.json;
            json.writeStartObject();
            {
                json.writeStringField("type", "meter");
                json.writeStringField("event_type", meter.eventType());
                writeMeteredFields(meter, json);
            }
            json.writeEndObject();
        }

        @Override
        public void processCounter(MetricName name, Counter counter, Context context) throws Exception {
            final JsonGenerator json = context.json;
            json.writeStartObject();
            {
                json.writeStringField("type", "counter");
                json.writeNumberField("count", counter.count());
            }
            json.writeEndObject();
        }

        @Override
        public void processHistogram(MetricName name, Histogram histogram, Context context) throws Exception {
            final JsonGenerator json = context.json;
            json.writeStartObject();
            {
                json.writeStringField("type", "histogram");
                json.writeNumberField("count", histogram.count());
                writeSummarizable(histogram, json);
                writeSampling(histogram, json);

                if (context.showFullSamples) {
                    json.writeObjectField("values", histogram.getSnapshot().getValues());
                }
            }
            json.writeEndObject();
        }

        private static Object evaluateGauge(Gauge<?> gauge) {
            try {
                return gauge.value();
            } catch (RuntimeException e) {
                Logger.warn("Error evaluating gauge", e);
                return "error reading gauge: " + e.getMessage();
            }
        }

        @Override
        public void processTimer(MetricName name, Timer timer, Context context) throws Exception {
            final JsonGenerator json = context.json;
            json.writeStartObject();
            {
                json.writeStringField("type", "timer");
                json.writeFieldName("duration");
                json.writeStartObject();
                {
                    json.writeStringField("unit", timer.durationUnit().toString().toLowerCase());
                    writeSummarizable(timer, json);
                    writeSampling(timer, json);
                    if (context.showFullSamples) {
                        json.writeObjectField("values", timer.getSnapshot().getValues());
                    }
                }
                json.writeEndObject();

                json.writeFieldName("rate");
                json.writeStartObject();
                {
                    writeMeteredFields(timer, json);
                }
                json.writeEndObject();
            }
            json.writeEndObject();
        }

        @Override
        public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws Exception {
            final JsonGenerator json = context.json;
            json.writeStartObject();
            {
                json.writeStringField("type", "gauge");
                json.writeObjectField("value", evaluateGauge(gauge));
            }
            json.writeEndObject();
        }

        private static void writeSummarizable(Summarizable metric, JsonGenerator json) throws IOException {
            json.writeNumberField("min", metric.min());
            json.writeNumberField("max", metric.max());
            json.writeNumberField("mean", metric.mean());
            json.writeNumberField("std_dev", metric.stdDev());
        }

        private static void writeSampling(Sampling metric, JsonGenerator json) throws IOException {
            final Snapshot snapshot = metric.getSnapshot();
            json.writeNumberField("median", snapshot.getMedian());
            json.writeNumberField("p75", snapshot.get75thPercentile());
            json.writeNumberField("p95", snapshot.get95thPercentile());
            json.writeNumberField("p98", snapshot.get98thPercentile());
            json.writeNumberField("p99", snapshot.get99thPercentile());
            json.writeNumberField("p999", snapshot.get999thPercentile());
        }

        private static void writeMeteredFields(Metered metered, JsonGenerator json) throws IOException {
            json.writeStringField("unit", metered.rateUnit().toString().toLowerCase());
            json.writeNumberField("count", metered.count());
            json.writeNumberField("mean", metered.meanRate());
            json.writeNumberField("m1", metered.oneMinuteRate());
            json.writeNumberField("m5", metered.fiveMinuteRate());
            json.writeNumberField("m15", metered.fifteenMinuteRate());
        }
    }

    public static Result health() {
        response().setContentType("text/plain");
        response().setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        Map<String, HealthCheck.Result> results = HealthChecks.defaultRegistry().runHealthChecks();

        if (results.isEmpty()) {
            return badRequest("No health checks registered.");
        } else {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);

            for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
                final HealthCheck.Result result = entry.getValue();
                if (result.isHealthy()) {
                    if (result.getMessage() != null) {
                        writer.format("* %s: OK\n  %s\n", entry.getKey(), result.getMessage());
                    } else {
                        writer.format("* %s: OK\n", entry.getKey());
                    }
                } else {
                    if (result.getMessage() != null) {
                        writer.format("! %s: ERROR\n!  %s\n", entry.getKey(), result.getMessage());
                    }

                    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                    final Throwable error = result.getError();
                    if (error != null) {
                        writer.println();
                        error.printStackTrace(writer);
                        writer.println();
                    }
                }
            }

            if (isAllHealthy(results)) {
                return ok(stringWriter.toString());
            } else {
                return internalServerError(stringWriter.toString());
            }
        }
    }

    private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }

    public static Result threaddump() throws Exception {
        response().setContentType("text/plain");
        response().setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VirtualMachineMetrics.getInstance().threadDump(out);

        return ok(out.toString("UTF-8"));
    }
}
