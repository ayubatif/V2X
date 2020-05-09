import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;

import java.io.*;
import java.nio.charset.Charset;

import static com.google.common.hash.BloomFilter.readFrom;

public class DNSBloomFilter {
    public static final String exampleHostname = "artoria.saber.fgo";
    public static final String exampleIPv66Addr = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    public static final String exampleAAAA = exampleHostname+"="+exampleIPv66Addr;
    public static final int NUM_AAAA_RECORDS= 1000;
    public static final double MAX_FALSE_POSITIVE_RATE = 0.01;
    private final BloomFilter<String> signedIPs;
    private final Funnel<CharSequence> stringFunnel = Funnels.stringFunnel(Charset.forName("UTF-8"));
    private static final String BLOOM_FILTER_LOCATION = "Authentication/DNS-bloom-filter.bf";

    /**
     * Create a BF
     * @param size maximum num of elements
     */
    public DNSBloomFilter(int size) {
        signedIPs = BloomFilter.create(stringFunnel, size);
    }

    /**
     * Create a BF
     * @param size maximum num of elements
     * @param maxFalsePositiveRate maximum false positive rate due to hash collisions
     */
    public DNSBloomFilter(int size, double maxFalsePositiveRate) {
        signedIPs = BloomFilter.create(stringFunnel, size, maxFalsePositiveRate);
    }

    /**
     * Import a bloom filter from a file and return it
     * @param location a string of the location to import the BF
     * @throws IOException
     */
    public DNSBloomFilter(String location) throws IOException {
        File bfFile = new File(location);
        InputStream in = new FileInputStream(bfFile);
        signedIPs = readFrom(in, stringFunnel);
    }

    /**
     * Add an entry to the BF
     * @param aaaa signed record to be added
     */
    public void add(String aaaa) {
        signedIPs.put(aaaa);
    }

    /**
     * return whether the record was signed
     * @param aaaa record to be checked for signature
     * @return <code>true</code> if the AAAA record is signed by the DNS authority OR it is a false positive
     * <code>false</code> if the AAAA record is not signed by the DNS authority
     */
    public boolean probablyContains(String aaaa) {
        return signedIPs.mightContain(aaaa);
    }

    /**
     * export the current bloom filter to a file after emptying it
     * @param location a string of the location to export the BF
     * @throws IOException
     */
    public void exportBloomFilter(String location) throws IOException {
        File bfFile = new File(location);
        OutputStream out = new FileOutputStream(bfFile);
        signedIPs.writeTo(out);
    }

    public static void main(String[] args) {
        DNSBloomFilter dnsBloomFilter = new DNSBloomFilter(NUM_AAAA_RECORDS);

        dnsBloomFilter.add(exampleAAAA);

        System.out.println("Expected: "+false+" Actual: "+dnsBloomFilter.probablyContains(exampleHostname));
        System.out.println("Expected: "+false+" Actual: "+dnsBloomFilter.probablyContains(exampleIPv66Addr));
        System.out.println("Expected: "+true+"  Actual: "+dnsBloomFilter.probablyContains(exampleAAAA));

        try {
            DNSBloomFilter dnsBloomFilter2 = new DNSBloomFilter(NUM_AAAA_RECORDS);
            dnsBloomFilter2.add(exampleAAAA);
            dnsBloomFilter2.exportBloomFilter(BLOOM_FILTER_LOCATION);
            DNSBloomFilter dnsBloomFilter3 = new DNSBloomFilter(BLOOM_FILTER_LOCATION);

            System.out.println("Expected: "+false+" Actual: "+dnsBloomFilter3.probablyContains(exampleHostname));
            System.out.println("Expected: "+false+" Actual: "+dnsBloomFilter3.probablyContains(exampleIPv66Addr));
            System.out.println("Expected: "+true+"  Actual: "+dnsBloomFilter3.probablyContains(exampleAAAA));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}