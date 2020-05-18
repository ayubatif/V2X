package v2x;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TimeCounter {
    private long[] times;
    private long sumTimes;
    private static final String LOG_FILE_NAME = "v2x-time-log";
    private static final String LOG_FILE_EXTENSION = ".txt";
    private int testNumber;
    private int pseudoRate;
    private int numAdded;
    private JSONArray log = new JSONArray();

    public TimeCounter(int testnum, int testAmount) {
        this.times = new long[testAmount];
        this.testNumber = testnum;
        this.numAdded = 0;
    }

    public TimeCounter(int testnum, int rate, int testAmount) {
        this.times = new long[testAmount];
        this.testNumber = testnum;
        this.pseudoRate = rate;
        this.numAdded = 0;
    }

    public void addTime(long time) {
        this.times[numAdded] = time;
        if (numAdded < times.length - 1) this.numAdded++;
    }

    public long getPercentage() {
        return sumTimes / times.length;
    }

    public long getBiasedPercentage() {
        return (sumTimes - times[0]) / times.length;
    }

    /**
     * Appends the JSON log with the current state of times
     */
    public void logAnswers() {
        long average = getPercentage();
        long biasedAverage = getBiasedPercentage();
        int totalAnswers = times.length;
        JSONObject jo = new JSONObject();
        if (this.pseudoRate > 0) {
            jo.put("PSEUDO_RATE", this.pseudoRate);
        }
        jo.put("TOTAL", totalAnswers);
        jo.put("ALL_AVG_TQR", average);
        jo.put("NOT_AVG_TQR", biasedAverage);
        log.put(jo);
    }

    /**
     *
     * @return JSONArray containing avg time
     */
    private JSONArray getLog() {
        return this.log;
    }

    /**
     * imports the JSON log, but applicability is scarce in our scenario. Able to store different runs of test I suppose
     * @throws IOException
     */
    public void importJSONLog() throws IOException {
        File jsonFile = new File(LOG_FILE_NAME+this.testNumber+LOG_FILE_EXTENSION);
        InputStream in = new FileInputStream(jsonFile);

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (in, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }

        try {
            log = new JSONArray(textBuilder.toString());
        } catch (JSONException e) {
            System.err.println("Empty file isn't quite an empty JSON file");
        }

        in.close();
    }

    /**
     * Writes the log in JSON to a test specific file
     * @throws IOException
     */
    public void exportJSONLog() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_NAME+this.testNumber+LOG_FILE_EXTENSION));
        writer.write(log.toString());

        writer.close();
    }
}
