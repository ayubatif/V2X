package v2x;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ValidityCounter {
    private int outerMessageAuthenticationFail = 0;
    private int innerMessageAuthenticationFail = 0;
    private int allValid = 0;
    private static final String LOG_FILE_NAME = "v2x-validity-log";
    private static final String LOG_FILE_EXTENSION = ".txt";
    private int testNumber;
    private JSONArray log = new JSONArray();

    /**
     *
     * @param testnum which test is being run
     */
    public ValidityCounter(int testnum) {
        this.testNumber = testnum;
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

    /**
     * Prints the math related answers
     */
    public void printMath() {
        double[] answer = getPercentage();

        System.out.println("Percentage of outer message issue");
        System.out.println(answer[0]);
        System.out.println("Percentage of inner message issue:");
        System.out.println(answer[1]);
        System.out.println("Percentage of no issues:");
        System.out.println(answer[2]);
    }

    /**
     * Prints out the answers that it has been given.
     */
    public void printValidity() {
        System.out.println("Total validations attempted:");
        int totalAnswers = this.outerMessageAuthenticationFail + this.innerMessageAuthenticationFail + this.allValid;
        System.out.println(totalAnswers);
        System.out.println("Outer message issue");
        System.out.println(this.outerMessageAuthenticationFail);
        System.out.println("Inner message issue:");
        System.out.println(this.innerMessageAuthenticationFail);
        System.out.println("No issues:");
        System.out.println(this.allValid);
    }

    /**
     * Replaces and fills the JSON log with the current state of answers
     */
    public void logAnswers() {
        double[] answer = getPercentage();
        int totalAnswers = this.outerMessageAuthenticationFail + this.innerMessageAuthenticationFail + this.allValid;
        JSONObject jo;
        jo = new JSONObject();
        jo.put("TOTAL", totalAnswers);
        for (int i = 0; i < answer.length; i++) {
            jo = new JSONObject();
            jo.put("VALIDITY"+i, answer[i]);
            log.put(jo);
        }
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

        log = new JSONArray(textBuilder.toString());

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
        ValidityCounter validityCounter1 = new ValidityCounter(3);
        ValidityCounter validityCounter2 = new ValidityCounter(3);
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
