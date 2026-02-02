package robowled.wledpipe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.SerialPort;

import java.nio.charset.StandardCharsets;

/**
 * Serial-based communication pipe for WLED devices.
 * Communicates over USB serial, sending and receiving newline-delimited JSON.
 */
public class SerialPipe {
  private final SerialPort port;
  private final ObjectMapper mapper;

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
   * Sends an object as JSON over the serial port, followed by a newline.
   *
   * @param obj the object to serialize and send
   * @throws Exception if serialization or sending fails
   */
  public void sendObject(Object obj) throws Exception {
    String json = mapper.writeValueAsString(obj);
    String line = json + "\n";
    sendString(line);
  }

  /**
   * Sends a raw string over the serial port.
   *
   * @param str the string to send
   * @throws Exception if sending fails
   */
  public void sendString(String str) throws Exception {
    port.write(str.getBytes(StandardCharsets.UTF_8), str.length());
  }

  /**
   * Non-blocking-ish: call periodically; returns null if no full line yet.
   * Reads available data from the serial port and returns a complete line if available.
   *
   * @return a complete line (without newline), or null if no complete line is available
   * @throws Exception if reading fails
   */
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
   * Non-blocking-ish: call periodically; returns null if no full object yet.
   * Reads available data and deserializes a complete JSON line into the specified type.
   *
   * @param clazz the class to deserialize into
   * @param <T>   the type to return
   * @return a deserialized object, or null if no complete line is available
   * @throws Exception if reading or deserialization fails
   */
  public <T> T tryReadObject(Class<T> clazz) throws Exception {
    String line = tryReadString();

    if (line == null) return null;
    return mapper.readValue(line, clazz);
  }
}
