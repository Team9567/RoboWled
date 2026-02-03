package robowled.wledpipe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * A dummy/mock pipe for testing and simulation.
 * 
 * <p>This pipe doesn't communicate with any real device. Instead, it:
 * <ul>
 *   <li>Accumulates sent JSON values into a state map</li>
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
    private final ObjectMapper mapper;
    private final Queue<String> mockResponses = new LinkedList<>();
    private final Map<String, Object> accumulatedState = new HashMap<>();
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
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
     * <p>Each sent JSON object's properties are merged into this map.
     * Later values overwrite earlier values for the same key.
     *
     * @return the accumulated state map (modifiable)
     */
    public Map<String, Object> getAccumulatedState() {
        return accumulatedState;
    }

    /**
     * Gets a specific value from the accumulated state.
     *
     * @param key the JSON property name
     * @return the value, or null if not set
     */
    public Object getStateValue(String key) {
        return accumulatedState.get(key);
    }

    /**
     * Gets a specific value from the accumulated state, cast to the expected type.
     *
     * @param key the JSON property name
     * @param type the expected type
     * @param <T> the type to return
     * @return the value cast to the specified type, or null if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T getStateValue(String key, Class<T> type) {
        Object value = accumulatedState.get(key);
        if (value == null) return null;
        return (T) value;
    }

    /**
     * Clears the accumulated state.
     */
    public void clearState() {
        accumulatedState.clear();
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
     * Queues a mock JSON response by serializing the given object.
     *
     * @param obj the object to serialize and queue as a response
     * @throws Exception if serialization fails
     */
    public void queueResponseObject(Object obj) throws Exception {
        String json = mapper.writeValueAsString(obj);
        mockResponses.add(json);
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
    public void sendObject(Object obj) throws Exception {
        String json = mapper.writeValueAsString(obj);
        String line = json + "\n";
        sendString(line);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>In DummyPipe, this parses the string as JSON and merges the values
     * into the accumulated state. If parsing fails (e.g., not valid JSON),
     * the string is ignored for accumulation but the callback is still invoked.
     */
    @Override
    public void sendString(String str) throws Exception {
        // Notify callback if set
        if (sendCallback != null) {
            sendCallback.accept(str);
        }

        // Try to parse and accumulate JSON
        String trimmed = str.trim();
        if (trimmed.startsWith("{")) {
            try {
                Map<String, Object> parsed = mapper.readValue(trimmed, 
                        new TypeReference<Map<String, Object>>() {});
                accumulatedState.putAll(parsed);
            } catch (Exception e) {
                // Not valid JSON - ignore for accumulation
            }
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
     */
    @Override
    public <T> T tryReadObject(Class<T> clazz) throws Exception {
        String line = tryReadString();

        if (line == null) return null;
        return mapper.readValue(line, clazz);
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
        accumulatedState.clear();
    }
}
