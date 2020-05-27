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
    private long[] rawTQRDataArray = new long[2000];
    private long[] rawTSQDataArray = new long[2000];
    private long[] rawTPRDataArray = new long[2000];
    private int counterRawTQRDataArray = 0;
    private int counterRawTSQDataArray = 0;
    private int counterRawTPRDataArray = 0;


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

    public void addTimeToRawTQRData(long time) {
        this.rawTQRDataArray[this.counterRawTQRDataArray] = time;
        this.counterRawTQRDataArray++;
    }

    public void addTimeToRawTSQData(long time) {
        this.rawTSQDataArray[this.counterRawTSQDataArray] = time;
        this.counterRawTSQDataArray++;
    }

    public void addTimeToRawTPRData(long time) {
        this.rawTPRDataArray[this.counterRawTPRDataArray] = time;
        this.counterRawTPRDataArray++;
    }

    public long getPercentage1() {
        return sumTimeToQueryResolve / testAmount;
    }

    public double getPercentage2() {
        return (double) sumTimeToSendQuery / (double) testAmount;
    }

    public double getPercentage3() {
        return (double) sumTimeToProcessResponse / (double) testAmount;
    }

    public double getBiasedPercentage1() {
        return (double) (sumTimeToQueryResolve - theFirstimeToQueryResolve) / (double) testAmount;
    }

    public double getBiasedPercentage2() {
        return (double) (sumTimeToSendQuery - theFirstTimeToSendQuery) / (double) testAmount;
    }

    public double getBiasedPercentage3() {
        return (double) (sumTimeToProcessResponse - theFirstTimeToProcessResponse) / (double) testAmount;
    }

    /**
     * Appends the JSON log with the current state of times
     */
    public void logAnswers() {
        double average1 = getPercentage1();
        double average2 = getPercentage2();
        double average3 = getPercentage3();
        double biasedAverage1 = getBiasedPercentage1();
        double biasedAverage2 = getBiasedPercentage2();
        double biasedAverage3 = getBiasedPercentage3();
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

    public void logAnswersRawData() {
        int totalAnswers = testAmount;
        JSONObject jo = new JSONObject();
        if (this.pseudoRate > 0) {
            jo.put("PSEUDO_RATE", this.pseudoRate);
        }

//        JSONArray jsonArrayRawTQRDataArray  = new JSONArray(this.rawTQRDataArray);
//        JSONArray jsonArrayRawTSQDataArray = new JSONArray(this.rawTSQDataArray);
//        JSONArray jsonArrayRawTPRDataArray = new JSONArray(this.rawTPRDataArray);
//
//        jo.put("RAW_TQR_DATA", jsonArrayRawTQRDataArray);
//        jo.put("RAW_TSQ_DATA", jsonArrayRawTSQDataArray);
//        jo.put("RAW_TPR_DATA", jsonArrayRawTPRDataArray);

        double tqrSampleStandardDeviation = getSampleStandardDeviation(counterRawTQRDataArray + 1,
                rawTQRDataArray);
        double tsqSampleStandardDeviation = getSampleStandardDeviation(counterRawTSQDataArray + 1,
                rawTSQDataArray);
        double tprSampleStandardDeviation = getSampleStandardDeviation(counterRawTPRDataArray + 1,
                rawTPRDataArray);

        jo.put("TQR_SAMPLE_STANDARD_DEVIATION", tqrSampleStandardDeviation);
        jo.put("TSQ_SAMPLE_STANDARD_DEVIATION", tsqSampleStandardDeviation);
        jo.put("TPR_SAMPLE_STANDARD_DEVIATION", tprSampleStandardDeviation);

        log.put(jo);
    }

    public double getSampleStandardDeviation(int total, long[] inputArray) {
        double leftSide = 1 / ((double) total - 1);
        double totalRightSide = 0;
        long sum = 0;
        for (int i = 0; i < total - 1; i++) {
            sum += inputArray[i];
        }
        double average = (double) sum / (double) total;
        for (long number : inputArray) {
            totalRightSide +=  Math.pow(((double) number - average), 2);
        }
        double sampleStandardDeviation = Math.sqrt(leftSide * totalRightSide);

        return sampleStandardDeviation;
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

    public void importJSONLogRawData() throws IOException {
        File jsonFile = new File("v2x-time-log-raw"+this.testNumber+LOG_FILE_EXTENSION);
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

    public void exportJSONLogRawData() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("v2x-time-log-raw"+this.testNumber+LOG_FILE_EXTENSION));
        writer.write(log.toString());

        writer.close();
    }
}
