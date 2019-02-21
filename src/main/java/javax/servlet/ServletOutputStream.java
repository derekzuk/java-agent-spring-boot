//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package javax.servlet;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class ServletOutputStream extends OutputStream {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static final ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.LocalStrings");

    protected ServletOutputStream() {
    }

    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }

        int len = s.length();
        byte[] buffer = new byte[len];

        for(int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if ((c & '\uff00') != 0) {
                String errMsg = lStrings.getString("err.not_iso8859_1");
                Object[] errArgs = new Object[]{c};
                errMsg = MessageFormat.format(errMsg, errArgs);
                throw new CharConversionException(errMsg);
            }

            buffer[i] = (byte)(c & 255);
        }

        this.write(buffer);
    }

    public void print(boolean b) throws IOException {
        String msg;
        if (b) {
            msg = lStrings.getString("value.true");
        } else {
            msg = lStrings.getString("value.false");
        }

        this.print(msg);
    }

    public void print(char c) throws IOException {
        this.print(String.valueOf(c));
    }

    public void print(int i) throws IOException {
        this.print(String.valueOf(i));
    }

    public void print(long l) throws IOException {
        this.print(String.valueOf(l));
    }

    public void print(float f) throws IOException {
        this.print(String.valueOf(f));
    }

    public void print(double d) throws IOException {
        this.print(String.valueOf(d));
    }

    public void println() throws IOException {
        this.print("\r\n");
    }

    public void println(String s) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        sb.append("\r\n");
        this.print(sb.toString());
    }

    public void println(boolean b) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (b) {
            sb.append(lStrings.getString("value.true"));
        } else {
            sb.append(lStrings.getString("value.false"));
        }

        sb.append("\r\n");
        this.print(sb.toString());
    }

    public void println(char c) throws IOException {
        this.println(String.valueOf(c));
    }

    public void println(int i) throws IOException {
        this.println(String.valueOf(i));
    }

    public void println(long l) throws IOException {
        this.println(String.valueOf(l));
    }

    public void println(float f) throws IOException {
        this.println(String.valueOf(f));
    }

    public void println(double d) throws IOException {
        this.println(String.valueOf(d));
    }

    public abstract boolean isReady();

    public abstract void setWriteListener(WriteListener var1);
}
