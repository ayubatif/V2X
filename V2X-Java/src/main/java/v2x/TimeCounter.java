package v2x;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TimeCounter {
    private long sumTimeToQueryResolve;
    private long sumTimeToSendQuery;
    private long sumTimeToProcessResponse;
    private static final String LOG_FILE_NAME = "v2x-time-log";
    private static final String LOG_FILE_EXTENSION = ".txt";
    private int testNumber;
    private int testAmount;
    private int pseudoRate;
    private int num1Added;
    private int num2Added;
    private int num3Added;
    private long theFirstimeToQueryResolve;
    private long theFirstTimeToSendQuery;
    private long theFirstTimeToProcessResponse;
    private JSONArray log = new JSONArray();

    public TimeCounter(int testnum, int testAmount) {
        this.testNumber = testnum;
        this.testAmount = testAmount;
        this.num1Added = 0;
        this.num2Added = 0;
        this.num3Added = 0;
        this.sumTimeToQueryResolve = 0;
        this.sumTimeToSendQuery = 0;
        this.sumTimeToProcessResponse = 0;
    }

    public TimeCounter(int testnum, int rate, int testAmount) {
        this.testNumber = testnum;
        this.testAmount = testAmount;
        this.pseudoRate = rate;
        this.num1Added = 0;
        this.num2Added = 0;
        this.num3Added = 0;
        this.sumTimeToQueryResolve = 0;
        this.sumTimeToSendQuery = 0;
        this.sumTimeToProcessResponse = 0;
    }

    public void addTimeToQueryResolve(long time) {
        if (num1Added++ == 0) theFirstimeToQueryResolve = time;
        sumTimeToQueryResolve += time;
    }

    public void addTimeToSendQuery(long time) {
        if (num2Added++ == 0) theFirstTimeToSendQuery = time;
        sumTimeToSendQuery += time;
    }

    public void addTimeToProcessResponse(long time) {
        if (num3Added++ == 0) theFirstTimeToProcessResponse = time;
        sumTimeToProcessResponse += time;
    }

    public long getPercentage1() {
        return sumTimeToQueryResolve / testAmount;
    }

    public long getPercentage2() {
        return sumTimeToSendQuery / testAmount;
    }

    public long getPercentage3() {
        return sumTimeToProcessResponse / testAmount;
    }

    public long getBiasedPercentage1() {
        return (sumTimeToQueryResolve - theFirstimeToQueryResolve) / testAmount;
    }

    public long getBiasedPercentage2() {
        return (sumTimeToSendQuery - theFirstTimeToSendQuery) / testAmount;
    }

    public long getBiasedPercentage3() {
        return (sumTimeToProcessResponse - theFirstTimeToProcessResponse) / testAmount;
    }

    /**
     * Appends the JSON log with the current state of times
     */
    public void logAnswers() {
        long average1 = getPercentage1();
        long average2 = getPercentage2();
        long average3 = getPercentage3();
        long biasedAverage1 = getBiasedPercentage1();
        long biasedAverage2 = getBiasedPercentage2();
        long biasedAverage3 = getBiasedPercentage3();
        int totalAnswers = testAmount;
        JSONObject jo = new JSONObject();
        if (this.pseudoRate > 0) {
            jo.put("PSEUDO_RATE", this.pseudoRate);
        }
        jo.put("TOTAL", totalAnswers);
        jo.put("ALL_AVG_TQR", average1);
        jo.put("NOT_AVG_TQR", biasedAverage1);
        jo.put("ALL_AVG_TSQ", average2);
        jo.put("NOT_AVG_TSQ", biasedAverage2);
        jo.put("ALL_AVG_TPR", average3);
        jo.put("NOT_AVG_TPR", biasedAverage3);
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
