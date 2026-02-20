package robowled.wledpipe;

import com.google.gson.JsonElement;

/**
 * Common interface for WLED communication pipes.
 * 
 * <p>Implementations of this interface provide different transport mechanisms
 * (serial, network, etc.) for communicating with WLED devices using
 * newline-delimited JSON messages.
 */
public interface WledPipe {

    /**
     * Sends a Gson JsonElement as JSON text, followed by a newline.
     *
     * @param json the JsonElement to send (JsonObject, JsonArray, etc.)
     * @throws Exception if sending fails
     */
    void sendGson(JsonElement json) throws Exception;

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
     * Non-blocking read that parses the response as a Gson JsonElement.
     * Call periodically; returns null if no full line yet.
     *
     * @return a parsed JsonElement, or null if no complete line is available
     * @throws Exception if reading or parsing fails
     */
    JsonElement tryReadGson() throws Exception;

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

