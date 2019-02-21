//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package javax.servlet.http;

import java.text.MessageFormat;
import java.util.BitSet;
import java.util.ResourceBundle;

class CookieNameValidator {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    protected static final ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");
    protected final BitSet allowed = new BitSet(128);

    protected CookieNameValidator(String separators) {
        this.allowed.set(32, 127);

        for(int i = 0; i < separators.length(); ++i) {
            char ch = separators.charAt(i);
            this.allowed.clear(ch);
        }

    }

    void validate(String name) {
        if (name != null && name.length() != 0) {
            if (!this.isToken(name)) {
                String errMsg = lStrings.getString("err.cookie_name_is_token");
                throw new IllegalArgumentException(MessageFormat.format(errMsg, name));
            }
        } else {
            throw new IllegalArgumentException(lStrings.getString("err.cookie_name_blank"));
        }
    }

    private boolean isToken(String possibleToken) {
        int len = possibleToken.length();

        for(int i = 0; i < len; ++i) {
            char c = possibleToken.charAt(i);
            if (!this.allowed.get(c)) {
                return false;
            }
        }

        return true;
    }
}
