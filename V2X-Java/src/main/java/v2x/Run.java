package v2x;

import java.io.IOException;

public class Run {
    public static void main(String args[]) throws InterruptedException, IOException, ClassNotFoundException {
        int mode = Integer.parseInt(args[0]);
        int testAmount = Integer.parseInt(args[1]);
        System.out.println(System.getProperty("user.dir"));
        Querier querier = new Querier();
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                querier.runFirstTest(testAmount);
                break;
//            case 2:
//                System.out.println("running test 2");
//                querier.runSecondTest(testAmount);
//                break;
//            case 3:
//                System.out.println("running test 3");
//                querier.runThirdTest(testAmount, Integer.parseInt(args[2]));
//                break;
//            case 4:
//                System.out.println("running test 4");
//                querier.runFourthTest(testAmount, Integer.parseInt(args[2]));
//                break;
//            case 0:
//                System.out.println("running test 0");
//                test(testAmount);
//                break;
//            case -1:
//                System.out.println("running test -1");
//                crlTest();
//                break;
//            case -2:
//                System.out.println("running test -2");
//                bloomFilterTest();
//                break;
//            case -3:
//                System.out.println(System.getProperty("user.dir"));
        }
    }
}
