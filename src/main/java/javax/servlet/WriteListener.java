//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package javax.servlet;

import java.io.IOException;
import java.util.EventListener;

public interface WriteListener extends EventListener {
    void onWritePossible() throws IOException;

    void onError(Throwable var1);
}
