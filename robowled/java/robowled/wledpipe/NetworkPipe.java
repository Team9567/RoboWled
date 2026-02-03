package robowled.wledpipe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Network-based communication pipe for WLED devices.
 * Communicates over TCP, sending and receiving newline-delimited JSON.
 */
public class NetworkPipe implements WledPipe {
    private final Socket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final ObjectMapper mapper;

    // Simple receive buffer (you can make this more robust if you expect high traffic)
    private final StringBuilder rx = new StringBuilder(256);

    private static final int DEFAULT_TIMEOUT_MS = 20;

    /**
     * Creates a NetworkPipe connected to a WLED device.
     *
     * @param host the hostname or IP address of the WLED device
     * @param port the TCP port (WLED default is typically 80 for HTTP, or a custom port for raw JSON)
     * @throws IOException if the connection fails
     */
    public NetworkPipe(String host, int port) throws IOException {
        this(host, port, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Creates a NetworkPipe connected to a WLED device with a custom timeout.
     *
     * @param host      the hostname or IP address of the WLED device
     * @param port      the TCP port
     * @param timeoutMs socket read timeout in milliseconds
     * @throws IOException if the connection fails
     */
    public NetworkPipe(String host, int port, int timeoutMs) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 1000);
        socket.setSoTimeout(timeoutMs);
        socket.setTcpNoDelay(true); // Disable Nagle's algorithm for lower latency

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();

        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendObject(Object obj) throws Exception {
        String json = mapper.writeValueAsString(obj);
        String line = json + "\n";
        sendString(line);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendString(String str) throws Exception {
        outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String tryReadString() throws Exception {
        try {
            int available = inputStream.available();
            if (available > 0) {
                byte[] buf = new byte[available];
                int bytesRead = inputStream.read(buf);
                if (bytesRead > 0) {
                    rx.append(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            // Timeout or no data available - this is expected for non-blocking reads
        }

        int newlineIdx = rx.indexOf("\n");
        if (newlineIdx < 0) return null;

        String line = rx.substring(0, newlineIdx).trim();
        rx.delete(0, newlineIdx + 1);

        if (line.isEmpty()) return null;
        return line;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T tryReadObject(Class<T> clazz) throws Exception {
        String line = tryReadString();

        if (line == null) return null;
        return mapper.readValue(line, clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException ignored) {
        }
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
