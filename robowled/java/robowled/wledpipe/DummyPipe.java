package robowled.wledpipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * A dummy/mock pipe for testing and simulation.
 * 
 * <p>This pipe doesn't communicate with any real device. Instead, it:
 * <ul>
 *   <li>Accumulates sent JSON into a merged state object</li>
 *   <li>Optionally notifies a callback when messages are sent</li>
 *   <li>Allows injection of mock responses for testing receive logic</li>
 *   <li>Always reports as "connected" (unless explicitly set otherwise)</li>
 * </ul>
 * 
 * <p>Useful for:
 * <ul>
 *   <li>Unit testing LED subsystems without hardware</li>
 *   <li>Running robot code in simulation mode</li>
 *   <li>Verifying the accumulated state after a sequence of commands</li>
 * </ul>
 */
public class DummyPipe implements WledPipe {
    private final Queue<String> mockResponses = new LinkedList<>();
    private JsonObject accumulatedState = new JsonObject();
    private Consumer<String> sendCallback;
    private boolean connected = true;

    /**
     * Creates a DummyPipe with no send callback.
     */
    public DummyPipe() {
        this(null);
    }

    /**
     * Creates a DummyPipe with a callback for sent messages.
     *
     * @param sendCallback called with each string sent via {@link #sendString(String)},
     *                     or null to skip notifications
     */
    public DummyPipe(Consumer<String> sendCallback) {
        this.sendCallback = sendCallback;
    }

    /**
     * Sets the callback for sent messages.
     *
     * @param sendCallback called with each string sent, or null to skip
     */
    public void setSendCallback(Consumer<String> sendCallback) {
        this.sendCallback = sendCallback;
    }

    /**
     * Gets the accumulated state from all sent JSON messages.
     * 
     * <p>Each sent JSON object's properties are merged into this object.
     * Later values overwrite earlier values for the same key.
     *
     * @return the accumulated state as a JsonObject
     */
    public JsonObject getAccumulatedState() {
        return accumulatedState;
    }

    /**
     * Gets a specific value from the accumulated state.
     *
     * @param key the JSON property name
     * @return the JsonElement value, or null if not set
     */
    public JsonElement getStateValue(String key) {
        return accumulatedState.get(key);
    }

    /**
     * Checks if the accumulated state contains a specific key.
     *
     * @param key the JSON property name
     * @return true if the key exists in the accumulated state
     */
    public boolean hasStateValue(String key) {
        return accumulatedState.has(key);
    }

    /**
     * Clears the accumulated state.
     */
    public void clearState() {
        accumulatedState = new JsonObject();
    }

    /**
     * Queues a mock response to be returned by {@link #tryReadString()}.
     * Responses are returned in FIFO order.
     *
     * @param response the response string to queue (should not include newline)
     */
    public void queueResponse(String response) {
        mockResponses.add(response);
    }

    /**
     * Queues a mock JSON response.
     *
     * @param json the JsonElement to queue as a response
     */
    public void queueResponse(JsonElement json) {
        mockResponses.add(json.toString());
    }

    /**
     * Clears all queued mock responses.
     */
    public void clearResponses() {
        mockResponses.clear();
    }

    /**
     * Sets the simulated connection state.
     *
     * @param connected true to simulate connected, false for disconnected
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendGson(JsonElement json) throws Exception {
        String line = json.toString() + "\n";
        sendString(line);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>In DummyPipe, this parses the string as JSON and merges it into
     * the accumulated state. Non-JSON strings are ignored for accumulation
     * but the callback is still invoked.
     */
    @Override
    public void sendString(String str) throws Exception {
        if (sendCallback != null) {
            sendCallback.accept(str);
        }

        // Try to parse and merge JSON
        String trimmed = str.trim();
        if (trimmed.startsWith("{")) {
            try {
                JsonElement parsed = JsonParser.parseString(trimmed);
                if (parsed.isJsonObject()) {
                    mergeInto(accumulatedState, parsed.getAsJsonObject());
                }
            } catch (Exception e) {
                // Not valid JSON - ignore for accumulation
            }
        }
    }

    /**
     * Merges source JsonObject into target, overwriting existing keys.
     */
    private void mergeInto(JsonObject target, JsonObject source) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            target.add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>In DummyPipe, this returns queued mock responses in FIFO order,
     * or null if no responses are queued.
     */
    @Override
    public String tryReadString() throws Exception {
        return mockResponses.poll();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>In DummyPipe, this parses the next queued mock response as JSON,
     * or returns null if no responses are queued.
     */
    @Override
    public JsonElement tryReadGson() throws Exception {
        String line = tryReadString();
        if (line == null) return null;
        return JsonParser.parseString(line);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>In DummyPipe, this returns the simulated connection state
     * (default: true). Use {@link #setConnected(boolean)} to change.
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>In DummyPipe, this sets the connection state to false
     * and clears any queued responses and accumulated state.
     */
    @Override
    public void close() {
        connected = false;
        mockResponses.clear();
        accumulatedState = new JsonObject();
    }
}
