package ec.edu.espe.eurekadesktop.utils;

import java.util.regex.Pattern;

public class XmlUtils {
    private static final Pattern PATTERN_TAG = Pattern.compile("<([^>]+)>([^<]*)</\\1>");

    public static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    public static String extractTag(String xml, String tagName) {
        if (xml == null || tagName == null) return null;
        
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";
        
        int start = xml.indexOf(openTag);
        if (start == -1) {
            openTag = "<" + tagName + " ";
            start = xml.indexOf(openTag);
            if (start == -1) return null;
            start = xml.indexOf(">", start);
            if (start == -1) return null;
            start++;
        } else {
            start += openTag.length();
        }
        
        int end = xml.indexOf(closeTag, start);
        if (end == -1) return null;
        
        return xml.substring(start, end).trim();
    }

    public static String extractTagContent(String xml, String tagName) {
        return extractTag(xml, tagName);
    }
}