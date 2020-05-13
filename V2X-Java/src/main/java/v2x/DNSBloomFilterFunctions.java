package v2x;

import java.io.IOException;
import java.util.Random;

public class DNSBloomFilterFunctions {
    static final String FIXED_DNS_ENTRY = "KTH.Thesis.V2X=0000:1111:2222:3333:4444:5555:6666:7777";
    private static final String BLOOM_FILTER_LOCATION = "Authentication/DNS-bloom-filter.bf";

    /**
     * Makes a random amount except for one preset case of bloom filter entries
     *
     * @param amount amount of random - 1 AAAA entry for the bloom filter
     * @return <code>DNSBloomFilter</code> a dns bloom filter object
     */
    public static DNSBloomFilter generateRandomBloomFilter(int amount) throws IOException {
        DNSBloomFilter dnsBloomFilter = new DNSBloomFilter(amount);
        dnsBloomFilter.add(FIXED_DNS_ENTRY);

        for (int i = 1; i < amount; i++) {
            String randomIPV6 = generateRandomIPV6();
            String randomHostname = generateRandomHostname();
            String randomAAAA = randomHostname + "=" + randomIPV6;
            dnsBloomFilter.add(randomAAAA);
        }

        dnsBloomFilter.exportBloomFilter(BLOOM_FILTER_LOCATION);

        return dnsBloomFilter;
    }

    // https://stackoverflow.com/questions/9236197/generate-random-ip-address/9236244

    /**
     * Creates a random ipv6 string
     *
     * @return <code>String</code> a string of a random ipv6
     */
    public static String generateRandomIPV6() {
        Random random = new Random();
        String ipv6 = random.nextInt(256) + ":" + random.nextInt(256) + ":" +
                random.nextInt(256) + ":" + random.nextInt(256) + ":" +
                random.nextInt(256) + ":" + random.nextInt(256) + ":" +
                random.nextInt(256) + ":" + random.nextInt(256) + ":";
        return ipv6;
    }

    // https://www.baeldung.com/java-random-string

    /**
     * Creates a random alphanumeric string
     *
     * @return <code>String</code> a string of a random hostname
     */
    public static String generateRandomHostname() {
        Random random = new Random();
        int leftLimit = 48;
        int rightLimit = 123;
        int wordLength = random.nextInt(25);

        String generateWord = random.ints(leftLimit, rightLimit)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(wordLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generateWord;
    }

    /**
     * Gives a fixed dns entry
     *
     * @return <code>String</code> a fixed dns entry
     */
    public static String getFixedAAAA() {
        return FIXED_DNS_ENTRY;
    }
}
