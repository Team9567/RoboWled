package robowled.wledpipe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.SerialPort;

import java.nio.charset.StandardCharsets;

/**
 * Serial-based communication pipe for WLED devices.
 * Communicates over USB serial, sending and receiving newline-delimited JSON.
 */
public class SerialPipe implements WledPipe {
    private final SerialPort port;
    private final ObjectMapper mapper;
    private boolean closed = false;

    // Simple receive buffer (you can make this more robust if you expect high traffic)
    private final StringBuilder rx = new StringBuilder(256);

    /**
     * Creates a SerialPipe connected to a WLED device via USB serial.
     *
     * @param usbPort  the WPILib serial port (e.g., {@link SerialPort.Port#kUSB})
     * @param baudRate the baud rate for serial communication (e.g., 115200)
     */
    public SerialPipe(SerialPort.Port usbPort, int baudRate) {
        port = new SerialPort(baudRate, usbPort);
        port.setTimeout(0.02); // seconds
        port.setReadBufferSize(256);

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
        port.write(str.getBytes(StandardCharsets.UTF_8), str.length());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String tryReadString() throws Exception {
        int n = port.getBytesReceived();
        if (n <= 0) return null;

        byte[] buf = port.read(n);
        rx.append(new String(buf, StandardCharsets.UTF_8));

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
        return !closed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            port.close();
        }
    }
}
