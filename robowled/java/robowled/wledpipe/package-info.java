/**
 * WLED Communication Pipes.
 * 
 * <p>Provides communication channels for WLED devices:
 * <ul>
 *   <li>{@link WledPipe} - Common interface for all pipe implementations</li>
 *   <li>{@link SerialPipe} - USB/serial communication</li>
 *   <li>{@link NetworkPipe} - TCP/IP network communication</li>
 *   <li>{@link DummyPipe} - Mock pipe for testing and simulation</li>
 * </ul>
 * 
 * <p>All pipes communicate using newline-delimited JSON messages.
 */
package robowled.wledpipe;
