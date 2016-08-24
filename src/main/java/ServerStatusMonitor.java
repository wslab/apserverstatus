import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.ServerStatusResult;
import entity.QueuedServerStatusMessage;
import processor.MessageProcessor;
import storage.StatStorage;

public class ServerStatusMonitor {

	
	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(ServerStatusMonitor.class);
		logger.info("start");
		LinkedBlockingQueue<QueuedServerStatusMessage> queue = new LinkedBlockingQueue<QueuedServerStatusMessage>();
		StatStorage storage = new StatStorage();
		MessageProcessor messageProcessor = new MessageProcessor(queue, storage);
		Thread workerThread = new Thread(messageProcessor);
		workerThread.start();
		long start = System.currentTimeMillis();
		int count = 10000;
		for (int i = 0; i < count; i++) {
			QueuedServerStatusMessage qm = new QueuedServerStatusMessage("myserver", 0.1, 0.2);
			queue.add(qm);
		}
		while (!queue.isEmpty()) {}
		long end = System.currentTimeMillis();
		logger.info("processed " + count + " messages in " + (end-start) + "ms (" + (60.0/((end-start)/(double)count)) +" messages per minute)");
		ServerStatusResult result = storage.getDataForLast60Minutes("myserver");
		logger.info("result: " + result.toString());
		ServerStatusResult result2 = storage.getDataForLast60Minutes("myserver");
		logger.info("result2: " + result.toString());
		queue.add(new QueuedServerStatusMessage("", 0.0, 0.0, -1L));
		logger.info("Waiting for worker thread to finish");
		try {
			workerThread.join(5000);
		} catch (InterruptedException e) {
			logger.warn("Interrupted while waiting for worker thread to exit, continuing");
		}
		logger.info("done");
	}

}
