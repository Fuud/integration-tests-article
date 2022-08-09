package fuud.test.infra;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.Remote;

public interface PortAllocator extends Remote {
    int freePort();

    class PortAllocatorImpl implements PortAllocator {
        private static final int CHUNK_SIZE = 10_000;
        private static ServerSocket basePortHolder; // save ServerSocket to field to save it from GC and remain open for jvm lifetime
        private static int port;

        public synchronized int freePort() {
            if (PortAllocatorImpl.basePortHolder == null) {
                for (int i = 1; i < 6; i++) {
                    try {
                        PortAllocatorImpl.basePortHolder = new ServerSocket(i * PortAllocatorImpl.CHUNK_SIZE);
                        break;
                    } catch (IOException e) {
                        // ignore
                    }
                }
                if (PortAllocatorImpl.basePortHolder == null) {
                    throw new IllegalStateException("Cannot find port base, all ports are occupied");
                }
                PortAllocatorImpl.port = PortAllocatorImpl.basePortHolder.getLocalPort();
            }
            while (PortAllocatorImpl.port < PortAllocatorImpl.basePortHolder.getLocalPort() + PortAllocatorImpl.CHUNK_SIZE) {
                PortAllocatorImpl.port++;
                if (PortAllocatorImpl.portIsFree(PortAllocatorImpl.port)) {
                    return PortAllocatorImpl.port;
                }
            }
            throw new IllegalStateException("Cannot find free port starting from " + PortAllocatorImpl.basePortHolder.getLocalPort());
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
}
