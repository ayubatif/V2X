package v2x;

import java.util.Properties;

public class Message extends Properties {
    public void putValue(String key, String value) {
        this.put(key, value);
    }

    public String getValue(String key) {
        String value = this.getProperty(key);
        return value;
    }
}
