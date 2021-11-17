package org.marc4j.util;

import java.util.Arrays;
import java.util.List;

public enum Encoding {
    UTF8("UTF-8", "UTF8"),
    MARC8("MARC-8", "MARC8"),
    ISO8859_1("ISO-8859-1", "ISO8859_1", "ISO_8859_1")
    ;

    String standardName;
    List<String> names;

    Encoding(String... names) {
        this.standardName = names[0];
        this.names = Arrays.asList(names);
    }

    public static Encoding get(String encodingName) {
        for (Encoding encoding : values())
            for (String name : encoding.names)
                if (name.equals(encodingName))
                    return encoding;
        return null;
    }

    public String getStandardName() {
        return standardName;
    }

    public List<String> getNames() {
        return names;
    }
}
