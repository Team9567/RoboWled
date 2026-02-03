package robowled.wledpipe;

/**
 * Common interface for WLED communication pipes.
 * 
 * <p>Implementations of this interface provide different transport mechanisms
 * (serial, network, etc.) for communicating with WLED devices using
 * newline-delimited JSON messages.
 */
public interface WledPipe {

    /**
     * Sends an object as JSON, followed by a newline.
     *
     * @param obj the object to serialize and send
     * @throws Exception if serialization or sending fails
     */
    void sendObject(Object obj) throws Exception;

    /**
     * Sends a raw string.
     *
     * @param str the string to send
     * @throws Exception if sending fails
     */
    void sendString(String str) throws Exception;

    /**
     * Non-blocking read: call periodically; returns null if no full line yet.
     *
     * @return a complete line (without newline), or null if no complete line is available
     * @throws Exception if reading fails
     */
    String tryReadString() throws Exception;

    /**
     * Non-blocking read: call periodically; returns null if no full object yet.
     * Deserializes a complete JSON line into the specified type.
     *
     * @param clazz the class to deserialize into
     * @param <T>   the type to return
     * @return a deserialized object, or null if no complete line is available
     * @throws Exception if reading or deserialization fails
     */
    <T> T tryReadObject(Class<T> clazz) throws Exception;

    /**
     * Checks if the pipe is connected and ready for communication.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Closes the pipe and releases any resources.
     */
    void close();
}

