package Layout;

import java.io.IOException;

public class ParseException extends IOException {
    public String message;
    public ParseException(String s) {
        message = s;
    }
}
