package fuud.test.infra;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class PortAllocator {
    private static final int CHUNK_SIZE = 10_000;
    private static ServerSocket basePortHolder; // save ServerSocket to field to save it from GC and remain open for jvm lifetime
    private static int port;

    public static synchronized int freePort() {
        if (basePortHolder == null) {
            for (int i = 1; i < 6; i++) {
                try {
                    basePortHolder = new ServerSocket(i * CHUNK_SIZE);
                    break;
                } catch (IOException e) {
                    // ignore
                }
            }
            if (basePortHolder == null) {
                throw new IllegalStateException("Cannot find port base, all ports are occupied");
            }
            port = basePortHolder.getLocalPort();
        }
        while (port < basePortHolder.getLocalPort() + CHUNK_SIZE) {
            port++;
            if (portIsFree(port)) {
                return port;
            }
        }
        throw new IllegalStateException("Cannot find free port starting from " + basePortHolder.getLocalPort());
    }

    private static boolean portIsFree(int port) {
        try {
            // next line better than just new ServerSocket(port), check https://github.com/spring-projects/spring-framework/issues/17906 for discussion
            try (ServerSocket ignored = new ServerSocket(port, 0, InetAddress.getByName("localhost"))) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
