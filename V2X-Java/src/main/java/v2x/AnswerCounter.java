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

    private int[] getPercentage() {
        int[] answer = new int[3];
        int totalAnswers = this.answerZero + this.answerOne + this.answerTwo;
        answer[0] = (this.answerZero / totalAnswers) * 100;
        answer[1] = (this.answerOne / totalAnswers) * 100;
        answer[2] = (this.answerTwo / totalAnswers) * 100;

        return answer;
    }

    public void printMath() {
        int[] answer = getPercentage();
        for (int i = 0; i < answer.length; i++) {
            System.out.println("Percentage of answer type " + (i + 1) + ":");
            System.out.println(answer[i]);
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
