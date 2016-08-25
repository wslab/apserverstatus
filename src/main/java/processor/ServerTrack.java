package processor;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.IServerTrack;
import client.ServerStatusMessage;
import client.ServerStatusResult;
import entity.QueuedServerStatusMessage;
import storage.StatStorage;

public class ServerTrack implements IServerTrack {

	/**
	 * The queue we wil process.
	 */
	private LinkedBlockingQueue<QueuedServerStatusMessage> queue;

	/**
	 * Storage engine for data.
	 */
	private StatStorage storage;

	/**
	 * Number of messages processed since last reset.
	 */
	private int processedCount = 0;

	/**
	 * Message processor
	 */
	private MessageProcessor messageProcessor = null;

	Thread workerThread;

	private final static Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

	public ServerTrack() {
		this.queue = new LinkedBlockingQueue<QueuedServerStatusMessage>();
		Reset();
	}

	public void Reset() {
		queue.clear();
		processedCount = 0;
		storage = new StatStorage();
		if (null != messageProcessor) {
			logger.info("stopping existing messageProcessor");
			// stop existing message processor and start a new one
			queue.add(new QueuedServerStatusMessage("", 0.0, 0.0, -1L));
			try {
				workerThread.join(5000);
			} catch (InterruptedException e) {
				logger.warn("Interrupted while waiting for worker thread to exit, continuing");
			}
		}
		messageProcessor = new MessageProcessor(queue, storage);
		workerThread = new Thread(messageProcessor);
		workerThread.start();
	}

	/**
	 * @param serverStatusMessage
	 * @return true if the message is valid and was accepted, false otherwise.
	 * 
	 *         The message is valid if:
	 * 
	 *         - server name is not null or empty and cpuload and ramload are
	 *         positive.
	 * 
	 *         We may want to add a test to make sure that cpu and ram load are
	 *         "reasonable" (less than 100?)
	 * 
	 */
	public boolean validateMessage(ServerStatusMessage serverStatusMessage) {
		return serverStatusMessage != null && serverStatusMessage.getServerName() != null
				&& !serverStatusMessage.getServerName().equals("") && serverStatusMessage.getCpuLoad() > 0.0
				&& serverStatusMessage.getRamLoad() > 0.0;
	}

	@Override
	public boolean reportData(ServerStatusMessage serverStatusMessage) {
		// validate message
		if (!validateMessage(serverStatusMessage))
			return false;
		// queue for processing
		QueuedServerStatusMessage qm = new QueuedServerStatusMessage(serverStatusMessage.getServerName(),
				serverStatusMessage.getCpuLoad(), serverStatusMessage.getRamLoad());
		queue.add(qm);
		return true;
	}

	/**
	 * @param serverStatusMessage
	 * @param timestampUtc
	 * @return true if the message is valid and was accepted, false otherwise.
	 *
	 * For testing only. Making this public to allow testing from a command-line tool.
	 */
	public boolean reportData(ServerStatusMessage serverStatusMessage, long timestampUtc) {
		// validate message
		if (!validateMessage(serverStatusMessage))
			return false;
		// queue for processing
		QueuedServerStatusMessage qm = new QueuedServerStatusMessage(serverStatusMessage.getServerName(),
				serverStatusMessage.getCpuLoad(), serverStatusMessage.getRamLoad(), timestampUtc);
		queue.add(qm);
		return true;		
	}
	
	@Override
	public ServerStatusResult getDataForLast60Minutes(String serverName) {
		return storage.getDataForLast60Minutes(serverName);
	}

	@Override
	public ServerStatusResult getDataForLast24Hours(String serverName) {
		return storage.getDataForLast24Hours(serverName);
	}

	/**
	 * @return true if the queue is empty
	 * 
	 * Useful for testing.
	 */
	public boolean queueIsEmpty() {
		return queue.isEmpty();
	}
	
}
