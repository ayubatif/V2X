import com.google.common.hash.Funnels;
import com.google.common.hash.BloomFilter;
import java.nio.charset.Charset;

public class DNSBloomFilter {
    private static final String exampleHostname = "artoria.saber.fgo";
    private static final String exampleIPv66Addr = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    private static final String exampleAAAA = exampleHostname+"="+exampleIPv66Addr;
    private final BloomFilter<String> signedIPs;
    public static final int NUM_AAAA_RECORDS= 1000;
    public static final double MAX_FALSE_POSITIVE_RATE = 0.01;

    public DNSBloomFilter(int size) {
        signedIPs = BloomFilter.create(
                Funnels.stringFunnel(Charset.forName("UTF-8")), size);
    }


    public DNSBloomFilter(int size, double maxFalsePositiveRate) {
        signedIPs = BloomFilter.create(
                Funnels.stringFunnel(Charset.forName("UTF-8")), size, maxFalsePositiveRate);
    }

    public void add(String aaaa) {
        signedIPs.put(aaaa);
    }

    public boolean probablyContains(String aaaa) {
        return signedIPs.mightContain(aaaa);
    }


    public static void main(String[] args) {
        DNSBloomFilter dnsBloomFilter = new DNSBloomFilter(NUM_AAAA_RECORDS);

        dnsBloomFilter.add(exampleAAAA);

        assert !dnsBloomFilter.probablyContains(exampleHostname);
        assert !dnsBloomFilter.probablyContains(exampleIPv66Addr);
        assert dnsBloomFilter.probablyContains(exampleAAAA);

        System.out.println("Expected: "+false+" Actual: "+dnsBloomFilter.probablyContains(exampleHostname));
        System.out.println("Expected: "+false+" Actual: "+dnsBloomFilter.probablyContains(exampleIPv66Addr));
        System.out.println("Expected: "+true+"  Actual: "+dnsBloomFilter.probablyContains(exampleAAAA));
    }

}