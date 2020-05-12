package v2x;

public class PseudonymAuthority {
    static final String SCRIPT_X_LOCATION = "Bash/create-obu-x-certificate.sh";
    static final String SCRIPT_N_LOCATION = "Bash/create-obu-n-certificate.sh";
    static final int PSEUDONYM_RATE = 5;

    //TODO Can use pre gen pseudonyms if a la carte no work

    public static void genPseudonyms(String certLocation, int n) {
        //TODO gen n certs in location
    }
}
