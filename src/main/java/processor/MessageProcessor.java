package processor;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entity.QueuedServerStatusMessage;
import storage.IStatStorage;

/**
 * @author alex
 *
 * Single-threaded processor for the incoming queue. Reads the messages from the queue and forwards to storage engine.
 *
 */
public class MessageProcessor implements Runnable {

	/**
	 * The queue we wil process.
	 */
	private LinkedBlockingQueue<QueuedServerStatusMessage> queue;
	
	/**
	 * Storage engine reference
	 */
	private IStatStorage storage;
	
	private int processedCount = 0;

	private final static Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

	// we will stop the processor when we see a message with this timestamp
	private long STOP_REQUEST_TIMESTAMP = -1L;
	
	public MessageProcessor(LinkedBlockingQueue<QueuedServerStatusMessage> queue, IStatStorage storage) {
		this.queue = queue;
		this.storage = storage;
	}

	public void run() {
		QueuedServerStatusMessage message = null;
		while(true) {
			try {
				message = queue.take();
				if ( isStopRequest(message)) {
					logger.info("Request to stop processing received, exiting after " + processedCount + " messages");
					return;
				}
				processMessage(message);
				processedCount++;
			} catch (InterruptedException e) {
				logger.info("MessageProcessor interrupted, exiting after " + processedCount + " messages");
				return;
			} catch (Exception e) {
				logger.warn("Failed to process message: " + (message == null ? "NULL" : message.toString()));
			}
		}
	}
	
	/**
	 * @param message
	 * @return true if this message is a request to stop queue processing, false otherwise
	 */
	private boolean isStopRequest(QueuedServerStatusMessage message) {
		return null != message && message.getTimestampUtc() == STOP_REQUEST_TIMESTAMP;
	}
	
	/**
	 * @param message
	 * 
	 * Invoke storage method to record the data.
	 */
	void processMessage(QueuedServerStatusMessage message) {
		storage.addToMaps(message);
	}
}
