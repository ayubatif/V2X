package v2x;

public class ValidityCounter {
    private int outerMessageAuthenticationFail = 0;
    private int innerMessageAuthenticationFail = 0;
    private int allValid = 0;

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
}
