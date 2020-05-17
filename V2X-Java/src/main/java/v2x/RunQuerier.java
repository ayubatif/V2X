package v2x;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class RunQuerier {
    public static void main(String args[]) {
        int mode = Integer.parseInt(args[0]);
        int testAmount = Integer.parseInt(args[1]);
        System.out.println(System.getProperty("user.dir"));
        Querier querier = new Querier();
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                try {
                    querier.runFirstTest(testAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                System.out.println("running test 2");
                try {
                    querier.runSecondTest(testAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                System.out.println("running test 3");
                try {
                    querier.runThirdTest(testAmount, Integer.parseInt(args[2]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
//            case 4:
//                System.out.println("running test 4");
//                querier.runFourthTest(testAmount, Integer.parseInt(args[2]));
//                break;
//            case 0:
//                System.out.println("running test 0");
//                querier.test(testAmount);
//                break;
//            case -1:
//                System.out.println("running test -1");
//                querier.crlTest();
//                break;
//            case -2:
//                System.out.println("running test -2");
//                querier.bloomFilterTest();
//                break;
            case -3:
                System.out.println(System.getProperty("user.dir"));
        }
    }
}
