package v2x;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ValidityCounter {
    private int outerMessageAuthenticationFail = 0;
    private int innerMessageAuthenticationFail = 0;
    private int allValid = 0;
    private static final String LOG_FILE_NAME = "v2x-validity-log";
    private static final String PRINT_LOG_FILE_NAME= "v2x-data-print-log";
    private static final String LOG_FILE_EXTENSION = ".txt";
    private int testNumber;
    private int pseudoRate;
    private JSONArray log = new JSONArray();

    /**
     *
     * @param testnum which test is being run
     */
    public ValidityCounter(int testnum) {
        this.testNumber = testnum;
    }

    /**
     *
     * @param testnum which test
     * @param rate the pseudo change rate
     */
    public ValidityCounter(int testnum, int rate) {
        this.testNumber = testnum;
        this.pseudoRate = rate;
    }

    /**
     * Takes in answer and counts how much is correct and incorrect.
     *
     * @param validity a string of the answer received
     */
    public void addValidity(String validity) {
        int answerInt = Integer.parseInt(validity);

        switch (answerInt) {
            case 0:
                this.outerMessageAuthenticationFail++;
                break;
            case 1:
                this.innerMessageAuthenticationFail++;
                break;
            case 2:
                this.allValid++;
                break;
        }
    }

    /**
     * Calculates the percentages of the answers received
     *
     * @return <code>int[]</code> an array of the percentages
     */
    public double[] getPercentage() {
        double[] answer = new double[3];
        double totalAnswers = this.outerMessageAuthenticationFail + this.innerMessageAuthenticationFail + this.allValid;
        answer[0] = (this.outerMessageAuthenticationFail / totalAnswers) * 100;
        answer[1] = (this.innerMessageAuthenticationFail / totalAnswers) * 100;
        answer[2] = (this.allValid / totalAnswers) * 100;

        return answer;
    }

    public void exportLogOutput() throws IOException {
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append(printValidity());
        stringBuilder.append(printMath());
        String str = stringBuilder.toString();
        BufferedWriter writer = new BufferedWriter(new FileWriter(PRINT_LOG_FILE_NAME+this.testNumber+LOG_FILE_EXTENSION, true));
        writer.append(str);

        writer.close();
    }

    /**
     * Prints the math related answers
     */
    public String printMath() {
        StringBuilder stringBuilder = new StringBuilder();
        double[] answer = getPercentage();
        stringBuilder.append("Percentage of outer message issue").append("\n");
        stringBuilder.append(answer[0]).append("\n");
        stringBuilder.append("Percentage of inner message issue:").append("\n");
        stringBuilder.append(answer[1]).append("\n");
        stringBuilder.append("Percentage of no issues:").append("\n");
        stringBuilder.append(answer[2]).append("\n");
        return stringBuilder.toString();
    }

    /**
     * Prints out the answers that it has been given.
     */
    public String printValidity() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Total validations attempted:").append("\n");
        int totalAnswers = this.outerMessageAuthenticationFail + this.innerMessageAuthenticationFail + this.allValid;
        stringBuilder.append(totalAnswers).append("\n");
        stringBuilder.append("Outer message issue").append("\n");
        stringBuilder.append(this.outerMessageAuthenticationFail).append("\n");
        stringBuilder.append("Inner message issue:").append("\n");
        stringBuilder.append(this.innerMessageAuthenticationFail).append("\n");
        stringBuilder.append("No issues:").append("\n");
        stringBuilder.append(this.allValid).append("\n");
        return stringBuilder.toString();
    }

    /**
     * Replaces and fills the JSON log with the current state of answers
     */
    public void logAnswers() {
        double[] answer = getPercentage();
        int totalAnswers = this.outerMessageAuthenticationFail + this.innerMessageAuthenticationFail + this.allValid;
        JSONObject jo = new JSONObject();
        if (this.pseudoRate > 0) {
            jo.put("PSEUDO_RATE", this.pseudoRate);
        }
        jo.put("TOTAL", totalAnswers);
        for (int i = 0; i < answer.length; i++) {
            jo.put("VALIDITY"+i, answer[i]);
        }
        log.put(jo);
    }

    /**
     *
     * @return JSONArray containing answer percentages
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

    public static void main(String[] args) {
        ValidityCounter validityCounter1 = new ValidityCounter(3, 10);
        ValidityCounter validityCounter2 = new ValidityCounter(3, 10);
        int notSoRandomNumber = DNSBloomFilterFunctions.generateRandomHostname().length() * DNSBloomFilterFunctions.generateRandomHostname().length();
        for(int i = 0; i < notSoRandomNumber; i++) {
            validityCounter1.addValidity(Integer.valueOf(i % 3).toString());
        }
        validityCounter1.printValidity();
        validityCounter1.printMath();
        validityCounter1.logAnswers();
        try {
            System.out.println("Expected: "+validityCounter1.getLog().toString());
            validityCounter1.exportJSONLog();
            validityCounter2.importJSONLog();
            System.out.println("Actual: "+validityCounter2.getLog().toString());
        } catch (IOException e) {
            System.out.println("Check if "+LOG_FILE_NAME+validityCounter1.testNumber+LOG_FILE_EXTENSION+" exists");
            System.err.println(e);
        }
    }
}
