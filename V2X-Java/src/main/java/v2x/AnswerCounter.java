package v2x;

public class AnswerCounter {
    private int answerZero = 0;
    private int answerOne = 0;
    private int answerTwo = 0;

    /**
     * Takes in answer and counts how much is correct and incorrect.
     *
     * @param answer a string of the answer received
     */
    public void addAnswer(String answer) {
        int answerInt = Integer.parseInt(answer);

        switch (answerInt) {
            case 0:
                this.answerZero++;
                break;
            case 1:
                this.answerOne++;
                break;
            case 2:
                this.answerTwo++;
                break;
            default:
                System.out.println("This is not a number");
        }
    }

    /**
     * Prints out the answers that it has been given.
     */
    public void printAnswer() {
        System.out.println("Total answers received:");
        int totalAnswers = this.answerZero + this.answerOne + this.answerTwo;
        System.out.println(totalAnswers);
        System.out.println("Answer type zero amount:");
        System.out.println(this.answerZero);
        System.out.println("Answer type one amount:");
        System.out.println(this.answerOne);
        System.out.println("Answer type two amount:");
        System.out.println(this.answerTwo);
    }
}
