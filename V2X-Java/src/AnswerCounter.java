public class AnswerCounter {
    private int correctAnswer = 0;
    private int incorrectAnswer = 0;

    public void addAnswer(String answer) {
        int answerInt = Integer.parseInt(answer);

        switch (answerInt) {
            case 0:
                this.correctAnswer++;
                break;
            case 1:
                this.incorrectAnswer++;
                break;
            default:
                System.out.println("This is not a number");
        }
    }

    public void printAnswer() {
        System.out.println("Correct answer amount:");
        System.out.println(this.correctAnswer);
        System.out.println("Incorrect answer amount:");
        System.out.println(this.incorrectAnswer);
    }
}
