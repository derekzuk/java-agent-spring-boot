//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package javax.servlet.http;

import java.text.MessageFormat;

class RFC2109Validator extends RFC6265Validator {
    RFC2109Validator(boolean allowSlash) {
        if (allowSlash) {
            this.allowed.set(47);
        }

    }

    void validate(String name) {
        super.validate(name);
        if (name.charAt(0) == '$') {
            String errMsg = lStrings.getString("err.cookie_name_is_token");
            throw new IllegalArgumentException(MessageFormat.format(errMsg, name));
        }
    }
}
